package com.dewarim.cinnamon.application.service.index;

import org.dom4j.Node;

/**
 * <p>The CompleteStringIndexer expects an XPath parameter as searchString and will stored
 * the results of this search in the Lucene document.</p>
 * <p>Example: name="index.name", searchString="//name" will find all name-elements.
 * and stored the <i>raw</i> results of node.getText()</p>
 * <p>The difference between DefaultIndexer and CompleteStringIndexer is: the DefaultIndexer tokenizes
 * the indexed field while the CompleteStringIndexer indexes the field as-is.</p>
 */
public class CompleteStringIndexer extends DefaultIndexer {

    public CompleteStringIndexer() {
        fieldType.setTokenized(false);
    }

    public String convertNodeToString(Node node) {
        String stringValue = node.getText();
        if (stringValue == null) {
            return null;
        } else {
            return stringValue.toLowerCase();
        }
    }
}
