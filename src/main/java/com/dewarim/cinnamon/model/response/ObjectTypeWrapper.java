package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.api.ApiResponse;
import com.dewarim.cinnamon.model.ObjectType;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "cinnamon")
public class ObjectTypeWrapper extends BaseResponse implements Wrapper<ObjectType>, ApiResponse {

    @JacksonXmlElementWrapper(localName = "objectTypes")
    @JacksonXmlProperty(localName = "objectType")
    List<ObjectType> objectTypes = new ArrayList<>();

    public List<ObjectType> getObjectTypes() {
        return objectTypes;
    }

    public void setObjectTypes(List<ObjectType> objectTypes) {
        this.objectTypes = objectTypes;
    }

    @Override
    public List<ObjectType> list() {
        return getObjectTypes();
    }

    @Override
    public Wrapper<ObjectType> setList(List<ObjectType> objectTypes) {
        setObjectTypes(objectTypes);
        return this;
    }

    @Override
    public List<Object> examples() {
        ObjectTypeWrapper wrapper = new ObjectTypeWrapper();
        ObjectType        image   = new ObjectType(8L, "image");
        wrapper.getObjectTypes().add(image);
        return List.of(wrapper);
    }
}
