package com.dewarim.cinnamon.model.request;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;
import java.util.Optional;

@JsonRootName("idRequest")
public record IdRequest(Long id) implements ApiRequest<IdRequest> {

    public IdRequest() {
        this(null);
    }

    public boolean validated() {
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
    public List<ApiRequest<IdRequest>> examples() {
        return List.of(new IdRequest(7L));
    }
}
