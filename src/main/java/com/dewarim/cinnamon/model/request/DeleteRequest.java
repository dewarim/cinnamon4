package com.dewarim.cinnamon.model.request;

import com.dewarim.cinnamon.model.response.DeleteResponse;
import com.dewarim.cinnamon.application.service.RequestValidator;
import com.dewarim.cinnamon.model.response.Wrapper;

import java.util.List;
import java.util.Optional;

public interface DeleteRequest<T> {

    /**
     * @return a list of unique ids
     */
    List<Long> list();

    /**
     * Hand-written validation, for rules that constraint annotations cannot express. Defaults to
     * true: a request whose fields are fully described by annotations does not implement this.
     */
    default boolean validated() {
        return true;
    }

    default Optional<DeleteRequest<T>> validateRequest(){
        // annotation-based validation first: it throws INVALID_REQUEST naming the offending
        // fields, which is more than the Optional.empty() below can carry
        RequestValidator.validate(this, DeleteRequest.class);
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

     boolean ignoreNotFound();
}
