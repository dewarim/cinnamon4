package com.dewarim.cinnamon.test.integration;


import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.response.CinnamonContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.dewarim.cinnamon.api.Constants.JSON_MAPPER;
import static org.junit.jupiter.api.Assertions.assertFalse;


/**
 * Traditionally, Cinnamon did only use XML as content type for responses.
 * This class tests the optional use of JSON.
 */
public class JsonIntegrationTest extends CinnamonIntegrationTest{


    @BeforeAll
    public static void setup() {
        client.setMapper(JSON_MAPPER);
        client.setResponseContentType(CinnamonContentType.JSON);
    }

    @Test
    public void listAcls() throws IOException {
        List<Acl> acls = client.listAcls();
        assertFalse(acls.isEmpty());
    }
}
