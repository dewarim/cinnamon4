package com.dewarim.cinnamon.test.unit;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.FailedRequestException;
import com.dewarim.cinnamon.model.links.Link;
import com.dewarim.cinnamon.model.request.link.UpdateLinkRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * An update link request is validated by the annotations on Link, except for the one rule they
 * cannot express - a link points at a folder or at an object - which Link.validated() still carries.
 */
public class LinkUpdateRequestTest {

    /**
     * @return the violation message, which names every field that was wrong
     */
    private String assertRejected(UpdateLinkRequest request) {
        FailedRequestException ex = assertThrows(FailedRequestException.class, request::validateRequest);
        assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
        return ex.getMessage();
    }

    private void assertNamesFields(String message, String... fields) {
        for (String field : fields) {
            assertTrue(message.contains(field), "should name '" + field + "': " + message);
        }
    }

    @Test
    public void requestWithoutLinksIsRejected() {
        assertNamesFields(assertRejected(new UpdateLinkRequest()), "links");
    }

    @Test
    public void aLinkNeedsAnAclAnOwnerAParentAndAType() {
        Link onlyAnId = new Link(1L, null, null, null, null, null, null, null);
        String message = assertRejected(new UpdateLinkRequest(List.of(onlyAnId)));
        assertNamesFields(message, "links[0].type", "links[0].aclId", "links[0].ownerId", "links[0].parentId");
    }

    @Test
    public void idsHaveToBePositive() {
        assertNamesFields(assertRejected(new UpdateLinkRequest(0L, 1L, 1L, 1L, null, 1L)), "links[0].id");
        assertNamesFields(assertRejected(new UpdateLinkRequest(1L, 0L, 1L, 1L, null, 1L)), "links[0].aclId");
        assertNamesFields(assertRejected(new UpdateLinkRequest(1L, 1L, 0L, 1L, null, 1L)), "links[0].parentId");
        assertNamesFields(assertRejected(new UpdateLinkRequest(1L, 1L, 1L, 1L, null, 0L)), "links[0].ownerId");
        assertNamesFields(assertRejected(new UpdateLinkRequest(1L, 1L, 1L, 0L, 0L, 1L)),
                "links[0].objectId", "links[0].folderId");
    }

    @Test
    public void aLinkPointsAtAFolderOrAtAnObject() {
        // both set: no single field is at fault, so this one is reported as a bare INVALID_REQUEST
        assertTrue(new UpdateLinkRequest(1L, 1L, 1L, 1L, 1L, 1L).validateRequest().isEmpty(),
                "a link to both an object and a folder is invalid");
    }

    @Test
    public void aWellFormedLinkPasses() {
        assertTrue(new UpdateLinkRequest(1L, 1L, 1L, 1L, null, 1L).validateRequest().isPresent(),
                "okay object link");
        assertTrue(new UpdateLinkRequest(1L, 1L, 1L, null, 1L, 1L).validateRequest().isPresent(),
                "okay folder link");
    }
}
