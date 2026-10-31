package com.dewarim.cinnamon.model.request.osd;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.application.service.RequestValidator;
import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.Optional;

@JsonRootName("setSummaryRequest")
public record SetSummaryRequest(@NotNull @Positive Long id,
                                // NOT NULL in the database for both objects and folders
                                @NotNull String summary) implements ApiRequest<SetSummaryRequest> {

    public Optional<SetSummaryRequest> validateRequest() {
        RequestValidator.validate(this);
        return Optional.of(this);
    }

    @Override
    public List<ApiRequest<SetSummaryRequest>> examples() {
        return List.of(
                new SetSummaryRequest(45L, "<xml>summary</xml>"),
                new SetSummaryRequest(65L, "be careful when indexing non-xml summaries")
        );
    }
}
