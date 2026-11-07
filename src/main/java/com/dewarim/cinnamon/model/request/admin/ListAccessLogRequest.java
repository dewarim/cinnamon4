package com.dewarim.cinnamon.model.request.admin;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.application.service.RequestValidator;
import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;
import java.util.Optional;

/**
 * Ask for the newest rows of the access log: the requests that failed, with the error code, the
 * request body as it arrived and the error response that was sent back.
 * <p>
 * The limit is lower than the one for index events on purpose - every row holds a whole request and
 * a whole response body, so a hundred rows can be megabytes. The log keeps the newest
 * loggingConfig.truncateLogAfterRow rows and prunes the rest, so there is no point in asking for
 * more than that either.
 *
 * @param limit how many rows to return at most, newest first; defaults to {@link #DEFAULT_LIMIT} and
 *              cannot exceed {@link #MAX_LIMIT}
 */
@JsonRootName("listAccessLogRequest")
public record ListAccessLogRequest(
        @Min(1) @Max(ListAccessLogRequest.MAX_LIMIT) Integer limit
) implements ApiRequest<ListAccessLogRequest> {

    public static final int DEFAULT_LIMIT = 50;
    public static final int MAX_LIMIT     = 500;

    public ListAccessLogRequest {
        if (limit == null) {
            limit = DEFAULT_LIMIT;
        }
    }

    /**
     * Looks unused, and is: it is called reflectively, by the api.md generator and by
     * ApiDocConstraintsTest, which prefer a no-argument constructor over the canonical one.
     */
    public ListAccessLogRequest() {
        this(DEFAULT_LIMIT);
    }

    public Optional<ListAccessLogRequest> validateRequest() {
        RequestValidator.validate(this);
        return Optional.of(this);
    }

    @Override
    public List<ApiRequest<ListAccessLogRequest>> examples() {
        return List.of(new ListAccessLogRequest(20));
    }
}
