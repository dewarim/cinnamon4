package com.dewarim.cinnamon.model.request.changeTrigger;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.ChangeTrigger;
import com.dewarim.cinnamon.model.ChangeTriggerType;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.ChangeTriggerWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "createChangeTriggerRequest")
public class CreateChangeTriggerRequest implements CreateRequest<ChangeTrigger>, ApiRequest {

    @JacksonXmlElementWrapper(localName = "changeTriggers")
    @JacksonXmlProperty(localName = "changeTrigger")
    private List<ChangeTrigger> changeTriggers = new ArrayList<>();

    @Override
    public List<ChangeTrigger> list() {
        return changeTriggers;
    }

    public CreateChangeTriggerRequest() {
    }


    public CreateChangeTriggerRequest(List<ChangeTrigger> changeTriggers) {
        this.changeTriggers = changeTriggers;
    }


    @Override
    public boolean validated() {
        return changeTriggers.stream().noneMatch(changeTrigger ->
                changeTrigger == null || changeTrigger.getName() == null || changeTrigger.getName().trim().isEmpty());
    }

    @Override
    public Wrapper<ChangeTrigger> fetchResponseWrapper() {
        return new ChangeTriggerWrapper();
    }

    @Override
    public List<Object> examples() {
        return List.of(new CreateChangeTriggerRequest(
                List.of(new ChangeTrigger(1L, "triggerThumbnailGenerator", "osd", "setContent", true,
                        false,true,false,"<config><url>http://localhost:64888/createThumbnail</url></config>",
                        ChangeTriggerType.MICROSERVICE,100 ))));
    }
}
