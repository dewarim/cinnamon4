package com.dewarim.cinnamon.model.request.admin;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.application.service.RequestValidator;
import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;
import java.util.Optional;

/**
 * Ask for the objects whose Tika extraction failed: one row per object, with the number of attempts
 * made and the last error.
 * <p>
 * The same rows that index/info returns for listFailedTikaExtractions, which keeps that flag; this
 * endpoint exists so that an operator asking "why is this document not searchable" gets both halves
 * of the answer - the index events and the Tika failures - from one place and under one permission
 * rule, and so that the limit is the caller's to choose rather than fixed at the thousand rows
 * index/info returns.
 *
 * @param limit how many rows to return at most, newest failure first; defaults to
 *              {@link #DEFAULT_LIMIT} and cannot exceed {@link #MAX_LIMIT}
 */
@JsonRootName("listFailedTikaExtractionsRequest")
public record ListFailedTikaExtractionsRequest(
        @Min(1) @Max(ListFailedTikaExtractionsRequest.MAX_LIMIT) Integer limit
) implements ApiRequest<ListFailedTikaExtractionsRequest> {

    public static final int DEFAULT_LIMIT = 100;
    public static final int MAX_LIMIT     = 1000;

    public ListFailedTikaExtractionsRequest {
        if (limit == null) {
            limit = DEFAULT_LIMIT;
        }
    }

    /**
     * Looks unused, and is: it is called reflectively, by the api.md generator and by
     * ApiDocConstraintsTest, which prefer a no-argument constructor over the canonical one.
     */
    public ListFailedTikaExtractionsRequest() {
        this(DEFAULT_LIMIT);
    }

    public Optional<ListFailedTikaExtractionsRequest> validateRequest() {
        RequestValidator.validate(this);
        return Optional.of(this);
    }

    @Override
    public List<ApiRequest<ListFailedTikaExtractionsRequest>> examples() {
        return List.of(new ListFailedTikaExtractionsRequest(20));
    }
}
