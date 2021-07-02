package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.api.ApiResponse;
import com.dewarim.cinnamon.model.ConfigEntry;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "cinnamon")
public class ConfigEntryWrapper implements ApiResponse, Wrapper<ConfigEntry> {

    @JacksonXmlElementWrapper(localName = "configEntries")
    @JacksonXmlProperty(localName = "configEntry")
    List<ConfigEntry> configEntries = new ArrayList<>();

    public ConfigEntryWrapper() {
    }

    public ConfigEntryWrapper(List<ConfigEntry> configEntries) {
        this.configEntries = configEntries;
    }

    public ConfigEntryWrapper(ConfigEntry configEntry) {
        this.configEntries.add(configEntry);
    }

    public List<ConfigEntry> getConfigEntries() {
        return configEntries;
    }

    public void setConfigEntries(List<ConfigEntry> configEntries) {
        this.configEntries = configEntries;
    }

    @Override
    public List<ConfigEntry> list() {
        return getConfigEntries();
    }

    @Override
    public Wrapper<ConfigEntry> setList(List<ConfigEntry> configEntries) {
        setConfigEntries(configEntries);
        return this;
    }
}
