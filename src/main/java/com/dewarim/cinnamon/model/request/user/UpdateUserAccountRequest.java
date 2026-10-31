package com.dewarim.cinnamon.model.request.user;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.UserAccountWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

import static com.dewarim.cinnamon.model.LoginType.CINNAMON;

@JsonRootName("updateUserAccountRequest")
public record UpdateUserAccountRequest(
        @JacksonXmlElementWrapper(localName = "userAccounts")
        @JacksonXmlProperty(localName = "userAccount")
        @NotEmpty List<@Valid UserAccount> userAccounts) implements UpdateRequest<UserAccount>, ApiRequest<UpdateUserAccountRequest> {

    public UpdateUserAccountRequest {
        if (userAccounts == null) {
            userAccounts = new ArrayList<>();
        }
    }

    @Override
    public List<UserAccount> list() {
        return userAccounts;
    }

    @Override
    public Wrapper<UserAccount> fetchResponseWrapper() {
        return new UserAccountWrapper();
    }

    @Override
    public List<ApiRequest<UpdateUserAccountRequest>> examples() {
        UserAccount userAccount = new UserAccount("foo", "secretPassword", "Mr Foo Bar", "foo@example.com", 1L, CINNAMON.name(), true, true, true);
        userAccount.setGroupIds(List.of(10L, 12L, 45L));
        return List.of(new UpdateUserAccountRequest(List.of(userAccount)));
    }
}
