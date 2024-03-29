package com.dewarim.cinnamon.model.request.folder;


import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Folder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;
import java.util.Optional;

/**
 * Request for a single folder and its ancestors.
 */
@JacksonXmlRootElement(localName = "singleFolderRequest")
public class SingleFolderRequest implements ApiRequest<Folder> {

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

    @Override
    public List<ApiRequest<Folder>> examples() {
        return List.of(new SingleFolderRequest(123L,true), new SingleFolderRequest(321L,false));
    }
}
