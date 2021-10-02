package com.dewarim.cinnamon.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class DatabaseConfig {

    private String driver = "org.postgresql.Driver";
    private String host = "127.0.0.1";
    private String port = "5432";
    private String database = "cinnamon_test";
    private String user = "cinnamon";
    private String password = "cinnamon";

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    @JsonIgnore
    public String getDatabaseUrl(){
        return String.format("jdbc:postgresql://%s:%s/%s",host,port,database);
    }
}
