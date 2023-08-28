package com.dewarim.cinnamon.security;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.login.LoginProvider;
import com.dewarim.cinnamon.api.login.LoginResult;
import com.dewarim.cinnamon.api.login.LoginUser;
import com.dewarim.cinnamon.application.service.UserService;
import com.dewarim.cinnamon.model.LoginType;
import com.dewarim.cinnamon.model.UserAccount;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Provide LDAP login functionality for Cinnamon server.
 */
public class LdapLoginProvider implements LoginProvider {
    private static final Logger log = LogManager.getLogger();

    public final UserService userService = new UserService();

    @Override
    public String getName() {
        return LoginType.LDAP.name();
    }

    /**
     * For LDAP users, the initial user Account is just a dummy value, as their Cinnamon user accounts are
     * created here (after authentication vs LDAP server)
     * @param userAccount dummy user account, with only the name and loginType set
     * @param password the password
     */
    @Override
    public LoginResult connect(LoginUser userAccount, String password) {

        UnboundIdLdapConnector connector = new UnboundIdLdapConnector();
        if(!connector.isInitialized()){
            throw ErrorCode.LDAP_CONNECTOR_NOT_CONFIGURED.exception();
        }
        String username = userAccount.getUsername();
        LdapLoginResult result = connector.connect(username, password);
        if(result.isValidUser()) {
            List<String> cinnamonGroups = new ArrayList<>();
            result.getGroupMappings().forEach( mapping -> cinnamonGroups.add(mapping.getCinnamonGroup()));

            UserAccount user = userService.createOrUpdateUserAccount(username,cinnamonGroups,LoginType.LDAP, result.getDefaultLanguageCode());
            log.info("Created user account via LDAP: "+user);
            boolean passwordIsCorrect = HashMaker.compareWithHash(password, userAccount.getPasswordHash());
            return CinnamonLoginResult.createLoginResult(passwordIsCorrect);
        }
        throw ErrorCode.LDAP_LOGIN_FAILED.exception();
    }
}
