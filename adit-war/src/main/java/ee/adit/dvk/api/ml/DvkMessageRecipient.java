package ee.adit.dvk.api.ml;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.hibernate.Transaction;

import ee.adit.dvk.api.DVKAPI.DvkType;
import ee.adit.dvk.api.IMessageRecipient;
import ee.adit.dvk.api.MessageRecipientCreateArgs;
import ee.adit.dvk.api.SelectCriteria;

class DvkMessageRecipient extends DescendantFacade<PojoMessageRecipient> implements IMessageRecipient {
    private class CacheProxy extends CacheProxyBase<BigDecimal, DvkMessageRecipient, PojoMessageRecipient> {
        public CacheProxy(DvkSessionCacheBox cacheBox) {
            super(cacheBox);
        }

        public DvkMessageRecipient lookup(Object id, boolean allowCreateNew, Object... extraArgs) {
            if (id == null) {
                if (!allowCreateNew) {
                    throw new NullPointerException("Mandatory argument 'id' cannot be null");
                }

                String key = MessageRecipientCreateArgs.class.getName();

                asserExtraArgs(extraArgs);
                MessageRecipientCreateArgs args = (MessageRecipientCreateArgs) getArgumet(extraArgs[0], key, true);

                SelectCriteriaMessageRecipient criteria = (SelectCriteriaMessageRecipient) getSelectCriteria(true);
                criteria.setDhlMessageId(args.getDhlMessageId());
                criteria.setRecipientOrgCode(args.getRecipientOrgCode());
                criteria.setRecipientPersonCode(args.getRecipientPersonCode());
                criteria.setRecipientPositionId(args.getRecipientPositionId());
                criteria.setRecipientDivisionId(args.getRecipientDivisionId());
                criteria.setSendingStatusId(args.getSendingStatusId());
                //
                List<DvkMessageRecipient> list = select(selectCriteria);

                try {
                    if (list.size() > 1) {
                        throw new RuntimeException("Select fetched more than one MessageRecipient when expected maximum is one");
                    } else if (list.size() == 1) {
                        return list.get(0);
                    }
                } finally {
                    list.clear();
                }

                PojoMessageRecipient pojo = new PojoMessageRecipient(new BigDecimal(-1));
                pojo.dhlMessageId = args.getDhlMessageId();
                pojo.recipientOrgCode = args.getRecipientOrgCode();
                pojo.recipientPersonCode = args.getRecipientPersonCode();
                pojo.recipientDivisionId = args.getRecipientDivisionId();
                pojo.recipientPositionId = args.getRecipientPositionId();
                pojo.sendingStatusId = args.getSendingStatusId();

                DvkMessageRecipient facade = new DvkMessageRecipient(pojo, cacheBox, true);

                return facade;
            }

            if (cache.containsKey(id)) {
                return cache.get(id);
            }

            BigDecimal idMessageRecipient = Util.getBigDecimal(id);

            PojoMessageRecipient pojo = (PojoMessageRecipient) cacheBox.getFromHibernateCache(PojoMessageRecipient.class,
                    idMessageRecipient);

            if (pojo == null) {
                return null;
            }

            DvkMessageRecipient msg = new DvkMessageRecipient(pojo, cacheBox, false);

            cache.put(idMessageRecipient, msg);

            return msg;
        }

        @Override
        public String getIdFieldName() {
            return PojoMessageRecipient.FieldNames.id;
        }

        @Override
        protected String getPojoName() {
            return PojoMessageRecipient.PojoName;
        }

        @Override
        protected BigDecimal getPojoId(PojoMessageRecipient pojo) {
            return pojo.id;
        }

        @Override
        public SelectCriteria getSelectCriteria(boolean reset) {
            if (selectCriteria == null) {
                selectCriteria = new SelectCriteriaMessageRecipient();
                return selectCriteria;
            }

            return super.getSelectCriteria(reset);
        }
    }

    private PojoMessageRecipient pojo;

    DvkMessageRecipient(PojoMessageRecipient pojo, DvkSessionCacheBox cacheBox, boolean isNew) {
        super(cacheBox, isNew);

        this.pojo = pojo;
    }

    @Override
    protected PojoMessageRecipient clonePojo() {
        PojoMessageRecipient clonedPojo = new PojoMessageRecipient();

        Util.copyValues(pojo, clonedPojo);

        return clonedPojo;
    }

    private DvkMessageRecipient() {
        // for service needs
        super(null, false);
    }

    @Override
    PojoMessageRecipient getPojo() {
        return pojo;
    }

    public DvkType getType() {
        return DvkType.MessageRecipient;
    }

    @Override
    Object getPojoId() {
        return pojo.id;
    }

    @Override
    public void save(Transaction tx) {
        if (isNew()) {
            PojoMessageRecipient newPojo = (PojoMessageRecipient) createNewRecord(getType(), pojo.dhlMessageId + "");
            pojo.setId(newPojo.getId());
            substituteInCache();
        }

        super.save(tx);
    }

    @Override
    public void destroy() {
        pojo = null;

        super.destroy();
    }

    @Override
    public String toString() {
        return String.format("DHL MessageRecipient: dhlId=%s", pojo.getDhlId());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null || !(obj instanceof DvkMessageRecipient)) {
            return false;
        }

        DvkMessageRecipient other = (DvkMessageRecipient) obj;

