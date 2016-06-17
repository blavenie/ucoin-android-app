package org.duniter.app.model.Entity;

import java.io.Serializable;

/**
 * Created by naivalf27 on 21/04/16.
 */
public class Certification implements Serializable{
    private long id;
    private String publicKey;
    private String uid;
    private boolean isMember;
    private boolean wasMember;
    private long blockNumber;
    private long medianTime;
    private boolean written;
    private String hash;
    private Identity identity;
    private String type;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public boolean isMember() {
        return isMember;
    }

    public void setMember(boolean member) {
        isMember = member;
    }

    public boolean isWasMember() {
        return wasMember;
    }

    public void setWasMember(boolean wasMember) {
        this.wasMember = wasMember;
    }

    public long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(long blockNumber) {
        this.blockNumber = blockNumber;
    }

    public long getMedianTime() {
        return medianTime;
    }

    public void setMedianTime(long medianTime) {
        this.medianTime = medianTime;
    }

    public boolean isWritten() {
        return written;
    }

    public void setWritten(boolean written) {
        this.written = written;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Identity getIdentity() {
        return identity;
    }

    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Certification that = (Certification) o;

        if (isMember != that.isMember) return false;
        if (wasMember != that.wasMember) return false;
        if (blockNumber != that.blockNumber) return false;
        if (medianTime != that.medianTime) return false;
        if (written != that.written) return false;
        if (publicKey != null ? !publicKey.equals(that.publicKey) : that.publicKey != null)
            return false;
        if (uid != null ? !uid.equals(that.uid) : that.uid != null) return false;
        if (hash != null ? !hash.equals(that.hash) : that.hash != null) return false;
        return type != null ? type.equals(that.type) : that.type == null;

    }
}
