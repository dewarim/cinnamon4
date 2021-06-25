package com.dewarim.cinnamon.model.request.objectType;

import com.dewarim.cinnamon.model.ObjectType;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.ObjectTypeWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;

import java.util.ArrayList;
import java.util.List;

public class UpdateObjectTypeRequest implements UpdateRequest<ObjectType> {

    private List<ObjectType> ObjectTypes = new ArrayList<>();

    @Override
    public List<ObjectType> list() {
        return ObjectTypes;
    }

    public UpdateObjectTypeRequest() {
    }

    public UpdateObjectTypeRequest(Long id, String name) {
        ObjectTypes.add(new ObjectType(id,name));
    }

    public UpdateObjectTypeRequest(List<ObjectType> ObjectTypes) {
        this.ObjectTypes = ObjectTypes;
    }

    public List<ObjectType> getObjectTypes() {
        return ObjectTypes;
    }

    @Override
    public boolean validated() {
        return ObjectTypes.stream().allMatch(ObjectType ->
            ObjectType != null && ObjectType.getName() != null && !ObjectType.getName().trim().isEmpty()
                    && ObjectType.getId() != null && ObjectType.getId() > 0);
    }

    @Override
    public Wrapper<ObjectType> fetchResponseWrapper() {
        return new ObjectTypeWrapper();
    }
}
