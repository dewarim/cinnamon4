package com.dewarim.cinnamon.test.unit;

import com.dewarim.cinnamon.model.LinkResolver;
import com.dewarim.cinnamon.model.request.LinkUpdateRequest;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LinkUpdateRequestTest {

    @Test
    public void testValidated() {
        LinkUpdateRequest request = new LinkUpdateRequest();
        assertFalse("request without params is invalid",request.validated());
        
        request = new LinkUpdateRequest(1L);
        assertFalse("request with only id is invalid - nothing to update",request.validated());


        request = new LinkUpdateRequest(0L,1L,1L, 1L, null, LinkResolver.FIXED,1L);
        assertFalse("invalid id found",request.validated());

        request = new LinkUpdateRequest(1L,0L,1L, 1L, null, LinkResolver.FIXED,1L);
        assertFalse("invalid aclId found",request.validated());

        request = new LinkUpdateRequest(1L,1L,0L, 1L, null, LinkResolver.FIXED,1L);
        assertFalse("invalid parentId found",request.validated());

        request = new LinkUpdateRequest(1L,1L,1L, 0L, 0L, LinkResolver.FIXED,1L);
        assertFalse("invalid objectId and folderId found",request.validated());

               request = new LinkUpdateRequest(1L,1L,1L, 1L,1L, LinkResolver.FIXED,1L);
        assertFalse("objectId and folderId are both set",request.validated());

        request = new LinkUpdateRequest(1L,1L,1L, 1L, null, LinkResolver.FIXED,0L);
        assertFalse("invalid owner found",request.validated());

        request = new LinkUpdateRequest(1L,1L,1L, 1L, null, LinkResolver.FIXED,1L);
        assertTrue("okay object link",request.validated());
        
        request = new LinkUpdateRequest(1L,1L,1L, null, 1L, LinkResolver.FIXED,1L);
        assertTrue("okay folder link",request.validated());
   

    }

}
