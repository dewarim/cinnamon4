package com.dewarim.cinnamon.model.request.osd;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.application.service.RequestValidator;
import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Optional;

@JsonRootName("setContentRequest")
public record SetContentRequest(@NotNull @Positive Long id, @NotNull @Positive Long formatId) implements ApiRequest<SetContentRequest> {

    public Optional<SetContentRequest> validateRequest() {
        RequestValidator.validate(this);
        return Optional.of(this);
    }
}
