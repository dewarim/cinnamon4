package com.dewarim.cinnamon.model.response;

import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * CinnamonErrors should always be sent as part of and CinnamonErrorWrapper (easier for the client to have only
 * one type of error message).
 */
@JsonRootName("error")
public class CinnamonError {

    private String code;
    private String message;
    private Long   id;

    public CinnamonError() {
    }

    public CinnamonError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public CinnamonError(String code, Long id) {
        this.code = code;
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "CinnamonError{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", id=" + id +
                '}';
    }
}
