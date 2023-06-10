package com.dewarim.cinnamon.configuration;

import java.util.ArrayList;
import java.util.List;

public class CinnamonConfig {
    
    private ServerConfig serverConfig = new ServerConfig();

    private String systemAdministratorEmail;
    private MailConfig mailConfig = new MailConfig();

    private SecurityConfig securityConfig = new SecurityConfig();

    private DatabaseConfig databaseConfig = new DatabaseConfig();

    private LuceneConfig luceneConfig = new LuceneConfig();

    private CinnamonTikaConfig cinnamonTikaConfig = new CinnamonTikaConfig();
    
    private List<LoginProviderConfig> loginProviders = new ArrayList<>();

    private ChangeTriggerConfig changeTriggerConfig = new ChangeTriggerConfig();
    
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

    public LuceneConfig getLuceneConfig() {
        return luceneConfig;
    }

    public void setLuceneConfig(LuceneConfig luceneConfig) {
        this.luceneConfig = luceneConfig;
    }

    public CinnamonTikaConfig getCinnamonTikaConfig() {
        return cinnamonTikaConfig;
    }

    public void setCinnamonTikaConfig(CinnamonTikaConfig cinnamonTikaConfig) {
        this.cinnamonTikaConfig = cinnamonTikaConfig;
    }

    public ChangeTriggerConfig getChangeTriggerConfig() {
        return changeTriggerConfig;
    }

    public void setChangeTriggerConfig(ChangeTriggerConfig changeTriggerConfig) {
        this.changeTriggerConfig = changeTriggerConfig;
    }
}
