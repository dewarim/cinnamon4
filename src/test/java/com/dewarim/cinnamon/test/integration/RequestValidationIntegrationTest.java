package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.client.CinnamonClient;
import com.dewarim.cinnamon.client.CinnamonClientException;
import com.dewarim.cinnamon.model.LoginType;
import com.dewarim.cinnamon.model.Meta;
import com.dewarim.cinnamon.model.UiLanguage;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.request.osd.CreateOsdRequest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Annotation-based request validation. Shows the two things the hand-written validated() methods
 * could not do - reject an empty collection by default, and say which field was wrong.
 */
public class RequestValidationIntegrationTest extends CinnamonIntegrationTest {

    private String messageOf(CinnamonClientException ex) {
        return ex.getErrorWrapper().getErrors().getFirst().getMessage();
    }

    @Test
    public void emptyIdListIsNowRejected() {
        /*
         * IdListRequest.validated() used ids.stream().allMatch(...), and allMatch on an empty
         * collection is vacuously true - so an empty request passed validation and was handed to the
         * servlet. @NotEmpty says what was meant.
         */
        CinnamonClientException ex = assertThrows(CinnamonClientException.class, () -> client.lockOsd(List.of()));
        assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
        String message = messageOf(ex);
        System.out.println("empty id list -> " + message);
        assertTrue(message.contains("ids"), "message should name the field: " + message);
    }

    @Test
    public void everyBadFieldIsNamedAtOnce() {
        // blank name, missing parentId, non-positive ownerId: one request, three violations
        CreateOsdRequest request = new CreateOsdRequest("   ", null, 0L, 1L, 1L, null, 1L, null, "<sum/>");
        CinnamonClientException ex = assertThrows(CinnamonClientException.class, () -> client.createOsd(request));
        assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
        String message = messageOf(ex);
        System.out.println("bad createOsd -> " + message);
        assertTrue(message.contains("name"), "should name 'name': " + message);
        assertTrue(message.contains("parentId"), "should name 'parentId': " + message);
        assertTrue(message.contains("ownerId"), "should name 'ownerId': " + message);
    }

    @Test
    public void nestedMetasetViolationIsReportedWithItsIndex() {
        // the @Valid cascade reaches into the list and reports metasets[0].typeId
        CreateOsdRequest request = new CreateOsdRequest("osd-with-broken-meta", createFolderId, userId, 1L, 1L,
                null, 1L, null, "<sum/>", List.of(new Meta(null, null, "<xml/>")));
        CinnamonClientException ex = assertThrows(CinnamonClientException.class, () -> client.createOsd(request));
        assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
        String message = messageOf(ex);
        System.out.println("broken metaset -> " + message);
        assertTrue(message.contains("metasets[0].typeId"), "should name the element: " + message);
    }

    /**
     * A client authenticated as a freshly created user with the given ui language. The default test
     * users all have ui_language_id 1 ("DE"), so a second user is needed to tell a message rendered
     * in the user's language apart from one rendered in the server's default locale.
     */
    private CinnamonClient clientForUserWithLanguage(String name, Long uiLanguageId) throws IOException {
        adminClient.createUser(new UserAccount(name, "passwehde", "-", "-", uiLanguageId,
                LoginType.CINNAMON.name(), true, true, true));
        return new CinnamonClient(client, name, "passwehde");
    }

    private String triggerViolation(CinnamonClient asUser) {
        CinnamonClientException ex = assertThrows(CinnamonClientException.class, () -> asUser.lockOsd(List.of()));
        assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
        return messageOf(ex);
    }

    @Test
    public void messageIsRenderedInTheUsersUiLanguage() throws IOException {
        // user "doe", like every default test user, has ui_language_id 1 -> "DE"
        String german = triggerViolation(client);
        // id 2 is "EN" in CreateTestDB.sql
        String english = triggerViolation(clientForUserWithLanguage("validation-lang-en", 2L));

        System.out.println("DE user -> " + german);
        System.out.println("EN user -> " + english);
        assertEquals("ids darf nicht leer sein", german);
        assertEquals("ids must not be empty", english);
    }

    @Test
    public void aNewUiLanguageIsPickedUpWithoutARestart() throws IOException {
        /*
         * The locales a message can be rendered in are fixed when the validator is built, so
         * UiLanguageServlet invalidates the cache on create/update/delete and the validator is rebuilt
         * with the new language. Without that call the resolver finds FR missing from the locales the
         * validator was built for and falls back to English, so this assertion is what guards the
         * invalidation. The fallback is deliberate: asking the validator for a locale it was not built
         * for fails interpolation with HV000250 and would lose the constraint violation itself.
         */
        UiLanguage french = adminClient.createUiLanguage("FR");
        String message = triggerViolation(clientForUserWithLanguage("validation-lang-fr", french.getId()));
        System.out.println("FR user -> " + message);
        assertEquals("ids ne doit pas \u00eatre vide", message);
    }

    @Test
    public void validRequestStillPasses() throws Exception {
        // the happy path must be untouched
        var osd = client.createOsd(new CreateOsdRequest("spike-valid-osd", createFolderId, userId, 1L, 1L,
                null, 1L, null, "<sum/>"));
        assertEquals("spike-valid-osd", osd.getName());
    }
}
