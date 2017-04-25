package ee.adit.dhx.api.container.v2_1;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Hendrik Pärna
 * @since 28.01.14
 */
public class DvkDateHandler {
    private SimpleDateFormat formatter;

    public DvkDateHandler(String format) {
        this.formatter = new SimpleDateFormat(format);
    }

    /**
     * Parse datetime.
     *
     * @param value to parse from
     * @return date
     */
    public Date parseDateTime(String value) {
        Date date = null;
        if (StringUtils.isNotBlank(value)) {
            try {
                date = formatter.parse(value);
            } catch (ParseException px) {
                throw new IllegalArgumentException(px.getMessage());
            }
        }
        return date;
    }

    /**
     * Format date.
     * @param date to format
     * @return formatted date
     */
    public String formatDateTime(Date date) {
        return formatter.format(date);
    }
}
