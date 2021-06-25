package com.dewarim.cinnamon.model.request;

import java.util.ArrayList;
import java.util.List;

public abstract class DeleteByIdRequest<T> implements DeleteRequest<T>{

    private boolean ignoreNotFound =false;

    private List<Long> ids = new ArrayList<>();
    @Override
    public List<Long> list() {
        return ids;
    }

    public DeleteByIdRequest() {
    }

    public DeleteByIdRequest(List<Long> ids) {
        this.ids = ids;
    }

    public DeleteByIdRequest(Long id){
        ids.add(id);
    }

    @Override
    public boolean validated() {
        return ids.stream().allMatch(id -> id != null && id > 0 );
    }

    public List<Long> getIds() {
        return ids;
    }

    public boolean isIgnoreNotFound() {
        return ignoreNotFound;
    }

    public void setIgnoreNotFound(boolean ignoreNotFound) {
        this.ignoreNotFound = ignoreNotFound;
    }
}
