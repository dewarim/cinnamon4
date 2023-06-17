package com.dewarim.cinnamon.model.request.index;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.IndexItem;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.IndexItemWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JacksonXmlRootElement(localName = "updateIndexItemRequest")
public class UpdateIndexItemRequest implements UpdateRequest<IndexItem>, ApiRequest {

    @JacksonXmlElementWrapper(localName = "indexItems")
    @JacksonXmlProperty(localName = "indexItem")
    private List<IndexItem> indexItems = new ArrayList<>();

    @Override
    public List<IndexItem> list() {
        return indexItems;
    }

    public UpdateIndexItemRequest() {
    }

    public UpdateIndexItemRequest(List<IndexItem> indexItems) {
        this.indexItems = indexItems;
    }

    public List<IndexItem> getIndexItems() {
        return indexItems;
    }

    @Override
    public boolean validated() {
        if(indexItems == null || indexItems.isEmpty()){
            return false;
        }
        return indexItems.stream().noneMatch(item ->
                Objects.isNull(item) ||
                        Objects.isNull(item.getId()) ||
                        Objects.isNull(item.getName()) ||
                        Objects.isNull(item.getFieldName()) ||
                        Objects.isNull(item.getSearchCondition()) ||
                        Objects.isNull(item.getIndexType()) ||
                        Objects.isNull(item.getSearchString()) ||
                        item.getId() < 1 ||
                        item.getName().isBlank() ||
                        item.getFieldName().isBlank() ||
                        item.getSearchCondition().isBlank() ||
                        item.getSearchString().isBlank());
    }

    @Override
    public Wrapper<IndexItem> fetchResponseWrapper() {
        return new IndexItemWrapper();
    }
}
