package com.dewarim.cinnamon.configuration;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.hc.core5.http.ssl.TLS;

import java.util.List;

public class ChangeTriggerConfig {

    private int     socketTimeoutSeconds        = 60;
    private int     connectionTimeoutSeconds    = 60;
    private int     timeToLiveSeconds           = 600;
    private boolean useCustomTrustStore         = false;
    /**
     * keystore was created with:
     * keytool -import -v -keystore keystore-for-testing.jks -alias mockserver-ca -file MockServerCertificateAuthorityCertificate.pem -storepass changeit -trustcacerts -noprompt
     */
    private String  pathToCustomTrustStore      = "src/test/resources/keystore-for-testing.jks";
    private String  passwordForCustomTrustStore = "changeit";

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

    public boolean isUseCustomTrustStore() {
        return useCustomTrustStore;
    }

    public void setUseCustomTrustStore(boolean useCustomTrustStore) {
        this.useCustomTrustStore = useCustomTrustStore;
    }

    public String getPathToCustomTrustStore() {
        return pathToCustomTrustStore;
    }

    public void setPathToCustomTrustStore(String pathToCustomTrustStore) {
        this.pathToCustomTrustStore = pathToCustomTrustStore;
    }

    public String getPasswordForCustomTrustStore() {
        return passwordForCustomTrustStore;
    }

    public void setPasswordForCustomTrustStore(String passwordForCustomTrustStore) {
        this.passwordForCustomTrustStore = passwordForCustomTrustStore;
    }

    @Override
    public String toString() {
        return "ChangeTriggerConfig{" +
                "socketTimeoutSeconds=" + socketTimeoutSeconds +
                ", connectionTimeoutSeconds=" + connectionTimeoutSeconds +
                ", timeToLiveSeconds=" + timeToLiveSeconds +
                ", useCustomTrustStore=" + useCustomTrustStore +
                ", pathToCustomTrustStore='" + pathToCustomTrustStore + '\'' +
                ", passwordForCustomTrustStore='" + passwordForCustomTrustStore + '\'' +
                ", tlsVersions=" + tlsVersions +
                '}';
    }
}
