package com.dewarim.cinnamon.configuration;

public class CinnamonTikaConfig {

    private String baseUrl = "http://localhost:9998";
    private boolean useTika = false;

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

    @Override
    public String toString() {
        return "CinnamonTikaConfig{" +
                "baseUrl='" + baseUrl + '\'' +
                ", useTika=" + useTika +
                '}';
    }
}
