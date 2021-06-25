package com.dewarim.cinnamon.application.exception;

import java.util.Arrays;

public class CinnamonException extends RuntimeException {

    private String[] params = {};

    public CinnamonException() {
    }

    public CinnamonException(String message, String... params) {
        super(message);
        if (params != null && params.length > 0) {
            this.params = params;
        }
    }

    public CinnamonException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public CinnamonException(String message, Throwable exception, String... params) {
        super(message, exception);
        if (params != null && params.length > 0) {
            this.params = params;
        }
    }

    @Override
    public String toString() {
        return "CinnamonException{" +
                "params=" + Arrays.toString(params) +
                '}';
    }
}
