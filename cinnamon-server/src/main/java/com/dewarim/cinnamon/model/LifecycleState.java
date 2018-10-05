package com.dewarim.cinnamon.model;

import com.dewarim.cinnamon.api.lifecycle.LifecycleStateConfig;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.IOException;
import java.util.Objects;

@JsonIgnoreProperties(value = { "lifecycleStateConfig" })
public class LifecycleState {

    private Long   id;
    private String name;
    private String config = "<config />";
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

    public LifecycleStateConfig getLifecycleStateConfig() throws IOException {
        /*
         * Note: This is probably not optimal: parsing the config to a class from the cinnamon-api module.
         * On the other hand, the config is currently stored as String object in XML format.
         * If we had the configs in their own table as objects in their own right, I think I would use configId::config.
         */
        ObjectMapper xmlMapper = new XmlMapper();
        return xmlMapper.readValue(config, LifecycleStateConfig.class);
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
