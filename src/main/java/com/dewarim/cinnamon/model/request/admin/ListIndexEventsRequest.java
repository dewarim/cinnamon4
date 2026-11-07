package com.dewarim.cinnamon.model.request.admin;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.application.service.RequestValidator;
import com.dewarim.cinnamon.model.index.IndexEventType;
import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;
import java.util.Optional;

/**
 * Ask for the newest rows of the index_events table: what went wrong while indexing, with the
 * message Lucene or the indexer produced.
 * <p>
 * The limit is not optional in the sense that leaving it out means "everything": the rows carry
 * exception messages, and the ones written for an uncaught exception in the index loop carry a whole
 * stack trace, so an unbounded list is a request to render the server's error history into a single
 * XML document. Leaving it out means {@link #DEFAULT_LIMIT}.
 *
 * @param limit     how many rows to return at most, newest first; defaults to {@link #DEFAULT_LIMIT}
 *                  and cannot exceed {@link #MAX_LIMIT}
 * @param eventType return only events of this type, or every type when it is left out. LUCENE is
 *                  Lucene refusing the document, GENERIC a single item the indexer could not handle,
 *                  ERROR an uncaught exception in the index loop, and IO content that could not be
 *                  read at all. A failed Tika extraction is none of these: it is a row in
 *                  tika_failures, reported by admin/listFailedTikaExtractions.
 */
@JsonRootName("listIndexEventsRequest")
public record ListIndexEventsRequest(
        @Min(1) @Max(ListIndexEventsRequest.MAX_LIMIT) Integer limit,
        IndexEventType eventType
) implements ApiRequest<ListIndexEventsRequest> {

    public static final int DEFAULT_LIMIT = 100;
    public static final int MAX_LIMIT     = 1000;

    public ListIndexEventsRequest {
        if (limit == null) {
            limit = DEFAULT_LIMIT;
        }
    }

    /**
     * Looks unused, and is: it is called reflectively, by the api.md generator and by
     * ApiDocConstraintsTest, which prefer a no-argument constructor over the canonical one.
     */
    public ListIndexEventsRequest() {
        this(DEFAULT_LIMIT, null);
    }

    public Optional<ListIndexEventsRequest> validateRequest() {
        RequestValidator.validate(this);
        return Optional.of(this);
    }

    @Override
    public List<ApiRequest<ListIndexEventsRequest>> examples() {
        return List.of(new ListIndexEventsRequest(50, IndexEventType.LUCENE));
    }
}
