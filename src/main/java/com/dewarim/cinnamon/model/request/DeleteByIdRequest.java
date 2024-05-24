package com.dewarim.cinnamon.model.request;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class DeleteByIdRequest<T> implements DeleteRequest<T> {

    private boolean ignoreNotFound = false;

    @JacksonXmlElementWrapper(localName = "ids")
    @JacksonXmlProperty(localName = "id")
    private Set<Long> ids = new HashSet<>();

    public DeleteByIdRequest(List<Long> ids) {
        this.ids = new HashSet<>(ids);
    }

    @Override
    public List<Long> list() {
        return ids.stream().toList();
    }

    public DeleteByIdRequest() {
    }

    public DeleteByIdRequest(Set<Long> ids) {
        this.ids = ids;
    }

    public DeleteByIdRequest(Long id) {
        ids.add(id);
    }

    public DeleteByIdRequest(Long id, boolean ignoreNotFound) {
        ids.add(id);
        this.ignoreNotFound = ignoreNotFound;
    }

    @Override
    public boolean validated() {
        return ids != null && ids.stream().allMatch(id -> id != null && id > 0);
    }

    public Set<Long> getIds() {
        return ids;
    }

    public boolean isIgnoreNotFound() {
        return ignoreNotFound;
    }

    public void setIgnoreNotFound(boolean ignoreNotFound) {
        this.ignoreNotFound = ignoreNotFound;
    }

    @Override
    public String toString() {
        return "DeleteByIdRequest{" +
                "ignoreNotFound=" + ignoreNotFound +
                ", ids=" + ids +
                '}';
    }
}
