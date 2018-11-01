package com.dewarim.cinnamon.model.request;

public class CreateConfigEntryRequest {
    
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
    
}
