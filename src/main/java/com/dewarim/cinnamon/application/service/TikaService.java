package com.dewarim.cinnamon.application.service;

import com.dewarim.cinnamon.configuration.CinnamonTikaConfig;
import com.dewarim.cinnamon.model.Format;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.FileEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

import static java.net.HttpURLConnection.HTTP_OK;

public class TikaService {
    private static final Logger log = LogManager.getLogger(TikaService.class);

    private final CinnamonTikaConfig config;

    public TikaService(CinnamonTikaConfig config) {
        this.config = config;
    }

    public String parseData(File input, Format format) throws IOException {
        if (input.length() == 0) {
            log.debug("Tika was given a file without content.");
            return "<empty/>";
        }
        if (!config.isUseTika()) {
            return "<tikaIsDisabled/>";
        }
        try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {
            final HttpPut  httpPut = new HttpPut(config.getBaseUrl() + "/tika");
            httpPut.setEntity(new FileEntity(input, ContentType.parseLenient(format.getContentType())));
            return httpclient.execute(httpPut, response -> {

                if (response.getCode() != HTTP_OK) {
                    log.info("Failed to parse tika file: " + response.getCode());
                    // TODO: improve error handling & reporting of tikaService.
                    return "<tikaFailedToParse/>";
                }
                return new String(response.getEntity().getContent().readAllBytes());
            });

        }
    }

    public boolean isEnabled() {
        return config.isUseTika();
    }

}
