package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.api.ApiResponse;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;


@JacksonXmlRootElement(localName = "cinnamon")
public class ChangeTriggerResponseWrapper extends BaseResponse implements Wrapper<ChangeTriggerResponse>, ApiResponse {

    // we explicitly include the errors element here for testing
    @JacksonXmlElementWrapper(localName = "errors")
    @JacksonXmlProperty(localName = "error")
    List<CinnamonError> errors = new ArrayList<>();

    @Override
    public List<ChangeTriggerResponse> list() {
        return getChangeTriggerResponses();
    }

    @Override
    public Wrapper<ChangeTriggerResponse> setList(List<ChangeTriggerResponse> changeTriggerResponses) {
        setChangeTriggerResponses(changeTriggerResponses);
        return this;
    }

    public List<CinnamonError> getErrors() {
        return errors;
    }

    @Override
    public List<Object> examples() {
        return List.of(new ChangeTriggerResponseWrapper());
    }
}
