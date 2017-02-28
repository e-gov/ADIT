package ee.adit.dvk.api;

import java.math.BigDecimal;

import ee.adit.dvk.api.ml.Util;

/**
 * @author User
 *         Class that used at creating of a new message recipient and requesting all mandatory
 *         parameters which cannot be null values.
 */
public class MessageRecipientCreateArgs {
    private long dhlMessageId;
    private String recipientOrgCode;
    private String recipientPersonCode;
    private BigDecimal recipientDivisionId;
    private BigDecimal recipientPositionId;
    private long sendingStatusId;

    /**
     * Creates a new instance.
     *
     * @param dhlMessageId        DHL message ID
     * @param recipientOrgCode    recipient's organization code
     * @param recipientPersonCode recipient's person code
     * @param sendingStatusId     sending status ID
     * @param recipientDivisionId recipient's division ID
     * @param recipientPositionId recipient's division ID
     */
    public MessageRecipientCreateArgs(long dhlMessageId, String recipientOrgCode, String recipientPersonCode, long sendingStatusId,
                                      BigDecimal recipientDivisionId, BigDecimal recipientPositionId) {
        super();

        if (Util.isEmpty(recipientOrgCode)) {
            throw new NullPointerException("Argument 'recipientOrgCode' is mandatory and cannot be null or empty");
        }

        if (Util.isEmpty(recipientPersonCode)) {
            throw new NullPointerException("Argument 'recipientPersonCode' is mandatory and cannot be null or empty");
        }

        if (recipientDivisionId == null) {
            throw new NullPointerException("Argument 'recipientDivisionId' is mandatory and cannot be null or empty");
        }

        if (recipientPositionId == null) {
            throw new NullPointerException("Argument 'recipientPositionId' is mandatory and cannot be null or empty");
        }

        this.dhlMessageId = dhlMessageId;
        this.recipientOrgCode = recipientOrgCode;
        this.recipientPersonCode = recipientPersonCode;
        this.sendingStatusId = sendingStatusId;
        this.recipientDivisionId = recipientDivisionId;
        this.recipientPositionId = recipientPositionId;
    }

    /**
     * Returns DHL message ID.
     *
     * @return DHL message ID {@link long}
     */
    public long getDhlMessageId() {
        return dhlMessageId;
    }

    /**
     * Returns recipient's organization code.
     *
     * @return recipient's organization code {@link String}
     */
    public String getRecipientOrgCode() {
        return recipientOrgCode;
    }

    /**
     * Returns recipient's person code.
     *
     * @return recipient's person code {@link String}
     */
    public String getRecipientPersonCode() {
        return recipientPersonCode;
    }

    /**
     * Returns sending status ID.
     *
     * @return sending status ID {@link long}
     */
    public long getSendingStatusId() {
        return sendingStatusId;
    }

    /**
     * Returns recipient's position ID.
     *
     * @return recipient's position ID {@link BigDecimal}
     */
    public BigDecimal getRecipientPositionId() {
        return recipientPositionId;
    }

    /**
     * Returns recipient's division ID.
     *
     * @return recipient's division ID {@link BigDecimal}
     */
    public BigDecimal getRecipientDivisionId() {
        return recipientDivisionId;
    }
}
