package com.dewarim.cinnamon.model.request.osd;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "osdRequest")
public class OsdRequest implements ApiRequest {

    private List<Long> ids = new ArrayList<>();

    private boolean includeSummary;
    private boolean includeCustomMetadata;

    public OsdRequest() {
    }

    public OsdRequest(List<Long> ids, boolean includeSummary, boolean includeCustomMetadata) {
        this.includeSummary = includeSummary;
        this.includeCustomMetadata = includeCustomMetadata;
        this.ids = ids;
    }

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    public boolean isIncludeSummary() {
        return includeSummary;
    }

    public void setIncludeSummary(boolean includeSummary) {
        this.includeSummary = includeSummary;
    }

    public boolean isIncludeCustomMetadata() {
        return includeCustomMetadata;
    }

    public void setIncludeCustomMetadata(boolean includeCustomMetadata) {
        this.includeCustomMetadata = includeCustomMetadata;
    }

    @Override
    public String toString() {
        return "OsdRequest{" +
                "ids=" + ids +
                ", includeSummary=" + includeSummary +
                ", includeCustomMetadata=" + includeCustomMetadata +
                '}';
    }
}
