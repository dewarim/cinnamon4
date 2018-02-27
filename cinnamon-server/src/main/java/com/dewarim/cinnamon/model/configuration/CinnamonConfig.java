package com.dewarim.cinnamon.model.configuration;

public class CinnamonConfig {
    
    private ServerConfig serverConfig = new ServerConfig();

    private String logbackLoggingConfigPath = "/opt/cinnamon/logback.xml";
        
    private String systemAdministratorEmail;
    private MailConfig mailConfig = new MailConfig();

    private SecurityConfig securityConfig = new SecurityConfig();

    private DatabaseConfig databaseConfig = new DatabaseConfig();
    
    private LdapConfig ldapConfig = new LdapConfig();

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

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public void setServerConfig(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public LdapConfig getLdapConfig() {
        return ldapConfig;
    }

    public void setLdapConfig(LdapConfig ldapConfig) {
        this.ldapConfig = ldapConfig;
    }
}
