package com.dewarim.cinnamon.model;

import com.dewarim.cinnamon.api.Identifiable;

import java.util.Objects;

public class ObjectType implements Identifiable {
    
    private Long id;
    private String name;

    public ObjectType() {
    }

    public ObjectType(Long id, String name) {
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
        ObjectType that = (ObjectType) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "ObjectType{" +
               "id=" + id +
               ", name='" + name + '\'' +
               '}';
    }
}
