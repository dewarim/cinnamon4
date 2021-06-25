package com.dewarim.cinnamon.model;

import com.dewarim.cinnamon.api.Identifiable;

import java.util.Objects;

public class MetasetType implements Identifiable {
    
    private Long id;
    private String name;
    private Boolean unique;
    
    public MetasetType() {
    }

    public MetasetType(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getUnique() {
        return unique;
    }

    public void setUnique(Boolean unique) {
        this.unique = unique;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MetasetType that = (MetasetType) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(unique, that.unique);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "MetasetType{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", unique=" + unique +
                '}';
    }
}
