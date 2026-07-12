package com.dewarim.cinnamon.model.request.index;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.IndexItem;
import com.dewarim.cinnamon.model.index.IndexType;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.IndexItemWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonRootName("createIndexItemRequest")
public record CreateIndexItemRequest(
        @JacksonXmlElementWrapper(localName = "indexItems")
        @JacksonXmlProperty(localName = "indexItem")
        List<IndexItem> indexItems) implements CreateRequest<IndexItem>, ApiRequest<CreateIndexItemRequest> {

    public CreateIndexItemRequest {
        if (indexItems == null) {
            indexItems = new ArrayList<>();
        }
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
    public List<ApiRequest<CreateIndexItemRequest>> examples() {
        IndexItem item = new IndexItem("title", true, "Titles",
                "//title/text()", "true()", false, IndexType.DEFAULT_INDEXER);
        return List.of(new CreateIndexItemRequest(List.of(item)));
    }
}
