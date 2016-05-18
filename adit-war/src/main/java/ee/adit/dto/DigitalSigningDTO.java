package ee.adit.dto;

import java.io.Serializable;

import org.digidoc4j.Container;
import org.digidoc4j.DataToSign;

public class DigitalSigningDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Container container;
	
	private DataToSign dataToSign;

	public Container getContainer() {
		return container;
	}

	public void setContainer(Container container) {
		this.container = container;
	}

	public DataToSign getDataToSign() {
		return dataToSign;
	}

	public void setDataToSign(DataToSign dataToSign) {
		this.dataToSign = dataToSign;
	}
	
	public boolean isDataStored() {
		boolean response = false;
		
		if (container != null
				&& dataToSign != null 
				&& dataToSign.getSignatureParameters() != null
				&& dataToSign.getSignatureParameters().getSigningCertificate() != null) {
			
			response = true;
		}
		
		return response;
	}

}
