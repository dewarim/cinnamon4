package com.dewarim.cinnamon.model.event;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonRequest;
import com.dewarim.cinnamon.application.CinnamonResponse;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Set;

public class AccessLogEntry {

    private static Set<String> unloggedPaths;
    {
        unloggedPaths = Set.of(
                UrlMapping.CINNAMON__CONNECT.getPath(),
                UrlMapping.USER__SET_PASSWORD.getPath(),
                UrlMapping.USER__UPDATE.getPath()
        );
    }

    private Long      id;
    private String    request;
    private String    response;
    private ErrorCode errorCode;
    private String    errorMessage;
    private Timestamp created;
    private String    url;
    private Long      userId;

    public AccessLogEntry(CinnamonRequest cinnamonRequest, CinnamonResponse cinnamonResponse, String errorWrapper, ErrorCode errorCode, String message, Long userId) throws IOException {
        url = cinnamonRequest.getRequest().getRequestURI();
        if(unloggedPaths.contains(url)){
            request = "unlogged";
        }
        else {
            request = cinnamonRequest.getRequest().getRequestURI();
        }
        response = cinnamonResponse.getPendingContentAsString();
        errorMessage = message;
        if (errorWrapper != null) {
            errorMessage = errorWrapper;
        }
        this.errorCode = errorCode;
        this.userId = userId;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public Timestamp getCreated() {
        return created;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "AccessLogEntry{" +
                "id=" + id +
                ", request='" + request + '\'' +
                ", response='" + response + '\'' +
                ", errorCode=" + errorCode +
                ", errorMessage='" + errorMessage + '\'' +
                ", created=" + created +
                ", url='" + url + '\'' +
                ", userId=" + userId +
                '}';
    }
}
