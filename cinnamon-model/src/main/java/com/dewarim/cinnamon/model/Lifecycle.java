package com.dewarim.cinnamon.model;

import java.util.List;
import java.util.Objects;

public class Lifecycle {
    
    private Long id;
    private String name;
    private Long defaultStateId;
    private List<LifecycleState> lifecycleStates;

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

    public List<LifecycleState> getLifecycleStates() {
        return lifecycleStates;
    }

    public void setLifecycleStates(List<LifecycleState> lifecycleStates) {
        this.lifecycleStates = lifecycleStates;
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
                Objects.equals(defaultStateId, lifecycle.defaultStateId) &&
                Objects.equals(lifecycleStates, lifecycle.lifecycleStates);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, defaultStateId);
    }
}
