package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.api.ApiResponse;
import com.dewarim.cinnamon.model.LoginType;
import com.dewarim.cinnamon.model.UserAccount;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "cinnamon")
public class UserAccountWrapper implements Wrapper<UserAccount>, ApiResponse {


    @JacksonXmlElementWrapper(localName = "users")
    @JacksonXmlProperty(localName = "user")
    private List<UserAccount> users = new ArrayList<>();

    public UserAccountWrapper() {
    }

    public UserAccountWrapper(List<UserAccount> users) {
        this.users = users;
    }

    public List<UserAccount> getUsers() {
        return users;
    }

    public void setUsers(List<UserAccount> users) {
        this.users = users;
    }

    @Override
    public List<UserAccount> list() {
        return users;
    }

    @Override
    public Wrapper<UserAccount> setList(List<UserAccount> users) {
        this.users = users;
        return this;
    }

    @Override
    public List<Object> examples() {
        UserAccount userAccount = new UserAccount("user-wrapper-example", "see-creta", "U.W.Example", "user@example.com", 1L, LoginType.CINNAMON.name(), true, true, true);
        userAccount.setGroupIds(List.of(3L, 5L));
        return List.of(new UserAccountWrapper(List.of(userAccount)));
    }
}
