package com.dewarim.cinnamon.application.service.index;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.SAXException;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// copied from Cinnamon 3
public class ParamParser {
    private static final Logger log = LogManager.getLogger();


    private static final Pattern DOCTYPE_OR_ENTITY_PATTERN = Pattern.compile(ElementNameIndexer.DOCTYPE_ENTITY);

    public static Node parseXml(String xml, String message) {
        return parseXmlToDocument(DOCTYPE_OR_ENTITY_PATTERN.matcher(xml).replaceAll(""), message).getRootElement().detach();
    }

    public static Document parseXmlToDocument(String xml) {
        return parseXmlToDocument(DOCTYPE_OR_ENTITY_PATTERN.matcher(xml).replaceAll(""), null);
    }

    public static final Pattern bomReplacer           = Pattern.compile("^(?:\\xEF\\xBB\\xBF|\uFEFF)");
    public static final Pattern tikaBadEntityReplacer = Pattern.compile("&#0;");

    public static Document parseXmlToDocument(String xmlDocument, String message) {
        String xml = xmlDocument;
        if (message == null) {
            message = "error.parse.xml";
        }
        try {
            // remove BOM on UTF-8 Strings.
            Matcher matcher = bomReplacer.matcher(xml);
            xml = matcher.replaceAll("");
            Matcher tikaMatcher = tikaBadEntityReplacer.matcher(xml);
            xml = tikaMatcher.replaceAll(" ");
            SAXReader reader = new SAXReader();
            // ignore dtd-declarations, do not load external entities
            reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            reader.setFeature("http://xml.org/sax/features/external-general-entities", false);
            reader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            reader.setIncludeExternalDTDDeclarations(false);
            // do not validate - we are only interested in receiving a doc.
            reader.setValidation(false);
//            reader.setMergeAdjacentText(true);
//            reader.setStripWhitespaceText(true);
//            reader.setStringInternEnabled(true);
            return reader.read(new StringReader(xml));
        } catch (DocumentException | SAXException e) {
            log.debug("ParamParser::DocumentException::for content {}", e, xml);
            throw new RuntimeException(message);
        }
    }

    public static String dateToIsoString(Date date) {
        return String.format("%1$tFT%1$tT", date);
    }

    // Could be placed in a more specific XML class.

    /**
     * Turn a raw XML string and return it in an indented
     * and more human-readable form.
     *
     * @param xml an XML document as string.
     * @return an indented version of the input XML.
     */
    public static String prettyPrint(String xml) {
        String prettyXML;
        try {
            OutputFormat format = OutputFormat.createPrettyPrint();
            Document     doc    = DocumentHelper.parseText(xml);
            StringWriter sw     = new StringWriter();
            XMLWriter    xw     = new XMLWriter(sw, format);
            xw.write(doc);
            prettyXML = sw.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return prettyXML;
    }
}
