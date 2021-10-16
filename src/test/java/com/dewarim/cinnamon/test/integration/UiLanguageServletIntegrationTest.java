package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.model.UiLanguage;
import com.dewarim.cinnamon.model.request.uiLanguage.ListUiLanguageRequest;
import com.dewarim.cinnamon.model.response.UiLanguageWrapper;
import org.apache.http.HttpResponse;
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
        HttpResponse       response      = sendStandardRequest(UrlMapping.UI_LANGUAGE__LIST, new ListUiLanguageRequest());
        List<UiLanguage> uiLanguages = parseResponse(response);

        assertNotNull(uiLanguages);
        assertFalse(uiLanguages.isEmpty());
        assertEquals(2, uiLanguages.size());
        
        Optional<UiLanguage> uiLanguageOpt = uiLanguages.stream().filter(uiLanguage -> uiLanguage.getIsoCode().equals("DE"))
                .findFirst();
        assertTrue(uiLanguageOpt.isPresent());
        UiLanguage uiLanguage = uiLanguageOpt.get();
        assertThat(uiLanguage.getId(), equalTo(1L));
        assertThat(uiLanguage.getIsoCode(), equalTo("DE"));
    }

    private List<UiLanguage> parseResponse(HttpResponse response) throws IOException {
        assertResponseOkay(response);
        UiLanguageWrapper uiLanguageWrapper = mapper.readValue(response.getEntity().getContent(), UiLanguageWrapper.class);
        assertNotNull(uiLanguageWrapper);
        return uiLanguageWrapper.getUiLanguages();
    }


}
