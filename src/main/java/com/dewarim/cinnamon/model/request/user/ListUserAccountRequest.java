package com.dewarim.cinnamon.model.request.user;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListType;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.UserAccountWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;

@JsonRootName("listUserAccountRequest")
public record ListUserAccountRequest(ListType type) implements DefaultListRequest, ListRequest<UserAccount>, ApiRequest<ListUserAccountRequest> {

    public ListUserAccountRequest {
        if (type == null) {
            type = ListType.FULL;
        }
    }

    public ListUserAccountRequest() {
        this(ListType.FULL);
    }

    @Override
    public Wrapper<UserAccount> fetchResponseWrapper() {
        return new UserAccountWrapper();
    }

    @Override
    public List<ApiRequest<ListUserAccountRequest>> examples() {
        return List.of(new ListUserAccountRequest());
    }
}
