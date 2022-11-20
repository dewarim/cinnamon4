package com.dewarim.cinnamon.application.service.index;

import com.dewarim.cinnamon.application.exception.CinnamonException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;


/**
 * Index the names of XML elements
 */
// copied from Cinnamon 3, TODO: refactoring
public class ElementNameIndexer {

    public static final  String  DOCTYPE_ENTITY            = "(<!(?:DOCTYPE|ENTITY)[^>]*>)";
    private static final Pattern DOCTYPE_OR_ENTITY_PATTERN = Pattern.compile(DOCTYPE_ENTITY);

    private final static Logger log = LogManager.getLogger(ElementNameIndexer.class);


    boolean stored = true;

    public ElementNameIndexer() {
    }


    @SuppressWarnings("unchecked")
    public void indexObject(String fieldname) {
        try {
            SAXParserFactory factory    = SAXParserFactory.newInstance();
            SAXParser        saxParser  = factory.newSAXParser();
            LexHandler       lexHandler = new LexHandler();
            saxParser.setProperty("http://xml.org/sax/properties/lexical-handler", lexHandler);
            ElementNameHandler nameHandler    = new ElementNameHandler();
            String             withoutDoctype = DOCTYPE_OR_ENTITY_PATTERN.matcher("<test><p otherprops=\"xxx\"><!-- comment --></p></test>").replaceAll("");
            saxParser.parse(new ByteArrayInputStream(withoutDoctype.getBytes(StandardCharsets.UTF_8)), nameHandler);
            Set<String> elementNames = nameHandler.getNames();
            elementNames.forEach(name -> {
                System.out.println("fieldname: " + fieldname + " value: " + name );
            });
            List<String> comments = lexHandler.getComments();
            comments.forEach(comment -> {
                System.out.println("fieldname: " + "xml.comment" + " value: " + comment );
            });
        } catch (Exception e) {
            throw new CinnamonException("Could not parse document.", e);
        }
    }

    static class LexHandler implements LexicalHandler {
        static Logger log = LogManager.getLogger(LexHandler.class);

        List<String> comments = new ArrayList<>();

        @Override
        public void startDTD(String name, String publicId, String systemId) throws SAXException {

        }

        @Override
        public void endDTD() throws SAXException {

        }

        @Override
        public void startEntity(String name) throws SAXException {

        }

        @Override
        public void endEntity(String name) throws SAXException {

        }

        @Override
        public void startCDATA() throws SAXException {

        }

        @Override
        public void endCDATA() throws SAXException {

        }

        @Override
        public void comment(char[] ch, int start, int length) throws SAXException {
            log.debug("comment: " + new String(ch, start, length).trim());
            comments.add(new String(ch,start,length).trim());
        }

        public List<String> getComments() {
            return comments;
        }
    }

    static class ElementNameHandler extends DefaultHandler {
        Set<String> names = new HashSet<>();

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            names.add(qName);
            for (int x = 0; x < attributes.getLength(); x++) {
                String attributeQname = attributes.getQName(x);
                names.add(String.format("%s/@%s", qName, attributeQname));
                names.add(String.format("%s/@%s=\"%s\"", qName, attributeQname, attributes.getValue(x)));
            }

            super.startElement(uri, localName, qName, attributes);
        }

        public Set<String> getNames() {
            return names;
        }
    }

    public static void main(String[] args) {
        new ElementNameIndexer().indexObject("xml.element");
    }

}
