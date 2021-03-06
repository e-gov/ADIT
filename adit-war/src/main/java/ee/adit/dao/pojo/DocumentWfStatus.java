package ee.adit.dao.pojo;

// Generated 21.06.2010 14:02:03 by Hibernate Tools 3.2.4.GA

import java.util.HashSet;
import java.util.Set;

/**
 * DocumentWfStatus generated by hbm2java
 */
public class DocumentWfStatus implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private long id;
    private String description;
    private String name;
    private Set<DocumentSharing> documentSharings = new HashSet<DocumentSharing>(0);

    public DocumentWfStatus() {
    }

    public DocumentWfStatus(long id) {
        this.id = id;
    }

    public DocumentWfStatus(long id, String description, String name, Set<DocumentSharing> documentSharings) {
        this.id = id;
        this.description = description;
        this.name = name;
        this.documentSharings = documentSharings;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<DocumentSharing> getDocumentSharings() {
        return this.documentSharings;
    }

    public void setDocumentSharings(Set<DocumentSharing> documentSharings) {
        this.documentSharings = documentSharings;
    }

}
