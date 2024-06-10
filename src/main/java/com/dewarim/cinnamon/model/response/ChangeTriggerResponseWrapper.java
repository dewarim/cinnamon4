package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.api.ApiResponse;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;


@JacksonXmlRootElement(localName = "cinnamon")
public class ChangeTriggerResponseWrapper extends BaseResponse implements Wrapper<ChangeTriggerResponse>, ApiResponse {

    @Override
    public List<ChangeTriggerResponse> list() {
        return List.of();
    }

    @Override
    public Wrapper<ChangeTriggerResponse> setList(List<ChangeTriggerResponse> changeTriggerResponses) {
        setChangeTriggerResponses(changeTriggerResponses);
        return this;
    }

    @Override
    public List<Object> examples() {
        return List.of(new ChangeTriggerResponseWrapper());
    }
}
