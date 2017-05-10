package ee.adit.dhx.converter;

/**
 *
 * @author Hendrik Pärna
 * @param <From> type
 * @param <To> type
 */
public interface Converter<From, To> {
    /**
     * Convert.
     * @param from object
     * @return To object
     */
    To convert(From from);
}
