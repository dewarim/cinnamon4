package com.dewarim.cinnamon.model.request;

/**
 * Marker for the simple "list all of a resource" requests. Provides the {@link ListType} selector
 * (FULL vs. summary), supplied as a record component by the implementing {@code List*Request} records.
 */
public interface DefaultListRequest {

    ListType type();
}
