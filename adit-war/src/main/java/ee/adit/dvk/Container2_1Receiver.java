package ee.adit.dvk;

import dvk.api.container.v2_1.ContainerVer2_1;
import dvk.api.container.v2_1.DecRecipient;
import dvk.api.container.v2_1.Recipient;
import dvk.api.ml.PojoMessage;
import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.dvk.converter.ContainerVer2_1ToDocumentConverterImpl;
import ee.adit.dvk.converter.containerdocument.OutputDocumentFileBuilder;
import ee.adit.dvk.converter.containerdocument.RecipientsBuilder;
import ee.adit.pojo.OutputDocumentFile;
import ee.adit.pojo.SaveItemInternalResult;
import ee.adit.service.DocumentService;

import java.util.Calendar;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author Hendrik Pärna
 * @since 12.06.14
 */
public class Container2_1Receiver implements DvkReceiver {

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
    public boolean receive(final PojoMessage message) {
        ContainerVer2_1 containerVer2_1 = documentService.getDVKContainer2_1(message);
        ContainerVer2_1ToDocumentConverterImpl converter = createConverter(message, containerVer2_1);

        Document document = converter.convert(containerVer2_1);
        //this must be after conversion, because we need the data which is created during the conversion itself
        validateMessage(message, containerVer2_1, converter);

        saveDocumentToAdit(message, converter, document);
        sendToRecipients(message, document, containerVer2_1);

        try {
            documentService.getDvkDAO().updateDocumentLocalId(document.getId(), message.getDhlMessageId());
        } catch (Exception e) {
            throw new RuntimeException("Unable to update dhl_message_local_id in dvk client", e);
        }

        return true;
    }

    private ContainerVer2_1ToDocumentConverterImpl createConverter(
            final PojoMessage message, final ContainerVer2_1 containerVer2_1) {
        ContainerVer2_1ToDocumentConverterImpl converter = new ContainerVer2_1ToDocumentConverterImpl(message);
        converter.setAditUserDAO(documentService.getAditUserDAO());
        converter.setDocumentService(documentService);
        converter.setOutputDocumentFileBuilder(new OutputDocumentFileBuilder(documentService.getConfiguration(), containerVer2_1));
        converter.setJdigidocCfgTmpFile(jdigidocCfgTmpFile);
        return converter;
    }

    private void validateMessage(final PojoMessage message,
                                 final ContainerVer2_1 containerVer2_1,
                                 final ContainerVer2_1ToDocumentConverterImpl converter) {
        // Make sure that exactly the same document has not been received before.
        // This does not mean that future versions of the same document are blocked
        Boolean documentAlreadyReceived = documentService.getDocumentDAO()
                .checkIfDocumentExists(message.getDhlId(), converter.getSenderUser().getDvkOrgCode());
        if (documentAlreadyReceived) {
            throw new RuntimeException("Unable to receive document from DVK because"
                    + " this document has been received before. Message DVK ID: "
                    + message.getDhlId() + ". Message DVK GUID: "
                    + message.getDhlGuid());
        }

        // Get list of recipients and make sure that at least one recipient exists
        List<DecRecipient> recipients = containerVer2_1.getTransport().getDecRecipient();
        if ((recipients == null) || (recipients.size() < 1)) {
            throw new RuntimeException("Unable to receive document from DVK because DVK envelope"
                    + " does not contain recipient data. Message DVK ID: " + message.getDhlId()
                    + ". Message DVK GUID: " + message.getDhlGuid());
        }
    }

    private void saveDocumentToAdit(final PojoMessage message,
                                    final ContainerVer2_1ToDocumentConverterImpl converter,
                                    final Document document) {
        List<OutputDocumentFile> tempDocuments = converter.getOutputDocumentFileBuilder().build();

        try {
            AditUser senderUser = converter.getSenderUser();
            SaveItemInternalResult saveResult = documentService.getDocumentDAO()
                    .save(document, converter.fillFileTypes(tempDocuments), Long.MAX_VALUE);

            if (saveResult == null || !saveResult.isSuccess()) {
                throw new RuntimeException("Unable to save document: dvkId" + message.getDhlId());
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

    private void sendToRecipients(final PojoMessage message, final Document document,
                                  final ContainerVer2_1 containerVer2_1) {
        RecipientsBuilder recipientsBuilder = new RecipientsBuilder(containerVer2_1);
        recipientsBuilder.setAditUserDAO(documentService.getAditUserDAO());
        recipientsBuilder.setConfiguration(documentService.getConfiguration());

        for (Pair<AditUser, String> aditUserMessageForRecipient : recipientsBuilder.build()) {
            documentService.sendDocument(document,
                    aditUserMessageForRecipient.getLeft(), null,
                    message.getDhlId(), aditUserMessageForRecipient.getRight());
        }
    }


}
