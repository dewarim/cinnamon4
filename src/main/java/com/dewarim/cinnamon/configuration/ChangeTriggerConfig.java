package com.dewarim.cinnamon.configuration;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.hc.core5.http.ssl.TLS;

import java.util.List;

public class ChangeTriggerConfig {

    private int          socketTimeoutSeconds     = 60;
    private int          connectionTimeoutSeconds = 60;
    private int          timeToLiveSeconds        = 600;

    @JacksonXmlElementWrapper(localName = "tlsVersions")
    @JacksonXmlProperty(localName = "tlsVersion")
    private List<String> tlsVersions = List.of(TLS.V_1_3.name());

    public int getSocketTimeoutSeconds() {
        return socketTimeoutSeconds;
    }

    public void setSocketTimeoutSeconds(int socketTimeoutSeconds) {
        this.socketTimeoutSeconds = socketTimeoutSeconds;
    }

    public int getConnectionTimeoutSeconds() {
        return connectionTimeoutSeconds;
    }

    public void setConnectionTimeoutSeconds(int connectionTimeoutSeconds) {
        this.connectionTimeoutSeconds = connectionTimeoutSeconds;
    }

    public int getTimeToLiveSeconds() {
        return timeToLiveSeconds;
    }

    public void setTimeToLiveSeconds(int timeToLiveSeconds) {
        this.timeToLiveSeconds = timeToLiveSeconds;
    }

    public List<String> getTlsVersions() {
        return tlsVersions;
    }

    public void setTlsVersions(List<String> tlsVersions) {
        this.tlsVersions = tlsVersions;
    }
}
