package com.dewarim.cinnamon.model.configuration;

public class CinnamonConfig {
    
    private int passwordRounds = 10;
    private String systemRoot = "/opt/cinnamon/cinnamon-system";
    private String dataRoot = "/opt/cinnamon/cinnamon-data";
    private String luceneIndexPath = "/opt/cinnamon/cinnamon-data/index";
    private String logbackLoggingConfigPath = "/opt/cinnamon/logback.xml";
        
    private String systemAdministratorEmail;
    private MailConfig mailConfig;

    private SecurityConfig securityConfig;

    private DatabaseConfig databaseConfig;

    public int getPasswordRounds() {
        return passwordRounds;
    }

    public void setPasswordRounds(int passwordRounds) {
        this.passwordRounds = passwordRounds;
    }

    public String getSystemRoot() {
        return systemRoot;
    }

    public void setSystemRoot(String systemRoot) {
        this.systemRoot = systemRoot;
    }

    public String getDataRoot() {
        return dataRoot;
    }

    public void setDataRoot(String dataRoot) {
        this.dataRoot = dataRoot;
    }

    public String getLuceneIndexPath() {
        return luceneIndexPath;
    }

    public void setLuceneIndexPath(String luceneIndexPath) {
        this.luceneIndexPath = luceneIndexPath;
    }

    public String getLogbackLoggingConfigPath() {
        return logbackLoggingConfigPath;
    }

    public void setLogbackLoggingConfigPath(String logbackLoggingConfigPath) {
        this.logbackLoggingConfigPath = logbackLoggingConfigPath;
    }

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
}
