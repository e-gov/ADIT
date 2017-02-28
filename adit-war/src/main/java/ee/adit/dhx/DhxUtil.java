package ee.adit.dhx;

import com.jcabi.aspects.Loggable;

import ee.ria.dhx.util.StringUtil;
import ee.ria.dhx.ws.context.AppContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DhxUtil {

	private static AditDhxConfig config;

	/**
	 * Sometimes DHX addressee and DVK addresse might be different. In DHX there
	 * must be always registration code, in DVK there might be system also.
	 * 
	 * @param memberCode
	 *            memberCode to use to transform to DVK capsule addressee
	 * @param subsystem
	 *            subsystem to use to transform to DVK capsule addressee
	 * @return capsule addressee accordinbg to DVK
	 */
	@Loggable
	public static String toDvkCapsuleAddressee(String memberCode, String subsystem) {
		String dvkCode = null;
		if (!StringUtil.isNullOrEmpty(subsystem)
				&& subsystem.startsWith(getAditDhxConfig().getConfig().getDhxSubsystemPrefix() + ".")) {
			String system = subsystem.substring(getAditDhxConfig().getConfig().getDhxSubsystemPrefix().length() + 1);
			// String perfix = subsystem.substring(0, index);
			log.debug("found system with subsystem: " + system);
			if (isSpecialOrganisation(system)) {
				dvkCode = system;
			} else {
				dvkCode = system + "." + memberCode;
			}

		} else if (!StringUtil.isNullOrEmpty(subsystem)
				&& !subsystem.equals(getAditDhxConfig().getConfig().getDhxSubsystemPrefix())) {
			if (isSpecialOrganisation(subsystem)) {
				dvkCode = subsystem;
			} else {
				dvkCode = subsystem + "." + memberCode;
			}
		} else {

			dvkCode = memberCode;
		}
		return dvkCode;
	}

	/**
	 * Method defines if organisation is one of the special organisations that
	 * are in the capsule without registration code, but with system name.
	 * 
	 * @param organisationCode
	 *            organisation code to check
	 * @return whether organistion code is special or not
	 */
	@Loggable
	public static Boolean isSpecialOrganisation(String organisationCode) {
		String specialOrgs = "," + getAditDhxConfig().getSpecialOrganisations() + ",";
		log.debug("specialOrgs: " + specialOrgs + "  organisationCode:" + organisationCode);
		if (specialOrgs.indexOf("," + organisationCode + ",") >= 0) {
			return true;
		}
		return false;
	}

	private static AditDhxConfig getAditDhxConfig() {
		if (config == null) {
			AditDhxConfig conf = AppContext.getApplicationContext().getBean(AditDhxConfig.class);
			config = conf;
		}
		return config;
	}

	public static Boolean bothNullOrEqual (Object first, Object second) {
		if(first == null && second == null) {
			return true;
		} else if(first== null || second == null) {
			return false;
		} else if(first.equals(second)) {
			return true;
		} else {
			return false;
		}
	}
}
