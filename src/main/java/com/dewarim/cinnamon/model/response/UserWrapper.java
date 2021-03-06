package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.api.ApiResponse;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "cinnamon")
public class UserWrapper implements Wrapper<UserInfo>, ApiResponse {

    @JacksonXmlElementWrapper(localName = "users")
    @JacksonXmlProperty(localName = "user")
    private List<UserInfo> users = new ArrayList<>();

    public UserWrapper() {
    }

    public UserWrapper(List<UserInfo> users) {
        this.users = users;
    }

    public UserWrapper(UserInfo user){
        this.users.add(user);
    }

    public List<UserInfo> getUsers() {
        return users;
    }

    public void setUsers(List<UserInfo> users) {
        this.users = users;
    }

    @Override
    public List<UserInfo> list() {
        return getUsers();
    }

    @Override
    public Wrapper<UserInfo> setList(List<UserInfo> userInfos) {
        setUsers(userInfos);
        return this;
    }
}
