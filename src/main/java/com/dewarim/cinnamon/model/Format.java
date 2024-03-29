package com.dewarim.cinnamon.model;

import com.dewarim.cinnamon.api.Identifiable;

import java.util.Objects;

public class Format implements Identifiable {

    private Long      id;
    private String    contentType;
    private String    extension;
    private String    name;
    private Long      defaultObjectTypeId;
    private IndexMode indexMode;

    public Format() {
    }

    public Format(String contentType, String extension, String name, Long defaultObjectTypeId, IndexMode indexMode) {
        this.contentType = contentType;
        this.extension = extension;
        this.name = name;
        this.defaultObjectTypeId = defaultObjectTypeId;
        this.indexMode = indexMode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getDefaultObjectTypeId() {
        return defaultObjectTypeId;
    }

    public void setDefaultObjectTypeId(Long defaultObjectTypeId) {
        this.defaultObjectTypeId = defaultObjectTypeId;
    }

    public IndexMode getIndexMode() {
        return indexMode;
    }

    public void setIndexMode(IndexMode indexMode) {
        this.indexMode = indexMode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Format format = (Format) o;
        return Objects.equals(contentType, format.contentType) &&
                Objects.equals(extension, format.extension) &&
                Objects.equals(name, format.name) &&
                Objects.equals(indexMode, format.indexMode) &&
                Objects.equals(defaultObjectTypeId, format.defaultObjectTypeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contentType, extension, name, defaultObjectTypeId);
    }

    @Override
    public String toString() {
        return "Format{" +
                "id=" + id +
                ", contentType='" + contentType + '\'' +
                ", extension='" + extension + '\'' +
                ", name='" + name + '\'' +
                ", defaultObjectTypeId=" + defaultObjectTypeId +
                ", indexMode=" + indexMode +
                '}';
    }
}
