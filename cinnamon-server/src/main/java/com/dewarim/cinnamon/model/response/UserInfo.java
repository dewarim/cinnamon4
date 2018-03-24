package com.dewarim.cinnamon.model.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "user")
public class UserInfo {

    private Long id;
    private String name;
    private String loginType;

    public UserInfo() {
    }

    public UserInfo(Long id, String name, String loginType) {
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

    public String getLoginType() {
        return loginType;
    }
}
