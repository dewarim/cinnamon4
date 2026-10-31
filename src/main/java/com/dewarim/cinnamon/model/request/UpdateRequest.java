package com.dewarim.cinnamon.model.request;

import com.dewarim.cinnamon.application.service.RequestValidator;
import com.dewarim.cinnamon.model.response.Wrapper;

import java.util.List;
import java.util.Optional;

/**
 * Also a Bean Validation group: a constraint annotated {@code groups = UpdateRequest.class} on a shared
 * model class is only checked when that model arrives in an update request. Groups are plain
 * interfaces, so this interface can serve as its own marker - see FolderType for an example.
 */
public interface UpdateRequest<T> {

    List<T> list();

    /**
     * Hand-written validation, for rules that constraint annotations cannot express. Defaults to
     * true: a request whose fields are fully described by annotations does not implement this.
     */
    default boolean validated() {
        return true;
    }

    default Optional<UpdateRequest<T>> validateRequest(){
        // annotation-based validation first: it throws INVALID_REQUEST naming the offending
        // fields, which is more than the Optional.empty() below can carry. The group this interface
        // stands for is declared by ApiRequest.validationGroups(), not passed in here.
        RequestValidator.validate(this);
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    Wrapper<T> fetchResponseWrapper();

}
