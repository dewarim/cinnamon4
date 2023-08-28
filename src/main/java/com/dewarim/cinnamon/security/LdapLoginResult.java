package com.dewarim.cinnamon.security;

import com.dewarim.cinnamon.api.login.GroupMapping;
import com.dewarim.cinnamon.api.login.LoginResult;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.Collections;
import java.util.List;

@JacksonXmlRootElement(localName = "ldapResult")
public class LdapLoginResult implements LoginResult {
    private String errorMessage;
    private boolean validUser;
    private List<GroupMapping> groupMappings = Collections.emptyList();
    private String defaultLanguageCode;

    public LdapLoginResult(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LdapLoginResult(boolean validUser, List<GroupMapping> groupMappings, String defaultLanguageCode) {
        this.validUser = validUser;
        this.groupMappings = groupMappings;
        this.defaultLanguageCode = defaultLanguageCode;
    }

    public boolean isValidUser() {
        return validUser;
    }

    public void setValidUser(boolean validUser) {
        this.validUser = validUser;
    }

    @Override
    public boolean groupMappingsImplemented() {
        return true;
    }

    @Override
    public List<GroupMapping> getGroupMappings() {
        return groupMappings;
    }

    public void setGroupMappings(List<GroupMapping> groupMappings) {
        this.groupMappings = groupMappings;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String getUiLanguageCode() {
        return null;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getDefaultLanguageCode() {
        return defaultLanguageCode;
    }

    public void setDefaultLanguageCode(String defaultLanguageCode) {
        this.defaultLanguageCode = defaultLanguageCode;
    }
}
