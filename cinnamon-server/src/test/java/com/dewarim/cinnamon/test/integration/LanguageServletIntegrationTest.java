package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.application.UrlMapping;
import com.dewarim.cinnamon.model.Language;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.LanguageWrapper;
import org.apache.http.HttpResponse;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class LanguageServletIntegrationTest extends CinnamonIntegrationTest {

    @Test
    public void listLanguages() throws IOException {
        HttpResponse   response  = sendStandardRequest(UrlMapping.LANGUAGE__LIST__LANGUAGES, new ListRequest());
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
