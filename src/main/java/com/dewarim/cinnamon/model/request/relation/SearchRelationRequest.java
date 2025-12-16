package com.dewarim.cinnamon.model.request.relation;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@JacksonXmlRootElement(localName = "searchRelationRequest")
public class SearchRelationRequest implements ApiRequest<SearchRelationRequest> {

    private List<Long>       leftIds;
    private List<Long> rightIds;
    private List<Long> relationTypeIds;
    private boolean    includeMetadata;
    private boolean          orMode = false;

    public SearchRelationRequest() {
    }

    public SearchRelationRequest(List<Long> leftIds, List<Long> rightIds, List<Long> relationTypeIds,
                                 boolean includeMetadata, boolean orMode) {
        this.leftIds = leftIds;
        this.rightIds = rightIds;
        this.includeMetadata = includeMetadata;
        this.orMode = orMode;
        this.relationTypeIds=relationTypeIds;
    }

    public List<Long> getLeftIds() {
        return leftIds;
    }

    public List<Long> getRightIds() {
        return rightIds;
    }

    public List<Long> getRelationTypeIds() {
        return relationTypeIds;
    }

    public void setRelationTypeIds(List<Long> relationTypeIds) {
        this.relationTypeIds = relationTypeIds;
    }

    public boolean isIncludeMetadata() {
        return includeMetadata;
    }

    public void setLeftIds(List<Long> leftIds) {
        this.leftIds = leftIds;
    }

    public void setRightIds(List<Long> rightIds) {
        this.rightIds = rightIds;
    }


    public void setIncludeMetadata(boolean includeMetadata) {
        this.includeMetadata = includeMetadata;
    }

    public boolean validated() {
        // TODO: change validation to accept empty lists?
        return longCollectionIsValid(leftIds) || longCollectionIsValid(rightIds) || longCollectionIsValid(relationTypeIds);
    }

    private boolean longCollectionIsValid(Collection<Long> ids) {
        return Objects.nonNull(ids) && !ids.isEmpty() && ids.stream().noneMatch(o -> Objects.isNull(o) || o < 1);
    }

    public boolean isOrMode() {
        return orMode;
    }

    public void setOrMode(boolean orMode) {
        this.orMode = orMode;
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
