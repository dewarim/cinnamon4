package com.dewarim.cinnamon.model.request.configEntry;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.ConfigEntry;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.ConfigEntryWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "updateConfigEntryRequest")
public class UpdateConfigEntryRequest implements UpdateRequest<ConfigEntry>, ApiRequest {

    @JacksonXmlElementWrapper(localName = "configEntries")
    @JacksonXmlProperty(localName = "configEntry")
    private List<ConfigEntry> configEntries = new ArrayList<>();

    @Override
    public List<ConfigEntry> list() {
        return configEntries;
    }

    public UpdateConfigEntryRequest() {
    }

    public UpdateConfigEntryRequest(Long id, String name, String config, boolean publicVisibility) {
        configEntries.add(new ConfigEntry(id,name, config, publicVisibility));
    }

    public UpdateConfigEntryRequest(List<ConfigEntry> configEntries) {
        this.configEntries = configEntries;
    }

    public List<ConfigEntry> getConfigEntries() {
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
    public List<Object> examples() {
        return List.of(
                new CreateConfigEntryRequest(321L, "default-ui-settings", "<xml><show-logo>true</show-logo></xml>", true),
                new CreateConfigEntryRequest(444L, "render-server-password", "xxx", false)
        );
    }
}
