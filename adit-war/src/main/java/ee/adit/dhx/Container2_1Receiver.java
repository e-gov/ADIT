package ee.adit.dhx;

import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;

import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.dhx.api.container.v2_1.ContainerVer2_1;
import ee.adit.dhx.api.container.v2_1.DecRecipient;
import ee.adit.dhx.api.container.v2_1.Recipient;
import ee.adit.dhx.converter.ContainerVer2_1ToDocumentConverterImpl;
import ee.adit.dhx.converter.containerdocument.OutputDocumentFileBuilder;
import ee.adit.dhx.converter.containerdocument.RecipientsBuilder;
import ee.adit.pojo.OutputDocumentFile;
import ee.adit.pojo.SaveItemInternalResult;
import ee.adit.service.DocumentService;

/**
 * @author Hendrik Pärna
 * @since 12.06.14
 */
public class Container2_1Receiver implements DhxReceiver {
    private static Logger logger = LogManager.getLogger(Container2_1Receiver.class);

    private DocumentService documentService;
    private String jdigidocCfgTmpFile;

    /**
     * Constructor.
     *
     * @param documentService    {@link DocumentService}
     * @param jdigidocCfgTmpFile temp file
     */
    public Container2_1Receiver(final DocumentService documentService, final String jdigidocCfgTmpFile) {
        this.documentService = documentService;
        this.jdigidocCfgTmpFile = jdigidocCfgTmpFile;
    }
    


	@Override
    public Long receive(final String containerFile, String consignmentId) {
    	ContainerVer2_1 containerVer2_1 = ContainerVer2_1.parseFile(containerFile);
        //ContainerVer2_1 containerVer2_1 = documentService.getDVKContainer2_1(message);
        ContainerVer2_1ToDocumentConverterImpl converter = createConverter(containerVer2_1, consignmentId);

        Document document = converter.convert(containerVer2_1);
        //this must be after conversion, because we need the data which is created during the conversion itself
        validateMessage(containerVer2_1, converter, consignmentId);

        initDocumentParentId(containerVer2_1, document);
        saveDocumentToAdit(converter, document);
        sendToRecipients(converter.getSenderUser(), document, containerVer2_1);

        /*try {
            documentService.getDhxDAO().updateDocumentLocalId(document.getId(), message.getDhlMessageId());
        } catch (Exception e) {
            throw new RuntimeException("Unable to update dhl_message_local_id in dvk client", e);
        }*/

        return document.getId();
    }
    
    private ContainerVer2_1ToDocumentConverterImpl createConverter(
            final ContainerVer2_1 containerVer2_1, final String consignmentId) {
        ContainerVer2_1ToDocumentConverterImpl converter = new ContainerVer2_1ToDocumentConverterImpl(consignmentId);
        converter.setAditUserDAO(documentService.getAditUserDAO());
        converter.setDocumentService(documentService);
        converter.setOutputDocumentFileBuilder(new OutputDocumentFileBuilder(documentService.getConfiguration(), containerVer2_1));
        converter.setJdigidocCfgTmpFile(jdigidocCfgTmpFile);
        return converter;
    }

    private void validateMessage(
                                 final ContainerVer2_1 containerVer2_1,
                                 final ContainerVer2_1ToDocumentConverterImpl converter, final String consignmentId) {
        // Make sure that exactly the same document has not been received before.
        // This does not mean that future versions of the same document are blocked
        /*Boolean documentAlreadyReceived = documentService.getDocumentDAO()
                .checkIfDocumentExists(message.getDhlId(), converter.getSenderUser().getDvkOrgCode());*/
       /* if (documentAlreadyReceived) {
            throw new RuntimeException("Unable to receive document from DHX because"
                    + " this document has been received before. Message DHX ID: "
                    + message.getDhlId() + ". Message DHX GUID: "
                    + message.getDhlGuid());
        }*/

        // Get list of recipients and make sure that at least one recipient exists
        List<DecRecipient> recipients = containerVer2_1.getTransport().getDecRecipient();
        if ((recipients == null) || (recipients.size() < 1)) {
            throw new RuntimeException("Unable to receive document from DHX because DHX envelope"
                    + " does not contain recipient data. Message DHX consignment ID: " + consignmentId);
        }
    }

