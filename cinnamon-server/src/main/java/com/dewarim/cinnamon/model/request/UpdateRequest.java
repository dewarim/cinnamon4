package com.dewarim.cinnamon.model.request;

import com.dewarim.cinnamon.model.response.Wrapper;

import java.util.List;
import java.util.Optional;

public interface UpdateRequest<T> {

    List<T> list();

    boolean validated();

    default Optional<UpdateRequest<T>> validateRequest(){
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    Wrapper<T> fetchResponseWrapper();

}
