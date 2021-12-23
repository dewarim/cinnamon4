package com.dewarim.cinnamon.security;

import com.dewarim.cinnamon.api.login.LoginProvider;
import com.dewarim.cinnamon.api.login.LoginResult;
import com.dewarim.cinnamon.api.login.LoginUser;

import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

public class LoginProviderService {

    private final ServiceLoader<LoginProvider> serviceLoader;

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
        String loginType = loginUser.getLoginType();
        try {
            for (LoginProvider provider : serviceLoader) {
                if (provider.getName().equals(loginType)) {
                    return provider.connect(loginUser, password);
                }
            }
            throw new IllegalStateException("Found no valid login service provider for " + loginType);
        } catch (ServiceConfigurationError e) {
            throw new RuntimeException("Failed to find a valid login service provider for " + loginType, e);
        }
    }

    public List<LoginProvider> getProviderList(){
        return serviceLoader.stream().map(ServiceLoader.Provider::get).toList();
    }
}
