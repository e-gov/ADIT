//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.09.05 at 06:48:15 PM EEST 
//


package ee.adit.generated.xroad;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for XRoadObjectType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="XRoadObjectType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="MEMBER"/>
 *     &lt;enumeration value="SUBSYSTEM"/>
 *     &lt;enumeration value="SERVER"/>
 *     &lt;enumeration value="GLOBALGROUP"/>
 *     &lt;enumeration value="LOCALGROUP"/>
 *     &lt;enumeration value="SECURITYCATEGORY"/>
 *     &lt;enumeration value="SERVICE"/>
 *     &lt;enumeration value="CENTRALSERVICE"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "XRoadObjectType")
@XmlEnum
public enum XRoadObjectType {

    MEMBER,
    SUBSYSTEM,
    SERVER,
    GLOBALGROUP,
    LOCALGROUP,
    SECURITYCATEGORY,
    SERVICE,
    CENTRALSERVICE;

    public String value() {
        return name();
    }

    public static XRoadObjectType fromValue(String v) {
        return valueOf(v);
    }

}
