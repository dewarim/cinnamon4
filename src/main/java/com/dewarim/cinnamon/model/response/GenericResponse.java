package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.api.ApiResponse;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

/**
 * A simple class to report the success or failure of an operation.
 * (Just using http status code 204 is a little ambiguous)
 */
@JacksonXmlRootElement(localName = "genericResponse")
public class GenericResponse implements ApiResponse {

    private String  message;
    private boolean successful;

    public GenericResponse() {
    }

    public GenericResponse(boolean successful) {
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

    @Override
    public List<Object> examples() {
        return List.of(new GenericResponse( true));
    }
}
