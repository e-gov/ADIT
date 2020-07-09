package ee.adit.service.dhx;

import com.jcabi.aspects.Loggable;
import ee.adit.dao.DhxUserDAO;
import ee.adit.dao.DocumentDAO;
import ee.adit.dao.DocumentSharingDAO;
import ee.adit.dao.pojo.DhxUser;
import ee.adit.dao.pojo.DocumentSharing;
import ee.adit.dhx.DhxReceiver;
import ee.adit.dhx.DhxReceiverFactory;
import ee.adit.dhx.DhxUtil;
import ee.adit.service.DocumentService;
import ee.adit.service.RuuterService;
import ee.adit.util.Configuration;
import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.exception.DhxExceptionEnum;
import ee.ria.dhx.types.*;
import ee.ria.dhx.types.eu.x_road.dhx.producer.SendDocumentResponse;
import ee.ria.dhx.util.StringUtil;
import ee.ria.dhx.ws.DhxOrganisationFactory;
import ee.ria.dhx.ws.service.DhxImplementationSpecificService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ws.context.MessageContext;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@Transactional
@EnableAsync
public class AditDhxSpecificService implements DhxImplementationSpecificService {

    private List<InternalXroadMember> members;

    @Autowired
    DhxUserDAO dhxDao;

    @Autowired
    DhxService dhxService;

    @Autowired
    DocumentSharingDAO documentSharingDao;

    @Autowired
    DocumentDAO documentDao;

    @Autowired
    DocumentService documentService;

    @Autowired
    RuuterService ruuterService;


    @Autowired
    Configuration configuration;

    /**
     * Returns all organisations from database that are active and marked as own
     * representees.
     */
    @Override
    @Loggable
    public List<DhxRepresentee> getRepresentationList() throws DhxException {
        return new ArrayList<DhxRepresentee>();
    }

    @Override
    @Loggable
    public boolean isDuplicatePackage(InternalXroadMember from, String consignmentId) throws DhxException {

        DhxOrganisation dhxOrg = DhxOrganisationFactory.createDhxOrganisation(from);
        String orgCode = dhxService.toSingleCapsuleAddressee(dhxOrg.getCode(), dhxOrg.getSystem());
        log.debug("checking duplicates for organisation: {}", orgCode);
        Long cnt = documentDao.countIncomingMessagesBySenderAndConsignmentId(DhxUtil.addPrefixIfNecessary(orgCode), consignmentId);
        if (cnt != null && cnt > 0) {
            return true;
        }
        return false;
    }

    @Override
    @Loggable
    public String receiveDocument(IncomingDhxPackage document, MessageContext context) throws DhxException {

        log.debug(
                "String receiveDocument(DhxDocument document) " + "externalConsignmentId: {} recipient: {} service: {}",
                document.getExternalConsignmentId(), document.getRecipient(), document.getService());
        Long docId = null;
        String temporaryFile = null;
        DhxReceiverFactory receiverFactory = new DhxReceiverFactory(documentService, configuration.getjDigiDocConfigFile());
        try {
            dhxService.formatCapsuleRecipientAndSender(document.getParsedContainer(), document.getClient(),
                    document.getService(), false);
            log.debug("Saving document to DHX database");
            temporaryFile = dhxService.saveContainerToFile(document.getParsedContainer());
            docId = receiveDocument(receiverFactory, document, temporaryFile, false);

        } catch (Exception ex) {
            if (ruuterService.shouldRetryWithoutActiveUserValidation(temporaryFile, ex)) {
                // If we manage to process the document without active user check, return docId to original DHX sender
                docId = receiveDocument(receiverFactory, document, temporaryFile, true);
            } else {
                throw new DataRetrievalFailureException("Error occured while saving DHX message: ", ex);
            }
        } finally {
            if (temporaryFile != null) {
                new File(temporaryFile).delete();
            }
        }
        dhxService.cleanupContainer(document.getParsedContainer());
        return docId.toString();
    }

    private Long receiveDocument(DhxReceiverFactory receiverFactory, IncomingDhxPackage document, String temporaryFile, boolean allowSendingToInactiveUser) {
        try {
            DhxReceiver receiver = receiverFactory.getReceiver(document.getParsedContainer());
            Long docId = receiver.receive(temporaryFile, document.getExternalConsignmentId(), allowSendingToInactiveUser);
            if (docId == null || docId.longValue() == 0) {
                log.error("Error while saving outgoing DHX message - no ID returned by save method.");
                throw new DataRetrievalFailureException(
                        "Error while saving outgoing DHX message - no ID returned by save method.");
            } else {
                log.info("Incoming message saved to DHX database. ID: " + docId);
            }
            return docId;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new DataRetrievalFailureException("Error occured while saving DHX message: ", ex);
        }
    }


