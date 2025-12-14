package com.dewarim.cinnamon.model.request.folder;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JacksonXmlRootElement(localName = "folderRequest")
public class FolderRequest implements ApiRequest<FolderRequest> {

    @JacksonXmlElementWrapper(localName = "ids")
    @JacksonXmlProperty(localName = "id")
    private List<Long> ids = new ArrayList<>();

    private boolean includeSummary;
    private boolean addFolderPath;

    public FolderRequest() {
    }

    public FolderRequest(List<Long> ids, boolean includeSummary) {
        this.includeSummary = includeSummary;
        this.ids = ids;
    }

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
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
        return ids.size() > 0 && ids.stream().allMatch(id -> id != null && id > 0);
    }

    public Optional<FolderRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    public boolean isAddFolderPath() {
        return addFolderPath;
    }

    public void setAddFolderPath(boolean addFolderPath) {
        this.addFolderPath = addFolderPath;
    }

    @Override
    public List<ApiRequest<FolderRequest>> examples() {
        FolderRequest folderRequest = new FolderRequest(List.of(1L, 2L, 3L), true);
        folderRequest.setAddFolderPath(true);
        return List.of(folderRequest);
    }
}
