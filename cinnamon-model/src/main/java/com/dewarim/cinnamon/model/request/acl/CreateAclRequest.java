package com.dewarim.cinnamon.model.request.acl;

import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.AclWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class CreateAclRequest implements CreateRequest<Acl> {

    private List<String> names = new ArrayList<>();

    @Override
    public List<Acl> list() {
        return names.stream().map(name -> new Acl(null, name)).collect(Collectors.toList());
    }

    public CreateAclRequest() {
    }

    public CreateAclRequest(List<String> names) {
        this.names = names;
    }

    public List<String> getNames() {
        return names;
    }

    @Override
    public boolean validated() {
        AtomicBoolean valid = new AtomicBoolean(true);
        names.forEach(name -> {
            if (name == null || name.trim().isEmpty()) {
//                ErrorCode.NAME_PARAM_IS_INVALID.throwUp();
                valid.set(false);
            }
        });

        return valid.get();
    }

    @Override
    public Wrapper<Acl> fetchResponseWrapper() {
        return new AclWrapper();
    }
}
