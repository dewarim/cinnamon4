package com.dewarim.cinnamon.model.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "user")
public class UserInfo {

    private Long id;
    private String name;
    private String loginType;
    private boolean activated;
    private boolean locked;

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

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}
