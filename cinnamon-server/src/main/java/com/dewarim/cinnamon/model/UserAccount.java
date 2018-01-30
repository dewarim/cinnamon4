package com.dewarim.cinnamon.model;

import java.security.Principal;

/**
 */
public class UserAccount implements Principal {
    
    private Long id;
    private String name;
    private Long objVersion;
    private LoginType loginType;
    
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

    public LoginType getLoginType() {
        return loginType;
    }

    public void setLoginType(LoginType loginType) {
        this.loginType = loginType;
    }

    @Override
    public String toString() {
        return "UserAccount{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", objVersion=" + objVersion +
                ", loginType=" + loginType +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserAccount that = (UserAccount) o;

        if (!id.equals(that.id)) return false;
        if (!name.equals(that.name)) return false;
        return loginType == that.loginType;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
