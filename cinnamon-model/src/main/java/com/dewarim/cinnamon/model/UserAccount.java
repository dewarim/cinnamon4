package com.dewarim.cinnamon.model;

import com.dewarim.cinnamon.api.login.LoginUser;

import java.security.Principal;
import java.util.Objects;

/**
 */
public class UserAccount implements Principal, LoginUser {
    
    private Long id;
    private String name;
    private Long objVersion;
    private String loginType;
    private String password;
    private boolean activated;
    private boolean locked;
    private Long uiLanguageId;
    private String fullname;
    private String email;
    private boolean changeTracking = true;
        
    @Override
    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getObjVersion() {
        return objVersion;
    }

    public void setObjVersion(Long objVersion) {
        this.objVersion = objVersion;
    }

    public String getLoginType() {
        return loginType;
    }

    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    @Override
    public String getUsername() {
        return name;
    }

    @Override
    public String getPasswordHash() {
        return password;
    }

    public Long getUiLanguageId() {
        return uiLanguageId;
    }

    public void setUiLanguageId(Long uiLanguageId) {
        this.uiLanguageId = uiLanguageId;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isChangeTracking() {
        return changeTracking;
    }

    public void setChangeTracking(boolean changeTracking) {
        this.changeTracking = changeTracking;
    }

    @Override
    public String toString() {
        return "UserAccount{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", objVersion=" + objVersion +
                ", loginType='" + loginType + '\'' +
                ", password='" + password + '\'' +
                ", activated=" + activated +
                ", locked=" + locked +
                ", uiLanguageId=" + uiLanguageId +
                ", fullname='" + fullname + '\'' +
                ", email='" + email + '\'' +
                ", changeTracking=" + changeTracking +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserAccount that = (UserAccount) o;
        return activated == that.activated &&
                locked == that.locked &&
                changeTracking == that.changeTracking &&
                Objects.equals(name, that.name) &&
                Objects.equals(objVersion, that.objVersion) &&
                Objects.equals(loginType, that.loginType) &&
                Objects.equals(password, that.password) &&
                Objects.equals(uiLanguageId, that.uiLanguageId) &&
                Objects.equals(fullname, that.fullname) &&
                Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
    
}