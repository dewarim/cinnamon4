package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.model.ConfigEntry;
import com.dewarim.cinnamon.test.TestObjectHolder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.dewarim.cinnamon.ErrorCode.*;
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
        publicEntry.setName("updated");
        publicEntry.setPublicVisibility(false);
        var updatedEntry = adminClient.updateConfigEntry(publicEntry);
        assertEquals("<xml>updated</xml>", updatedEntry.getConfig());
        assertEquals("updated", updatedEntry.getName());
        // updated entry should invisible for normal users:
        assertClientError(() -> client.getConfigEntry(updatedEntry.getId()), OBJECT_NOT_FOUND);
    }

    @Test
    public void delete() throws IOException{
        var entry = adminClient.createConfigEntry(new ConfigEntry("delete-me", "<xml/>", true));
        adminClient.deleteConfigEntry(entry.getId());
    }

    @Test
    public void deleteNonAdmin() throws IOException{
        var entry = adminClient.createConfigEntry(new ConfigEntry("delete-me", "<xml/>", true));
        assertClientError( () -> client.deleteConfigEntry(entry.getId()),REQUIRES_SUPERUSER_STATUS);
    }

    @Test
    public void doesNotExistPath() {
        // more a test of the client than the server - which will return simply an empty list.
        assertClientError( () -> client.getConfigEntry(Long.MAX_VALUE),OBJECT_NOT_FOUND);
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

    @Test
    public void reloadLogging() throws IOException{
        var toh = new TestObjectHolder(client, userId);
        assertClientError(toh::reloadLogging, REQUIRES_SUPERUSER_STATUS);
        var adminToh = new TestObjectHolder(adminClient, adminId);
        assertClientError(adminToh::reloadLogging, NEED_EXTERNAL_LOGGING_CONFIG);
        CinnamonServer.config.getServerConfig().setLog4jConfigPath("log4j2-example.xml");
        adminToh.reloadLogging();
        CinnamonServer.config.getServerConfig().setLog4jConfigPath("src/test/resources/log4j2-test.xml");
        adminToh.reloadLogging();
    }
}
