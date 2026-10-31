package com.dewarim.cinnamon.model.request.index;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.IndexItem;
import com.dewarim.cinnamon.model.index.IndexType;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.IndexItemWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

@JsonRootName("updateIndexItemRequest")
public record UpdateIndexItemRequest(
        @JacksonXmlElementWrapper(localName = "indexItems")
        @JacksonXmlProperty(localName = "indexItem")
        @NotEmpty List<@Valid IndexItem> indexItems) implements UpdateRequest<IndexItem>, ApiRequest<UpdateIndexItemRequest> {

    public UpdateIndexItemRequest {
        if (indexItems == null) {
            indexItems = new ArrayList<>();
        }
    }

    @Override
    public List<IndexItem> list() {
        return indexItems;
    }

    @Override
    public Wrapper<IndexItem> fetchResponseWrapper() {
        return new IndexItemWrapper();
    }

    @Override
    public List<ApiRequest<UpdateIndexItemRequest>> examples() {
        IndexItem indexItem = new IndexItem("content", true, "headline", "//title", "true()", false, IndexType.DEFAULT_INDEXER);
        indexItem.setId(550L);
        return List.of(new UpdateIndexItemRequest(List.of(indexItem)));
    }
}
