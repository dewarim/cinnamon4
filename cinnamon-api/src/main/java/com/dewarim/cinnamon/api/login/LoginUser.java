package com.dewarim.cinnamon.api.login;

public interface LoginUser {
    
    String getLoginType();
    String getUsername();
    String getPasswordHash();
}
