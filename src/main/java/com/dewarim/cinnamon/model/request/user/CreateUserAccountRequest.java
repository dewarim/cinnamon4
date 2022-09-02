package com.dewarim.cinnamon.model.request.user;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.LoginType;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.UserAccountWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "createUserAccountRequest")
public class CreateUserAccountRequest implements CreateRequest<UserAccount>, ApiRequest {

    @JacksonXmlElementWrapper(localName = "userAccounts")
    @JacksonXmlProperty(localName = "userAccount")
    private List<UserAccount> userAccounts = new ArrayList<>();

    public CreateUserAccountRequest() {
    }

    public CreateUserAccountRequest(String name, String password, String fullname, String email, Long uiLanguageId,
                                    String loginType, Boolean changeTracking, Boolean activated, Boolean activateTriggers) {
        UserAccount userAccount = new UserAccount(name, password, fullname, email, uiLanguageId,
                loginType, changeTracking, activated, activateTriggers);
        userAccount.setLocked(false);
        userAccount.setPasswordExpired(false);
        userAccounts.add(userAccount);
    }

    public CreateUserAccountRequest(List<UserAccount> userAccounts) {
        this.userAccounts = userAccounts;
    }

    @Override
    public boolean validated() {
        return userAccounts.stream().allMatch(user -> {
            boolean nonNull = user.getName() != null && user.getPassword() != null && user.getFullname() != null
                    && user.getEmail() != null && user.getUiLanguageId() != null
                    && user.getLoginType() != null;
            if (!nonNull) {
                return false;
            }
            boolean empty = user.getName().isEmpty() || user.getPassword().isEmpty() || user.getFullname().isEmpty()
                    || user.getEmail().isEmpty() || user.getLoginType().isEmpty();
            if (empty) {
                return false;
            }
            return user.getUiLanguageId() > 0;
        });
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
    public List<Object> examples() {
        return List.of(new CreateUserAccountRequest("jane", "super-secret", "Jane Doe", "jane@example.com", 1L,
                LoginType.CINNAMON.name(), false, true, true));
    }
}
