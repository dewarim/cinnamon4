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

    @Override
    public String getUsername() {
        return name;
    }

    @Override
    public String getPasswordHash() {
        return password;
    }

    @Override
    public String toString() {
        return "UserAccount{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", objVersion=" + objVersion +
                ", loginType=" + loginType +
                ", password='" + "*** filtered ***" + '\'' +
                ", activated=" + activated +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAccount that = (UserAccount) o;
        return activated == that.activated &&
                Objects.equals(name, that.name) &&
                loginType.equals(that.loginType) &&
                Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
    
}
