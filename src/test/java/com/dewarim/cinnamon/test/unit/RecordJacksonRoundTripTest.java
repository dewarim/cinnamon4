package com.dewarim.cinnamon.test.unit;

import com.dewarim.cinnamon.api.Constants;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.Meta;
import org.junit.jupiter.api.Test;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression guard for the request-class -&gt; record refactoring.
 * <p>
 * The {@code model/request} classes are being converted to records. Jackson-XML deserialization of a
 * <em>record</em> with a wrapped collection (wrapper element name != item element name) is the fragile
 * spot: it does not work on jackson-dataformat-xml 2.x but does on 3.x. This test pins the working
 * recipe so a future Jackson change or a botched conversion is caught immediately.
 * <p>
 * <b>Recipe:</b> put {@code @JacksonXmlElementWrapper} + {@code @JacksonXmlProperty} directly on the
 * (plural) record component; add a compact constructor that null-normalizes collections; scalars need
 * no annotations. The local records below mirror the three shapes in {@code model/request}.
 */
public class RecordJacksonRoundTripTest {

    // mirrors acl/CreateAclRequest: wrapped List<Acl>
    @JsonRootName("createAclRequest")
    record CreateAclLike(
            @JacksonXmlElementWrapper(localName = "acls")
            @JacksonXmlProperty(localName = "acl")
            List<Acl> acls) {
        CreateAclLike {
            if (acls == null) {
                acls = new ArrayList<>();
            }
        }
    }

    // mirrors DeleteByIdRequest: wrapped Set<Long> + boolean flag
    @JsonRootName("deleteAclRequest")
    record DeleteByIdLike(
            @JacksonXmlElementWrapper(localName = "ids")
            @JacksonXmlProperty(localName = "id")
            Set<Long> ids,
            boolean ignoreNotFound) {
        DeleteByIdLike {
            if (ids == null) {
                ids = new HashSet<>();
            }
        }
    }

    // mirrors osd/CreateOsdRequest: scalars + wrapped List<Meta>
    @JsonRootName("createOsdRequest")
    record CreateOsdLike(
            String name,
            Long folderId,
            @JacksonXmlElementWrapper(localName = "metasets")
            @JacksonXmlProperty(localName = "metaset")
            List<Meta> metasets) {
        CreateOsdLike {
            if (metasets == null) {
                metasets = new ArrayList<>();
            }
        }
    }

    @Test
    public void wrappedBeanListXmlRoundTrip() {
        var original = new CreateAclLike(List.of(new Acl("default acl"), new Acl("reviewers")));
        String xml = Constants.XML_MAPPER.writeValueAsString(original);
        assertTrue(xml.contains("<createAclRequest"), xml);
        assertTrue(xml.contains("<acls>"), xml);
        assertTrue(xml.contains("<acl>"), xml);
        assertEquals(original, Constants.XML_MAPPER.readValue(xml, CreateAclLike.class));
    }

    @Test
    public void wrappedBeanListJsonRoundTrip() {
        var original = new CreateAclLike(List.of(new Acl("default acl"), new Acl("reviewers")));
        String json = Constants.JSON_MAPPER.writeValueAsString(original);
        assertEquals(original, Constants.JSON_MAPPER.readValue(json, CreateAclLike.class));
    }

    @Test
    public void wrappedLongSetXmlRoundTrip() {
        var original = new DeleteByIdLike(new HashSet<>(Set.of(43L, 99L)), true);
        String xml = Constants.XML_MAPPER.writeValueAsString(original);
        assertTrue(xml.contains("<ids>"), xml);
        assertTrue(xml.contains("<id>"), xml);
        var parsed = Constants.XML_MAPPER.readValue(xml, DeleteByIdLike.class);
        assertEquals(original, parsed);
        assertTrue(parsed.ignoreNotFound());
    }

    @Test
    public void wrappedLongSetJsonRoundTrip() {
        var original = new DeleteByIdLike(new HashSet<>(Set.of(43L, 99L)), true);
        String json = Constants.JSON_MAPPER.writeValueAsString(original);
        assertEquals(original, Constants.JSON_MAPPER.readValue(json, DeleteByIdLike.class));
    }

    @Test
    public void scalarPlusWrappedListXmlRoundTrip() {
        var original = new CreateOsdLike("my osd", 5L, List.of(new Meta(1L, 2L, "meta-content")));
        String xml = Constants.XML_MAPPER.writeValueAsString(original);
        assertTrue(xml.contains("<name>my osd</name>"), xml);
        assertTrue(xml.contains("<metasets>"), xml);
        assertTrue(xml.contains("<metaset>"), xml);
        assertEquals(original, Constants.XML_MAPPER.readValue(xml, CreateOsdLike.class));
    }

    @Test
    public void emptyWrappedCollectionNormalizesToEmpty() {
        var original = new CreateAclLike(new ArrayList<>());
        String xml = Constants.XML_MAPPER.writeValueAsString(original);
        var parsed = Constants.XML_MAPPER.readValue(xml, CreateAclLike.class);
        assertNotNull(parsed.acls());
        assertTrue(parsed.acls().isEmpty());
    }
}
