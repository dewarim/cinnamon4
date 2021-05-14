package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.model.Group;
import com.dewarim.cinnamon.model.request.group.ListGroupRequest;
import com.dewarim.cinnamon.model.response.GroupWrapper;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class GroupServletIntegrationTest extends CinnamonIntegrationTest {

    @Test
    public void listGroups() throws IOException {
        HttpResponse response = sendStandardRequest(UrlMapping.GROUP__LIST, new ListGroupRequest());
        List<Group>  groups   = parseResponse(response);

        assertNotNull(groups);
        assertFalse(groups.isEmpty());
        assertTrue(groups.size() >= 7);
        
        List<String> actualGroupNames = groups.stream().map(Group::getName).collect(Collectors.toList());
        String[] groupNames = {"_superusers", "_everyone", "_owner"};
        Arrays.stream(groupNames).forEach(name ->
                assertTrue(actualGroupNames.contains(name))
        );

    }

    private List<Group> parseResponse(HttpResponse response) throws IOException {
        assertResponseOkay(response);
        GroupWrapper groupWrapper = mapper.readValue(response.getEntity().getContent(), GroupWrapper.class);
        assertNotNull(groupWrapper);
        return groupWrapper.getGroups();
    }


}
