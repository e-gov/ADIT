package ee.adit.test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

import ee.adit.pojo.notification.LisaSyndmusRequest;
import ee.adit.pojo.notification.LisaSyndmusRequestKasutaja;
import ee.adit.pojo.notification.LisaSyndmusRequestLugejad;
import ee.adit.util.Util;

public class TestNotificationCastor {
	
	/**
	 * @param args
	 * @throws MappingException 
	 * @throws IOException 
	 * @throws ValidationException 
	 * @throws MarshalException 
	 */
	public static void main(String[] args) throws IOException, MappingException, MarshalException, ValidationException {
		
		// Load Mapping
		/*Mapping mapping = new Mapping();
		mapping.loadMapping("C:\\development\\adit\\adit-war\\src\\main\\resources\\adit-castor-mapping.xml");
		StringWriter sw = new StringWriter();
		
		Marshaller m = new Marshaller();
		m.setMapping(mapping);
		m.setWriter(sw);
		
		Date d = new Date();
		
		LisaSyndmusRequest o = new LisaSyndmusRequest();
		
		LisaSyndmusRequestLugejad l = new LisaSyndmusRequestLugejad();
		
		ArrayList<LisaSyndmusRequestKasutaja> lugejad = new ArrayList<LisaSyndmusRequestKasutaja>();
		LisaSyndmusRequestKasutaja lugeja = new LisaSyndmusRequestKasutaja();
		lugeja.setKasutajaTyyp("asutus");
		lugeja.setKood("70006317");
		lugejad.add(lugeja);		
		lugejad.add(lugeja);
		
		l.setKasutajad(lugejad);
		l.setType("kasutaja[1]");
		l.setXsiType("SOAP-ENC:Array");
		
		o.setNahtavOmanikule(false);
		o.setKirjeldus("Document Avaldus Jõgeva Linnavalitsusele was viewed by user EE70006317.");
		o.setTahtsus("keskmine");
		o.setSyndmuseTyyp("liigis");
		o.setLiik("Minu dokumentide teavitus");
		o.setAlgus(Util.dateToXMLDate(d));
		o.setLopp(Util.dateToXMLDate(d));
		o.setLugejad(l);
		
		
		
		m.marshal(o);

		System.out.println("Result: " + sw.getBuffer().toString());*/
		
		
		unmarshal();
	}
	
	public static void unmarshal() throws IOException, MappingException, MarshalException, ValidationException {
		String xml =  
					    "<tkal:lisaSyndmusResponse xmlns:tkal=\"\">" + 
					     "<keha xsi:type=\"tkal:lisaSyndmusVastus\">" + 
					        "<tulemus>" + 
					          "<tulemuseKood xsi:type=\"xsd:integer\">0</tulemuseKood>" + 
					          "<tulemuseTekst xsi:type=\"xsd:string\">Päring õnnestus</tulemuseTekst>" + 
					        "</tulemus>" + 
					        "<syndmusId xsi:type=\"xsd:integer\">5255151</syndmusId>" + 
					      "</keha>" + 
					    "</tkal:lisaSyndmusResponse>";
		
		// Load Mapping
		Mapping mapping = new Mapping();
		mapping.loadMapping("C:\\development\\adit\\adit-war\\src\\main\\resources\\adit-castor-mapping.xml");
		StringWriter sw = new StringWriter();
		
		Unmarshaller m = new Unmarshaller();
		m.setMapping(mapping);
		
		StringReader strr = new StringReader(xml);
		
		Object o = m.unmarshal(strr);
		
	}

}
