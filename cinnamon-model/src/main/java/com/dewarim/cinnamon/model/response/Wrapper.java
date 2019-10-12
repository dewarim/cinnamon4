package com.dewarim.cinnamon.model.response;

public interface Wrapper {

    default String contentType() {
        return "application/xml";
    }

}
