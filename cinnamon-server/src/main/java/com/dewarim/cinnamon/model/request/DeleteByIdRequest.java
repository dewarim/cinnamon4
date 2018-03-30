package com.dewarim.cinnamon.model.request;

public class DeleteByIdRequest {
    
    private Long id;

    public DeleteByIdRequest(Long id) {
        this.id = id;
    }

    public DeleteByIdRequest() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
