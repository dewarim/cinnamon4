package com.dewarim.cinnamon.model.request.user;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.UserAccountWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "listUserAccountRequest")
public class ListUserAccountRequest extends DefaultListRequest implements ListRequest<UserAccount>, ApiRequest<ListUserAccountRequest> {

    @Override
    public Wrapper<UserAccount> fetchResponseWrapper() {
        return new UserAccountWrapper();
    }

    @Override
    public List<ApiRequest<ListUserAccountRequest>> examples() {
        return List.of(new ListUserAccountRequest());
    }
}
