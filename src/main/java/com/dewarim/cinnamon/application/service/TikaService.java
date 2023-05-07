package com.dewarim.cinnamon.application.service;

import com.dewarim.cinnamon.configuration.CinnamonTikaConfig;
import com.dewarim.cinnamon.model.Format;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

import static java.net.HttpURLConnection.HTTP_OK;

public class TikaService {
    private static final Logger log = LogManager.getLogger(TikaService.class);

    private CinnamonTikaConfig config;

    public TikaService(CinnamonTikaConfig config) {
        this.config = config;
    }

    public String parseData(File input, Format format) throws IOException {
        if(input.length() == 0){
            log.debug("Tika was given a file without content.");
            return "<empty/>";
        }
        if(!config.isUseTika()){
            return "<tikaIsDisabled/>";
        }
        HttpResponse httpResponse = Request.Put(config.getBaseUrl() +"/tika")
                .bodyFile(input, ContentType.create(format.getContentType()))
                .execute().returnResponse();
        if(httpResponse.getStatusLine().getStatusCode() != HTTP_OK){
            log.info("Failed to parse tika file: "+httpResponse.getStatusLine());
            // TODO: improve error handling & reporting of tikaService.
            return "<tikaFailedToParse/>";
        }
        return new String(httpResponse.getEntity().getContent().readAllBytes());
    }

    public boolean isEnabled(){
        return config.isUseTika();
    }
}
