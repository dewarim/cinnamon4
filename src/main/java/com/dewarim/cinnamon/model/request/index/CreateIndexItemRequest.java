package com.dewarim.cinnamon.model.request.index;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.IndexItem;
import com.dewarim.cinnamon.model.index.IndexType;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.IndexItemWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JacksonXmlRootElement(localName = "createIndexItemRequest")
public class CreateIndexItemRequest implements CreateRequest<IndexItem>, ApiRequest {

    @JacksonXmlElementWrapper(localName = "indexItems")
    @JacksonXmlProperty(localName = "indexItem")
    private List<IndexItem> indexItems = new ArrayList<>();

    public CreateIndexItemRequest() {
    }

    public CreateIndexItemRequest(List<IndexItem> indexItems) {
        this.indexItems = indexItems;
    }

    public List<IndexItem> getIndexItems() {
        return indexItems;
    }

    public void setIndexItems(List<IndexItem> indexItems) {
        this.indexItems = indexItems;
    }

    @Override
    public List<IndexItem> list() {
        return indexItems;
    }

    @Override
    public boolean validated() {
        if (indexItems == null || indexItems.isEmpty()) {
            return false;
        }
        return indexItems.stream().noneMatch(item ->
                Objects.isNull(item) ||
                        Objects.isNull(item.getName()) ||
                        Objects.isNull(item.getFieldName()) ||
                        Objects.isNull(item.getSearchCondition()) ||
                        Objects.isNull(item.getIndexType()) ||
                        Objects.isNull(item.getSearchString()) ||
                        item.getName().isBlank() ||
                        item.getFieldName().isBlank() ||
                        item.getSearchCondition().isBlank() ||
                        item.getSearchString().isBlank());
    }

    @Override
    public Wrapper<IndexItem> fetchResponseWrapper() {
        return new IndexItemWrapper();
    }

    @Override
    public List<ApiRequest> examples() {
        IndexItem item = new IndexItem("title", true, "Titles",
                "//title/text()","true()", false, IndexType.DEFAULT_INDEXER );
        return List.of(new CreateIndexItemRequest(List.of(item)));
    }
}
