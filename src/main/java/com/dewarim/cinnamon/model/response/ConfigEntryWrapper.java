package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.api.ApiResponse;
import com.dewarim.cinnamon.model.ConfigEntry;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

@JsonRootName("cinnamon")
public class ConfigEntryWrapper extends BaseResponse implements ApiResponse, Wrapper<ConfigEntry> {

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

    @Override
    public List<Object> examples() {
        return List.of(
                new ConfigEntryWrapper(List.of(
                        new ConfigEntry(1L, "default-ui-settings", "<xml><show-logo>true</show-logo></xml>", true))
                )
        );
    }
}
