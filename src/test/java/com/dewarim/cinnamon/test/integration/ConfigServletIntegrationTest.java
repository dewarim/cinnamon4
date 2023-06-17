package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.client.StandardResponse;
import com.dewarim.cinnamon.model.ProviderType;
import com.dewarim.cinnamon.model.UrlMappingInfo;
import com.dewarim.cinnamon.model.request.config.ListConfigRequest;
import com.dewarim.cinnamon.model.response.ConfigWrapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class ConfigServletIntegrationTest extends CinnamonIntegrationTest {

    @Test
    public void listConfig() throws IOException {
        var response = sendStandardRequest(UrlMapping.CONFIG__LIST_ALL_CONFIGURATIONS, new ListConfigRequest());

        ConfigWrapper config = parseResponse(response);
        assertFalse(config.getAcls().isEmpty());
        assertFalse(config.getFolderTypes().isEmpty());
        assertFalse(config.getFormats().isEmpty());
        assertFalse(config.getGroups().isEmpty());
        assertFalse(config.getIndexItems().isEmpty());
        assertFalse(config.getLanguages().isEmpty());
        assertFalse(config.getLifecycles().isEmpty());
        assertFalse(config.getMetasetTypes().isEmpty());
        assertFalse(config.getObjectTypes().isEmpty());
        assertFalse(config.getObjectTypes().isEmpty());
        assertFalse(config.getPermissions().isEmpty());
        assertFalse(config.getRelationTypes().isEmpty());
        assertFalse(config.getUiLanguages().isEmpty());
        assertFalse(config.getUsers().isEmpty());
        assertFalse(config.getProviderClasses().isEmpty());
        assertTrue(config.getProviderClasses().stream().anyMatch(providerClass -> providerClass.getProviderType().equals(ProviderType.CONTENT_PROVIDER)));
        assertTrue(config.getProviderClasses().stream().anyMatch(providerClass -> providerClass.getProviderType().equals(ProviderType.LOGIN_PROVIDER)));
        assertTrue(config.getProviderClasses().stream().anyMatch(providerClass -> providerClass.getProviderType().equals(ProviderType.STATE_PROVIDER)));

        // note: actual configuration items will be tested in their own servlet integration tests.

        // debug output: (note: this will cause the complete test suite to stop for unknown reasons.
        // mapper.enable(SerializationFeature.INDENT_OUTPUT);
        // mapper.writeValue(System.out, config);
    }

    private ConfigWrapper parseResponse(StandardResponse response) throws IOException {
        try(response) {
            assertResponseOkay(response);
            ConfigWrapper wrapper = mapper.readValue(response.getEntity().getContent(), ConfigWrapper.class);
            assertNotNull(wrapper);
            return wrapper;
        }
    }

    @Test
    public void urlMappings()throws IOException{
        List<UrlMappingInfo> mappings = client.listUrlMappings();
        assertEquals(UrlMapping.values().length, mappings.size());
        UrlMapping echo = UrlMapping.TEST__ECHO;
        assertTrue(mappings.contains(new UrlMappingInfo(echo.getServlet(), echo.getAction(), echo.getPath(), echo.getDescription())));
    }


}
