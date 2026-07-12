package com.dewarim.cinnamon.model.request.user;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;
import java.util.Optional;

@JsonRootName("setUserConfigRequest")
public record SetUserConfigRequest(Long userId, String config) implements ApiRequest<SetUserConfigRequest> {

    public Optional<SetUserConfigRequest> validateRequest() {
        if (config != null && userId != null && userId > 0) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<ApiRequest<SetUserConfigRequest>> examples() {
        return List.of(new SetUserConfigRequest(123L,
                "<config><lastSearches><search>foo</search></lastSearches></config>"));
    }
}
