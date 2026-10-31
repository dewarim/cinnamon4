package com.dewarim.cinnamon.model.request.user;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.application.service.RequestValidator;
import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.Optional;

@JsonRootName("getUserAccountRequest")
public record GetUserAccountRequest(@Positive Long userId, String username) implements ApiRequest<GetUserAccountRequest> {

    /**
     * Only the annotations. Whether one of the two fields is set is checked by UserAccountServlet,
     * which answers the more specific USER_INFO_REQUEST_WITHOUT_NAME_OR_ID.
     */
    public Optional<GetUserAccountRequest> validateRequest() {
        RequestValidator.validate(this);
        return Optional.of(this);
    }

    /**
     * Not a validated() rule: UserAccountServlet checks this one so it can answer the more specific
     * error code. It is still part of the contract, so api.md has to state it.
     */
    @Override
    public String additionalRules() {
        return "At least one of userId and username has to be set.";
    }

    public boolean byId() {
        return userId != null;
    }

    public boolean byName() {
        return username != null;
    }

    @Override
    public List<ApiRequest<GetUserAccountRequest>> examples() {
        return List.of(new GetUserAccountRequest(1L, null), new GetUserAccountRequest(null, "by-name"));
    }
}
