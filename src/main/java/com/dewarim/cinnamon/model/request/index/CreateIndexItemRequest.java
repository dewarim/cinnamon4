package com.dewarim.cinnamon.model.request.index;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.IndexItem;
import com.dewarim.cinnamon.model.index.IndexType;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.IndexItemWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

@JsonRootName("createIndexItemRequest")
public record CreateIndexItemRequest(
        @JacksonXmlElementWrapper(localName = "indexItems")
        @JacksonXmlProperty(localName = "indexItem")
        @NotEmpty List<@Valid IndexItem> indexItems) implements CreateRequest<IndexItem>, ApiRequest<CreateIndexItemRequest> {

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
