package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.api.ApiResponse;
import com.dewarim.cinnamon.model.UserAccount;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.ArrayList;
import java.util.List;

public class UserAccountWrapper implements Wrapper<UserAccount>, ApiResponse {


    @JacksonXmlElementWrapper(localName = "users")
    @JacksonXmlProperty(localName = "user")
    private List<UserAccount> users = new ArrayList<>();

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

}