package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.model.LoginType;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "user")
public class UserInfo {

    private Long id;
    private String name;
    private LoginType loginType;

    public UserInfo() {
    }

    public UserInfo(Long id, String name, LoginType loginType) {
        this.id = id;
        this.name = name;
        this.loginType = loginType;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LoginType getLoginType() {
        return loginType;
    }
}
