package com.dewarim.cinnamon.test.integration;

import com.dewarim.cinnamon.model.IndexItem;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.dewarim.cinnamon.ErrorCode.INVALID_REQUEST;
import static com.dewarim.cinnamon.ErrorCode.REQUIRES_SUPERUSER_STATUS;
import static com.dewarim.cinnamon.model.index.IndexType.DEFAULT_INDEXER;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class IndexItemServletIntegrationTest extends CinnamonIntegrationTest {

    private final static Logger log = LogManager.getLogger(IndexItemServletIntegrationTest.class);

    @Test
    public void listIndexItems() throws IOException {
        List<IndexItem> indexItems = client.listIndexItems();

        assertNotNull(indexItems);
        assertFalse(indexItems.isEmpty());

        Optional<IndexItem> itemOpt = indexItems.stream().filter(indexItem -> indexItem.getName().equals("index.acl"))
                .findFirst();
        assertTrue(itemOpt.isPresent());
        IndexItem item = itemOpt.get();
        assertThat(item.getId(), equalTo(1L));
        assertThat(item.getFieldName(), equalTo("acl"));
        assertFalse(item.isMultipleResults());
        assertThat(item.getSearchString(), equalTo("/sysMeta/object/aclId"));
        assertThat(item.getSearchCondition(), equalTo("true()"));
        assertTrue(item.isStoreField());
        assertEquals(DEFAULT_INDEXER,item.getIndexType());
        log.info("Found IndexItem: {}", mapper.configure(SerializationFeature.INDENT_OUTPUT, true).writeValueAsString(item));
    }

    @Test
    public void createIndexItemInvalidRequest() {
        assertClientError( () -> adminClient.createIndexItem(new IndexItem()),INVALID_REQUEST);
    }

    @Test
    public void createIndexItemHappyPath() throws IOException {
        IndexItem item = adminClient.createIndexItem(new IndexItem("test", true, "test.field", "/test", "true()", true, DEFAULT_INDEXER));
        assertEquals(DEFAULT_INDEXER, item.getIndexType());
        IndexItem fromList = client.listIndexItems().stream().filter(i -> i.getName().equals(item.getName())).findFirst().orElseThrow();
        // Issue 330: indexType is empty in response
        assertEquals(DEFAULT_INDEXER, fromList.getIndexType());
    }

    @Test
    public void deleteIndexItemHappyPath() throws IOException {
        IndexItem item = adminClient.createIndexItem(new IndexItem("test", true, "test.field.delete", "/test", "true()", true, DEFAULT_INDEXER));
        adminClient.deleteIndexItem(item.getId());
        assertTrue(client.listIndexItems().stream().noneMatch(i -> i.getName().equals("test.field.delete")));
    }

    @Test
    public void updateIndexItemHappyPath() throws IOException {
        IndexItem item = adminClient.createIndexItem(new IndexItem("test", true, "test.field.update", "/test", "true()", true, DEFAULT_INDEXER));
        item.setName("update.index.name");
        adminClient.updateIndexItem(item);
        List<IndexItem> items = client.listIndexItems();
        assertTrue(items.stream().noneMatch(i -> i.getName().equals("test.field.update")));
        assertTrue(items.stream().anyMatch(i -> i.getName().equals("update.index.name")));

    }

    @Test
    public void createIndexItemNonSuperuser() {
        assertClientError( () -> client.createIndexItem(new IndexItem()),REQUIRES_SUPERUSER_STATUS);
    }

    @Test
    public void deleteIndexItemNonSuperuser() {
        assertClientError( () -> client.deleteIndexItem(1L),REQUIRES_SUPERUSER_STATUS);
    }

    @Test
    public void updateIndexItemNonSuperuser() {
        assertClientError( () -> client.updateIndexItem(new IndexItem()),REQUIRES_SUPERUSER_STATUS);
    }

}
