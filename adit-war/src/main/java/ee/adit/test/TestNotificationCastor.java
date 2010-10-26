package ee.adit.test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;

import ee.adit.pojo.notification.LisaSyndmusRequest;
import ee.adit.pojo.notification.LisaSyndmusRequestKasutaja;
import ee.adit.pojo.notification.LisaSyndmusRequestLugejad;

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
		Mapping mapping = new Mapping();
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
		l.setType("ns5:kasutaja[1]");
		
		o.setNahtavOmanikule(false);
		o.setKirjeldus("Document Avaldus Jõgeva Linnavalitsusele was viewed by user EE70006317.");
		o.setTahtsus("keskmine");
		o.setSyndmuseTyyp("liigis");
		o.setLiik("Minu dokumentide teavitus");
		o.setAlgus(d);
		o.setLopp(d);
		o.setLugejad(l);
		
		
		
		m.marshal(o);

		System.out.println("Result: " + sw.getBuffer().toString());
		
	}

}
