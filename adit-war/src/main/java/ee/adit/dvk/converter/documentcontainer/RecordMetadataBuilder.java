package ee.adit.dvk.converter.documentcontainer;

import dvk.api.container.v2_1.RecordMetadata;
import ee.adit.dao.DocumentTypeDAO;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentType;
import ee.adit.dao.pojo.Signature;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;

/**
 * @author Hendrik Pärna
 * @since 6.05.14
 */
public class RecordMetadataBuilder {

    private Document document;
    private DocumentTypeDAO documentTypeDAO;

    /**
     * Constructor.
     * @param document {@link Document}
     * @param documentTypeDAO {@link DocumentTypeDAO}
     */
    public RecordMetadataBuilder(final Document document, final DocumentTypeDAO documentTypeDAO) {
        this.document = document;
        this.documentTypeDAO = documentTypeDAO;
    }

    /**
     * Builds a {@link RecordMetadata}.
     * @return recordMetadata
     */
    public RecordMetadata build() {
        RecordMetadata recordMetadata = new RecordMetadata();

        DocumentType docType = documentTypeDAO.getDocumentType(document.getDocumentType());
        if (docType != null) {
            recordMetadata.setRecordType(docType.getDescription());
        }

        recordMetadata.setRecordOriginalIdentifier(String.valueOf(document.getId()));
        recordMetadata.setRecordDateRegistered(getRecordDateRegistered(document));
        recordMetadata.setRecordTitle(document.getTitle());

        return recordMetadata;
    }

    private Date getRecordDateRegistered(final Document document) {
        Date recordDateRegistered = getOldestSignatureDate(document.getSignatures());

        if (recordDateRegistered == null) {
            recordDateRegistered = document.getCreationDate();
        }

        return recordDateRegistered;
    }

    /**
     * Finds oldest signature of signing.
     * @param signatures Set of signatures
     * @return oldsest signatureSigningDate
     */
    private Date getOldestSignatureDate(final Set<Signature> signatures) {
        Date documentSignatureDate = null;
        if (signatures != null) {
            Calendar cal = Calendar.getInstance();
            for (Signature signature : signatures) {
                if (documentSignatureDate == null) {
                    documentSignatureDate = signature.getSigningDate();
                } else {
                    cal.setTime(documentSignatureDate);
                    if (cal.after(signature.getSigningDate())) {
                        documentSignatureDate = signature.getSigningDate();
                    }
                }
            }
        }
        return documentSignatureDate;
    }
}
