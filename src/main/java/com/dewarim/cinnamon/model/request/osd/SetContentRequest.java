package com.dewarim.cinnamon.model.request.osd;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.Optional;

@JsonRootName("setContentRequest")
public record SetContentRequest(Long id, Long formatId) implements ApiRequest<SetContentRequest> {

    public Optional<SetContentRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    private boolean validated() {
        return id != null && id > 0 &&
                formatId != null && formatId > 0;
    }
}
