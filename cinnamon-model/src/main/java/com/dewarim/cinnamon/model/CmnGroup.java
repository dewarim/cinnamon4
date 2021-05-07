package com.dewarim.cinnamon.model;

import com.dewarim.cinnamon.api.Identifiable;

import java.util.Objects;

public class CmnGroup implements Identifiable {
    
    private Long id;
    
    /**
     * Used to indicate if it's a user's personal group
     */
    private boolean groupOfOne;
    private String name;
    private Long objVersion = 0L;
    private Long parentId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isGroupOfOne() {
        return groupOfOne;
    }

    public void setGroupOfOne(boolean groupOfOne) {
        this.groupOfOne = groupOfOne;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getObjVersion() {
        return objVersion;
    }

    public void setObjVersion(Long objVersion) {
        this.objVersion = objVersion;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CmnGroup cmnGroup = (CmnGroup) o;
        return groupOfOne == cmnGroup.groupOfOne &&
                Objects.equals(id, cmnGroup.id) &&
                Objects.equals(name, cmnGroup.name) &&
                Objects.equals(objVersion, cmnGroup.objVersion) &&
                Objects.equals(parentId, cmnGroup.parentId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name);
    }

    @Override
    public String 
    toString() {
        return "CmnGroup{" +
                "id=" + id +
                ", groupOfOne=" + groupOfOne +
                ", name='" + name + '\'' +
                ", objVersion=" + objVersion +
                ", parentId=" + parentId +
                '}';
    }
}
