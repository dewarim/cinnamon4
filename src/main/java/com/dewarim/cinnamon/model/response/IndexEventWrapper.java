package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.api.ApiResponse;
import com.dewarim.cinnamon.model.index.IndexEvent;
import com.dewarim.cinnamon.model.index.IndexEventType;
import com.dewarim.cinnamon.model.index.IndexResult;
import com.fasterxml.jackson.annotation.JsonRootName;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.ArrayList;
import java.util.List;

import static com.dewarim.cinnamon.api.Constants.LOCAL_DATE_TIME_EXAMPLE;

@JsonRootName("cinnamon")
public class IndexEventWrapper extends BaseResponse implements Wrapper<IndexEvent>, ApiResponse {

    @JacksonXmlElementWrapper(localName = "indexEvents")
    @JacksonXmlProperty(localName = "indexEvent")
    List<IndexEvent> indexEvents = new ArrayList<>();

    public IndexEventWrapper() {
    }

    public IndexEventWrapper(List<IndexEvent> indexEvents) {
        this.indexEvents = indexEvents;
    }

    public List<IndexEvent> getIndexEvents() {
        return indexEvents;
    }

    public void setIndexEvents(List<IndexEvent> indexEvents) {
        this.indexEvents = indexEvents;
    }

    @Override
    public List<IndexEvent> list() {
        return getIndexEvents();
    }

    @Override
    public Wrapper<IndexEvent> setList(List<IndexEvent> indexEvents) {
        setIndexEvents(indexEvents);
        return this;
    }

    @Override
    public List<Object> examples() {
        IndexEvent event = new IndexEvent(143L, IndexEventType.LUCENE, IndexResult.ERROR,
                "Cannot index document: field is too large");
        event.setId(21L);
        // the example has to be the same on every build, so no LocalDateTime.now() here:
        event.setLocalTime(LOCAL_DATE_TIME_EXAMPLE);
        return List.of(new IndexEventWrapper(List.of(event)));
    }
}
