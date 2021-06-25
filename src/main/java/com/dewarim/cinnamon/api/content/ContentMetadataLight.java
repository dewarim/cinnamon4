package com.dewarim.cinnamon.api.content;

public class ContentMetadataLight implements ContentMetadata{

    private Long id;
    private String name;
    private String contentHash;
    private String contentPath;
    private Long contentSize;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getContentHash() {
        return contentHash;
    }

    @Override
    public void setContentHash(String contentHash) {
        this.contentHash = contentHash;
    }

    @Override
    public String getContentPath() {
        return contentPath;
    }

    @Override
    public void setContentPath(String contentPath) {
        this.contentPath = contentPath;
    }

    @Override
    public Long getContentSize() {
        return contentSize;
    }

    @Override
    public void setContentSize(Long contentSize) {
        this.contentSize = contentSize;
    }

    @Override
    public String toString() {
        return "ContentMetadataLight{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", contentHash='" + contentHash + '\'' +
                ", contentPath='" + contentPath + '\'' +
                ", contentSize=" + contentSize +
                '}';
    }
}
