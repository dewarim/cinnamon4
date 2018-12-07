package com.dewarim.cinnamon.model.request;


import java.util.Optional;

/**
 * Request for a single folder and its ancestors.
 */
public class SingleFolderRequest {

    private Long    id;
    private boolean includeSummary;

    public SingleFolderRequest() {
    }

    public SingleFolderRequest(Long id, boolean includeSummary) {
        this.includeSummary = includeSummary;
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isIncludeSummary() {
        return includeSummary;
    }

    public void setIncludeSummary(boolean includeSummary) {
        this.includeSummary = includeSummary;
    }

    /**
     * @return true if list of ids is non-empty and contains only positive long integers.
     */
    private boolean validated() {
        return id != null && id > 0;
    }

    public Optional<SingleFolderRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }
}
