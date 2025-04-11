/*
 * Copyright (c) 2012 Ingo Wiarda
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE
 */
package com.dewarim.cinnamon.application.service.search;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class ResultCollector implements Collector {

    private static final Logger log = LogManager.getLogger(ResultCollector.class);

    private       Set<Document> documents = new HashSet<>();
    private final IndexSearcher searcher;
    private       Set<Integer>  hits      = new HashSet<>();

    public ResultCollector(IndexSearcher searcher) {
        this.searcher = searcher;
    }

    @Override
    public LeafCollector getLeafCollector(LeafReaderContext leafReaderContext) {

        final int docBase = leafReaderContext.docBase;
        return new LeafCollector() {

            @Override
            public void setScorer(Scorable scorer) throws IOException {
                // ignore scorer
            }

            public void collect(int doc) throws IOException {
                log.debug("adding leaf hit for doc id {}", doc);
                hits.add(doc + docBase);
                Document d = searcher.doc(doc + docBase);
                documents.add(d);
            }

        };
    }

    @Override
    public ScoreMode scoreMode() {
        return ScoreMode.COMPLETE_NO_SCORES;
    }

    public Set<Document> getDocuments() {
        return documents;
    }
}
