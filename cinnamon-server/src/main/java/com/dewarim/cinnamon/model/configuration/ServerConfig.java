package com.dewarim.cinnamon.model.configuration;

public class ServerConfig {
    
    private int port = 9090;
    private String systemRoot = "/opt/cinnamon/cinnamon-system";
    private String dataRoot = "/opt/cinnamon/cinnamon-data";
    private String luceneIndexPath = "/opt/cinnamon/cinnamon-data/index";

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
}
