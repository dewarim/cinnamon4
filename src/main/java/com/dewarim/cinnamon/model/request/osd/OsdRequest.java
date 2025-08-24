package com.dewarim.cinnamon.model.request.osd;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "osdRequest")
public class OsdRequest implements ApiRequest<OsdRequest> {

    @JacksonXmlElementWrapper(localName = "ids")
    @JacksonXmlProperty(localName = "id")
    private List<Long> ids = new ArrayList<>();

    private boolean includeSummary;
    private boolean includeCustomMetadata;
    private boolean addFolderPath;

    public OsdRequest() {
    }

    public OsdRequest(List<Long> ids, boolean includeSummary, boolean includeCustomMetadata) {
        this.includeSummary = includeSummary;
        this.includeCustomMetadata = includeCustomMetadata;
        this.ids = ids;
    }

    public boolean validated(){
        return ids != null && !ids.isEmpty() && ids.stream().allMatch(id -> id != null && id > 0);
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

    public boolean isAddFolderPath() {
        return addFolderPath;
    }

    public void setAddFolderPath(boolean addFolderPath) {
        this.addFolderPath = addFolderPath;
    }

    @Override
    public String toString() {
        return "OsdRequest{" +
                "ids=" + ids +
                ", includeSummary=" + includeSummary +
                ", includeCustomMetadata=" + includeCustomMetadata +
                '}';
    }



    @Override
    public List<ApiRequest<OsdRequest>> examples() {
        OsdRequest osdRequest = new OsdRequest(List.of(45L, 23L, 2L), true, true);
        osdRequest.setAddFolderPath(true);
        return List.of(osdRequest);
    }
}
