package com.dewarim.cinnamon.model.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "cinnamon")
public class SummaryWrapper implements Wrapper {

    @JacksonXmlElementWrapper(localName = "summaries")
    @JacksonXmlProperty(localName = "summary")
    List<String> summaries = new ArrayList<>();

    public void setSummaries(List<String> summaries) {
        this.summaries = summaries;
    }

    public List<String> getSummaries() {
        return summaries;
    }
}
