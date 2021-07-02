package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.api.ApiResponse;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "cinnamon")
public class CinnamonErrorWrapper implements Wrapper<CinnamonError>, ApiResponse {

    @JacksonXmlElementWrapper(localName = "errors")
    @JacksonXmlProperty(localName = "error")
    List<CinnamonError> errors = new ArrayList<>();

    public CinnamonErrorWrapper() {
    }

    public CinnamonErrorWrapper(CinnamonError error) {
        this.errors.add(error);
    }

    public CinnamonErrorWrapper(List<CinnamonError> errors) {
        this.errors = errors;
    }

    public List<CinnamonError> getErrors() {
        return errors;
    }

    public void setErrors(List<CinnamonError> errors) {
        this.errors = errors;
    }

    @Override
    public List<CinnamonError> list() {
        return getErrors();
    }

    @Override
    public Wrapper<CinnamonError> setList(List<CinnamonError> cinnamonErrors) {
        setErrors(cinnamonErrors);
        return this;
    }
}
