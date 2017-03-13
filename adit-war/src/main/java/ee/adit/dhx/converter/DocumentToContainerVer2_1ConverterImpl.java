package ee.adit.dhx.converter;

import java.util.List;

import java.util.Set;

import ee.adit.dao.AditUserDAO;
import ee.adit.dao.DocumentTypeDAO;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentSharing;
import ee.adit.dhx.api.container.v2_1.Access;
import ee.adit.dhx.api.container.v2_1.File;
import ee.adit.dhx.api.container.v2_1.Recipient;
import ee.adit.dhx.api.container.v2_1.RecordCreator;
import ee.adit.dhx.api.container.v2_1.RecordMetadata;
import ee.adit.dhx.api.container.v2_1.RecordSenderToDec;
import ee.adit.dhx.api.container.v2_1.SignatureMetadata;
import ee.adit.dhx.api.container.v2_1.Transport;
import ee.adit.dhx.converter.documentcontainer.AccessBuilder;
import ee.adit.dhx.converter.documentcontainer.FileBuilder;
import ee.adit.dhx.converter.documentcontainer.RecipientBuilder;
import ee.adit.dhx.converter.documentcontainer.RecordCreatorBuilder;
import ee.adit.dhx.converter.documentcontainer.RecordMetadataBuilder;
import ee.adit.dhx.converter.documentcontainer.RecordSenderToDecBuilder;
import ee.adit.dhx.converter.documentcontainer.SignatureMetadataBuilder;
import ee.adit.dhx.converter.documentcontainer.TransportBuilder;
import ee.adit.util.Configuration;


/**
 * @author Hendrik Pärna
 * @since 22.04.14
 */
public class DocumentToContainerVer2_1ConverterImpl implements Converter<Document, ee.adit.dhx.api.container.v2_1.ContainerVer2_1> {

    private AditUserDAO aditUserDAO;
    private DocumentTypeDAO documentTypeDAO;
    private Configuration configuration;

    @Override
    public ee.adit.dhx.api.container.v2_1.ContainerVer2_1 convert(final Document document) {
        if (document == null) {
            throw new IllegalArgumentException("Document should not be null!");
        }

        ee.adit.dhx.api.container.v2_1.ContainerVer2_1 container = new ee.adit.dhx.api.container.v2_1.ContainerVer2_1();
        container.setTransport(createTransport(document));
        container.setRecordMetadata(createRecordMetadata(document));
        container.setRecordCreator(createRecordCreator(document));
        container.setRecordSenderToDec(createRecordSenderToDec(document));
        container.setSignatureMetadata(createSignatureMetadata(document));
        container.setRecipient(createRecipients(document, document.getDocumentSharings()));
        container.setAccess(createAccess());
        container.setRecordSenderToDec(createRecordSenderToDec(document));
        container.setFile(createFiles(document));

        return container;
    }

    protected List<File> createFiles(final Document document) {
        return new FileBuilder(document, configuration).build();
    }

    protected Access createAccess() {
        return new AccessBuilder().build();
    }

    protected List<Recipient> createRecipients(final Document document, final Set<DocumentSharing> documentSharings) {
        return new RecipientBuilder(document, aditUserDAO, documentSharings).build();
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

    protected Transport createTransport(final Document document) {
        return new TransportBuilder(document, aditUserDAO, configuration).build();
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

    public void setDocumentTypeDAO(final DocumentTypeDAO documentTypeDAO) {
        this.documentTypeDAO = documentTypeDAO;
    }
}
