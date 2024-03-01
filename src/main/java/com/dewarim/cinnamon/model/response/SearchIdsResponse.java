package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.api.ApiResponse;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "searchIdsResponse")
public class SearchIdsResponse implements ApiResponse {

    @JacksonXmlElementWrapper(localName = "osdIds")
    @JacksonXmlProperty(localName = "osdId")
    private List<Long> osdIds;

    @JacksonXmlElementWrapper(localName = "folderIds")
    @JacksonXmlProperty(localName = "folderId")
    private List<Long> folderIds;

    public SearchIdsResponse() {
    }

    public SearchIdsResponse(List<Long> osdIds, List<Long> folderIds) {
        this.osdIds = osdIds;
        this.folderIds = folderIds;
    }

    public List<Long> getOsdIds() {
        if (osdIds == null){
            osdIds = new ArrayList<>();
        }
        return osdIds;
    }

    public void setOsdIds(List<Long> osdIds) {
        this.osdIds = osdIds;
    }

    public List<Long> getFolderIds() {
        if(folderIds == null){
            folderIds=new ArrayList<>();
        }
        return folderIds;
    }

    public void setFolderIds(List<Long> folderIds) {
        this.folderIds = folderIds;
    }

    @Override
    public List<Object> examples() {
        return List.of(new SearchIdsResponse(List.of(1L,32L), List.of(100L, 200L)));
    }

    @Override
    public String toString() {
        return "SearchIdsResponse{" +
                "osdIds=" + osdIds +
                ", folderIds=" + folderIds +
                '}';
    }

}
