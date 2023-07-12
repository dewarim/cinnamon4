package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.model.UiLanguage;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.dewarim.cinnamon.ErrorCode.*;
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
        adminClient.createUiLanguage("elf");
        assertClientError( () -> adminClient.createUiLanguage("elf"),DB_INSERT_FAILED);
    }

    @Test
    public void createUiLanguageNonSuperuser() {
        assertClientError(() -> client.createUiLanguage("elf"), REQUIRES_SUPERUSER_STATUS);
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
        assertClientError(() ->
                client.updateUiLanguage(language), REQUIRES_SUPERUSER_STATUS);
    }

    @Test
    public void updateUiLanguageDuplicateName() throws IOException {
        var language1 = adminClient.createUiLanguage("AA");
        var language2 = adminClient.createUiLanguage("ZZ");
        language2.setIsoCode("AA");
        assertClientError(() ->
                adminClient.updateUiLanguage(language2), DB_UPDATE_FAILED);
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
        assertClientError(() ->
                client.deleteUiLanguage(language.getId()), REQUIRES_SUPERUSER_STATUS);
    }


}
