package com.dewarim.cinnamon.model.request;

import java.util.List;
import java.util.Set;

/**
 * Shared behaviour for the "delete a set of objects by id" requests. Implemented by the
 * {@code Delete*Request} records, which supply the {@link #ids()} accessor via a record component
 * (annotated with the {@code @JacksonXmlElementWrapper}/{@code @JacksonXmlProperty} wire mapping,
 * and with the {@code @NotEmpty Set<@NotNull @Positive Long>} constraints that replace the
 * hand-written check this interface used to carry - a constraint on an interface accessor would
 * not be picked up, because {@code ids()} is not a JavaBeans getter).
 */
public interface DeleteByIdRequest<T> extends DeleteRequest<T> {

    Set<Long> ids();

    @Override
    default List<Long> list() {
        return ids().stream().toList();
    }
}
