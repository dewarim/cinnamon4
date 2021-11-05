package com.dewarim.cinnamon.model.request.user;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.UserAccountWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "updateUserAccountRequest")
public class UpdateUserAccountRequest implements UpdateRequest<UserAccount>, ApiRequest {

    @JacksonXmlElementWrapper(localName = "userAccounts")
    @JacksonXmlProperty(localName = "userAccount")
    private List<UserAccount> userAccounts = new ArrayList<>();

    public UpdateUserAccountRequest() {
    }

    public UpdateUserAccountRequest(List<UserAccount> userAccounts) {
        this.userAccounts = userAccounts;
    }

    public List<UserAccount> getUserAccounts() {
        return userAccounts;
    }

    public void setUserAccounts(List<UserAccount> userAccounts) {
        this.userAccounts = userAccounts;
    }

    @Override
    public List<UserAccount> list() {
        return userAccounts;
    }

    @Override
    public boolean validated() {
        return userAccounts.stream().allMatch(user -> {
            boolean hasValue = user.getName() != null ||
                    user.getLoginType() != null ||
                    (user.getUiLanguageId() != null && user.getUiLanguageId() > 0) ||
                    user.getEmail() != null ||
                    user.getFullname() != null ||
                    (user.getPassword() != null && !user.getPassword().trim().isEmpty());
            return user.getId() != null && user.getId() > 0 && hasValue;
        });
    }

    @Override
    public Wrapper<UserAccount> fetchResponseWrapper() {
        return new UserAccountWrapper();
    }
}
