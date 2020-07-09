package ee.adit.service.dhx;

import ee.adit.dao.pojo.DocumentType;
import ee.adit.dhx.api.container.v2_1.ContainerVer2_1;
import ee.adit.dhx.api.container.v2_1.PersonType;
import ee.adit.dhx.api.container.v2_1.Recipient;
import ee.adit.exception.AditUserInactiveException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class RuuterDhxProcessingErrorRequestsBuilder {

    private static Logger logger = LogManager.getLogger(RuuterDhxProcessingErrorRequestsBuilder.class);

    private final List<DocumentType> aditDocumentTypes;
    private ContainerVer2_1 containerVer2_1;
    private Exception error;


    public RuuterDhxProcessingErrorRequestsBuilder(ContainerVer2_1 containerVer2_1, Exception error, List<DocumentType> aditDocumentTypes) {
        this.containerVer2_1 = containerVer2_1;
        this.error = error;
        this.aditDocumentTypes = aditDocumentTypes;
    }

    public List<RuuterDhxProcessingErrorRequest> build() {
        List<RuuterDhxProcessingErrorRequest> dhxProcessingErrorRequests = new ArrayList<>();
        for (Recipient recipient : containerVer2_1.getRecipient()) {
            RuuterDhxProcessingErrorRequest request = new RuuterDhxProcessingErrorRequest();

            // Set recipient specific fields
            parseRecipientSpecificErrorDetails(recipient, request);
            // Set error specific fields
            parseErrorType(error, recipient, request);
            // Set ADIT Code for documentType
            parseAditDocumentType(containerVer2_1, aditDocumentTypes, request);

            request.setDocument(containerVer2_1);
            dhxProcessingErrorRequests.add(request);
        }
        return dhxProcessingErrorRequests;
    }

    private void parseAditDocumentType(ContainerVer2_1 containerVer2_1, List<DocumentType> aditDocumentTypes, RuuterDhxProcessingErrorRequest request) {
        String dhxRecordType = containerVer2_1.getRecordMetadata() != null ? containerVer2_1.getRecordMetadata().getRecordType() : null;
        for (DocumentType aditDocumentType : aditDocumentTypes) {
            if (aditDocumentType.getDescription().equals(dhxRecordType)) {
                request.setAditDocumentType(aditDocumentType.getShortName());
                return;
            }
        }
        logger.error("Container recordType {} did not match any allowed ADIT documentType values {}", dhxRecordType, aditDocumentTypes);
    }

    private void parseErrorType(Exception ex, Recipient recipient, RuuterDhxProcessingErrorRequest request) {
        DhxProcessingErrorType errorType = DhxProcessingErrorType.UNSPECIFIED;

        if (AditUserInactiveException.class.isInstance(ex)) {
            String inactiveUserPersonalCode = ((AditUserInactiveException) ex).getInactiveUserPersonalCode();
            if (recipient.getPerson() != null && inactiveUserPersonalCode.equals(recipient.getPerson().getPersonalIdCode())) {
                errorType = DhxProcessingErrorType.ACTIVE_USER_NOT_FOUND;
            }
        }
        request.setErrorCode(errorType);
    }

    private void parseRecipientSpecificErrorDetails(Recipient recipient, RuuterDhxProcessingErrorRequest request) {
        if (recipient.getPerson() != null) {
            request.setRecipientUserType(DhxRecipientUserType.PERSON);
            PersonType person = recipient.getPerson();
            request.setRecipientCode(person.getPersonalIdCode());
            request.setRecipientUserName(person.getGivenName() + " " + person.getSurname());
        } else if (recipient.getOrganisation() != null) {
            request.setRecipientUserType(DhxRecipientUserType.ORGANISATION);
            request.setRecipientCode(recipient.getOrganisation().getOrganisationCode());
            request.setRecipientUserName(recipient.getOrganisation().getName());
        } else {
            logger.error("Could not identify userType from the container");
        }
    }
}
