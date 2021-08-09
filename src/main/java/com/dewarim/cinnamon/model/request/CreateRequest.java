package com.dewarim.cinnamon.model.request;

import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;
import java.util.Optional;

public interface CreateRequest<T> {

    List<T> list();

    boolean validated();

    default Optional<CreateRequest<T>> validateRequest(){
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

     Wrapper<T> fetchResponseWrapper();

}
