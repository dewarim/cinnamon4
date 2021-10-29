package com.dewarim.cinnamon.model.request.objectType;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.ObjectType;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.ObjectTypeWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "createObjectTypeRequest")
public class CreateObjectTypeRequest implements CreateRequest<ObjectType>, ApiRequest {

    @JacksonXmlElementWrapper(localName = "objectTypes")
    @JacksonXmlProperty(localName = "objectType")
    private List<ObjectType> objectTypes = new ArrayList<>();

    @Override
    public List<ObjectType> list() {
        return objectTypes;
    }

    public CreateObjectTypeRequest() {
    }

    public CreateObjectTypeRequest(String name) {
        this.objectTypes.add(new ObjectType(name));
    }

    public CreateObjectTypeRequest(List<ObjectType> objectTypes) {
        this.objectTypes = objectTypes;
    }

    public List<ObjectType> getObjectTypes() {
        return objectTypes;
    }

    @Override
    public boolean validated() {
        return objectTypes.stream().noneMatch(type -> type == null ||
                type.getName() == null ||
                type.getName().trim().isEmpty());
    }

    @Override
    public Wrapper<ObjectType> fetchResponseWrapper() {
        return new ObjectTypeWrapper();
    }

    @Override
    public List<Object> examples() {
        return List.of(new CreateObjectTypeRequest(
                List.of(new ObjectType("default type"), new ObjectType("other type"))
        ));
    }

}
