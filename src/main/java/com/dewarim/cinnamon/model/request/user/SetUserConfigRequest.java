package com.dewarim.cinnamon.model.request.user;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.application.service.RequestValidator;
import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.Optional;

@JsonRootName("setUserConfigRequest")
public record SetUserConfigRequest(@NotNull @Positive Long userId,
                                   // users.config is NOT NULL
                                   @NotNull String config) implements ApiRequest<SetUserConfigRequest> {

    public Optional<SetUserConfigRequest> validateRequest() {
        RequestValidator.validate(this);
        return Optional.of(this);
    }

    @Override
    public List<ApiRequest<SetUserConfigRequest>> examples() {
        return List.of(new SetUserConfigRequest(123L,
                "<config><lastSearches><search>foo</search></lastSearches></config>"));
    }
}
