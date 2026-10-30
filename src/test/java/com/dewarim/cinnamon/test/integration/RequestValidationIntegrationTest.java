package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.client.CinnamonClientException;
import com.dewarim.cinnamon.model.Meta;
import com.dewarim.cinnamon.model.request.osd.CreateOsdRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SPIKE: annotation-based request validation. Shows the two things the hand-written validated()
 * methods could not do - reject an empty collection by default, and say which field was wrong.
 */
public class RequestValidationIntegrationTest extends CinnamonIntegrationTest {

    private String messageOf(CinnamonClientException ex) {
        return ex.getErrorWrapper().getErrors().getFirst().getMessage();
    }

    @Test
    public void emptyIdListIsNowRejected() {
        /*
         * IdListRequest.validated() used ids.stream().allMatch(...), and allMatch on an empty
         * collection is vacuously true - so an empty request passed validation and was handed to the
         * servlet. @NotEmpty says what was meant.
         */
        CinnamonClientException ex = assertThrows(CinnamonClientException.class, () -> client.lockOsd(List.of()));
        assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
        String message = messageOf(ex);
        System.out.println("empty id list -> " + message);
        assertTrue(message.contains("ids"), "message should name the field: " + message);
    }

    @Test
    public void everyBadFieldIsNamedAtOnce() {
        // blank name, missing parentId, non-positive ownerId: one request, three violations
        CreateOsdRequest request = new CreateOsdRequest("   ", null, 0L, 1L, 1L, null, 1L, null, "<sum/>");
        CinnamonClientException ex = assertThrows(CinnamonClientException.class, () -> client.createOsd(request));
        assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
        String message = messageOf(ex);
        System.out.println("bad createOsd -> " + message);
        assertTrue(message.contains("name"), "should name 'name': " + message);
        assertTrue(message.contains("parentId"), "should name 'parentId': " + message);
        assertTrue(message.contains("ownerId"), "should name 'ownerId': " + message);
    }

    @Test
    public void nestedMetasetViolationIsReportedWithItsIndex() {
        // the @Valid cascade reaches into the list and reports metasets[0].typeId
        CreateOsdRequest request = new CreateOsdRequest("osd-with-broken-meta", createFolderId, userId, 1L, 1L,
                null, 1L, null, "<sum/>", List.of(new Meta(null, null, "<xml/>")));
        CinnamonClientException ex = assertThrows(CinnamonClientException.class, () -> client.createOsd(request));
        assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
        String message = messageOf(ex);
        System.out.println("broken metaset -> " + message);
        assertTrue(message.contains("metasets[0].typeId"), "should name the element: " + message);
    }

    @Test
    public void validRequestStillPasses() throws Exception {
        // the happy path must be untouched
        var osd = client.createOsd(new CreateOsdRequest("spike-valid-osd", createFolderId, userId, 1L, 1L,
                null, 1L, null, "<sum/>"));
        assertEquals("spike-valid-osd", osd.getName());
    }
}
