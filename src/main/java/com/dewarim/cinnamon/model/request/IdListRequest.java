package com.dewarim.cinnamon.model.request;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;
import java.util.Optional;

@JacksonXmlRootElement(localName = "idListRequest")
public class IdListRequest implements ApiRequest<IdListRequest> {
    
    private List<Long> idList;

    public IdListRequest() {
    }

    public boolean validated(){
        if(idList == null){
            return false;
        }
        return idList.stream().allMatch(id -> id != null && id > 0);
    }

    public Optional<IdListRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    public IdListRequest(List<Long> idList) {
        this.idList = idList;
    }

    public List<Long> getIdList() {
        return idList;
    }

    public void setIdList(List<Long> idList) {
        this.idList = idList;
    }

    @Override
    public List<ApiRequest<IdListRequest>> examples() {
        return List.of(new IdListRequest(List.of(1L,44L,5L)));
    }
}
