package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.client.CinnamonClientException;
import com.dewarim.cinnamon.model.Language;
import com.dewarim.cinnamon.model.request.osd.UpdateOsdRequest;
import com.dewarim.cinnamon.test.TestObjectHolder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LanguageServletIntegrationTest extends CinnamonIntegrationTest {

    @Test
    public void listLanguages() throws IOException {
        List<Language> languages = client.listLanguages();

        assertNotNull(languages);
        assertFalse(languages.isEmpty());

        String[] isoCodes = {"de_DE", "en_EN", "zxx", "und", "mul"};

        Arrays.stream(isoCodes).forEach(code -> {
            Language language = new Language(null, code);
            assertTrue(languages.contains(language));
        });

    }

    @Test
    public void createLanguageHappyPath() throws IOException {
        var language = adminClient.createLanguage("orc");
        assertEquals("orc", language.getIsoCode());
    }

    @Test
    public void createDuplicateLanguage() throws IOException {
        var language = adminClient.createLanguage("elf");
        var ex       = assertThrows(CinnamonClientException.class, () -> adminClient.createLanguage("elf"));
        assertEquals(ErrorCode.DB_INSERT_FAILED, ex.getErrorCode());
    }

    @Test
    public void createTwoLanguages() throws IOException{
        var languages = adminClient.createLanguages(List.of("bicorn", "unicorn"));
        assertEquals(2, languages.size());
    }

    @Test
    public void createLanguageNonSuperuser() {
        var ex = assertThrows(CinnamonClientException.class, () -> client.createLanguage("elf"));
        assertEquals(ErrorCode.REQUIRES_SUPERUSER_STATUS, ex.getErrorCode());
    }

    @Test
    public void updateLanguageHappyPath() throws IOException {
        var language = adminClient.createLanguage("dwarf");
        language.setIsoCode("gnome");
        adminClient.updateLanguage(language);
    }

    @Test
    public void updateLanguageNonSuperuser() throws IOException {
        var language = adminClient.createLanguage("troll");
        language.setIsoCode("gnome");
        var ex = assertThrows(CinnamonClientException.class, () ->
                client.updateLanguage(language));
        assertEquals(ErrorCode.REQUIRES_SUPERUSER_STATUS, ex.getErrorCode());
    }

    @Test
    public void deleteLanguageHappyPath() throws IOException {
        var language = adminClient.createLanguage("goblin");
        assertTrue(adminClient.deleteLanguage(language.getId()));
        long remaining = client.listLanguages().stream().filter(lang -> lang.getIsoCode().equals("goblin")).count();
        assertEquals(0, remaining);
    }

    @Test
    public void deleteLanguageWhichIsInUse() throws IOException {
        var toh = new TestObjectHolder(adminClient,userId);
        var osd = toh.createOsd("delete-language-in-use-test").osd;
        var language = adminClient.createLanguage("test");
        var updateRequest = new UpdateOsdRequest(osd.getId(), null,null,null,null,null,language.getId());
        client.lockOsd(osd.getId());
        assertTrue(client.updateOsd(updateRequest));
        var ex = assertThrows(CinnamonClientException.class, () -> adminClient.deleteLanguage(language.getId()));
        assertEquals(ErrorCode.DB_DELETE_FAILED, ex.getErrorCode());
    }

    @Test
    public void deleteLanguageNonSuperuser() throws IOException {
        var language = adminClient.createLanguage("client");
        var ex = assertThrows(CinnamonClientException.class, () ->
                client.deleteLanguage(language.getId()));
        assertEquals(ErrorCode.REQUIRES_SUPERUSER_STATUS, ex.getErrorCode());
    }

}
