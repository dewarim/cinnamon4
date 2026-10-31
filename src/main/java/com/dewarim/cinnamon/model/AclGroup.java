package com.dewarim.cinnamon.model;

import com.dewarim.cinnamon.api.Identifiable;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonRootName("aclGroup")
public class AclGroup implements Identifiable {

    // Request validation, checked when a request cascades into this class with @Valid.
    @NotNull(groups = UpdateRequest.class)
    @Positive(groups = UpdateRequest.class)
    private Long id;
    @NotNull
    @Positive
    private Long aclId;
    @NotNull
    @Positive
    private Long groupId;

    @JacksonXmlElementWrapper(localName = "permissions")
    @JacksonXmlProperty(localName = "permissionId")
    private List<@NotNull @Positive Long> permissionIds = new ArrayList<>();

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

    public List<Long> getPermissionIds() {
        return permissionIds;
    }

    public void setPermissionIds(List<Long> permissionIds) {
        this.permissionIds = permissionIds;
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
                Objects.equals(groupId, aclGroup.groupId) &&
                Objects.equals(permissionIds, aclGroup.permissionIds);
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
                ", permissionIds=" + permissionIds +
                '}';
    }
}
