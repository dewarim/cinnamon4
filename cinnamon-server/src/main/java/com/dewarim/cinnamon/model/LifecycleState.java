package com.dewarim.cinnamon.model;

import java.util.Objects;

public class LifecycleState {

    private Long   id;
    private String name;
    private String config = "<meta />";
    private String stateClass;
    private Long   lifecycleId;
    private Long   lifecycleStateForCopyId;

    public LifecycleState() {
    }

    public LifecycleState(String name, String config, String stateClass, Long lifecycleId, Long lifecycleStateForCopyId) {
        this.name = name;
        this.config = config;
        this.stateClass = stateClass;
        this.lifecycleId = lifecycleId;
        this.lifecycleStateForCopyId = lifecycleStateForCopyId;
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

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getStateClass() {
        return stateClass;
    }

    public void setStateClass(String stateClass) {
        this.stateClass = stateClass;
    }

    public Long getLifecycleId() {
        return lifecycleId;
    }

    public void setLifecycleId(Long lifecycleId) {
        this.lifecycleId = lifecycleId;
    }

    public Long getLifecycleStateForCopyId() {
        return lifecycleStateForCopyId;
    }

    public void setLifecycleStateForCopyId(Long lifecycleStateForCopyId) {
        this.lifecycleStateForCopyId = lifecycleStateForCopyId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LifecycleState that = (LifecycleState) o;
        return Objects.equals(name, that.name) &&
               Objects.equals(config, that.config) &&
               Objects.equals(stateClass, that.stateClass) &&
               Objects.equals(lifecycleId, that.lifecycleId) &&
               Objects.equals(lifecycleStateForCopyId, that.lifecycleStateForCopyId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, config, stateClass, lifecycleId, lifecycleStateForCopyId);
    }
}
