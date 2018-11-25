package com.dewarim.cinnamon.model;

import java.util.Objects;

public class OsdMeta {

    private Long id;
    private Long osdId;
    private Long typeId;
    private String content;

    public OsdMeta() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOsdId() {
        return osdId;
    }

    public void setOsdId(Long osdId) {
        this.osdId = osdId;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OsdMeta osdMeta = (OsdMeta) o;
        return Objects.equals(osdId, osdMeta.osdId) &&
                Objects.equals(typeId, osdMeta.typeId) &&
                Objects.equals(content, osdMeta.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(osdId, typeId, content);
    }

    @Override
    public String toString() {
        return "OsdMeta{" +
                "id=" + id +
                ", osdId=" + osdId +
                ", typeId=" + typeId +
                ", content='" + content + '\'' +
                '}';
    }
}
