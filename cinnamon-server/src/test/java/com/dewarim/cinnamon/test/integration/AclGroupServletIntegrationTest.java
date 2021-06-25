package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.client.CinnamonClientException;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.AclGroup;
import com.dewarim.cinnamon.model.Group;
import com.dewarim.cinnamon.model.request.aclGroup.AclGroupListRequest;
import com.dewarim.cinnamon.model.request.aclGroup.UpdateAclGroupRequest;
import com.dewarim.cinnamon.model.response.AclGroupWrapper;
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

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AclGroupServletIntegrationTest extends CinnamonIntegrationTest {

    private final static Logger log = LogManager.getLogger(AclGroupServletIntegrationTest.class);

    static List<Acl>      acls       = new ArrayList<>();
    static List<Group>    groups     = new ArrayList<>();
    static List<AclGroup> aclGroups = new ArrayList<>();

    @Test
    public void testListAclGroupByAclId() throws IOException {
        AclGroupListRequest listRequest  = new AclGroupListRequest(1L, AclGroupListRequest.IdType.ACL);
        HttpResponse        httpResponse = sendStandardRequest(UrlMapping.ACL_GROUP__LIST_BY_GROUP_OR_ACL, listRequest);
        List<AclGroup>      aclGroups   = unwrapAclGroups(httpResponse, 3);
        aclGroups.forEach(entry -> assertEquals(Long.valueOf(1), entry.getAclId()));
    }

    @Test
    public void testListAclGroupByGroupId() throws IOException {
        AclGroupListRequest listRequest  = new AclGroupListRequest(4L, AclGroupListRequest.IdType.GROUP);
        HttpResponse        httpResponse = sendStandardRequest(UrlMapping.ACL_GROUP__LIST_BY_GROUP_OR_ACL, listRequest);
        List<AclGroup>      aclGroups   = unwrapAclGroups(httpResponse, 5);
        aclGroups.forEach(entry -> assertEquals(Long.valueOf(4), entry.getGroupId()));
    }

    @Test
    public void invalidAclGroupListRequest() throws IOException {
        AclGroupListRequest listRequest  = new AclGroupListRequest();
        HttpResponse        httpResponse = sendStandardRequest(UrlMapping.ACL_GROUP__LIST_BY_GROUP_OR_ACL, listRequest);
        assertCinnamonError(httpResponse, ErrorCode.INVALID_REQUEST);
    }

    @Test
    public void listAclGroups() throws IOException {
        List<AclGroup> aclGroups = client.listAclGroups();
        assertTrue(aclGroups.size() > 0);
    }

    @Test
    @Order(1)
    public void createAclGroup() throws IOException {
        acls = adminClient.createAcl(List.of("a1", "a2"));
        Acl a1 = acls.get(0);
        Acl a2 = acls.get(1);
        groups = adminClient.createGroups(List.of("g1", "g2"));
        var            g1      = groups.get(0);
        var            g2      = groups.get(1);
        List<AclGroup> entries = new ArrayList<>();
        entries.add(new AclGroup(a1.getId(), g1.getId()));
        entries.add(new AclGroup(a2.getId(), g2.getId()));
        aclGroups = adminClient.createAclGroups(entries);

        assertEquals(entries.size(), aclGroups.size());
        AclGroup ae1 = aclGroups.get(0);
        AclGroup ae2 = aclGroups.get(1);

        assertEquals(ae1.getAclId(), a1.getId());
        assertEquals(ae2.getAclId(), a2.getId());

        assertEquals(ae1.getGroupId(), g1.getId());
        assertEquals(ae2.getGroupId(), g2.getId());
    }

    @Test
    @Order(150)
    public void createAclGroupWithoutPermission() {
        List<AclGroup> entries = new ArrayList<>();
        entries.add(new AclGroup(acls.get(0).getId(), groups.get(0).getId()));
        CinnamonClientException ex = assertThrows(CinnamonClientException.class, () -> client.createAclGroups(entries));
        assertEquals(ErrorCode.REQUIRES_SUPERUSER_STATUS, ex.getErrorCode());
    }

    @Test
    public void createAclGroupWithInvalidRequest() {
        List<AclGroup> entries = new ArrayList<>();
        entries.add(new AclGroup(0L, 0L));
        CinnamonClientException ex = assertThrows(CinnamonClientException.class, () -> adminClient.createAclGroups(entries));
        assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
    }

    @Test
    @Order(200)
    public void updateAclGroup() throws IOException {
        Acl   a1           = acls.get(0);
        Group g2           = groups.get(1);
        var   idOfFirstAcl = aclGroups.get(0).getId();
        // replace g1 in aclGroup (a1,g1) with g2
        UpdateAclGroupRequest updateRequest  = new UpdateAclGroupRequest(idOfFirstAcl, a1.getId(), g2.getId());
        var                   updatedEntries = adminClient.updateAclGroups(updateRequest);
        var                   aclGroup       = updatedEntries.get(0);
        assertEquals(idOfFirstAcl, aclGroup.getId());
        assertEquals(g2.getId(), aclGroup.getGroupId());
        assertEquals(a1.getId(), aclGroup.getAclId());
    }

    @Test
    @Order(225)
    public void updateAclGroupWithoutPermisson() {
        UpdateAclGroupRequest updateRequest = new UpdateAclGroupRequest(1L, 1L, 1L);

        CinnamonClientException ex = assertThrows(CinnamonClientException.class, () -> client.updateAclGroups(updateRequest));
        assertEquals(ErrorCode.REQUIRES_SUPERUSER_STATUS, ex.getErrorCode());
    }

    @Test
    @Order(250)
    public void deleteAclGroupWithoutPermission() {
        CinnamonClientException ex = assertThrows(CinnamonClientException.class, () -> client.deleteAclGroups(aclGroups.stream().map(AclGroup::getId).collect(Collectors.toList())));
        assertEquals(ErrorCode.REQUIRES_SUPERUSER_STATUS, ex.getErrorCode());
    }

    @Test
    @Order(300)
    public void deleteAclGroup() throws IOException {
        boolean deleteResult = adminClient.deleteAclGroups(aclGroups.stream().map(AclGroup::getId).collect(Collectors.toList()));
        assertTrue(deleteResult);
        List<AclGroup> remainingEntries = client.listAclGroups();
        assertTrue(aclGroups.stream().noneMatch(remainingEntries::contains));
    }

    private List<AclGroup> unwrapAclGroups(HttpResponse httpResponse, int expectedSize) throws IOException {
        assertResponseOkay(httpResponse);
        AclGroupWrapper wrapper    = mapper.readValue(httpResponse.getEntity().getContent(), AclGroupWrapper.class);
        List<AclGroup>  aclGroups = wrapper.getAclGroups();
        assertEquals(expectedSize, aclGroups.size());
        return aclGroups;
    }

}
