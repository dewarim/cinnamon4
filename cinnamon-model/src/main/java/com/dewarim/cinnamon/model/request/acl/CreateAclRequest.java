package com.dewarim.cinnamon.model.request.acl;

public class CreateAclRequest {
    
    private String name;

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
