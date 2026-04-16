package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.client.StandardResponse;
import com.dewarim.cinnamon.dao.ChangeTriggerDao;
import com.dewarim.cinnamon.model.ChangeTrigger;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.request.changeTrigger.UpdateChangeTriggerRequest;
import com.dewarim.cinnamon.model.response.ChangeTriggerResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.dewarim.cinnamon.ErrorCode.REQUIRES_SUPERUSER_STATUS;
import static com.dewarim.cinnamon.api.UrlMapping.CHANGE_TRIGGER__UPDATE;
import static com.dewarim.cinnamon.api.UrlMapping.OSD__SET_CONTENT;
import static com.dewarim.cinnamon.filter.ChangeTriggerFilter.activeChangeTriggerFilter;
import static com.dewarim.cinnamon.model.ChangeTriggerType.MICROSERVICE;
import static com.dewarim.cinnamon.model.ChangeTriggerType.NOP_TRIGGER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class ChangeTriggerServletIntegrationTest extends CinnamonIntegrationTest {
    private final ChangeTrigger changeTrigger = new ChangeTrigger(1L, "triggerThumbnailGenerator", "osd", "setContent", true,
            false, true, true, "<config><remoteServer>http://localhost:" + cinnamonTestPort + "/echo</remoteServer></config>",
            NOP_TRIGGER, 100, false);

    @Test
    public void list() throws IOException {
        List<ChangeTrigger> triggers = client.listChangeTriggers();
        assertNotNull(triggers);
        assertFalse(triggers.isEmpty());
        ChangeTrigger testTrigger = triggers.getFirst();
        assertEquals(MICROSERVICE, testTrigger.getTriggerType());

    }

    @Test
    public void createChangeTriggerHappyPath() throws IOException {
        var trigger = adminClient.createChangeTrigger(changeTrigger);
        assertEquals(changeTrigger.getName(), trigger.getName());
        assertEquals(changeTrigger.getTriggerType(), trigger.getTriggerType());
        assertEquals(changeTrigger.isPostTrigger(), trigger.isPostTrigger());
        assertEquals(changeTrigger.isPreTrigger(), trigger.isPreTrigger());
        assertEquals(changeTrigger.getConfig(), trigger.getConfig());
        assertEquals(changeTrigger.getAction(), trigger.getAction());
        assertEquals(changeTrigger.getController(), trigger.getController());
        assertEquals(changeTrigger.isActive(), trigger.isActive());
        assertEquals(changeTrigger.getRanking(), trigger.getRanking());
        assertEquals(changeTrigger.isCopyFileContent(), trigger.isCopyFileContent());
        assertNotNull(trigger.getId());
    }

    @Test
    public void createChangeTriggerWithoutSuperuserStatus() {
        assertClientError(() -> client.createChangeTrigger(changeTrigger), REQUIRES_SUPERUSER_STATUS);
    }

    @Test
    public void changeTriggerCacheInvalidationOnCreateUpdateDelete() throws IOException {
        String triggerName = "cache-invalidation-" + UUID.randomUUID();
        ChangeTriggerDao.invalidateChangeTriggerCache();
        ChangeTriggerDao dao = new ChangeTriggerDao();

        // Prime cache once.
        List<ChangeTrigger> before = dao.listCached();
        assertTrue(before.stream().noneMatch(ct -> triggerName.equals(ct.getName())));

        ChangeTrigger created = adminClient.createChangeTrigger(new ChangeTrigger(null, triggerName, "osd", "setContent", true,
                false, true, false, "<config/>", NOP_TRIGGER, 100, false));
        // Refresh the test thread's SqlSession so the cache re-fill sees the committed data.
        ThreadLocalSqlSession.refreshSession();

        // Cache invalidation may be observed with a short delay, so poll briefly.
        ChangeTrigger cachedCreated = findTriggerInCacheByName(dao, triggerName, 1000)
                .orElseThrow(() -> new AssertionError("Created trigger not visible after cache invalidation."));
        assertEquals(triggerName, cachedCreated.getName());

        Long effectiveId = created.getId() != null ? created.getId() : cachedCreated.getId();
        assertNotNull(effectiveId, "Created trigger id could not be determined.");
        if (created.getId() == null) {
            created.setId(effectiveId);
        }

        created.setAction("createOsd");
        updateChangeTrigger(created);
        // Refresh session so the cache re-fill after update sees committed state.
        ThreadLocalSqlSession.refreshSession();

        // Cache must be invalidated by update and show changed action.
        ChangeTrigger updatedTrigger = findTriggerInCacheById(dao, effectiveId, 1000)
                .orElseThrow(() -> new AssertionError("Updated trigger not found in cache snapshot."));
        assertEquals("createOsd", updatedTrigger.getAction());
        assertTrue(dao.findApplicableTriggers(OSD__SET_CONTENT).stream().noneMatch(ct -> effectiveId.equals(ct.getId())));

        adminClient.deleteChangeTrigger(effectiveId);
        // Refresh session so the cache re-fill after delete sees committed state.
        ThreadLocalSqlSession.refreshSession();

        // Cache must be invalidated by delete and no longer contain the trigger id.
        assertTrue(findTriggerInCacheById(dao, effectiveId, 1000).isEmpty());
    }

    private Optional<ChangeTrigger> findTriggerInCacheByName(ChangeTriggerDao dao, String name, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            Optional<ChangeTrigger> trigger = dao.listCached().stream()
                    .filter(ct -> name.equals(ct.getName()))
                    .findFirst();
            if (trigger.isPresent()) {
                return trigger;
            }
            sleepBriefly();
        }
        return Optional.empty();
    }

    private Optional<ChangeTrigger> findTriggerInCacheById(ChangeTriggerDao dao, Long triggerId, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            Optional<ChangeTrigger> trigger = dao.listCached().stream()
                    .filter(ct -> triggerId.equals(ct.getId()))
                    .findFirst();
            if (trigger.isPresent()) {
                return trigger;
            }
            sleepBriefly();
        }
        return Optional.empty();
    }

    private void sleepBriefly() {
        try {
            Thread.sleep(20);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Interrupted while waiting for change trigger cache update.");
        }
    }

    private void updateChangeTrigger(ChangeTrigger changeTrigger) throws IOException {
        UpdateChangeTriggerRequest request = new UpdateChangeTriggerRequest(List.of(changeTrigger));
        String url = HOST + CHANGE_TRIGGER__UPDATE.getPath();
        try (StandardResponse response = httpClient.execute(ClassicRequestBuilder.post(url)
                .addHeader("ticket", ticket)
                .setEntity(mapper.writeValueAsString(request))
                .build(), StandardResponse::new)) {
            assertResponseOkay(response);
        }
    }

    @Test
    @Disabled("this is tested in MCT integration test (where we have a mocked server to respond)")
    public void nopTest() throws IOException {
        List<ChangeTriggerResponse> nops = client.changeTriggerNop(false);
        assertNotNull(nops);
    }

    // TODO: test changeTrigger-update + delete with/without superuser

    @Test
    public void ctActiveTest()  {

        UserAccount au = new UserAccount();
        au.setActivateTriggers(true);
        UserAccount pu = new UserAccount();
        pu.setActivateTriggers(false);
        CinnamonResponse response1 = new CinnamonResponse(mock(HttpServletRequest.class),mock(HttpServletResponse.class));
        CinnamonResponse response2 = new CinnamonResponse(mock(HttpServletRequest.class),mock(HttpServletResponse.class));

        assertTrue(activeChangeTriggerFilter(au, response1));
        assertFalse( activeChangeTriggerFilter(pu, response2));

        response1.setUser(au);
        response2.setUser(pu);
        assertTrue(activeChangeTriggerFilter(null, response1));
        assertFalse(activeChangeTriggerFilter(null, response2));

    }


}
