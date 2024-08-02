package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.model.request.index.ReindexRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.dewarim.cinnamon.ErrorCode.INVALID_REQUEST;
import static com.dewarim.cinnamon.ErrorCode.REQUIRES_SUPERUSER_STATUS;

/**
 * Note: actual implementation tests moved to ManualTest since they require a separate process running
 */
public class IndexAndSearchServletIntegrationTest extends CinnamonIntegrationTest {

    @Test
    public void reindexIsForbiddenForNonAdmins() {
        assertClientError(() -> client.reindex(new ReindexRequest()), REQUIRES_SUPERUSER_STATUS);
    }

    @Test
    public void reindexInvalidRequest() {
        assertClientError(() -> adminClient.reindex(new ReindexRequest(List.of(-1L), List.of())), INVALID_REQUEST);
    }

}
