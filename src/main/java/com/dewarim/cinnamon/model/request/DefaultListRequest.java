package com.dewarim.cinnamon.model.request;


import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

public class DefaultListRequest implements ApiRequest {

    private ListType type = ListType.FULL;

    public ListType getType() {
        return type;
    }

    public void setType(ListType type) {
        this.type = type;
    }
}
