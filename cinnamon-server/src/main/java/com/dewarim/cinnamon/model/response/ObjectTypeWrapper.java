package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.model.Format;
import com.dewarim.cinnamon.model.ObjectType;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "cinnamon")
public class ObjectTypeWrapper {

    @JacksonXmlElementWrapper(localName = "objectTypes")
    @JacksonXmlProperty(localName = "objectType")
    List<ObjectType> objectTypes = new ArrayList<>();

    public List<ObjectType> getObjectTypes() {
        return objectTypes;
    }

    public void setObjectTypes(List<ObjectType> objectTypes) {
        this.objectTypes = objectTypes;
    }
}
