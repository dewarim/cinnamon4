package com.dewarim.cinnamon.application.trigger;

import com.dewarim.cinnamon.application.CinnamonRequest;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.application.SslContextConfigurer;
import com.dewarim.cinnamon.application.exception.CinnamonException;
import com.dewarim.cinnamon.application.service.index.ParamParser;
import com.dewarim.cinnamon.configuration.ChangeTriggerConfig;
import com.dewarim.cinnamon.model.ChangeTrigger;
import com.dewarim.cinnamon.model.response.ChangeTriggerResponse;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.entity.mime.StringBody;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Node;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;

import static com.dewarim.cinnamon.api.Constants.CINNAMON_REQUEST_HEADER;
import static com.dewarim.cinnamon.api.Constants.CINNAMON_REQUEST_PART;

public class MicroserviceChangeTrigger implements Trigger {
    private static final Logger log = LogManager.getLogger(MicroserviceChangeTrigger.class);

    private HttpClient httpClient;

    public MicroserviceChangeTrigger() {

    }

    @Override
    public void configure(ChangeTriggerConfig config) {
        if (config.isUseCustomTrustStore()) {
            try {
                SslContextConfigurer.addAdditionalTrustStore(new File(config.getPathToCustomTrustStore()), config.getPasswordForCustomTrustStore());
            } catch (Exception e) {
                String message = "Failed to add custom trust store for MicroserviceChangeTrigger.";
                log.error(message, e);
                throw new CinnamonException(message, e);
            }
        }

        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(SSLConnectionSocketFactoryBuilder.create()
                        .setSslContext(SSLContexts.createSystemDefault())
                        .setTlsVersions(config.getTlsVersions().stream().map(TLS::valueOf).toList().toArray(new TLS[0]))
                        .build())
                .setDefaultSocketConfig(SocketConfig.custom()
                        .setSoTimeout(Timeout.ofSeconds(config.getSocketTimeoutSeconds()))
                        .build())
                .setPoolConcurrencyPolicy(PoolConcurrencyPolicy.STRICT)
                .setConnPoolPolicy(PoolReusePolicy.LIFO)
                .setDefaultConnectionConfig(ConnectionConfig.custom()
                        .setSocketTimeout(Timeout.ofSeconds(config.getSocketTimeoutSeconds()))
                        .setConnectTimeout(Timeout.ofSeconds(config.getSocketTimeoutSeconds()))
                        .setTimeToLive(TimeValue.ofSeconds(config.getTimeToLiveSeconds()))
                        .build())
                .build();
        // TODO: add cookieManager (if needed)
        // TODO: add handling for remote credentials (if needed)
        httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(StandardCookieSpec.STRICT)
                        .build())
                .build();
    }

    @Override
    public TriggerResult executePreCommand(ChangeTrigger changeTrigger, CinnamonRequest cinnamonRequest, CinnamonResponse cinnamonResponse) {
        if (httpClient == null) {
            throw new CinnamonException("MicroserviceChangeTrigger has not been initialized yet. You must call configure method before using it");
        }
        log.debug("preCommand of MicroserviceChangeTrigger");

        try {
            String url = findRemoteUrl(changeTrigger.getConfig());
            if (url == null) {
                log.warn("Found microserviceChangeTrigger without valid remoteServer url. Config is: " +
                        changeTrigger.getConfig());
                return TriggerResult.CONTINUE;
            }

            ClassicRequestBuilder requestBuilder = ClassicRequestBuilder.create("POST")
                    .setUri(url);
            Enumeration<String> headerNames = cinnamonRequest.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                if (headerName.equals("microservice")) {
                    continue;
                }
                requestBuilder.setHeader(headerName, cinnamonRequest.getHeader(headerName));
            }
            cleanupHeaders(requestBuilder);
            if (cinnamonRequest.isMultiPart()) {
                String                 cinnamonPayload = new String(cinnamonRequest.getCinnamonRequestPart().getInputStream().readAllBytes());
                StringBody             stringBody      = new StringBody(cinnamonPayload, ContentType.parseLenient(cinnamonRequest.getContentType()));
                MultipartEntityBuilder entityBuilder   = MultipartEntityBuilder.create().addPart(CINNAMON_REQUEST_PART, stringBody);
                if (cinnamonRequest.getFilename() != null) {
                    entityBuilder.addPart("file", new FileBody(new File(cinnamonRequest.getFilename())));
                }
                HttpEntity entity = entityBuilder.build();
                requestBuilder.setEntity(entity);
            }
            else {
                requestBuilder.setEntity(cinnamonRequest.getByteInput().getContent());
            }

            return executeRequest(cinnamonResponse, url, requestBuilder);
        } catch (Exception e) {
            log.debug("Failed to execute microserviceChangeTrigger.", e);
            throw new CinnamonException("Failed to execute microserviceChangeTrigger.", e);
        }
    }

    @Override
    public TriggerResult executePostCommand(ChangeTrigger changeTrigger, CinnamonRequest cinnamonRequest, CinnamonResponse cinnamonResponse) {
        log.debug("MicroserviceChangeTrigger executePostCommand");

        try {
            String url = findRemoteUrl(changeTrigger.getConfig());
            if (url == null) {
                log.warn("Found microserviceChangeTrigger without valid remoteServer url. Config is: " +
                        changeTrigger.getConfig());
                return TriggerResult.CONTINUE;
            }

            ClassicRequestBuilder requestBuilder = ClassicRequestBuilder.create("POST").setUri(url);
            Collection<String>    headerNames    = cinnamonResponse.getHeaderNames();
            for (String headerName : headerNames) {
                if (headerName.equals("microservice")) {
                    continue;
                }
                requestBuilder.setHeader(headerName, cinnamonResponse.getHeader(headerName));
            }

            cleanupHeaders(requestBuilder);
            if (cinnamonRequest.isMultiPart()) {
                byte[] cinnamonRequestPart = cinnamonRequest.getCinnamonRequestPart().getInputStream().readAllBytes();
                requestBuilder.setHeader(CINNAMON_REQUEST_HEADER, new String(cinnamonRequestPart));
            }
            else {
                requestBuilder.setHeader(CINNAMON_REQUEST_HEADER, cinnamonRequest.getByteInput().getContent());
            }
            requestBuilder.setEntity(cinnamonResponse.getPendingContentAsString());

            return executeRequest(cinnamonResponse, url, requestBuilder);
        } catch (Exception e) {
            log.debug("Failed to execute post-microserviceChangeTrigger.", e);
            throw new CinnamonException("Failed to execute post-microserviceChangeTrigger.", e);
        }

    }

    private TriggerResult executeRequest(CinnamonResponse cinnamonResponse, String url, ClassicRequestBuilder requestBuilder) throws IOException {
        return httpClient.execute(requestBuilder.build(), response -> {
            addChangeTriggerResponseToCinnamonResponse(response, cinnamonResponse, url);
            if (response.getCode() != HttpStatus.SC_OK) {
                log.warn("response from microservice call " + url + " was not OK but " + response.getCode());
                return TriggerResult.STOP;
            }
            else {
                return TriggerResult.CONTINUE;
            }
        });
    }

    private void cleanupHeaders(ClassicRequestBuilder requestBuilder) {
        requestBuilder.removeHeaders(HttpHeaders.CONTENT_LENGTH);
        requestBuilder.removeHeaders(HttpHeaders.HOST);
        // MUST NOT leak the client's session to a remote service! - superseded by: client needs session ticket
        // TODO: make removal of ticket header configurable #minor
//        requestBuilder.removeHeaders("ticket");

    }

    String findRemoteUrl(String config) {
        Document configDoc        = ParamParser.parseXmlToDocument(config, "error.param.config");
        Node     remoteServerNode = configDoc.selectSingleNode("//remoteServer");
        return remoteServerNode.getText();
    }

    void addChangeTriggerResponseToCinnamonResponse(ClassicHttpResponse remoteResponse, CinnamonResponse cinnamonResponse, String url) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        remoteResponse.getEntity().writeTo(os);
        String remoteContent = os.toString();
        log.debug("remoteResponse from  " + url + " is:\n" + remoteContent);

        if (remoteContent.length() > 0) {
            cinnamonResponse.getChangeTriggerResponses().add(
                    new ChangeTriggerResponse(url, remoteContent, remoteResponse.getCode())
            );
        }
        else {
            cinnamonResponse.getChangeTriggerResponses().add(
                    new ChangeTriggerResponse(url, "<no-content/>", remoteResponse.getCode())
            );
        }
    }
}
