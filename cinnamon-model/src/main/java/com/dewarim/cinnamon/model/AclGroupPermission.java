package com.dewarim.cinnamon.model;

import com.dewarim.cinnamon.api.Identifiable;

import java.util.Objects;

public class AclGroupPermission implements Identifiable {

    private Long id;
    private long aclGroupId;
    private long permissionId;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getAclGroupId() {
        return aclGroupId;
    }

    public void setAclGroupId(long aclGroupId) {
        this.aclGroupId = aclGroupId;
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
        AclGroupPermission that = (AclGroupPermission) o;
        return aclGroupId == that.aclGroupId &&
                permissionId == that.permissionId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(aclGroupId, permissionId);
    }

    @Override
    public String toString() {
        return "AclGroupPermission{" +
                "id=" + id +
                ", aclGroupId=" + aclGroupId +
                ", permissionId=" + permissionId +
                '}';
    }
}
