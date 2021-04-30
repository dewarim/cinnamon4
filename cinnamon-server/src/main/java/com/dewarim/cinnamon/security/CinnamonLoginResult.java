package com.dewarim.cinnamon.security;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.login.GroupMapping;
import com.dewarim.cinnamon.api.login.LoginResult;

import java.util.Collections;
import java.util.List;

public class CinnamonLoginResult implements LoginResult {
    
    private boolean validUser;
    private String errorMessage;
    
    private static CinnamonLoginResult invalidResult = new CinnamonLoginResult(false);
    private static CinnamonLoginResult validResult = new CinnamonLoginResult(true);
    
    private CinnamonLoginResult(boolean validUser){
        if(validUser){
           this.validUser = true;
           this.errorMessage = "";
        }
        else{
            this.errorMessage = ErrorCode.LOGIN_FAILED.getCode(); 
        }
    }
    
    public static CinnamonLoginResult createLoginResult(boolean isValid){
        if(isValid){
            return validResult;
        }
        return invalidResult;
    }
    
    public CinnamonLoginResult() {
        this.validUser = true;
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
}
