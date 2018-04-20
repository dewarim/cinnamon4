package com.dewarim.cinnamon.security.authorization;

import java.util.Objects;

public class AclPermission {
    
    private long aclId;
    private long permissionId;

    public AclPermission(long aclId, long permissionId) {
        this.aclId = aclId;
        this.permissionId = permissionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AclPermission that = (AclPermission) o;
        return aclId == that.aclId &&
                permissionId == that.permissionId;
    }

    @Override
    public int hashCode() {

        return Objects.hash(aclId, permissionId);
    }
}
