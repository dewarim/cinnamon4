package com.dewarim.cinnamon.security.authorization;

import java.util.Objects;

public class AclPermission {

    private final long aclId;
    private final long permissionId;

    /**
     * true if this AclPermission was checked with regards to owner's acl permission.
     * <br>
     * OSD and Folders may have an owner, and an Acl may grant (via AclGroup with system group '_owner')
     * specific permissions to the dynamically calculated owner. Dynamically calculated means it
     * is dependent on the ownerId of the object, not purely on the current user and acl.
     */
    private final boolean ownerPermission;

    public AclPermission(long aclId, long permissionId, boolean ownerPermission) {
        this.aclId = aclId;
        this.permissionId = permissionId;
        this.ownerPermission = ownerPermission;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AclPermission that = (AclPermission) o;
        return aclId == that.aclId &&
                ownerPermission == that.ownerPermission &&
                permissionId == that.permissionId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(aclId, permissionId, ownerPermission);
    }

    @Override
    public String toString() {
        return "AclPermission{" +
                "aclId=" + aclId +
                ", permissionId=" + permissionId +
                ", ownerPermission=" + ownerPermission +
                '}';
    }
}
