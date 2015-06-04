package io.ucoin.app.model.remote;

import io.ucoin.app.technical.DateUtils;
import io.ucoin.app.technical.ObjectUtils;

public class Identity extends BasicIdentity {

    private static final long serialVersionUID = -7451079677730158794L;

    private long timestamp = -1;

    private Boolean isMember = null;

    /**
     * local ID of the currency
     */
    private Long currencyId;

    /**
     * name of the currency
     */
    private String currency;

    /**
     * The timestamp value of the signature date
     * @return
     */
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Indicate whether the certification is written in the blockchain or not.
     */
    public Boolean getIsMember() {
        return isMember;
    }

    public void setMember(Boolean isMember) {
        this.isMember = isMember;
    }

    /**
     * like getIsMember, but never return <code>null</code> but <code>false</code> instead
     * @return true if member
     */
    public boolean isMember() {
        return Boolean.TRUE.equals(isMember);
    }

    public Long getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(Long currencyId) {
        this.currencyId = currencyId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
    @Override
    public String toString() {
        return new StringBuilder().append(super.toString())
                .append(timestamp == -1 ? "" : ",timestamp=")
                .append(timestamp == -1 ? "" : DateUtils.format(getTimestamp()))
                .append(isMember == null ? "" : ",isMember=")
                .append(isMember == null ? "" : isMember.toString())
                .toString();
    }

    public void copy(Identity identity) {
        super.copy(identity);
        this.timestamp = identity.timestamp;
        this.isMember = identity.isMember;
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        if (o instanceof  Identity) {
            Identity i = (Identity)o;
            return  ObjectUtils.equals(this.isMember, i.isMember)
                    && timestamp == timestamp;
        }
        return false;
    }
}