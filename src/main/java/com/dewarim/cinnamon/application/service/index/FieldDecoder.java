package com.dewarim.cinnamon.application.service.index;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

public class FieldDecoder {
    private static final Logger log = LogManager.getLogger(FieldDecoder.class);

    /*
     * Replace the text content of all nodes found via the XPath expression with their decoded XML elements.
     * For example, given //config replace <config>&lt;foo/&gt;</config> with <config><foo/>/config>
     */
    public void decodeField(Document doc, String xpath) {
        for (Node node : doc.selectNodes(xpath)) {
            if (!node.hasContent()) {
                // no children: nothing to do.
                continue;
            }
            Element element     = (Element) node;
            String  textContent = node.getText();
            element.clearContent();
            Node xml = ParamParser.parseXml(textContent, "Failed to parse XML: " + textContent);
            xml.detach();
            element.add(xml);
            log.trace("element: {} ", element.asXML());
        }
        if(log.isTraceEnabled()) {
            String x = doc.asXML();
            log.trace("decoded: {}", x);
        }
    }

}
