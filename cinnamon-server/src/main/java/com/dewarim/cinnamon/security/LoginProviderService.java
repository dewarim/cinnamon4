package com.dewarim.cinnamon.security;

import com.dewarim.cinnamon.api.login.LoginProvider;
import com.dewarim.cinnamon.api.login.LoginResult;
import com.dewarim.cinnamon.api.login.LoginUser;

import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

public class LoginProviderService {

    private ServiceLoader<LoginProvider> serviceLoader;

    private static LoginProviderService loginProviderService;

    private LoginProviderService() {
        serviceLoader = ServiceLoader.load(LoginProvider.class);
    }

    public static synchronized LoginProviderService getInstance() {
        if (loginProviderService == null) {
            loginProviderService = new LoginProviderService();
        }
        return loginProviderService;
    }

    public LoginResult connect(LoginUser loginUser, String password) {
        LoginProvider loginProvider = null;
        String loginType = loginUser.getLoginType();
        try {
            Iterator<LoginProvider> providers = serviceLoader.iterator();
            while (loginProvider == null && providers.hasNext()) {
                loginProvider = providers.next();
                if (loginProvider.getName().equals(loginType)) {
                    return loginProvider.connect(loginUser, password);
                }
            }
            throw new IllegalStateException("Found no valid login service provider for " + loginType);
        } catch (ServiceConfigurationError e) {
            throw new RuntimeException("Failed to find a valid login service provider for " + loginType, e);
        }


    }
}
