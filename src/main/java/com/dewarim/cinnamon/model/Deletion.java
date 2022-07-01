package com.dewarim.cinnamon.model;

import com.dewarim.cinnamon.api.Identifiable;

public class Deletion implements Identifiable {

    private long osdId;
    private String contentPath;
    private boolean deleted;

    public Deletion() {
    }

    public Deletion(long osdId, String contentPath, boolean deleted) {
        this.osdId = osdId;
        this.contentPath = contentPath;
        this.deleted = deleted;
    }

    public long getOsdId() {
        return osdId;
    }

    public void setOsdId(long osdId) {
        this.osdId = osdId;
    }

    public String getContentPath() {
        return contentPath;
    }

    public void setContentPath(String contentPath) {
        this.contentPath = contentPath;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public Long getId() {
        return osdId;
    }
}
