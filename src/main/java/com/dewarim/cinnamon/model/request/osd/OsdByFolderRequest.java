package com.dewarim.cinnamon.model.request.osd;

import com.dewarim.cinnamon.api.ApiRequest;

public class OsdByFolderRequest implements ApiRequest {

    private boolean includeSummary;
    private long    folderId;
    private boolean linksAsOsd = true;

    public OsdByFolderRequest() {
    }

    public OsdByFolderRequest(long folderId, boolean includeSummary, boolean linksAsOsd) {
        this.includeSummary = includeSummary;
        this.folderId = folderId;
        this.linksAsOsd = linksAsOsd;
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

    public boolean isLinksAsOsd() {
        return linksAsOsd;
    }

    public void setLinksAsOsd(boolean linksAsOsd) {
        this.linksAsOsd = linksAsOsd;
    }

    @Override
    public String toString() {
        return "OsdByFolderRequest{" +
                "includeSummary=" + includeSummary +
                ", folderId=" + folderId +
                ", linksAsOsd=" + linksAsOsd +
                '}';
    }
}
