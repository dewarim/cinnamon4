package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.api.ApiResponse;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "cinnamon")
public class SummaryWrapper implements Wrapper<Summary>, ApiResponse {

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
}
