package ee.adit.pojo;

import java.util.List;

public class GetDocumentListResponseAttachmentV2 {
    private int total;
    private List<OutputDocument> documentList;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<OutputDocument> getDocumentList() {
        return documentList;
    }

    public void setDocumentList(List<OutputDocument> documentList) {
        this.documentList = documentList;
    }
}
