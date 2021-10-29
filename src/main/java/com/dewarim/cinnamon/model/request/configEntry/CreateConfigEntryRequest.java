package com.dewarim.cinnamon.model.request.configEntry;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "createConfigEntryRequest")
public class CreateConfigEntryRequest implements ApiRequest {
    
    private String  name;
    private String  config;
    private boolean publicVisibility;
    

    public CreateConfigEntryRequest() {
    }

    public CreateConfigEntryRequest(String name, String config, boolean publicVisibility) {
        this.name = name;
        this.config = config;
        this.publicVisibility = publicVisibility;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public boolean isPublicVisibility() {
        return publicVisibility;
    }

    public void setPublicVisibility(boolean publicVisibility) {
        this.publicVisibility = publicVisibility;
    }

    @Override
    public List<Object> examples() {
        return List.of(
                new CreateConfigEntryRequest("default-ui-settings", "<xml><show-logo>true</show-logo></xml>",true),
                new CreateConfigEntryRequest("render-server-password", "xxx", false)
        );
    }
}
