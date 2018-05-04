package com.dewarim.cinnamon.model.response;


import com.dewarim.cinnamon.model.IndexItem;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "cinnamon")
public class IndexItemWrapper {

    @JacksonXmlElementWrapper(localName = "indexItems")
    @JacksonXmlProperty(localName = "indexItem")
    List<IndexItem> indexItems = new ArrayList<>();

    public List<IndexItem> getIndexItems() {
        return indexItems;
    }

    public void setIndexItems(List<IndexItem> indexItems) {
        this.indexItems = indexItems;
    }
}
