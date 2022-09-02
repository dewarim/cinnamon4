package com.dewarim.cinnamon.model;

import com.dewarim.cinnamon.api.Identifiable;
import com.dewarim.cinnamon.api.login.LoginUser;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 */
public class UserAccount implements Principal, LoginUser, Identifiable {

    private Long       id;
    private String     name;
    private String     loginType;
    private String     password;
    private boolean    activated;
    private boolean    locked;
    private Long       uiLanguageId;
    private String     fullname;
    private String     email;
    private boolean    changeTracking;
    private boolean    activateTriggers = true;
    @JsonIgnore
    private String     token;
    @JsonIgnore
    private int        tokensToday;
    private boolean    passwordExpired;
    private List<Long> groupIds         = new ArrayList<>();

    public UserAccount() {
    }

    public UserAccount(String name, String password, String fullname, String email, Long uiLanguageId, String loginType,
                       Boolean changeTracking, Boolean activated, Boolean activateTriggers) {
        this.name = name;
        this.password = password;
        this.fullname = fullname;
        this.email = email;
        this.uiLanguageId = uiLanguageId;
        this.loginType = loginType;
        this.changeTracking = changeTracking;
        this.activated = activated;
        this.activateTriggers = activateTriggers;
    }

    public boolean isActivateTriggers() {
        return activateTriggers;
    }

    public void setActivateTriggers(boolean activateTriggers) {
        this.activateTriggers = activateTriggers;
    }

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
    @JsonIgnore
    public String getUsername() {
        return name;
    }

    @Override
    @JsonIgnore
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getTokensToday() {
        return tokensToday;
    }

    public void setTokensToday(int tokensToday) {
        this.tokensToday = tokensToday;
    }

    public boolean isPasswordExpired() {
        return passwordExpired;
    }

    public void setPasswordExpired(boolean passwordExpired) {
        this.passwordExpired = passwordExpired;
    }

    public List<Long> getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(List<Long> groupIds) {
        this.groupIds = groupIds;
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
        return activated == that.activated && locked == that.locked && changeTracking == that.changeTracking
                && tokensToday == that.tokensToday && passwordExpired == that.passwordExpired
                && id.equals(that.id) && name.equals(that.name) && loginType.equals(that.loginType)
                && password.equals(that.password) && Objects.equals(uiLanguageId, that.uiLanguageId)
                && Objects.equals(fullname, that.fullname) && Objects.equals(email, that.email)
                && Objects.equals(activateTriggers, that.activateTriggers)
                && Objects.equals(token, that.token) && Objects.equals(groupIds, that.groupIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "UserAccount{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", loginType='" + loginType + '\'' +
                ", password='" + "***censored***" + '\'' +
                ", activated=" + activated +
                ", locked=" + locked +
                ", uiLanguageId=" + uiLanguageId +
                ", fullname='" + fullname + '\'' +
                ", email='" + email + '\'' +
                ", changeTracking=" + changeTracking +
                ", activateTriggers=" + activateTriggers +
                ", token='" + token + '\'' +
                ", tokensToday=" + tokensToday +
                ", passwordExpired=" + passwordExpired +
                '}';
    }

    public void filterInfo() {
        setPassword(null);
        setToken(null);
    }
}
