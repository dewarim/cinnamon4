package com.dewarim.cinnamon.model;

import com.dewarim.cinnamon.api.Identifiable;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Lifecycle implements Identifiable {
    
    private Long id;
    private String name;
    private Long defaultStateId;

    @JacksonXmlElementWrapper(localName = "lifecycleStates")
    @JacksonXmlProperty(localName = "lifecycleState")
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
        if(lifecycleStates == null){
            lifecycleStates = new ArrayList<>();
        }
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

    @Override
    public String toString() {
        return "Lifecycle{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", defaultStateId=" + defaultStateId +
                ", lifecycleStates=" + lifecycleStates +
                '}';
    }
}
