package com.dewarim.cinnamon.test.unit;

import com.dewarim.cinnamon.model.request.link.LinkUpdateRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class LinkUpdateRequestTest {

    @Test
    public void testValidated() {
        LinkUpdateRequest request = new LinkUpdateRequest();
        assertFalse(request.validated(), "request without params is invalid");

        request = new LinkUpdateRequest(1L);
        assertFalse(request.validated(), "request with only id is invalid - nothing to update");


        request = new LinkUpdateRequest(0L, 1L, 1L, 1L, null, 1L);
        assertFalse(request.validated(), "invalid id found");

        request = new LinkUpdateRequest(1L, 0L, 1L, 1L, null, 1L);
        assertFalse(request.validated(), "invalid aclId found");

        request = new LinkUpdateRequest(1L, 1L, 0L, 1L, null, 1L);
        assertFalse(request.validated(), "invalid parentId found");

        request = new LinkUpdateRequest(1L, 1L, 1L, 0L, 0L, 1L);
        assertFalse(request.validated(), "invalid objectId and folderId found");

        request = new LinkUpdateRequest(1L, 1L, 1L, 1L, 1L, 1L);
        assertFalse(request.validated(), "objectId and folderId are both set");

        request = new LinkUpdateRequest(1L, 1L, 1L, 1L, null, 0L);
        assertFalse(request.validated(), "invalid owner found");

        request = new LinkUpdateRequest(1L, 1L, 1L, 1L, null, 1L);
        assertTrue(request.validated(), "okay object link");

        request = new LinkUpdateRequest(1L, 1L, 1L, null, 1L, 1L);
        assertTrue(request.validated(), "okay folder link");


    }

}
