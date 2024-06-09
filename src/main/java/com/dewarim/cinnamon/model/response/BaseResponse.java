package com.dewarim.cinnamon.model.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.ArrayList;
import java.util.List;

public class BaseResponse {

    @JacksonXmlElementWrapper(localName = "changeTriggerResponses")
    @JacksonXmlProperty(localName = "response")
    List<ChangeTriggerResponse> changeTriggerResponses = new ArrayList<>();

    public List<ChangeTriggerResponse> getChangeTriggerResponses() {
        if(changeTriggerResponses==null){
            changeTriggerResponses = new ArrayList<>();
        }
        return changeTriggerResponses;
    }

    public void setChangeTriggerResponses(List<ChangeTriggerResponse> changeTriggerResponses) {
        this.changeTriggerResponses = changeTriggerResponses;
    }
}