    @Override
    @Loggable
    public List<InternalXroadMember> getAdresseeList() throws DhxException {
        if (members == null) {
            members = new ArrayList<InternalXroadMember>();
            List<DhxUser> orgs = this.dhxDao.getUsers();
            log.debug("found addressee organisations:" + orgs.size());
            for (DhxUser org : orgs) {
                members.add(getInternalXroadMemberFromOrganisation(org));
            }
        }
        return members;
    }

    @Loggable
    private InternalXroadMember getInternalXroadMemberFromOrganisation(DhxUser org) {
        DhxUser mainOrg = org;

        DhxRepresentee representee = null;
        if (org.getRepresentor() != null) {
            mainOrg = org.getRepresentor();
            representee = getRepresenteeFromOrganisation(org);

        }
        InternalXroadMember member = new InternalXroadMember(mainOrg.getXroadInstance(), mainOrg.getMemberClass(),
                mainOrg.getCode(), mainOrg.getSubSystem(), mainOrg.getName(), representee);
        if (org.getRepresentees() != null && org.getRepresentees().size() > 0) {
            member.setRepresentor(true);
        }
        log.trace("created member: {}", member);
        return member;
    }

    @Loggable
    private DhxRepresentee getRepresenteeFromOrganisation(DhxUser org) {
        return new DhxRepresentee(org.getOrgCode(), org.getRepresenteeStart(), org.getRepresenteeEnd(), org.getName(),
                org.getSubSystem());
    }

    @Override
    @Loggable
    public void saveAddresseeList(List<InternalXroadMember> members) throws DhxException {
        try {
            updateNotActiveAdressees(members);
            List<DhxUser> organisations = new ArrayList<DhxUser>();
            for (InternalXroadMember member : members) {
                if (member.getRepresentee() == null) {
                    organisations.add(dhxService.getDhxUserFromInternalXroadMember(member));
                }
            }
            log.debug("saving not representees. " + organisations.size());
            dhxDao.updateOrganisations(organisations);
            organisations = new ArrayList<DhxUser>();
            for (InternalXroadMember member : members) {
                if (member.getRepresentee() != null && dhxService.isRepresenteeValid(member)) {
                    organisations.add(dhxService.getDhxUserFromInternalXroadMember(member));
                }
            }
            log.debug("saving representees. " + organisations.size());
            dhxDao.updateOrganisations(organisations);

            this.members = members;
        } catch (Exception ex) {
            throw new DhxException("Error occured while updating organisations.", ex);
        }
    }

    private void updateNotActiveAdressees(List<InternalXroadMember> members) throws DhxException {
        // adressees count should not be too big, get ALL
        try {
            List<DhxUser> allOrgs = this.dhxDao.getUsers();
            Boolean found = false;
            List<DhxUser> changedOrgs = new ArrayList<DhxUser>();
            for (DhxUser org : allOrgs) {
                if (log.isDebugEnabled()) {
                    log.debug("checking if organisation needs to be deactivated organisation: {}", org);
                }
                // we need to check only active organisations
                if (org.isActive()) {
                    found = false;
                    for (InternalXroadMember member : members) {
                        if (log.isTraceEnabled()) {
                            log.trace("checking against member: {}", member);
                        }
                        if (member.getRepresentee() == null) {
                            if (member.getMemberCode().equals(org.getCode())
                                    && (member.getSubsystemCode() == null && org.getSubSystem() == null
                                    || member.getSubsystemCode().equals(org.getSubSystem()))) {
                                found = true;
                                break;
                            }
                        } else {
                            if (member.getRepresentee().getRepresenteeCode().equals(org.getCode())
                                    && (member.getRepresentee().getRepresenteeSystem() == null
                                    && org.getSubSystem() == null)
                                    || (member.getRepresentee().getRepresenteeSystem() != null
                                    && org.getSubSystem() != null && member.getRepresentee()
                                    .getRepresenteeSystem().equals(org.getSubSystem()))) {
                                found = true;
                                break;
                            }
                        }
                    }
                } else {
                    found = true;
                }
                if (!found) {
                    if (log.isDebugEnabled()) {
                        log.debug("organisation is not found in renewed address list, "
                                + "deactivating it. organisation: {}", org);
                    }
                    org.setActive(false);
                    changedOrgs.add(org);
                }
            }
            dhxDao.updateOrganisations(changedOrgs);
        } catch (Exception ex) {
            throw new DhxException("Error occured while updating inactive organisations.", ex);
        }
    }

