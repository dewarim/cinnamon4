package com.dewarim.cinnamon.model.request.changeTrigger;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.ChangeTrigger;
import com.dewarim.cinnamon.model.ChangeTriggerType;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.ChangeTriggerWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

@JsonRootName("createChangeTriggerRequest")
public record CreateChangeTriggerRequest(
        @JacksonXmlElementWrapper(localName = "changeTriggers")
        @JacksonXmlProperty(localName = "changeTrigger")
        @NotEmpty List<@Valid ChangeTrigger> changeTriggers) implements CreateRequest<ChangeTrigger>, ApiRequest<CreateChangeTriggerRequest> {

    public CreateChangeTriggerRequest {
        if (changeTriggers == null) {
            changeTriggers = new ArrayList<>();
        }
    }

    @Override
    public List<ChangeTrigger> list() {
        return changeTriggers;
    }

    @Override
    public Wrapper<ChangeTrigger> fetchResponseWrapper() {
        return new ChangeTriggerWrapper();
    }

    @Override
    public List<ApiRequest<CreateChangeTriggerRequest>> examples() {
        return List.of(new CreateChangeTriggerRequest(
                List.of(new ChangeTrigger(1L, "triggerThumbnailGenerator", "osd", "setContent", true,
                        false,true,false,"<config><remoteServer>http://localhost:64888/createThumbnail</remoteServer></config>",
                        ChangeTriggerType.MICROSERVICE,100, true ))));
    }
}
