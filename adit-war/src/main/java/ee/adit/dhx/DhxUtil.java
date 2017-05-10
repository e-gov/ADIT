package ee.adit.dhx;

import com.jcabi.aspects.Loggable;

import ee.ria.dhx.util.StringUtil;
import ee.ria.dhx.ws.context.AppContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DhxUtil {

	public static Boolean bothNullOrEqual(Object first, Object second) {
		if (first == null && second == null) {
			return true;
		} else if (first == null || second == null) {
			return false;
		} else if (first.equals(second)) {
			return true;
		} else {
			return false;
		}
	}
	
    public static String addPrefixIfNecessary(String code) {
        if (code != null && !code.toUpperCase().startsWith("EE")) {
            return "EE" + code;
        }
        return code;
    }
}
