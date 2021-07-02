package com.dewarim.cinnamon.model.request.acl;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.AclWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JacksonXmlRootElement(localName = "createAclRequest")
public class CreateAclRequest implements CreateRequest<Acl>, ApiRequest {

    private List<String> names = new ArrayList<>();

    @Override
    public List<Acl> list() {
        return names.stream().map(name -> new Acl(null, name)).collect(Collectors.toList());
    }

    public CreateAclRequest() {
    }

    public CreateAclRequest(String name) {
        names.add(name);
    }

    public CreateAclRequest(List<String> names) {
        this.names = names;
    }

    public List<String> getNames() {
        return names;
    }

    @Override
    public boolean validated() {
        return names.stream().noneMatch(name ->
                name == null || name.trim().isEmpty());
    }

    @Override
    public Wrapper<Acl> fetchResponseWrapper() {
        return new AclWrapper();
    }
}
