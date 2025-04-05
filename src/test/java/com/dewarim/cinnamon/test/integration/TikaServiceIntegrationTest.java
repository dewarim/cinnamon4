package com.dewarim.cinnamon.test.integration;


import com.dewarim.cinnamon.application.service.TikaService;
import com.dewarim.cinnamon.configuration.CinnamonTikaConfig;
import com.dewarim.cinnamon.model.Format;
import com.dewarim.cinnamon.model.IndexMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TikaServiceIntegrationTest {
    private static final Logger log = LogManager.getLogger(TikaServiceIntegrationTest.class);

    private final String baseUrl     = "http://localhost:9998";
    // cinnamon-bun.png has exif metadata from stable diffusion containing the prompt "delicious cinnamon bun"
    private final File   bun         = new File("data/cinnamon-bun.png");
    private final Format imageFormat = new Format("image/png", "png", "image-png", 1L, IndexMode.TIKA);
    private final long   osdId       = 0L;

    @Disabled("Requires Apache Tika server @ localhost:9998")
    @Test
    public void tikaHappyTest() throws IOException {
        CinnamonTikaConfig cinnamonTikaConfig = new CinnamonTikaConfig(baseUrl, true);
        String             bunData            = new TikaService(cinnamonTikaConfig).parseData(bun, imageFormat, osdId);
        log.info("bunData:\n" + bunData);
        assertTrue(bunData.contains("delicious cinnamon bun"));
    }

    @Test
    public void disabledTika() throws IOException {
        CinnamonTikaConfig cinnamonTikaConfig = new CinnamonTikaConfig(baseUrl, false);
        String             bunData            = new TikaService(cinnamonTikaConfig).parseData(bun, imageFormat, osdId);
        assertEquals("<tikaIsDisabled/>", bunData);
    }

}
