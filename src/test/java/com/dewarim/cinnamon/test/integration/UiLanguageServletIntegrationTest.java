package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.client.CinnamonClientException;
import com.dewarim.cinnamon.model.UiLanguage;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class UiLanguageServletIntegrationTest extends CinnamonIntegrationTest {

    @Test
    public void listUiLanguages() throws IOException {
        List<UiLanguage> uiLanguages = client.listUiLanguages();

        assertNotNull(uiLanguages);
        assertFalse(uiLanguages.isEmpty());

        Optional<UiLanguage> uiLanguageOpt = uiLanguages.stream().filter(uiLanguage -> uiLanguage.getIsoCode().equals("DE"))
                .findFirst();
        assertTrue(uiLanguageOpt.isPresent());
        UiLanguage uiLanguage = uiLanguageOpt.get();
        assertThat(uiLanguage.getId(), equalTo(1L));
        assertThat(uiLanguage.getIsoCode(), equalTo("DE"));
    }

    @Test
    public void createUiLanguageHappyPath() throws IOException {
        var language = adminClient.createUiLanguage("orc");
        assertEquals("orc", language.getIsoCode());
    }

    @Test
    public void createDuplicateUiLanguage() throws IOException {
        var language = adminClient.createUiLanguage("elf");
        var ex       = assertThrows(CinnamonClientException.class, () -> adminClient.createUiLanguage("elf"));
        assertEquals(ErrorCode.DB_INSERT_FAILED, ex.getErrorCode());
    }

    @Test
    public void createUiLanguageNonSuperuser() {
        var ex = assertThrows(CinnamonClientException.class, () -> client.createUiLanguage("elf"));
        assertEquals(ErrorCode.REQUIRES_SUPERUSER_STATUS, ex.getErrorCode());
    }

    @Test
    public void updateUiLanguageHappyPath() throws IOException {
        var language = adminClient.createUiLanguage("dwarf");
        language.setIsoCode("gnome");
        adminClient.updateUiLanguage(language);
    }

    @Test
    public void updateUiLanguageNonSuperuser() throws IOException {
        var language = adminClient.createUiLanguage("troll");
        language.setIsoCode("gnome");
        var ex = assertThrows(CinnamonClientException.class, () ->
                client.updateUiLanguage(language));
        assertEquals(ErrorCode.REQUIRES_SUPERUSER_STATUS, ex.getErrorCode());
    }

    @Test
    public void deleteUiLanguageHappyPath() throws IOException {
        var language = adminClient.createUiLanguage("goblin");
        assertTrue(adminClient.deleteUiLanguage(language.getId()));
        long remaining = client.listUiLanguages().stream().filter(lang -> lang.getIsoCode().equals("goblin")).count();
        assertEquals(0, remaining);
    }

    @Test
    public void deleteUiLanguageNonSuperuser() throws IOException {
        var language = adminClient.createUiLanguage("client");
        var ex = assertThrows(CinnamonClientException.class, () ->
                client.deleteUiLanguage(language.getId()));
        assertEquals(ErrorCode.REQUIRES_SUPERUSER_STATUS, ex.getErrorCode());
    }


}
