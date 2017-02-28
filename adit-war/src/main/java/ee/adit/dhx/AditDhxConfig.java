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
	@Setter
	@Getter
	String specialOrganisations;
}
