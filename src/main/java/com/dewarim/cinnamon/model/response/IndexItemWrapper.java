package com.dewarim.cinnamon.model.response;


import com.dewarim.cinnamon.api.ApiResponse;
import com.dewarim.cinnamon.model.IndexItem;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "cinnamon")
public class IndexItemWrapper implements Wrapper<IndexItem>, ApiResponse {

    @JacksonXmlElementWrapper(localName = "indexItems")
    @JacksonXmlProperty(localName = "indexItem")
    List<IndexItem> indexItems = new ArrayList<>();

    public List<IndexItem> getIndexItems() {
        return indexItems;
    }

    public void setIndexItems(List<IndexItem> indexItems) {
        this.indexItems = indexItems;
    }

    @Override
    public List<IndexItem> list() {
        return getIndexItems();
    }

    @Override
    public Wrapper<IndexItem> setList(List<IndexItem> indexItems) {
        setIndexItems(indexItems);
        return this;
    }
}
