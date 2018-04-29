package com.dewarim.cinnamon.model.response;

/**
 * A simple class to report the success or failure of an operation.
 * (Just using http status code 204 is a little ambiguous)
 */
public class GenericResponse {
    
    private String message;
    private boolean successful;

    public GenericResponse() {
    }

    public GenericResponse(boolean successful) {
        this.successful = successful;
    }

    public GenericResponse(String message, boolean successful) {
        this.message = message;
        this.successful = successful;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
