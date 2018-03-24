package com.dewarim.cinnamon.api.login;

public interface LoginProvider {

    /**
     * 
     * @return the name of this provider, used to differentiate between providers. 
     * Providers will be picked by name, depending on the value in UserAccount.loginType.
     */
    String getName();
    
    LoginResult connect(LoginUser loginUser, String password);
    
}
