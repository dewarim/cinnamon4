package com.dewarim.cinnamon.configuration;

public class ServerConfig {

    private String systemRoot = "data/config";
    private String dataRoot = "data";
    private boolean enableHttps = false;
    private int maxThreads = 200;
    private String log4jConfigPath = "";

    private HttpConnectorConfig httpConnectorConfig = new HttpConnectorConfig();

    private HttpsConnectorConfig httpsConnectorConfig = new HttpsConnectorConfig();

    /**
     * If true, check if an object exists in the database before trying to update it.
     */
    private boolean verifyExistence = true;

    /**
     * If true, do not check if an update actually changed something in the database.
     * (So you can decide if sending no-effect update requests to the server is allowed or not.)
     */
    private boolean ignoreNopUpdates = false;

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

    public boolean isVerifyExistence() {
        return verifyExistence;
    }

    public void setVerifyExistence(boolean verifyExistence) {
        this.verifyExistence = verifyExistence;
    }

    public boolean isIgnoreNopUpdates() {
        return ignoreNopUpdates;
    }

    public void setIgnoreNopUpdates(boolean ignoreNopUpdates) {
        this.ignoreNopUpdates = ignoreNopUpdates;
    }

    public boolean isEnableHttps() {
        return enableHttps;
    }

    public void setEnableHttps(boolean enableHttps) {
        this.enableHttps = enableHttps;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public HttpConnectorConfig getHttpConnectorConfig() {
        return httpConnectorConfig;
    }

    public void setHttpConnectorConfig(HttpConnectorConfig httpConnectorConfig) {
        this.httpConnectorConfig = httpConnectorConfig;
    }

    public HttpsConnectorConfig getHttpsConnectorConfig() {
        return httpsConnectorConfig;
    }

    public void setHttpsConnectorConfig(HttpsConnectorConfig httpsConnectorConfig) {
        this.httpsConnectorConfig = httpsConnectorConfig;
    }


    public void setLog4jConfigPath(String log4jConfigPath) {
        this.log4jConfigPath = log4jConfigPath;
    }

    public String getLog4jConfigPath() {
        return log4jConfigPath;
    }

    @Override
    public String toString() {
        return "ServerConfig{" +
                "systemRoot='" + systemRoot + '\'' +
                ", dataRoot='" + dataRoot + '\'' +
                ", enableHttps=" + enableHttps +
                ", maxThreads=" + maxThreads +
                ", httpConnectorConfig=" + httpConnectorConfig +
                ", httpsConnectorConfig=" + httpsConnectorConfig +
                ", verifyExistence=" + verifyExistence +
                ", ignoreNopUpdates=" + ignoreNopUpdates +
                '}';
    }
}
