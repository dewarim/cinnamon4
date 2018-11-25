package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.model.OsdMeta;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "cinnamon")
public class OsdMetaWrapper {

    @JacksonXmlElementWrapper(localName = "metasets")
    @JacksonXmlProperty(localName = "metaset")
    List<OsdMeta> metasets = new ArrayList<>();

    public List<OsdMeta> getMetasets() {
        return metasets;
    }

    public void setMetasets(List<OsdMeta> metasets) {
        this.metasets = metasets;
    }
}
