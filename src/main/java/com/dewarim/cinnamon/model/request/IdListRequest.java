package com.dewarim.cinnamon.model.request;

import com.dewarim.cinnamon.api.ApiRequest;

import java.util.List;

public class IdListRequest implements ApiRequest {
    
    private List<Long> idList;

    public IdListRequest() {
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
}
