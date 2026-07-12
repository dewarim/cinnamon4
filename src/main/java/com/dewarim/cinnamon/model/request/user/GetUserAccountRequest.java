package com.dewarim.cinnamon.model.request.user;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;

@JsonRootName("getUserAccountRequest")
public record GetUserAccountRequest(Long userId, String username) implements ApiRequest<GetUserAccountRequest> {

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
