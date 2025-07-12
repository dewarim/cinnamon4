package com.dewarim.cinnamon.model.request.folder;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "folderByRelativePathRequest")
public class FolderByRelativePathRequest implements ApiRequest<FolderByRelativePathRequest> {

    private String  relativePath;
    private Long    parentId;
    private boolean includeSummary;

    public FolderByRelativePathRequest() {
    }

    public FolderByRelativePathRequest(String relativePath, Long parentId, boolean includeSummary) {
        this.relativePath = relativePath;
        this.parentId = parentId;
        this.includeSummary = includeSummary;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public boolean isIncludeSummary() {
        return includeSummary;
    }

    public void setIncludeSummary(boolean includeSummary) {
        this.includeSummary = includeSummary;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public boolean validated() {
        return relativePath != null &&
                relativePath.length() > 0 &&
                !(relativePath.startsWith("/") || relativePath.endsWith("/")) &&
                parentId != null && parentId > 0;
    }

    @Override
    public List<ApiRequest<FolderByRelativePathRequest>> examples() {
        return List.of(new FolderByRelativePathRequest("creation/some-sub-folder", 213L, true));
    }
}
