package com.dewarim.cinnamon.model.request.index;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "indexInfoRequest")

public class IndexInfoRequest implements ApiRequest {
    private boolean countDocuments = true;

    public IndexInfoRequest() {
    }

    public IndexInfoRequest(boolean countDocuments) {
        this.countDocuments = countDocuments;
    }

    public boolean isCountDocuments() {
        return countDocuments;
    }

    public void setCountDocuments(boolean countDocuments) {
        this.countDocuments = countDocuments;
    }

    @Override
    public String toString() {
        return "InfoRequest{" +
                "countDocuments=" + countDocuments +
                '}';
    }
}
