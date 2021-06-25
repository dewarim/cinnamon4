package com.dewarim.cinnamon.configuration;

public class ServerConfig {
    
    private int port = 9090;
    private String systemRoot = "/opt/cinnamon/cinnamon-system";
    private String dataRoot = "/opt/cinnamon/cinnamon-data";
    private String luceneIndexPath = "/opt/cinnamon/cinnamon-data/index";

    /**
     * If true, check if an object exists in the database before trying to update it.
     */
    private boolean verifyExistence = true;

    /**
     * If true, do not check if an update actually changed something in the database.
     * (So you can decide if sending no-effect update requests to the server is allowed or not.)
     */
    private boolean ignoreNopUpdates = false;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
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
}
