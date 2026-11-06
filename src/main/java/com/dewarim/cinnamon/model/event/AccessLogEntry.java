package com.dewarim.cinnamon.model.event;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonRequest;
import com.dewarim.cinnamon.application.CinnamonResponse;
import jakarta.servlet.http.Part;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;

public class AccessLogEntry {

    private static final Logger log = LogManager.getLogger(AccessLogEntry.class);

    /**
     * The requests whose body is not written to the access log, because it carries a password.
     * <p>
     * Assigned where it is declared rather than from an instance initializer: the initializer ran on
     * every constructor call, which since the list statement gained a no-argument constructor means
     * once per row read - an unsynchronized write to a static field, from whichever thread happens
     * to be reading the log.
     */
    private static final Set<String> unloggedPaths = Set.of(
            UrlMapping.CINNAMON__CONNECT.getPath(),
            UrlMapping.USER__SET_PASSWORD.getPath(),
            UrlMapping.USER__UPDATE.getPath()
    );

    private Long      id;
    private String    request;
    private String    response;
    private ErrorCode errorCode;
    private String    errorMessage;
    private LocalDateTime created;
    private String    url;
    private Long      userId;

    /**
     * For MyBatis, which has no other way to create the entries the list statement reads: there is
     * no resultMap declaring constructor arguments, so it needs a no-argument constructor and the
     * setters below.
     */
    public AccessLogEntry() {
    }

    public AccessLogEntry(CinnamonRequest cinnamonRequest, CinnamonResponse cinnamonResponse, String errorWrapper, ErrorCode errorCode, String message, Long userId) throws IOException {
        url = cinnamonRequest.getRequest().getRequestURI();
        if(unloggedPaths.contains(url)){
            request = "unlogged";
        }
        else {
            if(cinnamonRequest.isMultiPart()) {
                Part cinnamonRequestPart = cinnamonRequest.getCinnamonRequestPart();
                if (cinnamonRequestPart != null && cinnamonRequestPart.getSize() > 0) {
                    request = new String(cinnamonRequest.getInputStream().readAllBytes());
                } else {
                    log.info("CinnamonRequestPart is null");
                }
            }
            else{
                request = new String(cinnamonRequest.getInputStream().readAllBytes());
            }
        }
        response = cinnamonResponse.getResponseAsString();
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

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
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
