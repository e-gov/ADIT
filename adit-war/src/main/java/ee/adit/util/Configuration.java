package ee.adit.util;

import java.math.BigInteger;
import java.util.List;

/**
 * Application configuration holder class. The configuring takes place during
 * startup and the parameters are specified in the servlet configuration.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class Configuration {

    /**
     * Specifies the maximum result size of the getJoined query
     */
    private BigInteger getJoinedMaxResults;

    /**
     * Working directory. Temporary files are held here.
     */
    private String tempDir;

    /**
     * Specifies if the temporary files are to be deleted immediately after use.
     * This parameter is useful when debugging.
     */
    private String deleteTemporaryFiles;

    /**
     * The name of the event type that is used in the X-Tee notifications
     * calendar.
     */
    private String schedulerEventTypeName;

    /**
     * The location of the stylesheet that is used to generate a response
     * message to DVK.
     */
    private String dvkResponseMessageStylesheet;

    /**
     * Global disk quota per user specified in bytes. This is used in case no
     * specific quota is declared for the user.
     */
    private Long globalDiskQuota;

    /**
     * The locales that specify in which languages the error messages are
     * returned. The format for the locale is "[langCode]_[countryCode]", for
     * example "en_us" / "et_ee".
     */
    private List<String> locales;

    private String dvkOrgCode;

    private String xteeInstitution;

    private String xteeSecurityServer;

    private String xteeIdCode;

    /**
     * Retrieves the list of the locales
     * 
     * @return Locales list
     */
    public List<String> getLocales() {
        return locales;
    }

    /**
     * Sets the locales list
     * 
     * @param locales
     *            Locales list
     */
    public void setLocales(List<String> locales) {
        this.locales = locales;
    }

    /**
     * Retrieves the max result size for the getJoined query
     * 
     * @return Max result size for the getJoined query
     */
    public BigInteger getGetJoinedMaxResults() {
        return getJoinedMaxResults;
    }

    /**
     * Sets the max result size for the getJoined query
     * 
     * @param getJoinedMaxResults
     *            Max result size for the getJoined query
     */
    public void setGetJoinedMaxResults(BigInteger getJoinedMaxResults) {
        this.getJoinedMaxResults = getJoinedMaxResults;
    }

    /**
     * Retrieves the absolute path to the temporary files directory
     * 
     * @return Absolute path to temporary files directory
     */
    public String getTempDir() {
        return tempDir;
    }

    /**
     * Sets the absolute path to the temporary files directory
     * 
     * @param tempDir
     *            Absolute path to temporary files directory
     */
    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
    }

    /**
     * Retrieves the value for deleting temporary files
     * 
     * @return String value of temporary files deletaion setting
     */
    public String getDeleteTemporaryFiles() {
        return deleteTemporaryFiles;
    }

    /**
     * Sets the value for deleting temporary files
     * 
     * @param deleteTemporaryFiles
     */
    public void setDeleteTemporaryFiles(String deleteTemporaryFiles) {
        this.deleteTemporaryFiles = deleteTemporaryFiles;
    }

    /**
     * Retrieves the value for deleting temporary files as a boolean
     * 
     * @return true, if temporary files are to be deleted
     */
    public boolean getDeleteTemporaryFilesAsBoolean() {
        boolean result = false;
        if (this.getDeleteTemporaryFiles() != null) {
            result = (new Boolean(this.getDeleteTemporaryFiles())).booleanValue();
        } else {
            result = false;
        }
        return result;
    }

    /**
     * Retrieves the scheduler event type name
     * 
     * @return Scheduler event type name
     */
    public String getSchedulerEventTypeName() {
        return schedulerEventTypeName;
    }

    /**
     * Sets the scheduler event type name
     */
    public void setSchedulerEventTypeName(String schedulerEventTypeName) {
        this.schedulerEventTypeName = schedulerEventTypeName;
    }

    /**
     * Retrieves the absolute path to the DVK response message stylesheet
     * 
     * @return Absolute path to the DVK response message stylesheet
     */
    public String getDvkResponseMessageStylesheet() {
        return dvkResponseMessageStylesheet;
    }

    /**
     * Sets the absolute path to the DVK response message stylesheet
     */
    public void setDvkResponseMessageStylesheet(String dvkResponseMessageStylesheet) {
        this.dvkResponseMessageStylesheet = dvkResponseMessageStylesheet;
    }

    /**
     * Retrieves the global disk quota
     * 
     * @return Global disk quota in bytes
     */
    public Long getGlobalDiskQuota() {
        return globalDiskQuota;
    }

    /**
     * Sets the global disk quota
     * 
     * @param globalDiskQuota
     *            Global disk quota in bytes
     */
    public void setGlobalDiskQuota(Long globalDiskQuota) {
        this.globalDiskQuota = globalDiskQuota;
    }

    public String getDvkOrgCode() {
        return dvkOrgCode;
    }

    public void setDvkOrgCode(String dvkOrgCode) {
        this.dvkOrgCode = dvkOrgCode;
    }

    public String getXteeInstitution() {
        return xteeInstitution;
    }

    public void setXteeInstitution(String xteeInstitution) {
        this.xteeInstitution = xteeInstitution;
    }

    public String getXteeSecurityServer() {
        return xteeSecurityServer;
    }

    public void setXteeSecurityServer(String xteeSecurityServer) {
        this.xteeSecurityServer = xteeSecurityServer;
    }

    public String getXteeIdCode() {
        return xteeIdCode;
    }

    public void setXteeIdCode(String xteeIdCode) {
        this.xteeIdCode = xteeIdCode;
    }

}
