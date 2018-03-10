package com.dewarim.cinnamon.api.login;

public interface LoginProvider {
    
    LoginResult connect(String username, String password);
    
}
