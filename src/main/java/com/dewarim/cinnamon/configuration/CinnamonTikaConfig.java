package com.dewarim.cinnamon.configuration;

public class CinnamonTikaConfig {

    private String  baseUrl                = "http://localhost:9998";
    private boolean useTika                = false;
    private int     retryDelayInSeconds    = 1;
    private int     maxRetryDelayInSeconds = 10;
    private int     tikaBatchSize          = 8;
    private long    tikaPauseInMillis      = 3000;

    public CinnamonTikaConfig() {
    }

    public CinnamonTikaConfig(String baseUrl, boolean useTika) {
        this.baseUrl = baseUrl;
        this.useTika = useTika;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public boolean isUseTika() {
        return useTika;
    }

    public void setUseTika(boolean useTika) {
        this.useTika = useTika;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public int getRetryDelayInSeconds() {
        return retryDelayInSeconds;
    }

    public void setRetryDelayInSeconds(int retryDelayInSeconds) {
        this.retryDelayInSeconds = retryDelayInSeconds;
    }

    public int getMaxRetryDelayInSeconds() {
        return maxRetryDelayInSeconds;
    }

    public void setMaxRetryDelayInSeconds(int maxRetryDelayInSeconds) {
        this.maxRetryDelayInSeconds = maxRetryDelayInSeconds;
    }

    @Override
    public String toString() {
        return "CinnamonTikaConfig{" +
                "baseUrl='" + baseUrl + '\'' +
                ", useTika=" + useTika +
                ", retryDelayInSeconds=" + retryDelayInSeconds +
                ", maxRetryDelayInSeconds=" + maxRetryDelayInSeconds +
                ", tikaBatchSize=" + tikaBatchSize +
                '}';
    }

    public void setTikaBatchSize(int tikaBatchSize) {
        this.tikaBatchSize = tikaBatchSize;
    }

    public int getTikaBatchSize() {
        return tikaBatchSize;
    }

    public long getTikaPauseInMillis() {
        return tikaPauseInMillis;
    }
}
