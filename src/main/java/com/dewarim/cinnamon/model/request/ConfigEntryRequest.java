package com.dewarim.cinnamon.model.request;

import com.dewarim.cinnamon.api.ApiRequest;

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
}
