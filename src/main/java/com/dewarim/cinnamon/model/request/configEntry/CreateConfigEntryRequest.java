package com.dewarim.cinnamon.model.request.configEntry;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.ConfigEntry;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.ConfigEntryWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

@JsonRootName("createConfigEntryRequest")
public record CreateConfigEntryRequest(
        @JacksonXmlElementWrapper(localName = "configEntries")
        @JacksonXmlProperty(localName = "configEntry")
        List<ConfigEntry> configEntries) implements CreateRequest<ConfigEntry>, ApiRequest<CreateConfigEntryRequest> {

    public CreateConfigEntryRequest {
        if (configEntries == null) {
            configEntries = new ArrayList<>();
        }
    }

    public CreateConfigEntryRequest(String name, String config, boolean publicVisibility) {
        this(new ArrayList<>(List.of(new ConfigEntry(name, config, publicVisibility))));
    }

    public CreateConfigEntryRequest(Long id, String name, String config, boolean publicVisibility) {
        this(new ArrayList<>(List.of(new ConfigEntry(id, name, config, publicVisibility))));
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

    @Override
    public List<ApiRequest<CreateConfigEntryRequest>> examples() {
        return List.of(
                new CreateConfigEntryRequest("default-ui-settings", "<xml><show-logo>true</show-logo></xml>", true),
                new CreateConfigEntryRequest("render-server-password", "xxx", false)
        );
    }
}
