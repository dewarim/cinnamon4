package com.dewarim.cinnamon.application.service.index;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.List;

/**
 *
 */
public class AttributeStringIndexer extends DefaultIndexer {
    public String convertNodeToString(Node node) {
        StringBuilder builder = descendIntoNodes(node);
        return builder.toString().trim();
    }

    public StringBuilder descendIntoNodes(Node node) {
        List<Attribute>    children = ((Element) node).attributes();
        StringBuilder result   = new StringBuilder();
        for (Attribute n : children) {
            result.append(n.getStringValue());
            result.append(" ");
        }
        return result;
    }
}
