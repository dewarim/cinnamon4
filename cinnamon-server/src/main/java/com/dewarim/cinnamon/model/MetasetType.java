package com.dewarim.cinnamon.model;

import java.util.Objects;

public class MetasetType {
    
    private Long id;
    private String name;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MetasetType that = (MetasetType) o;
        return Objects.equals(name, that.name);
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
               '}';
    }
}
