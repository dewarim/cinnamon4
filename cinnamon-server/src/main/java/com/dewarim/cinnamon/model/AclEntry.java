package com.dewarim.cinnamon.model;

import java.util.Objects;

public class AclEntry {
    
    private Long id;
    private Long aclId;
    private Long groupId;

    public AclEntry() {
    }

    public AclEntry(Long aclId, Long groupId) {
        this.aclId = aclId;
        this.groupId = groupId;
    }

    public Long getAclId() {
        return aclId;
    }

    public void setAclId(Long aclId) {
        this.aclId = aclId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AclEntry aclEntry = (AclEntry) o;
        return Objects.equals(aclId, aclEntry.aclId) &&
                Objects.equals(groupId, aclEntry.groupId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(aclId, groupId);
    }

    @Override
    public String toString() {
        return "AclEntry{" +
                "id=" + id +
                ", aclId=" + aclId +
                ", groupId=" + groupId +
                '}';
    }
}
