package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.client.CinnamonClientException;
import com.dewarim.cinnamon.model.ConfigEntry;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigEntryServletIntegrationTest extends CinnamonIntegrationTest {

    @Test
    public void create() throws IOException{
        var publicEntry = adminClient.createConfigEntry(new ConfigEntry("create", "<xml/>", true));
        assertEquals("create", publicEntry.getName());
        assertEquals("<xml/>", publicEntry.getConfig());
        assertTrue(publicEntry.isPublicVisibility());
    }

    @Test
    public void update() throws IOException{
        var publicEntry = adminClient.createConfigEntry(new ConfigEntry("update", "<xml/>", true));
        publicEntry.setConfig("<xml>updated</xml>");
        var updatedEntry = adminClient.updateConfigEntry(publicEntry);
        assertEquals("<xml>updated</xml>", updatedEntry.getConfig());
    }

    @Test
    public void delete() throws IOException{
        var entry = adminClient.createConfigEntry(new ConfigEntry("delete-me", "<xml/>", true));
        adminClient.deleteConfigEntry(entry.getId());
    }

    @Test
    public void deleteNonAdmin() throws IOException{
        var entry = adminClient.createConfigEntry(new ConfigEntry("delete-me", "<xml/>", true));
        var ex = assertThrows(CinnamonClientException.class, () -> client.deleteConfigEntry(entry.getId()));
        assertEquals(ErrorCode.REQUIRES_SUPERUSER_STATUS, ex.getErrorCode());
    }

    @Test
    public void getByName() throws IOException{
        var publicEntry = adminClient.createConfigEntry(new ConfigEntry("get-me", "<xml/>", true));
        var entry = client.getConfigEntry("get-me");
        assertEquals(publicEntry,entry);
        var entryById = client.getConfigEntry(publicEntry.getId());
        assertEquals(publicEntry, entryById);
    }

    @Test
    public void getByNameNonPublic() throws IOException{
        var nonPublicEntry = adminClient.createConfigEntry(new ConfigEntry("non-public-entry", "<xml/>", false));
        var entry = client.getConfigEntries(List.of(nonPublicEntry.getId()));
        assertTrue(entry.isEmpty());
    }

    @Test
    public void doesNotExistPath() {
        // more a test of the client than the server - which will return simply an empty list.
        var ex = assertThrows(CinnamonClientException.class, () -> client.getConfigEntry("unknown-entry"));
        assertEquals(ErrorCode.OBJECT_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    public void listConfigEntries() throws IOException {
        var publicEntry = adminClient.createConfigEntry(new ConfigEntry("test", "<xml>test</xml>", true));
        var adminEntry  = adminClient.createConfigEntry(new ConfigEntry("test-admin", "<xml>test</xml>", false));

        List<ConfigEntry> configEntries = adminClient.listConfigEntries();
        assertTrue(configEntries.size() >= 2);
        assertTrue(configEntries.contains(publicEntry));
        assertTrue(configEntries.contains(adminEntry));

        List<ConfigEntry> publicEntries = client.listConfigEntries();
        assertTrue(publicEntries.size() >= 1);
        assertTrue(publicEntries.contains(publicEntry));
        assertFalse(publicEntries.contains(adminEntry));
    }

}
