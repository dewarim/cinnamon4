package com.dewarim.cinnamon.model.request.osd;

import com.dewarim.cinnamon.api.ApiRequest;

public class OsdByFolderRequest implements ApiRequest {

    private boolean          includeSummary;
    private long             folderId;
    private boolean          linksAsOsd       = true;
    private boolean          includeCustomMetadata;

    /**
     * Select whether to return all, the newest versions or the most recent branch objects.
     * Default is HEAD for compatibility with Cinnamon 3
     */
    private VersionPredicate versionPredicate = VersionPredicate.HEAD;


    public OsdByFolderRequest() {
    }

    public OsdByFolderRequest(long folderId, boolean includeSummary, boolean linksAsOsd, boolean includeCustomMetadata,
                              VersionPredicate versionPredicate) {
        this.includeSummary = includeSummary;
        this.folderId = folderId;
        this.linksAsOsd = linksAsOsd;
        this.includeCustomMetadata = includeCustomMetadata;
        this.versionPredicate = versionPredicate;

    }

    public VersionPredicate getVersionPredicate() {
        return versionPredicate;
    }

    public void setVersionPredicate(VersionPredicate versionPredicate) {
        this.versionPredicate = versionPredicate;
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

    public boolean isIncludeCustomMetadata() {
        return includeCustomMetadata;
    }

    public void setIncludeCustomMetadata(boolean includeCustomMetadata) {
        this.includeCustomMetadata = includeCustomMetadata;
    }

    @Override
    public String toString() {
        return "OsdByFolderRequest{" +
                "includeSummary=" + includeSummary +
                ", folderId=" + folderId +
                ", linksAsOsd=" + linksAsOsd +
                ", includeCustomMetadata=" + includeCustomMetadata +
                '}';
    }
}
