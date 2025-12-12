package com.dewarim.cinnamon.model.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.core5.http.ContentType;

import static com.dewarim.cinnamon.api.Constants.JSON_MAPPER;
import static com.dewarim.cinnamon.api.Constants.XML_MAPPER;
import static org.apache.hc.core5.http.ContentType.APPLICATION_JSON;
import static org.apache.hc.core5.http.ContentType.APPLICATION_XML;

public enum CinnamonContentType {

    XML(APPLICATION_XML,XML_MAPPER ),
    JSON(APPLICATION_JSON, JSON_MAPPER),
    ;

    final ContentType  contentType;
    final ObjectMapper objectMapper;

    CinnamonContentType(ContentType contentType, ObjectMapper objectMapper) {
        this.contentType = contentType;
        this.objectMapper = objectMapper;
    }

    public static CinnamonContentType getByHttpContentType(String httpContentType) {
        if (httpContentType == null) {
            return XML;
        }
        String mimeType = ContentType.parseLenient(httpContentType).getMimeType();
        // not wort it to use functional map...filter...first for two items:
        if (mimeType.equals("application/json")) {
            return JSON;
        }
        return XML;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
