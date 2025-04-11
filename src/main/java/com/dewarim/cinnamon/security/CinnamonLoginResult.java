package com.dewarim.cinnamon.security;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.login.GroupMapping;
import com.dewarim.cinnamon.api.login.LoginResult;

import java.util.Collections;
import java.util.List;

public class CinnamonLoginResult implements LoginResult {

    private boolean validUser;
    private boolean newUserCreated;
    private String  errorMessage;

    private CinnamonLoginResult(boolean validUser, boolean newUserCreated) {
        if (validUser) {
            this.validUser      = true;
            this.errorMessage   = "";
            this.newUserCreated = newUserCreated;
        }
        else {
            this.errorMessage = ErrorCode.LOGIN_FAILED.getCode();
        }
    }

    public static CinnamonLoginResult createLoginResult(boolean isValid, boolean newUserCreated) {
        return new CinnamonLoginResult(isValid, newUserCreated);
    }

    @Override
    public List<GroupMapping> getGroupMappings() {
        return Collections.emptyList();
    }

    @Override
    public boolean isValidUser() {
        return validUser;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public boolean groupMappingsImplemented() {
        return false;
    }

    @Override
    public String getUiLanguageCode() {
        // default is ISO 639-3 code "und" for undetermined language. 
        return "und";
    }

    @Override
    public boolean newUserCreated() {
        return newUserCreated;
    }
}
