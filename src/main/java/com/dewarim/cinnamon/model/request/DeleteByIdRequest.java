package com.dewarim.cinnamon.model.request;

import java.util.List;
import java.util.Set;

/**
 * Shared behaviour for the "delete a set of objects by id" requests. Implemented by the
 * {@code Delete*Request} records, which supply the {@link #ids()} accessor via a record component
 * (annotated with the {@code @JacksonXmlElementWrapper}/{@code @JacksonXmlProperty} wire mapping).
 */
public interface DeleteByIdRequest<T> extends DeleteRequest<T> {

    Set<Long> ids();

    @Override
    default List<Long> list() {
        return ids().stream().toList();
    }

    @Override
    default boolean validated() {
        return ids() != null && !ids().isEmpty() && ids().stream().allMatch(id -> id != null && id > 0);
    }
}
