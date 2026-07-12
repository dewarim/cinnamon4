package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.api.ApiResponse;
import com.dewarim.cinnamon.model.Permission;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

@JsonRootName("cinnamon")
public class PermissionWrapper extends BaseResponse implements Wrapper<Permission>, ApiResponse {

    @JacksonXmlElementWrapper(localName = "permissions")
    @JacksonXmlProperty(localName = "permission")
    List<Permission> permissions = new ArrayList<>();

    public List<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }

    @Override
    public List<Permission> list() {
        return getPermissions();
    }

    @Override
    public Wrapper<Permission> setList(List<Permission> permissions) {
        setPermissions(permissions);
        return this;
    }

    @Override
    public List<Object> examples() {
        PermissionWrapper wrapper = new PermissionWrapper();
        Permission permission1 = new Permission(98L, "log viewer permission");
        Permission permission2 = new Permission(99L, "log writer permission ");
        wrapper.setPermissions(List.of(permission1,permission2));
        return List.of(wrapper);
    }
}
