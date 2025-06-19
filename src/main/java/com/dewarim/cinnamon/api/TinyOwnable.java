package com.dewarim.cinnamon.api;

/**
 * Used to create ownables on-the-fly (for example: in link filtering)
 */
public class TinyOwnable implements Ownable {

    private Long aclId;
    private Long ownerId;
    private Long id;

    public void setAclId(Long aclId) {
        this.aclId = aclId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Long getOwnerId() {
        return ownerId;
    }

    @Override
    public Long getAclId() {
        return aclId;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "TinyOwnable{" +
                "aclId=" + aclId +
                ", ownerId=" + ownerId +
                ", id=" + id +
                '}';
    }
}
