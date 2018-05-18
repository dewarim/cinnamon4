package com.dewarim.cinnamon.model.request;

public class ConfigEntryRequest {
    
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
