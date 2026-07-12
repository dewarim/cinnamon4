package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.api.ApiResponse;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

@JsonRootName("cinnamon")
public class SummaryWrapper extends BaseResponse implements Wrapper<Summary>, ApiResponse {

    @JacksonXmlElementWrapper(localName = "summaries")
    @JacksonXmlProperty(localName = "summary")
    List<Summary> summaries = new ArrayList<>();

    public List<Summary> getSummaries() {
        return summaries;
    }

    public void setSummaries(List<Summary> summaries) {
        this.summaries = summaries;
    }

    @Override
    public List<Summary> list() {
        return summaries;
    }

    @Override
    public Wrapper<Summary> setList(List<Summary> summaries) {
        this.summaries = summaries;
        return this;
    }

    @Override
    public List<Object> examples() {
        SummaryWrapper wrapper = new SummaryWrapper();
        Summary summary = new Summary(5L, "<summary> a summary of an OSD's content </summary>");
        wrapper.getSummaries().add(summary);
        return List.of(wrapper);
    }
}
