package ee.adit.pojo;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ListMethodsResponse", propOrder = {"item" })
public class ListMethodsResponse {
    @XmlElement(name = "item")
    private List<String> item;

    public List<String> getItem() {
        if (item == null) {
            item = new ArrayList<String>();
        }
        return this.item;
    }

    public void setItem(List<String> itemParam) {
        this.item = itemParam;
    }

    public void addItem(String itemParam) {
        if (this.item == null) {
            this.item = new ArrayList<String>();
        }
        this.item.add(itemParam);
    }
}
