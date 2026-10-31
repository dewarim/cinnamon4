package com.dewarim.cinnamon.model.request.relation;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.application.service.RequestValidator;
import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@JsonRootName("searchRelationRequest")
public record SearchRelationRequest(List<@NotNull @Positive Long> leftIds,
                                    List<@NotNull @Positive Long> rightIds,
                                    List<@NotNull @Positive Long> relationTypeIds,
                                    boolean includeMetadata, boolean orMode) implements ApiRequest<SearchRelationRequest> {

    public SearchRelationRequest() {
        this(null, null, null, false, false);
    }

    /**
     * The search has to be narrowed by at least one of the three lists; what the ids in them have
     * to look like is annotated on the components. Which list it is cannot be said per field, so
     * this one rule stays hand-written.
     * <p>
     * TODO: change validation to accept empty lists?
     */
    public boolean validated() {
        return hasEntries(leftIds) || hasEntries(rightIds) || hasEntries(relationTypeIds);
    }

    private boolean hasEntries(Collection<Long> ids) {
        return Objects.nonNull(ids) && !ids.isEmpty();
    }

    @Override
    public String additionalRules() {
        return "At least one of leftIds, rightIds and relationTypeIds has to be non-empty.";
    }

    public Optional<SearchRelationRequest> validateRequest() {
        RequestValidator.validate(this);
        return validated() ? Optional.of(this) : Optional.empty();
    }

    @Override
    public List<ApiRequest<SearchRelationRequest>> examples() {
        return List.of(new SearchRelationRequest(List.of(1L, 2L, 3L), List.of(4L, 5L, 6L),
                List.of(2L), true, true));
    }
}
