package ee.adit.dvk.converter;

import dvk.api.container.v2_1.*;
import ee.adit.dao.AditUserDAO;
import ee.adit.dao.DocumentTypeDAO;
import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentSharing;
import ee.adit.dvk.converter.documentcontainer.*;
import ee.adit.util.Configuration;

import java.util.*;

/**
 * @author Hendrik Pärna
 * @since 22.04.14
 */
public class DocumentContainerVer2_1ConverterImpl implements Converter<Document, ContainerVer2_1> {

    private AditUserDAO aditUserDAO;
    private DocumentTypeDAO documentTypeDAO;
    private Configuration configuration;

    @Override
    public ContainerVer2_1 convert(final Document document) {
        if (document == null) {
            throw new IllegalArgumentException("Document should not be null!");
        }

        ContainerVer2_1 container = new ContainerVer2_1();
        container.setTransport(createTransport(document));
        container.setDecMetadata(createDecMetaData(document));
        container.setRecordMetadata(createRecordMetadata(document));
        container.setRecordCreator(createRecordCreator(document));
        container.setRecordSenderToDec(createRecordSenderToDec(document));
        container.setSignatureMetadata(createSignatureMetadata(document));
        container.setRecipient(createRecipients(document, findRecipientsAditUsers(document)));
        container.setAccess(createAccess(document));
        container.setRecordSenderToDec(createRecordSenderToDec(document));
        container.setFile(createFiles(document));

        return container;
    }

    private List<File> createFiles(final Document document) {
        return new FileBuilder(document).build();
    }

    private List<AditUser> findRecipientsAditUsers(final Document document) {
        List<AditUser> results = new ArrayList<AditUser>();
        if (document.getDocumentSharings() != null) {
            for (DocumentSharing documentSharing: document.getDocumentSharings()) {
                AditUser aditUser = aditUserDAO.getUserByID(documentSharing.getUserCode());
                if (aditUser != null) {
                    results.add(aditUser);
                }
            }
        }

        return results;
    }

    private Access createAccess(final Document document) {
        return new AccessBuilder(document).build();
    }

    private List<Recipient> createRecipients(final Document document, final List<AditUser> recipients) {
        return new RecipientBuilder(document, recipients).build();
    }

    protected List<SignatureMetadata> createSignatureMetadata(final Document document) {
        return new SignatureMetadataBuilder(document).build();
    }

    private RecordSenderToDec createRecordSenderToDec(final Document document) {
        return new RecordSenderToDecBuilder(document, aditUserDAO).build();
    }

    protected RecordMetadata createRecordMetadata(final Document document) {
        return new RecordMetadataBuilder(document, documentTypeDAO).build();
    }

    protected RecordCreator createRecordCreator(final Document document) {
        return new RecordCreatorBuilder(document, aditUserDAO).build();
    }

    //TODO: should it be removed
    protected DecMetadata createDecMetaData(final Document document) {
        DecMetadata decMetadata = new DecMetadata();
        decMetadata.setDecId(document.getDvkId().toString());
        //TODO: finish me
        //decMetadata.setDecFolder();
        //decMetadata.setDecReceiptDate();
        return decMetadata;
    }

    protected Transport createTransport(final Document document) {
        return new TransportBuilder(document, aditUserDAO, configuration).build();
    }

    public AditUserDAO getAditUserDAO() {
        return aditUserDAO;
    }

    public void setAditUserDAO(final AditUserDAO aditUserDAO) {
        this.aditUserDAO = aditUserDAO;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(final Configuration configuration) {
        this.configuration = configuration;
    }

    public DocumentTypeDAO getDocumentTypeDAO() {
        return documentTypeDAO;
    }

    public void setDocumentTypeDAO(DocumentTypeDAO documentTypeDAO) {
        this.documentTypeDAO = documentTypeDAO;
    }
}
