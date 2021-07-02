package com.dewarim.cinnamon.model.response;


import com.dewarim.cinnamon.api.ApiResponse;
import com.dewarim.cinnamon.model.MetasetType;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "cinnamon")
public class MetasetTypeWrapper implements Wrapper<MetasetType>, ApiResponse {

    @JacksonXmlElementWrapper(localName = "metasetTypes")
    @JacksonXmlProperty(localName = "metasetType")
    List<MetasetType> metasetTypes = new ArrayList<>();

    public List<MetasetType> getMetasetTypes() {
        return metasetTypes;
    }

    public void setMetasetTypes(List<MetasetType> metasetTypes) {
        this.metasetTypes = metasetTypes;
    }

    @Override
    public List<MetasetType> list() {
        return getMetasetTypes();
    }

    @Override
    public Wrapper<MetasetType> setList(List<MetasetType> metasetTypes) {
        setMetasetTypes(metasetTypes);
        return this;
    }
}
