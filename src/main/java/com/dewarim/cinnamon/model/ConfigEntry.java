package com.dewarim.cinnamon.model;

import com.dewarim.cinnamon.api.Identifiable;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Objects;

public class ConfigEntry implements Identifiable {
    
    // Request validation, checked when a request cascades into this class with @Valid.
    @NotNull(groups = UpdateRequest.class)
    @Positive(groups = UpdateRequest.class)
    private Long    id;
    @NotBlank
    private String  name;
    @NotNull
    private String  config;
    private boolean publicVisibility;

    public ConfigEntry() {
    }

    public ConfigEntry(String name, String config, boolean publicVisibility) {
        this.name = name;
        this.config = config;
        this.publicVisibility = publicVisibility;
    }

    public ConfigEntry(Long id, String name, String config, boolean publicVisibility) {
        this.id = id;
        this.name = name;
        this.config = config;
        this.publicVisibility = publicVisibility;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public boolean isPublicVisibility() {
        return publicVisibility;
    }

    public void setPublicVisibility(boolean publicVisibility) {
        this.publicVisibility = publicVisibility;
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
        ConfigEntry that = (ConfigEntry) o;
        return publicVisibility == that.publicVisibility &&
               Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "ConfigEntry{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", content='" + config + '\'' +
               ", publicVisibility=" + publicVisibility +
               '}';
    }
}
