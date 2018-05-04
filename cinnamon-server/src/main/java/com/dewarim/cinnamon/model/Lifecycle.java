package com.dewarim.cinnamon.model;

import java.util.Objects;

public class Lifecycle {
    
    private Long id;
    private String name;
    private Long defaultStateId;

    public Lifecycle() {
    }

    public Lifecycle(String name, Long defaultStateId) {
        this.name = name;
        this.defaultStateId = defaultStateId;
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

    public Long getDefaultStateId() {
        return defaultStateId;
    }

    public void setDefaultStateId(Long defaultStateId) {
        this.defaultStateId = defaultStateId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Lifecycle lifecycle = (Lifecycle) o;
        return Objects.equals(name, lifecycle.name) &&
               Objects.equals(defaultStateId, lifecycle.defaultStateId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, defaultStateId);
    }
}
