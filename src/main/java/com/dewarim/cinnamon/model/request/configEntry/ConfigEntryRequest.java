package com.dewarim.cinnamon.model.request.configEntry;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "configEntryRequest")
public class ConfigEntryRequest implements ApiRequest {
    
    private String name;

    public ConfigEntryRequest() {
    }

    public ConfigEntryRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public List<Object> examples() {
        return List.of(new ConfigEntryRequest("company-name"), new ConfigEntryRequest("default-ui-settings"));
    }
}
