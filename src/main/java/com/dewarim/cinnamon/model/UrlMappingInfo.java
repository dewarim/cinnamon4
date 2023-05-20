package com.dewarim.cinnamon.model;

import java.util.Objects;

public class UrlMappingInfo {

    private String controller;
    private String action;
    private String path;
    private String description;

    public UrlMappingInfo() {
    }

    public UrlMappingInfo(String controller, String action, String path, String description) {
        this.controller = controller;
        this.action = action;
        this.path = path;
        this.description = description;
    }

    public String getController() {
        return controller;
    }

    public void setController(String controller) {
        this.controller = controller;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "UrlMappingInfo{" +
                "servlet='" + controller + '\'' +
                ", action='" + action + '\'' +
                ", prefix='" + path + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UrlMappingInfo that = (UrlMappingInfo) o;
        return Objects.equals(controller, that.controller) && Objects.equals(action, that.action) && Objects.equals(path, that.path) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(controller, action, path, description);
    }
}
