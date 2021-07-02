package com.dewarim.cinnamon.model.request.folder;

import com.dewarim.cinnamon.api.ApiRequest;

public class FolderPathRequest implements ApiRequest {

    private String path;
    private boolean includeSummary;

    public FolderPathRequest() {
    }

    public FolderPathRequest(String path, boolean includeSummary) {
        this.path = path;
        this.includeSummary = includeSummary;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isIncludeSummary() {
        return includeSummary;
    }

    public void setIncludeSummary(boolean includeSummary) {
        this.includeSummary = includeSummary;
    }

    public boolean validated(){
        return path != null && path.length() > 0;
    }

}
