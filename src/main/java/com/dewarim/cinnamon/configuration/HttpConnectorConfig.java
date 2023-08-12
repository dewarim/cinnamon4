package com.dewarim.cinnamon.configuration;

public class HttpConnectorConfig {

    private int port = 8080;
    private int acceptors = 1;
    private int selectors = 1;
    private int acceptQueueSize = 128;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getAcceptors() {
        return acceptors;
    }

    public void setAcceptors(int acceptors) {
        this.acceptors = acceptors;
    }

    public int getSelectors() {
        return selectors;
    }

    public void setSelectors(int selectors) {
        this.selectors = selectors;
    }

    public int getAcceptQueueSize() {
        return acceptQueueSize;
    }

    public void setAcceptQueueSize(int acceptQueueSize) {
        this.acceptQueueSize = acceptQueueSize;
    }

    @Override
    public String toString() {
        return "HttpConnectorConfig{" +
                "port=" + port +
                ", acceptors=" + acceptors +
                ", selectors=" + selectors +
                ", acceptQueueSize=" + acceptQueueSize +
                '}';
    }
}
