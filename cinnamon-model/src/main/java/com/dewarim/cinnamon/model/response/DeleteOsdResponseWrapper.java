package com.dewarim.cinnamon.model.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "cinnamon")
public class DeleteOsdResponseWrapper implements Wrapper {

    private boolean success = false;

    @JacksonXmlElementWrapper(localName = "errors")
    @JacksonXmlProperty(localName = "error")
    private List<CinnamonError> errors = new ArrayList<>();

    public DeleteOsdResponseWrapper() {
    }

    public DeleteOsdResponseWrapper(List<CinnamonError> errors) {
        this.errors = errors;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<CinnamonError> getErrors() {
        return errors;
    }

    public void setErrors(List<CinnamonError> errors) {
        this.errors = errors;
    }

    @Override
    public String toString() {
        return "DeleteOsdResponse{" +
                "success=" + success +
                ", errors=" + errors +
                '}';
    }
}
