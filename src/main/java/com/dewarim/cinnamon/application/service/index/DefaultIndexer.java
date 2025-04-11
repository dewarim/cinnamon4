package com.dewarim.cinnamon.application.service.index;

import com.dewarim.cinnamon.model.index.Indexer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>The DefaultIndexer expects an XPath parameter as searchString and will stored
 * the results of this search in the Lucene document.</p>
 * <p>Example: name="index.name", searchString="//name" will find all name-elements.
 * and stored the <i>analyzed</i> results of node.getText()</p>
 */
// copied from Cinnamon 3, TODO: refactoring
public class DefaultIndexer implements Indexer {

    protected FieldType fieldType;
    boolean stored = false;

    public DefaultIndexer() {
        fieldType = new FieldType();
        fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
        fieldType.setStored(false);
        fieldType.setTokenized(true);
    }

    private static final Logger log = LogManager.getLogger(DefaultIndexer.class);

    @Override
    public void indexObject(org.dom4j.Document xml, Element contentNode, Document luceneDoc, String fieldName,
                            String searchString, Boolean multipleResults) {

        List<Node> hits = new ArrayList<>();

        if (multipleResults) {
            hits = xml.selectNodes(searchString);
        } else {
            Node node = xml.selectSingleNode(searchString);
            if (node != null) {
                hits.add(node);
            }
        }

        for (Node node : hits) {
            String nodeValue = convertNodeToString(node);
            if (nodeValue != null && !nodeValue.isBlank()) {
                log.trace("fieldName: {} value: {} stored:{}", fieldName, nodeValue, fieldType.stored());
                addToDoc(luceneDoc, fieldName, nodeValue, fieldType);
            } else {
                log.trace("nodeValue for '{}' is null", searchString);
            }
        }
    }

    protected void addToDoc(Document luceneDoc, String fieldName, String nodeValue, FieldType typeOfField) {
        luceneDoc.add(new Field(fieldName, nodeValue, typeOfField));
    }

    public String convertNodeToString(Node node) {
        return node.getText();
    }

    public StringBuilder descendIntoNodes(Node node) {
        List<Node>    children = node.selectNodes("descendant::*");
        StringBuilder result   = new StringBuilder();
        appendNonEmptyText(node, result);
        for (Node n : children) {
            appendNonEmptyText(n, result);
        }
        return result;
    }

    /**
     * If a node contains non-whitespace text, add it to the builder, followed by one whitespace.
     *
     * @param node    the node from which the text content is read
     * @param builder the StringBuilder to which the text is appended (that is, this parameter object will be modified)
     */
    public void appendNonEmptyText(Node node, StringBuilder builder) {
        String text = node.getText();
        if (text != null && text.trim().length() > 0) {
            builder.append(node.getText());
            builder.append(' ');
        }
        String attributeValue = ((Element) node).attributes().stream().map(Attribute::getValue).collect(Collectors.joining(" "));
        if (!attributeValue.isBlank()) {
            builder.append(attributeValue);
            builder.append(' ');
        }
    }

    public void setStored(boolean stored) {
        this.stored = stored;
        fieldType.setStored(stored);
    }

    public boolean isStored() {
        return this.stored;
    }
}
