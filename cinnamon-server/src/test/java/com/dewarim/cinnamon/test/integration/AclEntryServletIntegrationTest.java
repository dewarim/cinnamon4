package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.AclEntry;
import com.dewarim.cinnamon.model.Group;
import com.dewarim.cinnamon.model.request.aclEntry.AclEntryListRequest;
import com.dewarim.cinnamon.model.request.aclEntry.UpdateAclEntryRequest;
import com.dewarim.cinnamon.model.response.AclEntryWrapper;
import org.apache.http.HttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AclEntryServletIntegrationTest extends CinnamonIntegrationTest {

    private final static Logger log = LogManager.getLogger(AclEntryServletIntegrationTest.class);

    static List<Acl>      acls       = new ArrayList<>();
    static List<Group>    groups     = new ArrayList<>();
    static List<AclEntry> aclEntries = new ArrayList<>();

    @Test
    public void testListAclEntryByAclId() throws IOException {
        AclEntryListRequest listRequest  = new AclEntryListRequest(1L, AclEntryListRequest.IdType.ACL);
        HttpResponse        httpResponse = sendStandardRequest(UrlMapping.ACL_ENTRY__LIST_ACL_ENTRIES_BY_GROUP_OR_ACL, listRequest);
        List<AclEntry>      aclEntries   = unwrapAclEntries(httpResponse, 3);
        aclEntries.forEach(entry -> assertEquals(Long.valueOf(1), entry.getAclId()));
    }

    @Test
    public void testListAclEntryByGroupId() throws IOException {
        AclEntryListRequest listRequest  = new AclEntryListRequest(4L, AclEntryListRequest.IdType.GROUP);
        HttpResponse        httpResponse = sendStandardRequest(UrlMapping.ACL_ENTRY__LIST_ACL_ENTRIES_BY_GROUP_OR_ACL, listRequest);
        List<AclEntry>      aclEntries   = unwrapAclEntries(httpResponse, 5);
        aclEntries.forEach(entry -> assertEquals(Long.valueOf(4), entry.getGroupId()));
    }

    @Test
    public void invalidAclEntryListRequest() throws IOException {
        AclEntryListRequest listRequest  = new AclEntryListRequest();
        HttpResponse        httpResponse = sendStandardRequest(UrlMapping.ACL_ENTRY__LIST_ACL_ENTRIES_BY_GROUP_OR_ACL, listRequest);
        assertCinnamonError(httpResponse, ErrorCode.INVALID_REQUEST);
    }

    @Test
    public void listAclEntries() throws IOException {
        List<AclEntry> aclEntries = client.listAclEntries();
        assertTrue(aclEntries.size() > 0);
    }

    @Test
    @Order(1)
    public void createAclEntry() throws IOException {
        acls = adminClient.createAcl(List.of("a1", "a2"));
        Acl a1 = acls.get(0);
        Acl a2 = acls.get(1);
        groups = adminClient.createGroups(List.of("g1", "g2"));
        var            g1      = groups.get(0);
        var            g2      = groups.get(1);
        List<AclEntry> entries = new ArrayList<>();
        entries.add(new AclEntry(a1.getId(), g1.getId()));
        entries.add(new AclEntry(a2.getId(), g2.getId()));
        aclEntries = adminClient.createAclEntries(entries);

        assertEquals(entries.size(), aclEntries.size());
        AclEntry ae1 = aclEntries.get(0);
        AclEntry ae2 = aclEntries.get(1);

        assertEquals(ae1.getAclId(), a1.getId());
        assertEquals(ae2.getAclId(), a2.getId());

        assertEquals(ae1.getGroupId(), g1.getId());
        assertEquals(ae2.getGroupId(), g2.getId());
    }

    @Test
    @Order(2)
    public void updateAclEntry() throws IOException {
        Acl   a1           = acls.get(0);
        Group g2           = groups.get(1);
        var   idOfFirstAcl = aclEntries.get(0).getId();
        // replace g1 in aclEntry (a1,g1) with g2
        UpdateAclEntryRequest updateRequest  = new UpdateAclEntryRequest(idOfFirstAcl, a1.getId(), g2.getId());
        var                   updatedEntries = adminClient.updateAclEntries(updateRequest);
        var aclEntry = updatedEntries.get(0);
        assertEquals(idOfFirstAcl, aclEntry.getId());
        assertEquals(g2.getId(), aclEntry.getGroupId());
        assertEquals(a1.getId(), aclEntry.getAclId());
    }

    @Test
    @Order(3)
    public void deleteAclEntry() throws IOException {
        boolean deleteResult = adminClient.deleteAclEntries(aclEntries.stream().map(AclEntry::getId).collect(Collectors.toList()));
        assertTrue(deleteResult);
        List<AclEntry> remainingEntries = client.listAclEntries();
        assertTrue(aclEntries.stream().noneMatch(remainingEntries::contains));
    }

    // TODO: add non-admin-client calls to delete/create/update and verify errors.


    private List<AclEntry> unwrapAclEntries(HttpResponse httpResponse, int expectedSize) throws IOException {
        assertResponseOkay(httpResponse);
        AclEntryWrapper wrapper    = mapper.readValue(httpResponse.getEntity().getContent(), AclEntryWrapper.class);
        List<AclEntry>  aclEntries = wrapper.getAclEntries();
        assertEquals(expectedSize, aclEntries.size());
        return aclEntries;
    }

}
