package com.dewarim.cinnamon.model;

import com.dewarim.cinnamon.api.Identifiable;

import java.util.Objects;

public class AclEntryPermission implements Identifiable {

    private Long id;
    private long aclEntryId;
    private long permissionId;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getAclEntryId() {
        return aclEntryId;
    }

    public void setAclEntryId(long aclEntryId) {
        this.aclEntryId = aclEntryId;
    }

    public long getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(long permissionId) {
        this.permissionId = permissionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AclEntryPermission that = (AclEntryPermission) o;
        return aclEntryId == that.aclEntryId &&
                permissionId == that.permissionId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(aclEntryId, permissionId);
    }

    @Override
    public String toString() {
        return "AclEntryPermission{" +
                "id=" + id +
                ", aclEntryId=" + aclEntryId +
                ", permissionId=" + permissionId +
                '}';
    }
}
