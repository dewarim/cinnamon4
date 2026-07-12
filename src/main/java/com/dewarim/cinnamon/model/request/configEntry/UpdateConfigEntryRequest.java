package com.dewarim.cinnamon.model.request.configEntry;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.ConfigEntry;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.ConfigEntryWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

@JsonRootName("updateConfigEntryRequest")
public record UpdateConfigEntryRequest(
        @JacksonXmlElementWrapper(localName = "configEntries")
        @JacksonXmlProperty(localName = "configEntry")
        List<ConfigEntry> configEntries) implements UpdateRequest<ConfigEntry>, ApiRequest<UpdateRequest<ConfigEntry>> {

    public UpdateConfigEntryRequest {
        if (configEntries == null) {
            configEntries = new ArrayList<>();
        }
    }

    public UpdateConfigEntryRequest(Long id, String name, String config, boolean publicVisibility) {
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
                        && configEntry.getId() != null && configEntry.getId() > 0);
    }

    @Override
    public Wrapper<ConfigEntry> fetchResponseWrapper() {
        return new ConfigEntryWrapper();
    }

    @Override
    public List<ApiRequest<UpdateRequest<ConfigEntry>>> examples() {
        return List.of(
                new UpdateConfigEntryRequest(321L, "default-ui-settings", "<xml><show-logo>true</show-logo></xml>", true),
                new UpdateConfigEntryRequest(444L, "render-server-password", "xxx", false)
        );
    }
}
