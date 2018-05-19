package com.dewarim.cinnamon.model.request;

import java.util.List;

public class IdListRequest {
    
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
