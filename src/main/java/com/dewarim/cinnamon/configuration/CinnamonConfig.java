package com.dewarim.cinnamon.configuration;

import java.util.ArrayList;
import java.util.List;

public class CinnamonConfig {
    
    private ServerConfig serverConfig = new ServerConfig();

    private String systemAdministratorEmail;
    private MailConfig mailConfig = new MailConfig();

    private SecurityConfig securityConfig = new SecurityConfig();

    private DatabaseConfig databaseConfig = new DatabaseConfig();
    
    private List<LoginProviderConfig> loginProviders = new ArrayList<>();
    
    public String getSystemAdministratorEmail() {
        return systemAdministratorEmail;
    }

    public void setSystemAdministratorEmail(String systemAdministratorEmail) {
        this.systemAdministratorEmail = systemAdministratorEmail;
    }

    public MailConfig getMailConfig() {
        return mailConfig;
    }

    public void setMailConfig(MailConfig mailConfig) {
        this.mailConfig = mailConfig;
    }

    public SecurityConfig getSecurityConfig() {
        return securityConfig;
    }

    public void setSecurityConfig(SecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
    }

    public DatabaseConfig getDatabaseConfig() {
        return databaseConfig;
    }

    public void setDatabaseConfig(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public void setServerConfig(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public List<LoginProviderConfig> getLoginProviders() {
        return loginProviders;
    }

    public void setLoginProviders(List<LoginProviderConfig> loginProviders) {
        this.loginProviders = loginProviders;
    }
}
