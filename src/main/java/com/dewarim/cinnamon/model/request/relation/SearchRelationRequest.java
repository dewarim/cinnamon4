package com.dewarim.cinnamon.model.request.relation;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@JsonRootName("searchRelationRequest")
public record SearchRelationRequest(List<Long> leftIds, List<Long> rightIds, List<Long> relationTypeIds,
                                    boolean includeMetadata, boolean orMode) implements ApiRequest<SearchRelationRequest> {

    public SearchRelationRequest() {
        this(null, null, null, false, false);
    }

    public boolean validated() {
        // TODO: change validation to accept empty lists?
        return longCollectionIsValid(leftIds) || longCollectionIsValid(rightIds) || longCollectionIsValid(relationTypeIds);
    }

    private boolean longCollectionIsValid(Collection<Long> ids) {
        return Objects.nonNull(ids) && !ids.isEmpty() && ids.stream().noneMatch(o -> Objects.isNull(o) || o < 1);
    }

    public Optional<SearchRelationRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<ApiRequest<SearchRelationRequest>> examples() {
        return List.of(new SearchRelationRequest(List.of(1L, 2L, 3L), List.of(4L, 5L, 6L),
                List.of(2L), true, true));
    }
}
