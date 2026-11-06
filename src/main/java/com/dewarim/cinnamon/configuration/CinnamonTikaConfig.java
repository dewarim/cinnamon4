package com.dewarim.cinnamon.configuration;

public class CinnamonTikaConfig {

    private String  baseUrl                   = "http://localhost:9998";
    private boolean useTika                   = false;
    private int     retryDelayInSeconds       = 1;
    private int     maxRetryDelayInSeconds    = 10;
    private int     tikaBatchSize             = 8;
    private long    tikaPauseInMillis         = 3000;
    private int     maxTikaAttempts           = 3;
    private boolean removeNewXmlVersionHeader = false;

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

    /**
     * How often TikaService tries a single document before giving up on it. Only failures that are
     * specific to the document count - a Tika server that cannot be reached is retried
     * indefinitely. An OSD that is given up on is listed by the index info endpoint and tried
     * again after a reindex with updateTikaMetaset.
     */
    public int getMaxTikaAttempts() {
        return maxTikaAttempts;
    }

    public void setMaxTikaAttempts(int maxTikaAttempts) {
        this.maxTikaAttempts = maxTikaAttempts;
    }

    @Override
    public String toString() {
        return "CinnamonTikaConfig{" +
                "baseUrl='" + baseUrl + '\'' +
                ", useTika=" + useTika +
                ", retryDelayInSeconds=" + retryDelayInSeconds +
                ", maxRetryDelayInSeconds=" + maxRetryDelayInSeconds +
                ", tikaBatchSize=" + tikaBatchSize +
                ", tikaPauseInMillis=" + tikaPauseInMillis +
                ", maxTikaAttempts=" + maxTikaAttempts +
                ", removeNewXmlVersionHeader=" + removeNewXmlVersionHeader +
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

    public boolean isRemoveNewXmlVersionHeader() {
        return removeNewXmlVersionHeader;
    }

    public void setRemoveNewXmlVersionHeader(boolean removeNewXmlVersionHeader) {
        this.removeNewXmlVersionHeader = removeNewXmlVersionHeader;
    }
}
