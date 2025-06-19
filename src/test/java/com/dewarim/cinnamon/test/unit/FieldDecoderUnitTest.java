package com.dewarim.cinnamon.test.unit;

import com.dewarim.cinnamon.application.service.index.FieldDecoder;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FieldDecoderUnitTest {

    String encoded = "<xml><foo><config>&lt;bar&gt;123&lt;/bar&gt;</config></foo></xml>";

    String decoded = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xml><foo><config><bar>123</bar></config></foo></xml>";

    @Test
    public void decodeFieldTest() throws DocumentException {
        Document doc = DocumentHelper.parseText(encoded);
        new FieldDecoder().decodeField(doc, "//foo/config");
        assertEquals(decoded, doc.asXML());
        System.out.println(doc.asXML());
    }

}
