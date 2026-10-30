package com.dewarim.cinnamon.model.request;

import com.dewarim.cinnamon.application.service.RequestValidator;
import com.dewarim.cinnamon.model.response.Wrapper;

import java.util.List;
import java.util.Optional;

public interface CreateRequest<T> {

    List<T> list();

    /**
     * Hand-written validation, for rules that constraint annotations cannot express. Defaults to
     * true: a request whose fields are fully described by annotations does not implement this.
     */
    default boolean validated() {
        return true;
    }

    default Optional<CreateRequest<T>> validateRequest(){
        // annotation-based validation first: it throws INVALID_REQUEST naming the offending
        // fields, which is more than the Optional.empty() below can carry
        RequestValidator.validate(this, CreateRequest.class);
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

     Wrapper<T> fetchResponseWrapper();

}
