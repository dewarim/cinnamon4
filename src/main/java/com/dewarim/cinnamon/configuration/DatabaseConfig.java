package com.dewarim.cinnamon.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class DatabaseConfig {

    /**
     * The value MyBatis used before this became configurable, so an existing config file that does not
     * mention it behaves exactly as it did.
     */
    public static final int DEFAULT_POOL_MAXIMUM_ACTIVE_CONNECTIONS = 50;

    private String driver = "org.postgresql.Driver";
    private String host = "127.0.0.1";
    private String port = "5432";
    private String database = "cinnamon_test";
    private String user = "cinnamon";
    private String password = "cinnamon";
    /**
     * How many connections MyBatis' pooled data source may hand out at once. The server needs one per
     * request in flight plus one per background thread, so a value below the Jetty thread pool size
     * turns load into waiting for a connection rather than into work.
     */
    private int    poolMaximumActiveConnections = DEFAULT_POOL_MAXIMUM_ACTIVE_CONNECTIONS;

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

    public int getPoolMaximumActiveConnections() {
        return poolMaximumActiveConnections;
    }

    public void setPoolMaximumActiveConnections(int poolMaximumActiveConnections) {
        this.poolMaximumActiveConnections = poolMaximumActiveConnections;
    }

    @JsonIgnore
    public String getDatabaseUrl(){
        return String.format("jdbc:postgresql://%s:%s/%s",host,port,database);
    }

    @Override
    public String toString() {
        return "DatabaseConfig{" +
                "driver='" + driver + '\'' +
                ", host='" + host + '\'' +
                ", port='" + port + '\'' +
                ", database='" + database + '\'' +
                ", user='" + user + '\'' +
                ", password='" + password + '\'' +
                ", poolMaximumActiveConnections=" + poolMaximumActiveConnections +
                '}';
    }
}
