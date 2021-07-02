package com.dewarim.cinnamon.model.request.user;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.UserInfo;
import com.dewarim.cinnamon.model.response.UserWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;

public class ListUserInfoRequest extends DefaultListRequest implements ListRequest<UserInfo>, ApiRequest {

    @Override
    public Wrapper<UserInfo> fetchResponseWrapper() {
        return new UserWrapper();
    }
}
