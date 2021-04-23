package com.dewarim.cinnamon.model.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.Objects;

@JacksonXmlRootElement(localName = "user")
public class UserInfo {

    private Long    id;
    private String  name;
    private String  loginType;
    private boolean activated;
    private boolean locked;
    private Long    uiLanguageId;
    private String  fullname;
    private String  email;
    private boolean changeTracking;
    private boolean passwordExpired;

    public UserInfo() {
    }

    public UserInfo(Long id, String name, String loginType, boolean activated, boolean locked, Long uiLanguageId, String email, String fullname,
                    boolean changeTracking, boolean passwordExpired) {
        this.id = id;
        this.name = name;
        this.loginType = loginType;
        this.activated = activated;
        this.locked = locked;
        this.uiLanguageId = uiLanguageId;
        this.email = email;
        this.fullname = fullname;
        this.changeTracking = changeTracking;
        this.passwordExpired = passwordExpired;
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

    public Long getUiLanguageId() {
        return uiLanguageId;
    }

    public void setUiLanguageId(Long uiLanguageId) {
        this.uiLanguageId = uiLanguageId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public boolean isChangeTracking() {
        return changeTracking;
    }

    public void setChangeTracking(boolean changeTracking) {
        this.changeTracking = changeTracking;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserInfo userInfo = (UserInfo) o;
        return activated == userInfo.activated &&
                locked == userInfo.locked &&
                changeTracking == userInfo.changeTracking &&
                Objects.equals(name, userInfo.name) &&
                Objects.equals(fullname, userInfo.fullname) &&
                Objects.equals(loginType, userInfo.loginType) &&
                Objects.equals(uiLanguageId, userInfo.uiLanguageId) &&
                Objects.equals(email, userInfo.email);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, loginType, activated, locked, uiLanguageId);
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", fullname='" + fullname + '\'' +
                ", loginType='" + loginType + '\'' +
                ", activated=" + activated +
                ", locked=" + locked +
                ", uiLanguageId=" + uiLanguageId +
                ", email='" + email + '\'' +
                ", changeTracking=" + changeTracking +
                '}';
    }
}