    private void saveDocumentToAdit(
                                    final ContainerVer2_1ToDocumentConverterImpl converter,
                                    final Document document) {
        List<OutputDocumentFile> tempDocuments = converter.getOutputDocumentFileBuilder().build();

        try {
            AditUser senderUser = converter.getSenderUser();
            SaveItemInternalResult saveResult = documentService.getDocumentDAO()
                    .save(document, converter.fillFileTypes(tempDocuments), Long.MAX_VALUE);

            if (saveResult == null || !saveResult.isSuccess()) {
                throw new RuntimeException("Unable to save document");
            }

            addHistoryEventForSignatureExtraction(document, senderUser, saveResult);
            updateUserQuotaLimit(senderUser, saveResult);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void updateUserQuotaLimit(final AditUser senderUser, final SaveItemInternalResult saveResult) {
        // Update user quota limit
        Long usedDiskQuota = senderUser.getDiskQuotaUsed();
        if (usedDiskQuota == null) {
            usedDiskQuota = 0L;
        }
        senderUser.setDiskQuotaUsed(usedDiskQuota + saveResult.getAddedFilesSize());
        documentService.getAditUserDAO().saveOrUpdate(senderUser, true);
    }

    private void addHistoryEventForSignatureExtraction(final Document document,
                                                       final AditUser senderUser,
                                                       final SaveItemInternalResult saveResult) {
        if (document.getSignatures() != null && document.getSignatures().size() > 0) {
            // Add signature container extraction history event
            documentService.addHistoryEvent("", saveResult.getItemId(), senderUser.getUserCode(),
                    DocumentService.HISTORY_TYPE_EXTRACT_FILE, senderUser.getUserCode(), senderUser.getFullName(),
                    DocumentService.DOCUMENT_HISTORY_DESCRIPTION_EXTRACT_FILE, senderUser.getFullName(),
                    Calendar.getInstance().getTime());
        }
    }

    private void sendToRecipients(final AditUser sender, final Document document,
                                  final ContainerVer2_1 containerVer2_1) {
        RecipientsBuilder recipientsBuilder = new RecipientsBuilder(containerVer2_1);
        recipientsBuilder.setAditUserDAO(documentService.getAditUserDAO());
        recipientsBuilder.setConfiguration(documentService.getConfiguration());

        for (Pair<AditUser, Recipient> aditUserRecipient : recipientsBuilder.build()) {
            documentService.sendDocumentAndNotifyRecipient(document, sender, aditUserRecipient.getLeft(), null,
                    document.getDvkId(), aditUserRecipient.getRight().getMessageForRecipient(), String.valueOf(document.getId()), document.getDhxConsignmentId());
        }
    }

    private void initDocumentParentId(final ContainerVer2_1 container, final Document document) {
        RecipientsBuilder recipientsBuilder = new RecipientsBuilder(container);
        recipientsBuilder.setAditUserDAO(documentService.getAditUserDAO());
        recipientsBuilder.setConfiguration(documentService.getConfiguration());

        // Get the first not null RecipientRecordOriginalIdentifier found in adit recipients
        for (Pair<AditUser, Recipient> aditUserRecipient : recipientsBuilder.build()) {
            String recipientRecordOriginalIdentifier = aditUserRecipient.getRight().getRecipientRecordOriginalIdentifier();
            if (recipientRecordOriginalIdentifier != null && recipientRecordOriginalIdentifier.length() != 0) {
                try {
                    Long parentId = Long.valueOf(recipientRecordOriginalIdentifier);
                    Document parentDocument = documentService.getDocumentDAO().getDocument(parentId);
                    if (parentDocument != null) {
                        document.setDocument(parentDocument);
                        return;
                    }
                } catch (NumberFormatException ex) {
                    logger.info("Invalid related adit doc id: " +  recipientRecordOriginalIdentifier);
                }
            }
        }
    }

}
