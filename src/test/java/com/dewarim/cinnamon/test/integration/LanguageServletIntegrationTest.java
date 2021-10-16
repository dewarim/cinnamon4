package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.model.Language;
import com.dewarim.cinnamon.model.request.language.ListLanguageRequest;
import com.dewarim.cinnamon.model.response.LanguageWrapper;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LanguageServletIntegrationTest extends CinnamonIntegrationTest {

    @Test
    public void listLanguages() throws IOException {
        HttpResponse   response  = sendStandardRequest(UrlMapping.LANGUAGE__LIST, new ListLanguageRequest());
        List<Language> languages = parseResponse(response);

        assertNotNull(languages);
        assertFalse(languages.isEmpty());
        assertEquals(5, languages.size());

        String[] isoCodes = {"de_DE", "en_EN", "zxx", "und", "mul"};

        Arrays.stream(isoCodes).forEach(code -> {
            Language language = new Language(null, code);
            assertTrue(languages.contains(language));
        });

    }

    private List<Language> parseResponse(HttpResponse response) throws IOException {
        assertResponseOkay(response);
        LanguageWrapper LanguageWrapper = mapper.readValue(response.getEntity().getContent(), LanguageWrapper.class);
        assertNotNull(LanguageWrapper);
        return LanguageWrapper.getLanguages();
    }


}
