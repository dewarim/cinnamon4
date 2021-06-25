package com.dewarim.cinnamon.test.unit;

import com.dewarim.cinnamon.model.links.Link;
import com.dewarim.cinnamon.model.request.link.UpdateLinkRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class LinkUpdateRequestTest {

    @Test
    public void testValidated() {
        UpdateLinkRequest request = new UpdateLinkRequest();
        assertFalse(request.validated(), "request without params is invalid");

        var newLink = new Link(1L,null,null,null,null,null,null);
        request = new UpdateLinkRequest(List.of(newLink));
        assertFalse(request.validated(), "request with only id is invalid - nothing to update");


        request = new UpdateLinkRequest(0L, 1L, 1L, 1L, null, 1L);
        assertFalse(request.validated(), "invalid id found");

        request = new UpdateLinkRequest(1L, 0L, 1L, 1L, null, 1L);
        assertFalse(request.validated(), "invalid aclId found");

        request = new UpdateLinkRequest(1L, 1L, 0L, 1L, null, 1L);
        assertFalse(request.validated(), "invalid parentId found");

        request = new UpdateLinkRequest(1L, 1L, 1L, 0L, 0L, 1L);
        assertFalse(request.validated(), "invalid objectId and folderId found");

        request = new UpdateLinkRequest(1L, 1L, 1L, 1L, 1L, 1L);
        assertFalse(request.validated(), "objectId and folderId are both set");

        request = new UpdateLinkRequest(1L, 1L, 1L, 1L, null, 0L);
        assertFalse(request.validated(), "invalid owner found");

        request = new UpdateLinkRequest(1L, 1L, 1L, 1L, null, 1L);
        assertTrue(request.validated(), "okay object link");

        request = new UpdateLinkRequest(1L, 1L, 1L, null, 1L, 1L);
        assertTrue(request.validated(), "okay folder link");


    }

}
