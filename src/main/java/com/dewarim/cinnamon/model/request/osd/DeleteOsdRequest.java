package com.dewarim.cinnamon.model.request.osd;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@JacksonXmlRootElement(localName = "deleteOsdRequest")
public class DeleteOsdRequest implements ApiRequest {

    @JacksonXmlElementWrapper(localName = "ids")
    @JacksonXmlProperty(localName = "id")
    private List<Long> ids               = new ArrayList<>();
    private boolean    deleteDescendants = false;
    private boolean    deleteAllVersions = false;

    public DeleteOsdRequest() {
    }

    public DeleteOsdRequest(List<Long> ids, boolean deleteDescendants, boolean deleteAllVersions) {
        this.ids = ids;
        this.deleteDescendants = deleteDescendants;
        this.deleteAllVersions = deleteAllVersions;
    }

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    public boolean isDeleteDescendants() {
        return deleteDescendants;
    }

    public void setDeleteDescendants(boolean deleteDescendants) {
        this.deleteDescendants = deleteDescendants;
    }

    public boolean isDeleteAllVersions() {
        return deleteAllVersions;
    }

    public void setDeleteAllVersions(boolean deleteAllVersions) {
        this.deleteAllVersions = deleteAllVersions;
    }

    private boolean validated() {
        return ids != null && ids.stream().allMatch(id -> Objects.nonNull(id) && id > 0);
    }

    public Optional<DeleteOsdRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        }
        return Optional.empty();
    }
}
