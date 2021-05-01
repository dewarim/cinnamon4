package com.dewarim.cinnamon.model.request;

import com.dewarim.cinnamon.model.response.Wrapper;

import java.util.Optional;

/**
 * A simple list request. Currently this class is empty, but should be used to avoid
 * sending empty POST requests to the server.
 * <p>
 * Future versions may include filter fields (for example, String nameFilter).
 */
public interface ListRequest<T> {

    default boolean validated() {
        // not much to validate about a simple list request at the moment
        return true;
    }

    default Optional<ListRequest<T>> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    Wrapper<T> fetchResponseWrapper();

}
