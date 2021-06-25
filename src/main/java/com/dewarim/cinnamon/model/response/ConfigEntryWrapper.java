package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.model.ConfigEntry;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "cinnamon")
public class ConfigEntryWrapper {

    @JacksonXmlElementWrapper(localName = "configEntries")
    @JacksonXmlProperty(localName = "configEntry")
    List<ConfigEntry> configEntries = new ArrayList<>();

    public List<ConfigEntry> getConfigEntries() {
        return configEntries;
    }

    public void setConfigEntries(List<ConfigEntry> configEntries) {
        this.configEntries = configEntries;
    }
}
