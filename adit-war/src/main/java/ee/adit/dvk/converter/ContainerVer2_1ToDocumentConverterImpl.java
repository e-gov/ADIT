package ee.adit.dvk.converter;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;

import ee.adit.dvk.api.container.v2_1.ContainerVer2_1;
import ee.adit.dvk.api.container.v2_1.DecSender;
import ee.adit.dvk.api.container.v2_1.File;
import ee.adit.dvk.api.ml.PojoMessage;
import ee.adit.dao.AditUserDAO;
import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentFile;
import ee.adit.dvk.converter.containerdocument.OutputDocumentFileBuilder;
import ee.adit.pojo.OutputDocumentFile;
import ee.adit.service.DocumentService;
import ee.adit.util.DigiDocExtractionResult;
import ee.adit.util.Util;

/**
 * @author Hendrik Pärna
 * @since 10.06.14
 */
public class ContainerVer2_1ToDocumentConverterImpl implements Converter<ContainerVer2_1, Document> {

    private static Logger logger = LogManager.getLogger(ContainerVer2_1ToDocumentConverterImpl.class);
    private PojoMessage pojoMessage;
    private AditUserDAO aditUserDAO;
    private String jdigidocCfgTmpFile;
    private DocumentService documentService;
    private OutputDocumentFileBuilder outputDocumentFileBuilder;
    private AditUser senderUser;

    /**
     * Constructor.
     *
     * @param pojoMessage dvk message
     */
    public ContainerVer2_1ToDocumentConverterImpl(final PojoMessage pojoMessage) {
        this.pojoMessage = pojoMessage;
    }


    @Override
    public Document convert(final ContainerVer2_1 container) {
        Document document = new Document();
        document.setCreationDate(new Date());
        document.setLastModifiedDate(new Date());
        document.setDocumentDvkStatusId(DocumentService.DVK_STATUS_SENT);
        document.setDvkId(pojoMessage.getDhlId());
        document.setDhxId(String.valueOf(pojoMessage.getDhlMessageId()));
        document.setGuid(pojoMessage.getDhlGuid());
        document.setLocked(true);
        document.setLockingDate(new Date());
        document.setSignable(true);
        document.setTitle(pojoMessage.getTitle());
        document.setDocumentType(DocumentService.DOCTYPE_LETTER);
        document.setSigned(isSigned(container.getFile()));
        if(container.getRecordMetadata() != null) document.setContent(container.getRecordMetadata().getRecordAbstract()); 
        // Make sure that document sender exists as a user in ADIT
        senderUser = getSender(container.getTransport().getDecSender());

        if (senderUser == null) {
            throw new IllegalStateException("Unable to find the sender user!");
        }

        // The creator is the sender
        document.setCreatorCode(senderUser.getUserCode());
        document.setCreatorName(senderUser.getFullName());

        List<OutputDocumentFile> documentFiles = new OutputDocumentFileBuilder(documentService.getConfiguration(), container).build();
        getAndUpdateSignatures(fillFileTypes(documentFiles), document);

        return document;
    }

    /**
     * Fill the fileType for Digidoc files.
     * @param documentFiles
     * @return
     */
    public List<OutputDocumentFile> fillFileTypes(List<OutputDocumentFile> documentFiles) {
        if (documentFiles != null) {
            for (OutputDocumentFile file : documentFiles) {
                DigiDocExtractionResult digiDocExtractionResult = getDigiDocExtractionResult(file);
                if (digiDocExtractionResult != null && digiDocExtractionResult.isSuccess()) {
                    file.setFileType(DocumentService.FILETYPE_NAME_SIGNATURE_CONTAINER);
                }
            }
        }

        return documentFiles;
    }

    private DigiDocExtractionResult getDigiDocExtractionResult(OutputDocumentFile file) {
        DigiDocExtractionResult result = null;
        String extension = Util.getFileExtension(file.getName());

        if (((file.getId() == null) || (file.getId() <= 0)) && "ddoc".equalsIgnoreCase(extension)) {
            result = documentService.extractDigiDocContainer(file.getSysTempFile(), jdigidocCfgTmpFile);
        }

        return result;
    }

    private void getAndUpdateSignatures(final List<OutputDocumentFile> outputDocumentFiles, final Document document) {
        if (outputDocumentFiles != null) {
            for (OutputDocumentFile file : outputDocumentFiles) {
                DigiDocExtractionResult extractionResult = getDigiDocExtractionResult(file);

                if (extractionResult != null && extractionResult.isSuccess()) {
                    document.setSigned((extractionResult.getSignatures() != null) && (extractionResult.getSignatures().size() > 0));

                    for (int i = 0; i < extractionResult.getFiles().size(); i++) {
                        DocumentFile df = extractionResult.getFiles().get(i);
                        df.setDocument(document);
                        document.getDocumentFiles().add(df);
                    }

                    for (int i = 0; i < extractionResult.getSignatures().size(); i++) {
                        ee.adit.dao.pojo.Signature sig = extractionResult.getSignatures().get(i);
                        sig.setDocument(document);
                        document.getSignatures().add(sig);
                    }
                }
            }
        }
    }

    private AditUser getSender(final DecSender decSender) {
        if (decSender == null) {
            throw new IllegalStateException("Unable to get the sender from container");
        }
        AditUser result = this.getAditUserDAO().getUserByID(addPrefixIfNecessary(decSender.getOrganisationCode()));

        if (result == null || Util.isNullOrEmpty(result.getUserCode())) {
            throw new IllegalStateException("Unable to get the sender from database");
        }

        return result;
    }

    private String addPrefixIfNecessary(String code) {
        if (code != null && !code.toUpperCase().startsWith("EE")) {
            return "EE" + code;
        }
        return code;
    }

    private boolean isSigned(final List<File> files) {
        boolean result = false;

        if (files != null) {
            for (File file : files) {
                if (file.getFileName().toLowerCase().contains(".ddoc")) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public AditUserDAO getAditUserDAO() {
        return aditUserDAO;
    }

    public void setAditUserDAO(final AditUserDAO aditUserDAO) {
        this.aditUserDAO = aditUserDAO;
    }

    public String getJdigidocCfgTmpFile() {
        return jdigidocCfgTmpFile;
    }

    public void setJdigidocCfgTmpFile(final String jdigidocCfgTmpFile) {
        this.jdigidocCfgTmpFile = jdigidocCfgTmpFile;
    }

    public DocumentService getDocumentService() {
        return documentService;
    }

    public void setDocumentService(final DocumentService documentService) {
        this.documentService = documentService;
    }

    public OutputDocumentFileBuilder getOutputDocumentFileBuilder() {
        return outputDocumentFileBuilder;
    }

    public void setOutputDocumentFileBuilder(final OutputDocumentFileBuilder outputDocumentFileBuilder) {
        this.outputDocumentFileBuilder = outputDocumentFileBuilder;
    }

    public AditUser getSenderUser() {
        return senderUser;
    }
}
