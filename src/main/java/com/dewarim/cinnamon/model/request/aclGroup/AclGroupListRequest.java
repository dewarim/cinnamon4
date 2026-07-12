package com.dewarim.cinnamon.model.request.aclGroup;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;
import java.util.Optional;

@JsonRootName("aclGroupListRequest")
public record AclGroupListRequest(Long id, IdType idType) implements ApiRequest<AclGroupListRequest> {

    public enum IdType {
        ACL, GROUP
    }

    public AclGroupListRequest() {
        this(null, null);
    }

    private boolean validated() {
        return id != null && id > 0 || (idType != null);
    }

    public Optional<AclGroupListRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<ApiRequest<AclGroupListRequest>> examples() {
        return List.of(new AclGroupListRequest(1L, IdType.GROUP), new AclGroupListRequest(2L, IdType.ACL));
    }
}
