package com.dewarim.cinnamon.model;

import com.dewarim.cinnamon.api.Identifiable;

import java.util.Objects;

public class AclGroup implements Identifiable {

    private Long id;
    private Long aclId;
    private Long groupId;

    public AclGroup() {
    }

    public AclGroup(Long aclId, Long groupId) {
        this.aclId = aclId;
        this.groupId = groupId;
    }

    public AclGroup(Long id, Long aclId, Long groupId) {
        this.id = id;
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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AclGroup aclGroup = (AclGroup) o;
        return Objects.equals(aclId, aclGroup.aclId) &&
                Objects.equals(groupId, aclGroup.groupId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(aclId, groupId);
    }

    @Override
    public String toString() {
        return "AclGroup{" +
                "id=" + id +
                ", aclId=" + aclId +
                ", groupId=" + groupId +
                '}';
    }
}
