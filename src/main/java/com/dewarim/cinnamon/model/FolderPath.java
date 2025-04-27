package com.dewarim.cinnamon.model;

public class FolderPath {

    private Long id;
    private String path;

    public FolderPath() {
    }

    public FolderPath(Long id, String path) {
        this.id   = id;
        this.path = path;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "FolderPath{" +
                "id=" + id +
                ", path='" + path + '\'' +
                '}';
    }
}
