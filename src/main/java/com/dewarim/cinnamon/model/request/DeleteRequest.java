package com.dewarim.cinnamon.model.request;

import com.dewarim.cinnamon.model.response.DeleteResponse;
import com.dewarim.cinnamon.model.response.Wrapper;

import java.util.List;
import java.util.Optional;

public interface DeleteRequest<T> {

    List<Long> list();

    boolean validated();

    default Optional<DeleteRequest<T>> validateRequest(){
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

     default Wrapper<DeleteResponse> fetchResponseWrapper(){
         // at the moment, you get either a successful DeleteResponse or an ErrorResponse.
         return new DeleteResponse(true);
     }

     boolean isIgnoreNotFound();
}
