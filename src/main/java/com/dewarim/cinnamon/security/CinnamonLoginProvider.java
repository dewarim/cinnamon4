package com.dewarim.cinnamon.security;

import com.dewarim.cinnamon.api.login.LoginProvider;
import com.dewarim.cinnamon.api.login.LoginResult;
import com.dewarim.cinnamon.api.login.LoginUser;
import com.dewarim.cinnamon.model.LoginType;

/**
 * Provide the basic login functionality for Cinnamon server.
 */
public class CinnamonLoginProvider implements LoginProvider {

    @Override
    public String getName() {
        return LoginType.CINNAMON.name();
    }

    @Override
    public LoginResult connect(LoginUser userAccount, String password) {
        boolean passwordIsCorrect = HashMaker.compareWithHash(password, userAccount.getPasswordHash());
        return CinnamonLoginResult.createLoginResult(passwordIsCorrect,false);
        
    }
}
