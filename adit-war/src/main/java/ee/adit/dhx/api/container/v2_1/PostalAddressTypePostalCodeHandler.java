package ee.adit.dhx.api.container.v2_1;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.exolab.castor.mapping.FieldHandler;
import org.exolab.castor.mapping.ValidityException;

/**
 * @author Hendrik Pärna
 * @since 28.01.14
 */
public class PostalAddressTypePostalCodeHandler implements FieldHandler {

    private static final String patternRegex = "[0-9]{5}";
    private Pattern pattern;

    public PostalAddressTypePostalCodeHandler() {
        super();
        pattern = Pattern.compile(patternRegex);
    }

    @Override
    public Object getValue(Object o) throws IllegalStateException {
        PostalAddressType postalAddress = (PostalAddressType) o;
        String postalCode = postalAddress.getPostalCode();
        if (postalCode != null) {
           checkPostalCodeValidity(postalCode);
        }
        return postalCode;
    }

    private void checkPostalCodeValidity(String postalCode) {
        Matcher matcher = pattern.matcher(postalCode);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Illegal postal code input.");
        }
    }

    @Override
    public void setValue(Object parent, Object value) throws IllegalStateException, IllegalArgumentException {
        PostalAddressType postalAddress = (PostalAddressType) parent;
        String postalCode = (String) value;

        if (StringUtils.isNotBlank(postalCode)) {
            checkPostalCodeValidity(postalCode);
            postalAddress.setPostalCode(postalCode);
        }
    }

    @Override
    public void resetValue(Object parent) throws IllegalStateException, IllegalArgumentException {
        PostalAddressType postalAddress = (PostalAddressType) parent;
        postalAddress.setPostalCode(null);
    }

    @Override
    @Deprecated
    public void checkValidity(Object parent) throws ValidityException, IllegalStateException {
    }

    @Override
    public Object newInstance(Object o) throws IllegalStateException {
        return null;
    }
}
