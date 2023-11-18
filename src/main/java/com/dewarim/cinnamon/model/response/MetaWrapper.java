package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.api.ApiResponse;
import com.dewarim.cinnamon.model.Meta;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "cinnamon")
public class MetaWrapper implements Wrapper<Meta>, ApiResponse {

    @JacksonXmlElementWrapper(localName = "metasets")
    @JacksonXmlProperty(localName = "metaset")
    List<Meta> metasets = new ArrayList<>();

    public MetaWrapper() {
    }

    public MetaWrapper(List<Meta> metasets) {
        this.metasets = metasets;
    }

    public List<Meta> getMetasets() {
        return metasets;
    }

    public void setMetasets(List<Meta> metasets) {
        this.metasets = metasets;
    }

    public List<Meta> list() {
        return metasets;
    }

    @Override
    public Wrapper<Meta> setList(List<Meta> metas) {
        this.metasets = metas;
        return this;
    }

    @Override
    public List<Object> examples() {
        Meta folderMeta = new Meta(6L, 8L, "<xml> folder meta object </xml>");
        Meta osdMeta = new Meta(7L, 65L, "<xml> osd meta object </xml>");
        return List.of(new MetaWrapper(List.of(folderMeta,osdMeta)));
    }
}
