package com.dewarim.cinnamon.application;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;

/*
 adapted from: https://github.com/eugenp/tutorials/blob/master/core-java-modules/core-java-security-4/src/main/java/com/baeldung/multiple_truststores/SslContextConfigurer.java
 License: https://github.com/eugenp/tutorials/blob/master/LICENSE (MIT)
 */

public class SslContextConfigurer {

    public static X509TrustManager addAdditionalTrustStore(File trustStoreLocation, String trustStorePassword)
      throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, KeyManagementException {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init((KeyStore) null);

        X509TrustManager defaultX509CertificateTrustManager = null;
        for (TrustManager trustManager : trustManagerFactory.getTrustManagers()) {
            if (trustManager instanceof X509TrustManager) {
                defaultX509CertificateTrustManager = (X509TrustManager) trustManager;
                break;
            }
        }

        try (InputStream myKeys = new FileInputStream(trustStoreLocation)) {
            KeyStore myTrustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            myTrustStore.load(myKeys, trustStorePassword.toCharArray());
            trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(myTrustStore);

            X509TrustManager myTrustManager = null;
            for (TrustManager tm : trustManagerFactory.getTrustManagers()) {
                if (tm instanceof X509TrustManager) {
                    myTrustManager = (X509TrustManager) tm;
                    break;
                }
            }

            X509TrustManager wrapper = getX509TrustManager(defaultX509CertificateTrustManager, myTrustManager);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[] { wrapper }, null);
            SSLContext.setDefault(sslContext);

            return wrapper;
        }
    }

    private static X509TrustManager getX509TrustManager(X509TrustManager defaultX509CertificateTrustManager, X509TrustManager myTrustManager) {
        final X509TrustManager finalDefaultTm = defaultX509CertificateTrustManager;
        final X509TrustManager finalMyTm = myTrustManager;

        return new X509TrustManager() {

            private X509Certificate[] mergeCertificates() {
                ArrayList<X509Certificate> resultingCerts = new ArrayList<>();
                resultingCerts.addAll(Arrays.asList(finalDefaultTm.getAcceptedIssuers()));
                resultingCerts.addAll(Arrays.asList(finalMyTm.getAcceptedIssuers()));
                return resultingCerts.toArray(new X509Certificate[0]);
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return mergeCertificates();
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                try {
                    finalMyTm.checkServerTrusted(chain, authType);
                } catch (CertificateException e) {
                    finalDefaultTm.checkServerTrusted(chain, authType);
                }
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                finalDefaultTm.checkClientTrusted(mergeCertificates(), authType);
            }
        };
    }
}
