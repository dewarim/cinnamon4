package com.dewarim.cinnamon.model.request;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.application.service.RequestValidator;
import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.Optional;

@JsonRootName("idRequest")
public record IdRequest(@NotNull @Positive Long id) implements ApiRequest<IdRequest> {

    public IdRequest() {
        this(null);
    }

    public Optional<IdRequest> validateRequest() {
        RequestValidator.validate(this);
        return Optional.of(this);
    }

    @Override
    public List<ApiRequest<IdRequest>> examples() {
        return List.of(new IdRequest(7L));
    }
}
