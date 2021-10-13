package com.dewarim.cinnamon.model.request.objectType;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.ObjectType;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.ObjectTypeWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "updateObjectTypeRequest")
public class UpdateObjectTypeRequest implements UpdateRequest<ObjectType>, ApiRequest {

    private List<ObjectType> objectTypes = new ArrayList<>();

    @Override
    public List<ObjectType> list() {
        return objectTypes;
    }

    public UpdateObjectTypeRequest() {
    }

    public UpdateObjectTypeRequest(Long id, String name) {
        objectTypes.add(new ObjectType(id,name));
    }

    public UpdateObjectTypeRequest(List<ObjectType> ObjectTypes) {
        this.objectTypes = ObjectTypes;
    }

    public List<ObjectType> getObjectTypes() {
        return objectTypes;
    }

    @Override
    public boolean validated() {
        return objectTypes.stream().allMatch(ObjectType ->
            ObjectType != null && ObjectType.getName() != null && !ObjectType.getName().trim().isEmpty()
                    && ObjectType.getId() != null && ObjectType.getId() > 0);
    }

    @Override
    public Wrapper<ObjectType> fetchResponseWrapper() {
        return new ObjectTypeWrapper();
    }

    @Override
    public List<Object> examples() {
        return List.of(new UpdateObjectTypeRequest(123L,"updated-object-type-name"));
    }

}
