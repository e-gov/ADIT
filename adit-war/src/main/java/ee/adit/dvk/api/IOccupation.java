package ee.adit.dvk.api;

/**
 * @author User
 *         Delegate for work with a record from the table DHL_OCCUPATION.
 */
public interface IOccupation extends IOccupationObserver, IDvkElement {
    /**
     * Sets subdivision's name of this occupation.
     *
     * @param subdivisionName subdivision's name {@link String}
     */
    void setName(String subdivisionName);

    /**
     * Returns organization entry related with this occupation of type {@link IOrganization}.
     */
    IOrganization getOrganization();

    /**
     * Returns immutable proxy-object of this occupation containing actual values directly
     * from the data storage.
     *
     * @return {@link IOccupationObserver}
     */
    IOccupationObserver getOrigin();
}
