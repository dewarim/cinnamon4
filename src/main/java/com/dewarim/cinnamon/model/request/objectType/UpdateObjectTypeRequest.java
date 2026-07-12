package com.dewarim.cinnamon.model.request.objectType;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.ObjectType;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.ObjectTypeWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

@JsonRootName("updateObjectTypeRequest")
public record UpdateObjectTypeRequest(
        @JacksonXmlElementWrapper(localName = "objectTypes")
        @JacksonXmlProperty(localName = "objectType")
        List<ObjectType> objectTypes) implements UpdateRequest<ObjectType>, ApiRequest<UpdateObjectTypeRequest> {

    public UpdateObjectTypeRequest {
        if (objectTypes == null) {
            objectTypes = new ArrayList<>();
        }
    }

    public UpdateObjectTypeRequest(Long id, String name) {
        this(new ArrayList<>(List.of(new ObjectType(id, name))));
    }

    @Override
    public List<ObjectType> list() {
        return objectTypes;
    }

    @Override
    public boolean validated() {
        return objectTypes.stream().allMatch(objectType ->
                objectType != null && objectType.getName() != null && !objectType.getName().trim().isEmpty()
                        && objectType.getId() != null && objectType.getId() > 0);
    }

    @Override
    public Wrapper<ObjectType> fetchResponseWrapper() {
        return new ObjectTypeWrapper();
    }

    @Override
    public List<ApiRequest<UpdateObjectTypeRequest>> examples() {
        return List.of(new UpdateObjectTypeRequest(123L, "updated-object-type-name"));
    }
}
