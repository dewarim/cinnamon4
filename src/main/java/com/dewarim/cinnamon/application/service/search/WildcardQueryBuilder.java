package com.dewarim.cinnamon.application.service.search;

import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.xml.DOMUtils;
import org.apache.lucene.queryparser.xml.ParserException;
import org.apache.lucene.queryparser.xml.QueryBuilder;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.WildcardQuery;
import org.w3c.dom.Element;

/**
 * The WildcardQueryBuilder is an extension to Lucene-XML-Query-Parser.
 * It adds the ability to search for Terms with leading wildcards.
 * --> copied from Cinnamon3
 */
public class WildcardQueryBuilder implements QueryBuilder {

    @Override
    public Query getQuery(Element e) throws ParserException {
        String field = DOMUtils.getAttributeWithInheritanceOrFail(e, "fieldName");
        String value = DOMUtils.getNonBlankTextOrFail(e);
        return new WildcardQuery(new Term(field, value));
    }

}