        return pojo.equals(other.pojo);
    }

    static ICacheProxy<DvkMessageRecipient> createCacheProxy(DvkSessionCacheBox cacheBox) {
        DvkMessageRecipient counter = new DvkMessageRecipient();
        return counter.new CacheProxy(cacheBox);
    }

    public String getRecipientOrgName() {
        return pojo.recipientOrgName;
    }

    public void setRecipientOrgName(String recipientOrgName) {
        if (!hasSameValue(pojo.recipientOrgName, recipientOrgName)) {
            pojo.recipientOrgName = recipientOrgName;
            setDirty(true);
        }
    }

    public String getRecipientName() {
        return pojo.recipientName;
    }

    public void setRecipientName(String recipientName) {
        if (!hasSameValue(pojo.recipientName, recipientName)) {
            pojo.recipientName = recipientName;
            setDirty(true);
        }
    }

    public Date getSendingDate() {
        return pojo.sendingDate;
    }

    public void setSendingDate(Date sendingDate) {
        if (!hasSameValue(pojo.sendingDate, sendingDate)) {
            pojo.sendingDate = sendingDate;
            setDirty(true);
        }
    }

    public Date getReceivedDate() {
        return pojo.receivedDate;
    }

    public void setReceivedDate(Date receivedDate) {
        if (!hasSameValue(pojo.receivedDate, receivedDate)) {
            pojo.receivedDate = receivedDate;
            setDirty(true);
        }
    }

    public long getSendingStatusId() {
        return pojo.sendingStatusId;
    }

    public Long getRecipientStatusId() {
        return pojo.recipientStatusId;
    }

    public void setRecipientStatusId(Long recipientStatusId) {
        if (!hasSameValue(pojo.recipientStatusId, recipientStatusId)) {
            pojo.recipientStatusId = recipientStatusId;
            setDirty(true);
        }
    }

    public String getFaultCode() {
        return pojo.faultCode;
    }

    public void setFaultCode(String faultCode) {
        if (!hasSameValue(pojo.faultCode, faultCode)) {
            pojo.faultCode = faultCode;
            setDirty(true);
        }
    }

    public String getFaultActor() {
        return pojo.faultActor;
    }

    public void setFaultActor(String faultActor) {
        if (!hasSameValue(pojo.faultActor, faultActor)) {
            pojo.faultActor = faultActor;
            setDirty(true);
        }
    }

    public String getFaultString() {
        return pojo.faultString;
    }

    public void setFaultString(String faultString) {
        if (!hasSameValue(pojo.faultString, faultString)) {
            pojo.faultString = faultString;
            setDirty(true);
        }
    }

    public String getFaultDetail() {
        return pojo.faultDetail;
    }

    public void setFaultDetail(String faultDetail) {
        if (!hasSameValue(pojo.faultDetail, faultDetail)) {
            pojo.faultDetail = faultDetail;
            setDirty(true);
        }
    }

    public String getMetaxml() {
        return pojo.metaxml;
    }

    public void setMetaxml(String metaxml) {
        if (!hasSameValue(pojo.metaxml, metaxml)) {
            pojo.metaxml = metaxml;
            setDirty(true);
        }
    }

    public BigDecimal getDhlId() {
        return pojo.dhlId;
    }

    public void setDhlId(BigDecimal dhlId) {
        if (!hasSameValue(pojo.dhlId, dhlId)) {
            pojo.dhlId = dhlId;
            setDirty(true);
        }
    }

    public String getQueryId() {
        return pojo.queryId;
    }

    public void setQueryId(String queryId) {
        if (!hasSameValue(pojo.queryId, queryId)) {
            pojo.queryId = queryId;
            setDirty(true);
        }
    }

    public String getProducerName() {
        return pojo.producerName;
    }

    public void setProducerName(String producerName) {
        if (!hasSameValue(pojo.producerName, producerName)) {
            pojo.producerName = producerName;
            setDirty(true);
        }
    }

    public String getServiceUrl() {
        return pojo.serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        if (!hasSameValue(pojo.serviceUrl, serviceUrl)) {
            pojo.serviceUrl = serviceUrl;
            setDirty(true);
        }
    }

    public String getRecipientDivisionName() {
        return pojo.recipientDivisionName;
    }

    public void setRecipientDivisionName(String recipientDivisionName) {
        if (!hasSameValue(pojo.recipientDivisionName, recipientDivisionName)) {
            pojo.recipientDivisionName = recipientDivisionName;
            setDirty(true);
        }
    }

    public String getRecipientPositionName() {
        return pojo.recipientPositionName;
    }

    public void setRecipientPositionName(String recipientPositionName) {
        if (!hasSameValue(pojo.recipientPositionName, recipientPositionName)) {
            pojo.recipientPositionName = recipientPositionName;
            setDirty(true);
        }
    }

    public Long getDhlMessageId() {
        return pojo.dhlMessageId;
    }

    void setDhlMessageId(Long dhlMessageId) {
        if (!hasSameValue(dhlMessageId, pojo.dhlMessageId)) {
            pojo.dhlMessageId = dhlMessageId;
            setDirty(true);
        }
    }

    public String getRecipientOrgCode() {
        return pojo.recipientOrgCode;
    }

    public String getRecipientPersonCode() {
        return pojo.recipientPersonCode;
    }

    public BigDecimal getRecipientDivisionId() {
        return pojo.recipientDivisionId;
    }

    public BigDecimal getRecipientPositionId() {
        return pojo.recipientPositionId;
    }

    public BigDecimal getId() {
        return pojo.getId();
    }
}
