package ee.adit.dvk.api;

/**
 * @author User
 *         Delegate for a certain organization entry which contains actual values taken
 *         directly from the data storage but not from the cache using organization's
 *         identification attribute(s).
 */
public interface IOrganizationObserver extends IElementObserver {
    /**
     * Returns organization's code.
     *
     * @return organization's code {@link String}
     */
    String getCode();

    /**
     * Returns organization's name.
     *
     * @return organization's name {@link String}
     */
    String getName();

    /**
     * Returns true if this organization is DHL capable.
     *
     * @return DHL capable boolean flag {@link boolean}
     */
    boolean isDhlCapable();

    /**
     * Returns true if this organization is DHL direct capable.
     *
     * @return DHL direct capable boolean flag {@link boolean}
     */
    boolean isDhlDirectCapable();

    /**
     * Returns DHL direct producer's name of this organization.
     *
     * @return organization's code {@link String}
     */
    String getDhlDirectProducerName();

    /**
     * Returns DHL direct service URL of this organization.
     *
     * @return service URL {@link String}
     */
    String getDhlDirectServiceUrl();
}
