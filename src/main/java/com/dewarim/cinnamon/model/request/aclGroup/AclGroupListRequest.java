package com.dewarim.cinnamon.model.request.aclGroup;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.application.service.RequestValidator;
import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.Optional;

@JsonRootName("aclGroupListRequest")
public record AclGroupListRequest(@Positive Long id, IdType idType) implements ApiRequest<AclGroupListRequest> {

    public enum IdType {
        ACL, GROUP
    }

    public AclGroupListRequest() {
        this(null, null);
    }

    /**
     * The request has to name something to list by. Which of the two fields it is cannot be said
     * per field, so this one rule stays hand-written.
     */
    private boolean validated() {
        return id != null || idType != null;
    }

    public Optional<AclGroupListRequest> validateRequest() {
        RequestValidator.validate(this);
        return validated() ? Optional.of(this) : Optional.empty();
    }

    @Override
    public List<ApiRequest<AclGroupListRequest>> examples() {
        return List.of(new AclGroupListRequest(1L, IdType.GROUP), new AclGroupListRequest(2L, IdType.ACL));
    }
}
