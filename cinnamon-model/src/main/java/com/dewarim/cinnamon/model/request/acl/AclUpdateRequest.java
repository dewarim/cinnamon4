package com.dewarim.cinnamon.model.request.acl;

/**
 */
public class AclUpdateRequest {

    private Long id;
    private String name;

    public AclUpdateRequest() {
    }

    public AclUpdateRequest(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
