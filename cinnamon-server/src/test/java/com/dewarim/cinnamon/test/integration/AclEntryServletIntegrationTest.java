package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.model.AclEntry;
import com.dewarim.cinnamon.model.request.AclEntryListRequest;
import com.dewarim.cinnamon.model.response.AclEntryWrapper;
import org.apache.http.HttpResponse;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class AclEntryServletIntegrationTest extends CinnamonIntegrationTest {


    @Test
    public void testListAclEntryByAclId() throws IOException {
        AclEntryListRequest listRequest  = new AclEntryListRequest(1L, AclEntryListRequest.IdType.ACL);
        HttpResponse        httpResponse = sendStandardRequest(UrlMapping.ACL_ENTRY__LIST_ACL_ENTRIES, listRequest);
        List<AclEntry>      aclEntries   = unwrapAclEntries(httpResponse, 3);
        aclEntries.forEach(entry -> assertEquals(Long.valueOf(1), entry.getAclId()));
    }

    @Test
    public void testListAclEntryByGroupId() throws IOException {
        AclEntryListRequest listRequest  = new AclEntryListRequest(4L, AclEntryListRequest.IdType.GROUP);
        HttpResponse        httpResponse = sendStandardRequest(UrlMapping.ACL_ENTRY__LIST_ACL_ENTRIES, listRequest);
        List<AclEntry>      aclEntries   = unwrapAclEntries(httpResponse, 5);
        aclEntries.forEach(entry -> assertEquals(Long.valueOf(4), entry.getGroupId()));
    }

    @Test
    public void invalidAclEntryListRequest() throws IOException {
        AclEntryListRequest listRequest  = new AclEntryListRequest();
        HttpResponse        httpResponse = sendStandardRequest(UrlMapping.ACL_ENTRY__LIST_ACL_ENTRIES, listRequest);
        assertCinnamonError(httpResponse, ErrorCode.INVALID_REQUEST);
    }

    private List<AclEntry> unwrapAclEntries(HttpResponse httpResponse, int expectedSize) throws IOException {
        assertResponseOkay(httpResponse);
        AclEntryWrapper wrapper    = mapper.readValue(httpResponse.getEntity().getContent(), AclEntryWrapper.class);
        List<AclEntry>  aclEntries = wrapper.getAclEntries();
        assertEquals(expectedSize, aclEntries.size());
        return aclEntries;
    }

}
