package com.dewarim.cinnamon.model;

import com.dewarim.cinnamon.api.Identifiable;

public class Deletion implements Identifiable {

    private long osdId;
    private String contentPath;
    private boolean deleted;

    private boolean deleteFailed;

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

    public boolean isDeleteFailed() {
        return deleteFailed;
    }

    public void setDeleteFailed(boolean deleteFailed) {
        this.deleteFailed = deleteFailed;
    }

    @Override
    public Long getId() {
        return osdId;
    }

    @Override
    public String toString() {
        return "Deletion{" +
                "osdId=" + osdId +
                ", contentPath='" + contentPath + '\'' +
                ", deleted=" + deleted +
                ", deleteFailed=" + deleteFailed +
                '}';
    }
}
