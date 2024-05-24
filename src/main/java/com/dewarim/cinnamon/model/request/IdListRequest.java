package com.dewarim.cinnamon.model.request;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@JacksonXmlRootElement(localName = "idListRequest")
public class IdListRequest implements ApiRequest<IdListRequest> {

    @JacksonXmlElementWrapper(localName = "ids")
    @JacksonXmlProperty(localName = "id")
    private Set<Long> ids = new HashSet<>();

    public IdListRequest() {
    }

    public boolean validated(){
        if(ids == null){
            return false;
        }
        return ids.stream().allMatch(id -> id != null && id > 0);
    }

    public Optional<IdListRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    public IdListRequest(List<Long> ids) {
        this.ids = new HashSet<>(ids);
    }

    public Set<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        if(ids == null){
            this.ids = new HashSet<>();
        }
        else {
            this.ids = new HashSet<>(ids);
        }
    }

    @Override
    public List<ApiRequest<IdListRequest>> examples() {
        return List.of(new IdListRequest(List.of(1L,44L,5L)));
    }
}
