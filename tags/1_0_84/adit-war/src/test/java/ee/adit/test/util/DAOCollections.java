package ee.adit.test.util;

import ee.adit.dao.AditUserDAO;
import ee.adit.dao.DocumentDAO;
import ee.adit.dao.DocumentFileDAO;
import ee.adit.dao.DocumentSharingDAO;

public class DAOCollections {
    private DocumentDAO documentDAO;
    private AditUserDAO aditUserDAO;
    private DocumentSharingDAO documentSharingDAO;
    private DocumentFileDAO documentFileDAO;

    public DAOCollections() {
    }

    public DAOCollections(DocumentDAO documentDAO, AditUserDAO aditUserDAO,
                          DocumentSharingDAO documentSharingDAO,
                          DocumentFileDAO documentFileDAO) {
        this.setDocumentDAO(documentDAO);
        this.setAditUserDAO(aditUserDAO);
        this.setDocumentSharingDAO(documentSharingDAO);
        this.setDocumentFileDAO(documentFileDAO);
    }

    public DocumentDAO getDocumentDAO() {
        return documentDAO;
    }

    public void setDocumentDAO(DocumentDAO documentDAO) {
        this.documentDAO = documentDAO;
    }

    public AditUserDAO getAditUserDAO() {
        return aditUserDAO;
    }

    public void setAditUserDAO(AditUserDAO aditUserDAO) {
        this.aditUserDAO = aditUserDAO;
    }

    public DocumentSharingDAO getDocumentSharingDAO() {
        return documentSharingDAO;
    }

    public void setDocumentSharingDAO(DocumentSharingDAO documentSharingDAO) {
        this.documentSharingDAO = documentSharingDAO;
    }

    public DocumentFileDAO getDocumentFileDAO() {
        return documentFileDAO;
    }

    public void setDocumentFileDAO(DocumentFileDAO documentFileDAO) {
        this.documentFileDAO = documentFileDAO;
    }
}
