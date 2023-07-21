package com.dewarim.cinnamon.model.request;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;
import java.util.Optional;

@JacksonXmlRootElement(localName = "idListRequest")
public class IdListRequest implements ApiRequest<IdListRequest> {

    @JacksonXmlElementWrapper(localName = "ids")
    @JacksonXmlProperty(localName = "id")
    private List<Long> ids;

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
        this.ids = ids;
    }

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    @Override
    public List<ApiRequest<IdListRequest>> examples() {
        return List.of(new IdListRequest(List.of(1L,44L,5L)));
    }
}
