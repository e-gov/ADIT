package ee.adit.dhx;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import ee.ria.dhx.ws.config.DhxConfig;
import ee.ria.dhx.ws.config.SoapConfig;
import lombok.Getter;
import lombok.Setter;

@Configuration
public class AditDhxConfig {

	@Autowired
	@Getter
	SoapConfig config;

	@Value("${dhx.server.special-orgnisations}")
	String specialOrganisations;

	@Value("${dhx.resend.timeout}")
	Integer resendTimeout;

	/**
	 * Specialorganisations are organisations which will be presented in address
	 * list by string withoud registry code of the organisation. For example
	 * adit, rt.
	 * 
	 * @return the specialOrganisations
	 */
	public String getSpecialOrganisations() {
		return specialOrganisations;
	}

	/**
	 * Specialorganisations are organisations which will be presented in address
	 * list by string withoud registry code of the organisation. For example
	 * adit, rt.
	 * 
	 * @param specialOrganisations
	 *            the specialOrganisations to set
	 */
	public void setSpecialOrganisations(String specialOrganisations) {
		this.specialOrganisations = specialOrganisations;
	}

	/**
	 * If asynchronous sending was interrupted(for example server were
	 * shutdown), then messages in sending status will be resent after that
	 * timeout.
	 * 
	 * @return the resendTimeout
	 */
	public Integer getResendTimeout() {
		return resendTimeout;
	}

	/**
	 * If asynchronous sending was interrupted(for example server were
	 * shutdown), then messages in sending status will be resent after that
	 * timeout.
	 * @param resendTimeout
	 *            the resendTimeout to set
	 */
	public void setResendTimeout(Integer resendTimeout) {
		this.resendTimeout = resendTimeout;
	}
}
