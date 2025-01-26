package com.dewarim.cinnamon.api;

import java.util.Objects;

public class RootAndLatestHead {

    private Long headId;
    private Long rootId;

    public RootAndLatestHead() {
    }

    public Long getHeadId() {
        return headId;
    }

    public void setHeadId(Long headId) {
        this.headId = headId;
    }

    public Long getRootId() {
        return rootId;
    }

    public void setRootId(Long rootId) {
        this.rootId = rootId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RootAndLatestHead that = (RootAndLatestHead) o;
        return Objects.equals(headId, that.headId) && Objects.equals(rootId, that.rootId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(headId, rootId);
    }

    @Override
    public String toString() {
        return "LatestHead{" +
                "id=" + headId +
                ", rootId=" + rootId +
                '}';
    }
}
