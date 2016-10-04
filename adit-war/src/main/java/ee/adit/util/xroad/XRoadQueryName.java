package ee.adit.util.xroad;

/**
 * Class holding X-Tee query name data. A query name consists of the name and
 * version parts.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class XRoadQueryName {

    /**
     * Query name.
     */
    private String name;

    /**
     * Query version.
     */
    private int version;

    /**
     * Retrieves the query name.
     * 
     * @return Query name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the query name.
     * 
     * @param name
     *            Query name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retrieves the query version.
     * 
     * @return Query version
     */
    public int getVersion() {
        return version;
    }

    /**
     * Sets the query version.
     * 
     * @param version
     *            Query version
     */
    public void setVersion(int version) {
        this.version = version;
    }
}
