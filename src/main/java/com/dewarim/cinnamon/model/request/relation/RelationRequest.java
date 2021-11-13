package com.dewarim.cinnamon.model.request.relation;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

@JacksonXmlRootElement(localName = "relationRequest")
public class RelationRequest implements ApiRequest {

    private Collection<Long>   leftIds;
    private Collection<Long>   rightIds;
    private Collection<String> names;
    private boolean            includeMetadata;
    private boolean            orMode = false;

    public RelationRequest() {
    }

    public RelationRequest(Collection<Long> leftIds, Collection<Long> rightIds, Collection<String> names, boolean includeMetadata) {
        this.leftIds = leftIds;
        this.rightIds = rightIds;
        this.names = names;
        this.includeMetadata = includeMetadata;
    }

    public Collection<Long> getLeftIds() {
        return leftIds;
    }

    public Collection<Long> getRightIds() {
        return rightIds;
    }

    public Collection<String> getNames() {
        return names;
    }

    public boolean isIncludeMetadata() {
        return includeMetadata;
    }

    public void setLeftIds(Collection<Long> leftIds) {
        this.leftIds = leftIds;
    }

    public void setRightIds(Collection<Long> rightIds) {
        this.rightIds = rightIds;
    }

    public void setNames(Collection<String> names) {
        this.names = names;
    }

    public void setIncludeMetadata(boolean includeMetadata) {
        this.includeMetadata = includeMetadata;
    }

    public boolean validated() {
        return longCollectionIsValid(leftIds) || longCollectionIsValid(rightIds) || stringCollectionIsValid(names);
    }

    private boolean longCollectionIsValid(Collection<Long> ids) {
        return Objects.nonNull(ids) && !ids.isEmpty() && ids.stream().noneMatch(o -> Objects.isNull(o) || o < 1);
    }

    private boolean stringCollectionIsValid(Collection<String> names) {
        return Objects.nonNull(names) && !names.isEmpty() && names.stream().noneMatch(o -> Objects.isNull(o) || o.trim().length() == 0);
    }

    public boolean isOrMode() {
        return orMode;
    }

    public void setOrMode(boolean orMode) {
        this.orMode = orMode;
    }

    public Optional<RelationRequest> validateRequest(){
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }
}
