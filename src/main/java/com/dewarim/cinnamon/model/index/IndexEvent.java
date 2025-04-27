package com.dewarim.cinnamon.model.index;

import com.dewarim.cinnamon.api.Identifiable;

import java.time.LocalDateTime;

public class IndexEvent implements Identifiable {

    private Long           id;
    private LocalDateTime  localTime = LocalDateTime.now();
    private Long           jobId;
    private IndexEventType eventType = IndexEventType.GENERIC;
    private IndexResult    indexResult;
    private String         message = "-";

    public IndexEvent() {
    }

    public IndexEvent(Long jobId, IndexEventType eventType, IndexResult indexResult, String message) {
        this.jobId       = jobId;
        this.eventType   = eventType;
        this.indexResult = indexResult;
        this.message     = message;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getLocalTime() {
        return localTime;
    }

    public void setLocalTime(LocalDateTime localTime) {
        this.localTime = localTime;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public IndexEventType getEventType() {
        return eventType;
    }

    public void setEventType(IndexEventType eventType) {
        this.eventType = eventType;
    }

    public IndexResult getIndexResult() {
        return indexResult;
    }

    public IndexEvent setIndexResult(IndexResult indexResult) {
        this.indexResult = indexResult;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "IndexEvent{" +
                "id=" + id +
                ", localTime=" + localTime +
                ", jobId=" + jobId +
                ", eventType=" + eventType +
                ", indexResult=" + indexResult +
                ", message='" + message + '\'' +
                '}';
    }
}
