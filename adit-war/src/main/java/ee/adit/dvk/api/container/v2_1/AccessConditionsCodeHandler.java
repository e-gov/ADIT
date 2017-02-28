package ee.adit.dvk.api.container.v2_1;

import org.exolab.castor.mapping.FieldHandler;
import org.exolab.castor.mapping.ValidityException;

/**
 * @author Hendrik Pärna
 * @since 29.01.14
 */
public class AccessConditionsCodeHandler implements FieldHandler {

    private enum AccessCondition {
        AVALIK("Avalik"), AK("AK");

        private String text;

        AccessCondition(String text) {
            this.text = text;
        }

        public String getText() {
            return this.text;
        }

        public static AccessCondition fromString(String text) {
            if (text != null) {
               for (AccessCondition accessCondition:  AccessCondition.values()) {
                   if (text.equalsIgnoreCase(accessCondition.text)) {
                       return accessCondition;
                   }
               }
            }

            throw new IllegalArgumentException("Unable to parse Access.AccessConditionsCode.");
        }
    }


    @Override
    public Object getValue(Object parent) throws IllegalStateException {
        Access access = (Access) parent;
        return access.getAccessConditionsCode();
    }

    @Override
    public void setValue(Object parent, Object value) throws IllegalStateException, IllegalArgumentException {
        Access access = (Access) parent;
        String accessConditionsCode = (String) value;
        access.setAccessConditionsCode(AccessCondition.fromString(accessConditionsCode).getText());
    }

    @Override
    public void resetValue(Object parent) throws IllegalStateException, IllegalArgumentException {
        Access access = (Access) parent;
        access.setAccessConditionsCode(null);
    }

    @Override
    public void checkValidity(Object o) throws ValidityException, IllegalStateException {
    }

    @Override
    public Object newInstance(Object o) throws IllegalStateException {
        return null;
    }
}
