package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.model.ConfigEntry;
import com.dewarim.cinnamon.model.request.ConfigEntryRequest;
import com.dewarim.cinnamon.model.request.CreateConfigEntryRequest;
import com.dewarim.cinnamon.model.response.ConfigEntryWrapper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class ConfigEntryServletIntegrationTest extends CinnamonIntegrationTest {

    @Test
    public void setConfigEntry() throws IOException {

        CreateConfigEntryRequest createRequest = new CreateConfigEntryRequest("test-config", "<config>test</config>", false);

        // must be admin to create config entry
        HttpResponse failedCreate = sendStandardRequest(UrlMapping.CONFIG_ENTRY__SET_CONFIG_ENTRY, createRequest);
        assertCinnamonError(failedCreate, ErrorCode.UNAUTHORIZED, HttpStatus.SC_FORBIDDEN);

        HttpResponse validCreate = sendAdminRequest(UrlMapping.CONFIG_ENTRY__SET_CONFIG_ENTRY, createRequest);
        assertResponseOkay(validCreate);

        // non-admin cannot read non-public config entry:
        ConfigEntryRequest configEntryRequest = new ConfigEntryRequest("test-config");
        HttpResponse       failedRequest      = sendStandardRequest(UrlMapping.CONFIG_ENTRY__GET_CONFIG_ENTRY, configEntryRequest);
        assertCinnamonError(failedRequest, ErrorCode.UNAUTHORIZED, HttpStatus.SC_FORBIDDEN);

        // must be admin to get non-public config entry
        HttpResponse okayRequest = sendAdminRequest(UrlMapping.CONFIG_ENTRY__GET_CONFIG_ENTRY, configEntryRequest);
        assertResponseOkay(okayRequest);

        // non-admin may retrieve public entry
        CreateConfigEntryRequest publicEntryRequest  = new CreateConfigEntryRequest("public-test", "<pub>1</pub>", true);
        HttpResponse             publicCreate = sendAdminRequest(UrlMapping.CONFIG_ENTRY__SET_CONFIG_ENTRY, publicEntryRequest);
        assertResponseOkay(publicCreate);
        HttpResponse publicResponse = sendStandardRequest(UrlMapping.CONFIG_ENTRY__GET_CONFIG_ENTRY, new ConfigEntryRequest("public-test"));
        assertResponseOkay(publicResponse);

        ConfigEntryWrapper entryWrapper = mapper.readValue(publicResponse.getEntity().getContent(), ConfigEntryWrapper.class);
        assertNotNull(entryWrapper.getConfigEntries());
        assertFalse(entryWrapper.getConfigEntries().isEmpty());
        assertThat(entryWrapper.getConfigEntries().size(),equalTo(1));

        ConfigEntry configEntry = entryWrapper.getConfigEntries().get(0);
        assertTrue(configEntry.isPublicVisibility());
        assertThat(configEntry.getId(), equalTo(2L));
        assertThat(configEntry.getName(), equalTo("public-test"));
        assertThat(configEntry.getConfig(), equalTo("<pub>1</pub>"));
        
        // admin may update config entry:
        CreateConfigEntryRequest updatedEntryRequest  = new CreateConfigEntryRequest("public-test", "<pub>2</pub>", false);
        HttpResponse             updateResponse      = sendAdminRequest(UrlMapping.CONFIG_ENTRY__SET_CONFIG_ENTRY, updatedEntryRequest);
        assertResponseOkay(updateResponse);
        HttpResponse       updatedEntryResponse   = sendAdminRequest(UrlMapping.CONFIG_ENTRY__GET_CONFIG_ENTRY, new ConfigEntryRequest("public-test"));
        assertResponseOkay(updatedEntryResponse);
        
        ConfigEntryWrapper updateWrapper = mapper.readValue(updatedEntryResponse.getEntity().getContent(), ConfigEntryWrapper.class);
        ConfigEntry updatedEntry = updateWrapper.getConfigEntries().get(0);
        assertFalse(updatedEntry.isPublicVisibility());
        assertThat(updatedEntry.getId(), equalTo(2L));
        assertThat(updatedEntry.getName(), equalTo("public-test"));
        assertThat(updatedEntry.getConfig(), equalTo("<pub>2</pub>"));

    }

    @Test
    public void doesNotExistPath() throws IOException {
        HttpResponse response = sendStandardRequest(UrlMapping.CONFIG_ENTRY__GET_CONFIG_ENTRY, new ConfigEntryRequest("unknown-entry"));
        assertCinnamonError(response, ErrorCode.OBJECT_NOT_FOUND, HttpStatus.SC_NOT_FOUND);
    }

}
