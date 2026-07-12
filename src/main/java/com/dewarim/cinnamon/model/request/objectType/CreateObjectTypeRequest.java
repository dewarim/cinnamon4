package com.dewarim.cinnamon.model.request.objectType;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.ObjectType;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.ObjectTypeWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

@JsonRootName("createObjectTypeRequest")
public record CreateObjectTypeRequest(
        @JacksonXmlElementWrapper(localName = "objectTypes")
        @JacksonXmlProperty(localName = "objectType")
        List<ObjectType> objectTypes) implements CreateRequest<ObjectType>, ApiRequest<CreateObjectTypeRequest> {

    public CreateObjectTypeRequest {
        if (objectTypes == null) {
            objectTypes = new ArrayList<>();
        }
    }

    public CreateObjectTypeRequest(String name) {
        this(new ArrayList<>(List.of(new ObjectType(name))));
    }

    @Override
    public List<ObjectType> list() {
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
    public List<ApiRequest<CreateObjectTypeRequest>> examples() {
        return List.of(new CreateObjectTypeRequest(
                List.of(new ObjectType("default type"), new ObjectType("other type"))
        ));
    }
}
