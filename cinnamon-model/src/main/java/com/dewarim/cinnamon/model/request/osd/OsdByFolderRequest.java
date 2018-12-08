package com.dewarim.cinnamon.model.request.osd;

public class OsdByFolderRequest {

    private boolean includeSummary;
    private long folderId;

    public OsdByFolderRequest() {
    }

    public OsdByFolderRequest(long folderId, boolean includeSummary) {
        this.includeSummary = includeSummary;
        this.folderId = folderId;
    }

    public boolean isIncludeSummary() {
        return includeSummary;
    }

    public void setIncludeSummary(boolean includeSummary) {
        this.includeSummary = includeSummary;
    }

    public long getFolderId() {
        return folderId;
    }

    public void setFolderId(long folderId) {
        this.folderId = folderId;
    }
}
