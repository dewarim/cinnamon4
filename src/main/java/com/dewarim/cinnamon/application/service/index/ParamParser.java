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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// copied from Cinnamon 3
public class ParamParser {
    private static final Logger log = LogManager.getLogger(ParamParser.class);

    private static final Pattern DOCTYPE_OR_ENTITY_PATTERN = Pattern.compile(ElementNameIndexer.DOCTYPE_ENTITY);

    public static Node parseXml(String xml, String message) {
        return parseXmlToDocument(DOCTYPE_OR_ENTITY_PATTERN.matcher(xml).replaceAll(""), message).getRootElement().detach();
    }

    public static Document parseXmlToDocument(String xml) {
        return parseXmlToDocument(DOCTYPE_OR_ENTITY_PATTERN.matcher(xml).replaceAll(""), null);
    }

    public static final Pattern bomReplacer  = Pattern.compile("^(?:\\xEF\\xBB\\xBF|\uFEFF)");
    public static final String  UNICODE_ZERO = "&#0;";

    public static Document parseXmlToDocument(String xmlDocument, String message) {
        String xml = xmlDocument;
        if (message == null) {
            message = "error.parse.xml";
        }
        try {
            // remove BOM on UTF-8 Strings.
            Matcher matcher = bomReplacer.matcher(xml);
            xml = matcher.replaceFirst("");

            // very large document handling:
            if(xml.length() > 10_000_000){
                xml = stripWhitespace(xml);
            }

            // TikaService now replaces bad entity, keep it for now :
            if (xml.contains(UNICODE_ZERO)) {
                xml = xml.replace(UNICODE_ZERO, "");
            }
            SAXReader reader = new SAXReader();
            // ignore dtd-declarations, do not load external entities
            reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            reader.setFeature("http://xml.org/sax/features/external-general-entities", false);
            reader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            reader.setIncludeExternalDTDDeclarations(false);
            // do not validate - we are only interested in receiving a doc.
            reader.setValidation(false);
            return reader.read(new StringReader(xml));
        } catch (IOException|DocumentException | SAXException e) {
            log.debug("ParamParser::DocumentException::", e);
            throw new RuntimeException(message);
        }
    }

    private static String stripWhitespace(String xml) throws IOException {
        BufferedReader bis = new BufferedReader(new StringReader(xml));
        StringBuilder builder = new StringBuilder();
        String line;
        int whitespaceCount = 0;
        while((line = bis.readLine()) != null){
            String trimmedLine = line.trim();
            if(trimmedLine.length() < line.length()){
                whitespaceCount += line.length() - trimmedLine.length();
                builder.append(trimmedLine);
            }
            else{
                builder.append(trimmedLine);
            }
            builder.append("\n");
        }
        log.debug("removed {} whitespace characters, {} remain", whitespaceCount, builder.length());
        return builder.toString();
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
