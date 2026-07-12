package com.dewarim.cinnamon.model.request.permission;

import com.dewarim.cinnamon.api.ApiRequest;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JsonRootName("changePermissionsRequest")
public record ChangePermissionsRequest(
        Long aclGroupId,
        @JacksonXmlElementWrapper(localName = "addPermissions")
        @JacksonXmlProperty(localName = "addId")
        List<Long> add,
        @JacksonXmlElementWrapper(localName = "removePermissions")
        @JacksonXmlProperty(localName = "removeId")
        List<Long> remove) implements ApiRequest<ChangePermissionsRequest> {

    public ChangePermissionsRequest {
        if (add == null) {
            add = new ArrayList<>();
        }
        if (remove == null) {
            remove = new ArrayList<>();
        }
    }

    public boolean validated() {
        boolean hasAdd    = add != null && !add.isEmpty();
        boolean hasRemove = remove != null && !remove.isEmpty();
        if (!hasAdd && !hasRemove) {
            return false;
        }
        return aclGroupId != null && aclGroupId > 0;
    }

    public Optional<ChangePermissionsRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<ApiRequest<ChangePermissionsRequest>> examples() {
        return List.of(new ChangePermissionsRequest(3L, List.of(4L, 5L, 6L), List.of(7L, 8L, 9L)));
    }
}
