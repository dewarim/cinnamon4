package com.dewarim.cinnamon.model.index;

import java.util.Objects;

public class IndexJob {

    private Long id;
    private IndexJobType jobType;
    private Long itemId;
    private Integer failed = 0;

    private IndexJobAction action;

    public IndexJob() {
    }

    public IndexJob(IndexJobType jobType, Long itemId, IndexJobAction action) {
        Objects.requireNonNull(jobType);
        this.jobType = jobType;
        Objects.requireNonNull(action);
        this.action = action;
        Objects.requireNonNull(itemId);
        this.itemId = itemId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Integer getFailed() {
        return failed;
    }

    public void setFailed(Integer failed) {
        this.failed = failed;
    }

    public IndexJobType getJobType() {
        return jobType;
    }

    public void setJobType(IndexJobType jobType) {
        this.jobType = jobType;
    }

    public IndexJobAction getAction() {
        return action;
    }

    public void setAction(IndexJobAction action) {
        this.action = action;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IndexJob indexJob = (IndexJob) o;
        return jobType == indexJob.jobType && itemId.equals(indexJob.itemId) && failed.equals(indexJob.failed) && action == indexJob.action;
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobType, itemId);
    }

    @Override
    public String toString() {
        return "IndexJob{" +
                "id=" + id +
                ", jobType=" + jobType +
                ", itemId=" + itemId +
                ", failed=" + failed +
                ", action=" + action +
                '}';
    }
}
