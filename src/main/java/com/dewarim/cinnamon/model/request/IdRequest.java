package com.dewarim.cinnamon.model.request;

import com.dewarim.cinnamon.api.ApiRequest;

import java.util.Optional;

public class IdRequest implements ApiRequest {
    
    private Long id;

    public IdRequest() {
    }

    public IdRequest(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // TODO: switch from validated to validateRequest in all places
    public boolean validated(){
        return id != null && id > 0;
    }

    public Optional<IdRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public String toString() {
        return "IdRequest{" +
                "id=" + id +
                '}';
    }
}
