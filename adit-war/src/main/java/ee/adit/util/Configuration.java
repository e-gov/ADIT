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
	 * Specifies the x-road producer name of current application.
	 */
	private String xteeProducerName;

    /**
     * Specifies the maximum result size of the getJoined query.
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
     * Global disk quota per user specified in bytes. This is used in case no
     * specific quota is declared for the user.
     */
    private Long globalDiskQuota;

    /**
     * Document retention deadline in days.
     * When given number of days has passed since last modification
     * of document, then document will be deflated (document file contents removed)
     */
    private Long documentRetentionDeadlineDays;

    /**
     * The locales that specify in which languages the error messages are
     * returned. The format for the locale is "[langCode]_[countryCode]", for
     * example "en_us" / "et_ee".
     */
    private List<String> locales;

    /**
     * DVK org code.
     */
    private String dvkOrgCode;

    /**
     * XTee institution code.
     */
    private String xteeInstitution;

    /**
     * XTee security server.
     */
    private String xteeSecurityServer;

    /**
     * XTee user code.
     */
    private String xteeIdCode;

    /**
     * Lifetime of digital signatures that have been prepared but not confirmed.
     */
    private Long unfinishedSignatureLifetimeSeconds;
    
    /**
     * Checking of certificates for test certificate is performed or not
     * */
    private Boolean doCheckTestCert;
    
    /**
     * X-Road instance of the ADIT service in accordance with X-Road message protocol version 4.0
     */
    private String xroadInstance;
    
    /**
     * X-Road member class of the ADIT service in accordance with X-Road message protocol version 4.0
     */
    private String xroadMemberClass;
    
    /**
     * X-Road member code of the ADIT service in accordance with X-Road message protocol version 4.0
     */
    private String xroadMemberCode;
    
    /**
     * jDigiDoc configuration file.
     */
    private String jDigiDocConfigFile;

    /**
     * Ruuter component base url for constructing queries
     */
    private String ruuterServiceUrl;
    
    public List<String> getLocales() {
        return locales;
    }

    public void setLocales(List<String> locales) {
        this.locales = locales;
    }

    public String getXteeProducerName() {
		return xteeProducerName;
	}

	public void setXteeProducerName(String xroadProducerName) {
		this.xteeProducerName = xroadProducerName;
	}

	public BigInteger getGetJoinedMaxResults() {
        return getJoinedMaxResults;
    }

    public void setGetJoinedMaxResults(BigInteger getJoinedMaxResults) {
        this.getJoinedMaxResults = getJoinedMaxResults;
    }

    public String getTempDir() {
        return tempDir;
    }

    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
    }

    public String getDeleteTemporaryFiles() {
        return deleteTemporaryFiles;
    }

    public void setDeleteTemporaryFiles(String deleteTemporaryFiles) {
        this.deleteTemporaryFiles = deleteTemporaryFiles;
    }

    /**
     * Retrieves the value for deleting temporary files as a boolean.
     *
     * @return true, if temporary files are to be deleted
     */
    public boolean getDeleteTemporaryFilesAsBoolean() {
        boolean result = false;
        if (this.getDeleteTemporaryFiles() != null) {
            result = Boolean.valueOf(this.getDeleteTemporaryFiles()).booleanValue();
        } else {
            result = false;
        }
        return result;
    }

    public String getSchedulerEventTypeName() {
        return schedulerEventTypeName;
    }

    public void setSchedulerEventTypeName(String schedulerEventTypeName) {
        this.schedulerEventTypeName = schedulerEventTypeName;
    }

    public Long getGlobalDiskQuota() {
        return globalDiskQuota;
    }

    public void setGlobalDiskQuota(Long globalDiskQuota) {
        this.globalDiskQuota = globalDiskQuota;
    }

    public Long getDocumentRetentionDeadlineDays() {
		return documentRetentionDeadlineDays;
	}

	public void setDocumentRetentionDeadlineDays(Long documentRetentionDeadlineDays) {
		this.documentRetentionDeadlineDays = documentRetentionDeadlineDays;
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

	public Long getUnfinishedSignatureLifetimeSeconds() {
		return unfinishedSignatureLifetimeSeconds;
	}

	public void setUnfinishedSignatureLifetimeSeconds(
			Long unfinishedSignatureLifetimeSeconds) {
		this.unfinishedSignatureLifetimeSeconds = unfinishedSignatureLifetimeSeconds;
	}

	public Boolean getDoCheckTestCert() {
		return doCheckTestCert;
	}

	public void setDoCheckTestCert(Boolean doCheckTestCert) {
		this.doCheckTestCert = doCheckTestCert;
	}

	public String getXroadInstance() {
		return xroadInstance;
	}

	public void setXroadInstance(String xroadInstance) {
		this.xroadInstance = xroadInstance;
	}

	public String getXroadMemberClass() {
		return xroadMemberClass;
	}

	public void setXroadMemberClass(String xroadMemberClass) {
		this.xroadMemberClass = xroadMemberClass;
	}

	public String getXroadMemberCode() {
		return xroadMemberCode;
	}

	public void setXroadMemberCode(String xroadMemberCode) {
		this.xroadMemberCode = xroadMemberCode;
	}

	/**
	 * @return the jDigiDocConfigFile
	 */
	public String getjDigiDocConfigFile() {
		return jDigiDocConfigFile;
	}

	/**
	 * @param jDigiDocConfigFile the jDigiDocConfigFile to set
	 */
	public void setjDigiDocConfigFile(String jDigiDocConfigFile) {
		this.jDigiDocConfigFile = jDigiDocConfigFile;
	}

    /**
     * @return the base-url of Ruuter service
     */
    public String getRuuterServiceUrl() {
        return ruuterServiceUrl;
    }

    /**
     *
     * @param ruuterServiceUrl the base-url of Ruuter service
     */
    public void setRuuterServiceUrl(String ruuterServiceUrl) {
        this.ruuterServiceUrl = ruuterServiceUrl;
    }
}
