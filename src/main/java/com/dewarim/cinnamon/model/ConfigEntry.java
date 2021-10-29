package com.dewarim.cinnamon.model;

import com.dewarim.cinnamon.api.Identifiable;

import java.util.Objects;

public class ConfigEntry implements Identifiable {
    
    private Long    id;
    private String  name;
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
               Objects.equals(name, that.name) &&
               Objects.equals(config, that.config);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, config, publicVisibility);
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
