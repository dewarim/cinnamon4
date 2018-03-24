package com.dewarim.cinnamon.model.request;

public class CreateAclRequest {
    
    String name;

    public CreateAclRequest() {
    }

    public CreateAclRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
