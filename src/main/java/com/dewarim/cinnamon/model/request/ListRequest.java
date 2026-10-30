package com.dewarim.cinnamon.model.request;

import com.dewarim.cinnamon.application.service.RequestValidator;
import com.dewarim.cinnamon.model.response.Wrapper;

import java.util.Optional;

/**
 * A simple list request. Currently, this class is empty, but should be used to avoid
 * sending empty POST requests to the server.
 * <p>
 * Future versions may include filter fields (for example, String nameFilter).
 */
public interface ListRequest<T> {

    default Optional<ListRequest<T>> validateRequest() {
        // there is nothing to check on a list request yet; a future filter field gets validated
        // by annotation, like every other request
        RequestValidator.validate(this);
        return Optional.of(this);
    }

    Wrapper<T> fetchResponseWrapper();

}
