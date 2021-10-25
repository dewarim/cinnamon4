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
import java.util.stream.Collectors;

@JacksonXmlRootElement(localName = "createObjectTypeRequest")
public class CreateObjectTypeRequest implements CreateRequest<ObjectType>, ApiRequest {

    @JacksonXmlElementWrapper(localName = "names")
    @JacksonXmlProperty(localName = "name")
    private List<String> names = new ArrayList<>();

    @Override
    public List<ObjectType> list() {
        return names.stream().map(name -> new ObjectType(null, name)).collect(Collectors.toList());
    }

    public CreateObjectTypeRequest() {
    }

    public CreateObjectTypeRequest(List<String> names) {
        this.names = names;
    }

    public List<String> getNames() {
        return names;
    }

    @Override
    public boolean validated() {
        return names.stream().noneMatch(name -> name == null || name.trim().isEmpty());
    }

    @Override
    public Wrapper<ObjectType> fetchResponseWrapper() {
        return new ObjectTypeWrapper();
    }

    @Override
    public List<Object> examples() {
        return List.of(new CreateObjectTypeRequest(List.of("default type", "other type")));
    }

}
