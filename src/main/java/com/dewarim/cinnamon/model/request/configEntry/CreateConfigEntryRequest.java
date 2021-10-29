package com.dewarim.cinnamon.model.request.configEntry;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.ConfigEntry;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.ConfigEntryWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "createConfigEntryRequest")
public class CreateConfigEntryRequest implements CreateRequest<ConfigEntry>, ApiRequest {

    @JacksonXmlElementWrapper(localName = "configEntries")
    @JacksonXmlProperty(localName = "configEntry")
    private List<ConfigEntry> configEntries = new ArrayList<>();

    public CreateConfigEntryRequest() {
    }

    public CreateConfigEntryRequest(String name, String config, boolean publicVisibility) {
        configEntries.add(new ConfigEntry(name, config, publicVisibility));
    }

    public CreateConfigEntryRequest(Long id, String name, String config, boolean publicVisibility) {
        configEntries.add(new ConfigEntry(id, name, config, publicVisibility));
    }

    public CreateConfigEntryRequest(List<ConfigEntry> configEntries) {
        this.configEntries = configEntries;
    }

    @Override
    public List<Object> examples() {
        return List.of(
                new CreateConfigEntryRequest("default-ui-settings", "<xml><show-logo>true</show-logo></xml>", true),
                new CreateConfigEntryRequest("render-server-password", "xxx", false)
        );
    }

    public List<ConfigEntry> getConfigEntries() {
        return configEntries;
    }

    public void setConfigEntries(List<ConfigEntry> configEntries) {
        this.configEntries = configEntries;
    }

    @Override
    public List<ConfigEntry> list() {
        return configEntries;
    }

    @Override
    public boolean validated() {
        return configEntries.stream().allMatch(configEntry ->
                configEntry != null && configEntry.getName() != null && !configEntry.getName().trim().isEmpty()
                        && configEntry.getConfig() != null
        );
    }

    @Override
    public Wrapper<ConfigEntry> fetchResponseWrapper() {
        return new ConfigEntryWrapper();
    }


}
