package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.CinnamonClientException;
import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.model.Group;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GroupServletIntegrationTest extends CinnamonIntegrationTest {

    private static List<Group> groups;

    @Test
    public void listGroups() throws IOException {
        List<Group> groups = client.listGroups();

        assertNotNull(groups);
        assertFalse(groups.isEmpty());
        assertTrue(groups.size() >= 7);

        List<String> actualGroupNames = groups.stream().map(Group::getName).collect(Collectors.toList());
        String[]     groupNames       = {"_superusers", "_everyone", "_owner"};
        Arrays.stream(groupNames).forEach(name ->
                assertTrue(actualGroupNames.contains(name))
        );
    }

    @Test
    public void createGroupWithInvalidRequest() {
        CinnamonClientException e = assertThrows(CinnamonClientException.class, () -> adminClient.createGroups(List.of("")));
        assertEquals(ErrorCode.INVALID_REQUEST, e.getErrorCode());
    }

    @Test
    public void createGroupWithSameName() throws IOException{
        String name = "a-group-with-a-name";
        adminClient.createGroups(List.of(name));
        CinnamonClientException e = assertThrows(CinnamonClientException.class, () -> adminClient.createGroups(List.of(name)));
        assertEquals(ErrorCode.DB_INSERT_FAILED, e.getErrorCode());
    }

    @Test
    @Order(100)
    public void createGroups() throws IOException {
        groups = adminClient.createGroups(List.of("test1", "test2", "test3"));
        var allGroupNames = client.listGroups().stream().map(Group::getName).collect(Collectors.toList());
        var newGroupNames = groups.stream().map(Group::getName).collect(Collectors.toList());
        assertTrue(allGroupNames.containsAll(newGroupNames));
    }

    @Test
    public void createGroupsWithoutPermission() {
        CinnamonClientException ex = assertThrows(CinnamonClientException.class, () -> client.createGroups(List.of("no-perm-group")));
        assertEquals(ErrorCode.REQUIRES_SUPERUSER_STATUS, ex.getErrorCode());
    }

    @Test
    public void deleteGroupsWithoutPermission() {
        CinnamonClientException ex = assertThrows(CinnamonClientException.class, () -> client.deleteGroups(List.of(1L)));
        assertEquals(ErrorCode.REQUIRES_SUPERUSER_STATUS, ex.getErrorCode());
    }

    @Test
    @Order(400)
    public void deleteGroups() throws IOException {
        var deleteResult = adminClient.deleteGroups(groups.stream().map(Group::getId).collect(Collectors.toList()));
        assertTrue(deleteResult);
        var allGroupnames     = client.listGroups().stream().map(Group::getName).collect(Collectors.toList());
        var deletedGroupNames = groups.stream().map(Group::getName).collect(Collectors.toList());
        assertFalse(allGroupnames.containsAll(deletedGroupNames));
    }

    @Test
    public void upgradeGroup() throws IOException {
        List<Group> groups = adminClient.createGroups(List.of("update-my-name"));
        groups.get(0).setName("is-updated-group");
        List<Group> updatedGroups = adminClient.updateGroups(groups);
        assertEquals(groups.get(0).getName(), updatedGroups.get(0).getName());
    }

    @Test
    public void updateGroupWithoutPermission() {
        CinnamonClientException ex = assertThrows(CinnamonClientException.class, () -> client.updateGroups(Collections.emptyList()));
        assertEquals(ErrorCode.REQUIRES_SUPERUSER_STATUS, ex.getErrorCode());
    }

    @Test
    public void updateNonExistentGroup(){
        var group = new Group("foo");
        group.setId(Long.MAX_VALUE);
        CinnamonClientException ex = assertThrows(CinnamonClientException.class, () ->
                adminClient.updateGroups(Collections.singletonList(group)));
        assertEquals(ErrorCode.OBJECT_NOT_FOUND, ex.getErrorCode());
    }
}
