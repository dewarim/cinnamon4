package com.dewarim.cinnamon.api;

import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.request.DeleteRequest;
import com.dewarim.cinnamon.model.request.UpdateRequest;

import java.util.List;

public interface ApiRequest<T> {

    default List<ApiRequest<T>> examples(){
        return List.of(this);
    }

    /**
     * The Bean Validation groups this request is validated with, besides Default. This is the single
     * place that decides whether a constraint scoped {@code groups = CreateRequest.class} applies to a
     * given endpoint: RequestValidator reads it when checking a request, and the api.md generator reads
     * it to decide which constraints to document. Passing the group at the validate() call site instead
     * would leave the generator guessing.
     * <p>
     * The default covers the common case - a request implementing CreateRequest, UpdateRequest or
     * DeleteRequest is validated with that interface as its group. Override it where a request carries a
     * model in create or update shape without implementing the interface (UpdateOsdRequest,
     * UpdateFolderRequest), or needs a group of its own (CreateMetaRequest).
     */
    default Class<?>[] validationGroups() {
        if (this instanceof CreateRequest<?>) {
            return new Class<?>[]{CreateRequest.class};
        }
        if (this instanceof UpdateRequest<?>) {
            return new Class<?>[]{UpdateRequest.class};
        }
        if (this instanceof DeleteRequest<?>) {
            return new Class<?>[]{DeleteRequest.class};
        }
        return new Class<?>[0];
    }

    /**
     * The rules on this request that no constraint annotation expresses, in one sentence, so that
     * api.md can state them. Implemented by every request that declares a {@code validated()} method,
     * and by the few whose extra rule lives in the servlet: a hand-written rule the documentation does
     * not mention is a rule clients find out about by being rejected.
     *
     * @return the rule in prose, or an empty String if the annotations say everything.
     */
    default String additionalRules() {
        return "";
    }
}