    @Override
    @Loggable
    public void saveSendResult(DhxSendDocumentResult finalResult, List<AsyncDhxSendDocumentResult> retryResults) {
        Session dhxSession = null;
        Transaction dhxTransaction = null;
        log.info("saveSendResult invoked.");
        try {
            SessionFactory sessionFactory = documentSharingDao.getSessionFactory();
            dhxSession = sessionFactory.openSession();
            dhxTransaction = dhxSession.beginTransaction();
            String recipientIdStr = finalResult.getSentPackage().getInternalConsignmentId();
            Long recipientId = Long.parseLong(recipientIdStr);
            log.debug("searching recipient to save send result. " + recipientId);
            DocumentSharing recipient = documentSharingDao.getDocumentSharing(recipientId);
            if (recipient == null) {
                throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
                        "Recipient is not found in database. recipientId: " + recipientId);
            }
            SendDocumentResponse docResponse = finalResult.getResponse();
            log.trace("saving send result for recipient: {}", recipient);
            boolean allSent = true;
            if (docResponse.getFault() == null) {
                log.debug("Document was succesfuly sent to DHX");
                recipient.setDhxReceiptId(docResponse.getReceiptId());
                recipient.setDocumentDvkStatus(DocumentService.DHX_STATUS_SENT);
            } else {
                allSent = false;
                log.debug("Fault occured while sending document to DHX");
                log.debug("All attempts to send documents were done. Saving document as failed.");
                recipient.setDocumentDvkStatus(DocumentService.DHX_STATUS_ABORTED);
                String faultString = "";
                if (retryResults != null && retryResults.size() > 0) {
                    faultString = faultString + " Total retries count: " + retryResults.size()
                            + " Results for individual retries: ";
                    for (AsyncDhxSendDocumentResult result : retryResults) {
                        faultString = faultString + "\n Retry date: " + result.getTryDate() + " Technical error:";
                        if (result.getResult().getOccuredException() != null) {
                            faultString = faultString + " Error message:"
                                    + result.getResult().getOccuredException().getMessage() + " Stacktrace: "
                                    + ExceptionUtils.getStackTrace(result.getResult().getOccuredException());
                        }
                    }
                }
                faultString = docResponse.getFault().getFaultString() + faultString;
                if (!StringUtil.isNullOrEmpty(docResponse.getFault().getFaultCode())) {
                    faultString = docResponse.getFault().getFaultCode().substring(0,
                            docResponse.getFault().getFaultCode().length() > 250 ? 250
                                    : docResponse.getFault().getFaultCode().length()) + faultString;
                }
                recipient.setDhxFault(
                        faultString.substring(0, (faultString.length() > 1900 ? 1900 : faultString.length())));
            }
            if (allSent) {
                List<DocumentSharing> recipients = documentSharingDao.getDVKSharings(recipient.getDocumentId());
                for (DocumentSharing docRecipient : recipients) {
                    if (DocumentService.SHARINGTYPE_SEND_DHX.equalsIgnoreCase(docRecipient.getDocumentSharingType())
                            && !(docRecipient.getDocumentDvkStatus() == null)
                            && !new Long(docRecipient.getDocumentDvkStatus()).equals(DocumentService.DHX_STATUS_SENT)
                            && !docRecipient.getId().equals(recipient.getId())) {
                        allSent = false;
                        break;
                    }
                }
            }
            dhxSession.saveOrUpdate(recipient);
            dhxTransaction.commit();
        } catch (Throwable ex) {
            log.error("Error occured while saving send results. " + ex.getMessage(), ex);
            if (dhxTransaction != null) {
                dhxTransaction.rollback();
            }
        } finally {
            if ((dhxSession != null) && dhxSession.isOpen()) {
                dhxSession.close();
            }
            try {
                dhxService.cleanupContainer(finalResult.getSentPackage().getParsedContainer());
            } catch (DhxException ex) {
                log.error("Error occured while cleaningup container.", ex);
            }

        }

    }
}
