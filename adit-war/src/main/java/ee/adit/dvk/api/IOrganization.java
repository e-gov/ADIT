package ee.adit.dvk.api;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author User
 *         Delegate for work with a record from the table DHL_ORGANIZATION.
 */
public interface IOrganization extends IOrganizationObserver, IDvkElement {
    /**
     * Sets organization's name.
     *
     * @param orgName organization's name {@link String}
     */
    void setName(String orgName);

    /**
     * Sets boolean flag meaning is this organization DHL capable or not.
     *
     * @param dhlCapable boolean flag {@link boolean}
     */
    void setDhlCapable(boolean dhlCapable);

    /**
     * Sets boolean flag meaning is this organization DHL direct capable or not.
     *
     * @param dhlDirectCapable boolean flag {@link boolean}
     */
    void setDhlDirectCapable(boolean dhlDirectCapable);

    /**
     * Sets DHL direct producer's name of this organization.
     *
     * @param dhlDirectProducerName DHL direct producer's name {@link String}
     */
    void setDhlDirectProducerName(String dhlDirectProducerName);

    /**
     * Sets DHL direct service URL of this organization.
     *
     * @param dhlDirectServiceUrl DHL direct service URL {@link String}
     */
    void setDhlDirectServiceUrl(String dhlDirectServiceUrl);

    /**
     * Returns a list of occupations what are related with this organization.
     *
     * @return list of occupations {@link IOccupation}
     */
    List<IOccupation> getOccupations();

    /**
     * Returns a list of subdivisions what are related with this organization.
     *
     * @return list of occupations {@link ISubdivision}
     */
    List<ISubdivision> getSubdivisions();

    /**
     * Adds an existing occupation to a pending list of occupations if it doesn't belong to this
     * organization yet and will change its organization code after organization's or occupation's
     * save method will be called.
     *
     * @param occupation {@link IOccupation}
     * @return true if adding occupation doesn't belong to this organization yet
     */
    boolean add(IOccupation occupation);

    /**
     * Creates a new occupation and puts it to the pending list of occupations and will save it
     * after organization's or occupation's save method will be called.
     *
     * @param occupationCode new occupation's code {@link BigDecimal}
     * @return a new created but not saved in the data storage occupation
     */
    IOccupation createOccupation(BigDecimal occupationCode);

    /**
     * Adds an existing subdivision to a pending list of subdivisions if it doesn't belong to this
     * organization yet and will change its organization code after organization's or subdivision's
     * save method will be called.
     *
     * @param subdivision {@link ISubdivision}
     * @return true if adding subdivision doesn't belong to this organization yet
     */
    boolean add(ISubdivision subdivision);

    /**
     * Creates a new subdivision and puts it to the pending list of subdivisions and will save it
     * after organization's or subdivision's save method will be called.
     *
     * @param subdivisionCode new subdivision's code {@link BigDecimal}
     * @return a new created but not saved in the data storage subdivision
     */
    ISubdivision createSubdivision(BigDecimal subdivisionCode);

    /**
     * Returns immutable proxy-object of this organization containing actual
     * values directly from the data storage.
     *
     * @return {@link IOrganizationObserver}
     */
    IOrganizationObserver getOrigin();
}
