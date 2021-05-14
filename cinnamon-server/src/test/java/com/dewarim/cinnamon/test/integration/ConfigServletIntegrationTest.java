package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.model.request.config.ListConfigRequest;
import com.dewarim.cinnamon.model.response.ConfigWrapper;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class ConfigServletIntegrationTest extends CinnamonIntegrationTest {

    @Test
    public void listConfig() throws IOException {
        HttpResponse response = sendStandardRequest(UrlMapping.CONFIG__LIST_ALL_CONFIGURATIONS, new ListConfigRequest());

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

        // note: actual configuration items will be tested in their own servlet integration tests.

        // debug output: (note: this will cause the complete test suite to stop for unknown reasons.
        // mapper.enable(SerializationFeature.INDENT_OUTPUT);
        // mapper.writeValue(System.out, config);
    }

    private ConfigWrapper parseResponse(HttpResponse response) throws IOException {
        assertResponseOkay(response);
        ConfigWrapper wrapper = mapper.readValue(response.getEntity().getContent(), ConfigWrapper.class);
        assertNotNull(wrapper);
        return wrapper;
    }


}
