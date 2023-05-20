package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.api.ApiResponse;
import com.dewarim.cinnamon.model.ChangeTrigger;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "cinnamon")
public class ChangeTriggerWrapper implements Wrapper<ChangeTrigger>, ApiResponse {

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
}
