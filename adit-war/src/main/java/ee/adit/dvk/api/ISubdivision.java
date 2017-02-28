package ee.adit.dvk.api;

public interface ISubdivision extends ISubdivisionObserver, IDvkElement {
    /**
     * Sets subdivision's name of this subdivision.
     *
     * @param subdivisionName {@link String}
     */
    void setName(String subdivisionName);

    /**
     * Returns an organization to which this subdivision belongs.
     *
     * @return organization entry {@link IOrganization}
     */
    IOrganization getOrganization();

    /**
     * Returns immutable proxy-object of this subdivision containing actual
     * values directly from the data storage.
     *
     * @return {@link ISubdivisionObserver}
     */
    ISubdivisionObserver getOrigin();
}
