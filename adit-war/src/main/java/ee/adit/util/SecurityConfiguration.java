package ee.adit.util;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityConfiguration {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(SecurityConfiguration.class);

	static {
		LOGGER.error("adding BouncyCastleProvider to Security");
		Security.addProvider(new BouncyCastleProvider());
	}

	/**
	 * Call static block to add {@link BouncyCastleProvider} as provider to
	 * {@link java.security.Security}
	 */
	public static void init() {
		// do nothing, needed to run static block
	}
}
