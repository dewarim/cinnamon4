package com.dewarim.cinnamon.model.request.changeTrigger;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.ChangeTrigger;
import com.dewarim.cinnamon.model.ChangeTriggerType;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.ChangeTriggerWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

@JsonRootName("updateChangeTriggerRequest")
public record UpdateChangeTriggerRequest(
        @JacksonXmlElementWrapper(localName = "changeTriggers")
        @JacksonXmlProperty(localName = "changeTrigger")
        List<ChangeTrigger> changeTriggers) implements UpdateRequest<ChangeTrigger>, ApiRequest<UpdateRequest<ChangeTrigger>> {

    public UpdateChangeTriggerRequest {
        if (changeTriggers == null) {
            changeTriggers = new ArrayList<>();
        }
    }

    @Override
    public List<ChangeTrigger> list() {
        return changeTriggers;
    }

    @Override
    public boolean validated() {
        return changeTriggers.stream().allMatch(changeTrigger ->
                changeTrigger != null && changeTrigger.getName() != null && !changeTrigger.getName().trim().isEmpty()
                        && changeTrigger.getId() != null && changeTrigger.getId() > 0);
    }

    @Override
    public Wrapper<ChangeTrigger> fetchResponseWrapper() {
        return new ChangeTriggerWrapper();
    }

    @Override
    public List<ApiRequest<UpdateRequest<ChangeTrigger>>> examples() {
        return List.of(new UpdateChangeTriggerRequest(List.of(new ChangeTrigger(1L, "triggerThumbnailGenerator", "osd", "setContent", true,
                false, true, false, "<config><url>http://localhost:64888/createThumbnail</url></config>",
                ChangeTriggerType.MICROSERVICE,100, false ))));
    }
}
