package com.dewarim.cinnamon.model.request.osd;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DeleteOsdRequest {

    private List<Long> ids               = new ArrayList<>();
    private boolean    deleteDescendants = false;

    public DeleteOsdRequest() {
    }

    public DeleteOsdRequest(List<Long> ids, boolean deleteDescendants) {
        this.ids = ids;
        this.deleteDescendants = deleteDescendants;
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
