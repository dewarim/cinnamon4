package com.dewarim.cinnamon.api;

import java.util.Objects;

public class IdAndRootId {

    private Long id;
    private Long rootId;

    public IdAndRootId() {
    }

    public IdAndRootId(Long id, Long rootId) {
        this.id = id;
        this.rootId = rootId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRootId() {
        return rootId;
    }

    public void setRootId(Long rootId) {
        this.rootId = rootId;
    }



    @Override
    public int hashCode() {
        return Objects.hash(id, rootId);
    }

    @Override
    public String toString() {
        return "IdAndRootId{" +
                "id=" + id +
                ", rootId=" + rootId +
                '}';
    }
}
