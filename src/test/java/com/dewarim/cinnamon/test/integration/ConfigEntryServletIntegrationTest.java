package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.model.ConfigEntry;
import com.dewarim.cinnamon.model.request.configEntry.ConfigEntryRequest;
import com.dewarim.cinnamon.model.request.configEntry.CreateConfigEntryRequest;
import com.dewarim.cinnamon.model.response.ConfigEntryWrapper;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class ConfigEntryServletIntegrationTest extends CinnamonIntegrationTest {

    @Test
    public void setConfigEntry() throws IOException {

        CreateConfigEntryRequest createRequest = new CreateConfigEntryRequest("test-config", "<config>test</config>", false);

        // must be admin to create config entry
        HttpResponse failedCreate = sendStandardRequest(UrlMapping.CONFIG_ENTRY__SET, createRequest);
        assertCinnamonError(failedCreate, ErrorCode.REQUIRES_SUPERUSER_STATUS);

        HttpResponse validCreate = sendAdminRequest(UrlMapping.CONFIG_ENTRY__SET, createRequest);
        assertResponseOkay(validCreate);

        // non-admin cannot read non-public config entry:
        ConfigEntryRequest configEntryRequest = new ConfigEntryRequest("test-config");
        HttpResponse       failedRequest      = sendStandardRequest(UrlMapping.CONFIG_ENTRY__GET, configEntryRequest);
        assertCinnamonError(failedRequest, ErrorCode.REQUIRES_SUPERUSER_STATUS);

        // must be admin to get non-public config entry
        HttpResponse okayRequest = sendAdminRequest(UrlMapping.CONFIG_ENTRY__GET, configEntryRequest);
        assertResponseOkay(okayRequest);

        // non-admin may retrieve public entry
        CreateConfigEntryRequest publicEntryRequest = new CreateConfigEntryRequest("public-test", "<pub>1</pub>", true);
        HttpResponse             publicCreate       = sendAdminRequest(UrlMapping.CONFIG_ENTRY__SET, publicEntryRequest);
        assertResponseOkay(publicCreate);
        HttpResponse publicResponse = sendStandardRequest(UrlMapping.CONFIG_ENTRY__GET, new ConfigEntryRequest("public-test"));
        assertResponseOkay(publicResponse);

        ConfigEntryWrapper entryWrapper = mapper.readValue(publicResponse.getEntity().getContent(), ConfigEntryWrapper.class);
        assertNotNull(entryWrapper.getConfigEntries());
        assertFalse(entryWrapper.getConfigEntries().isEmpty());
        assertThat(entryWrapper.getConfigEntries().size(), equalTo(1));

        ConfigEntry configEntry = entryWrapper.getConfigEntries().get(0);
        assertTrue(configEntry.isPublicVisibility());
        assertTrue(configEntry.getId() >= 2L);
        assertThat(configEntry.getName(), equalTo("public-test"));
        assertThat(configEntry.getConfig(), equalTo("<pub>1</pub>"));

        // admin may update config entry:
        // TODO: check create/update response contains actual wrapped ConfigEntry
        CreateConfigEntryRequest updatedEntryRequest = new CreateConfigEntryRequest("public-test", "<pub>2</pub>", false);
        HttpResponse             updateResponse      = sendAdminRequest(UrlMapping.CONFIG_ENTRY__SET, updatedEntryRequest);
        assertResponseOkay(updateResponse);
        HttpResponse updatedEntryResponse = sendAdminRequest(UrlMapping.CONFIG_ENTRY__GET, new ConfigEntryRequest("public-test"));
        assertResponseOkay(updatedEntryResponse);

        ConfigEntryWrapper updateWrapper = mapper.readValue(updatedEntryResponse.getEntity().getContent(), ConfigEntryWrapper.class);
        ConfigEntry        updatedEntry  = updateWrapper.getConfigEntries().get(0);
        assertFalse(updatedEntry.isPublicVisibility());
        assertTrue(updatedEntry.getId() >= 2L);
        assertThat(updatedEntry.getName(), equalTo("public-test"));
        assertThat(updatedEntry.getConfig(), equalTo("<pub>2</pub>"));

    }

    @Test
    public void doesNotExistPath() throws IOException {
        HttpResponse response = sendStandardRequest(UrlMapping.CONFIG_ENTRY__GET, new ConfigEntryRequest("unknown-entry"));
        assertCinnamonError(response, ErrorCode.OBJECT_NOT_FOUND);
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
