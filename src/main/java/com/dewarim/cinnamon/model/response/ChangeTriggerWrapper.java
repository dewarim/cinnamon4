package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.api.ApiResponse;
import com.dewarim.cinnamon.model.ChangeTrigger;
import com.dewarim.cinnamon.model.ChangeTriggerType;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

@JsonRootName("cinnamon")
public class ChangeTriggerWrapper extends BaseResponse implements Wrapper<ChangeTrigger>, ApiResponse {

    @JacksonXmlElementWrapper(localName = "changeTriggers")
    @JacksonXmlProperty(localName = "changeTrigger")
    List<ChangeTrigger> changeTriggers = new ArrayList<>();

    public ChangeTriggerWrapper(List<ChangeTrigger> changeTriggers) {
        this.changeTriggers = changeTriggers;
    }

    public ChangeTriggerWrapper() {
    }

    public void setChangeTriggers(List<ChangeTrigger> changeTriggers) {
        this.changeTriggers = changeTriggers;
    }

    public List<ChangeTrigger> getChangeTriggers() {
        return changeTriggers;
    }

    @Override
    public List<ChangeTrigger> list() {
        return getChangeTriggers();
    }

    @Override
    public Wrapper<ChangeTrigger> setList(List<ChangeTrigger> changeTriggers) {
        setChangeTriggers(changeTriggers);
        return this;
    }

    @Override
    public List<Object> examples() {
        return List.of(new ChangeTriggerWrapper(List.of(new ChangeTrigger(4L, "logging-trigger", "osd", "delete", true, false, true,
                false, "<config><!-- define URL to send notice of successful delete events to --></config>", ChangeTriggerType.MICROSERVICE, 1000, true))));
    }
}
