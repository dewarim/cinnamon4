package com.dewarim.cinnamon.model.response.index;

import com.dewarim.cinnamon.api.ApiResponse;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "indexInfoResponse")
public class IndexInfoResponse implements ApiResponse {

    private Integer documentsInIndex;

    public IndexInfoResponse() {
    }

    public IndexInfoResponse(Integer documentsInIndex) {
        this.documentsInIndex = documentsInIndex;
    }

    public Integer getDocumentsInIndex() {
        return documentsInIndex;
    }

    public void setDocumentsInIndex(Integer documentsInIndex) {
        this.documentsInIndex = documentsInIndex;
    }
}
