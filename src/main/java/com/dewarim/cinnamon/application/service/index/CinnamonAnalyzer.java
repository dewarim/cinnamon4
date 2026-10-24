package com.dewarim.cinnamon.application.service.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.cjk.CJKBigramFilter;
import org.apache.lucene.analysis.cjk.CJKWidthFilter;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;

/**
 * The analyzer used for indexing and for parsing search queries. Both sides must use the same one,
 * or analyzed queries stop matching what was indexed.
 * <p>
 * It is {@link StandardAnalyzer} plus two filters for CJK text:
 * <ul>
 *     <li>{@link CJKWidthFilter} folds full-width Latin to ASCII and half-width Katakana to
 *     full-width. Documents authored in Asia routinely write part numbers in full width, so without
 *     this a search for {@code ABC-123} does not find a document containing {@code ＡＢＣ－１２３}.</li>
 *     <li>{@link CJKBigramFilter} indexes overlapping bigrams of CJK text. StandardTokenizer follows
 *     UAX#29 and therefore emits one token per Han character, so {@code 液压泵} became
 *     {@code [液, 压, 泵]}: a search for the word itself matched nothing, and single Han characters
 *     are so common that relevance was poor. With bigrams it becomes {@code [液压, 压泵]}.</li>
 * </ul>
 * Latin text is unaffected: CJKBigramFilter only combines tokens whose script is Han, Hiragana,
 * Katakana or Hangul, and everything else passes through untouched. CinnamonAnalyzerUnitTest pins
 * that down by asserting the output is identical to StandardAnalyzer's for Latin input.
 * <p>
 * Note that this deliberately does <em>not</em> use Lucene's ready-made
 * {@link org.apache.lucene.analysis.cjk.CJKAnalyzer}, which bundles an English stop-word list: that
 * would drop the trailing "A" from a part number such as {@code P/N 4711-A}, and in technical
 * documentation such a letter is usually a variant or revision designator.
 */
public class CinnamonAnalyzer extends Analyzer {

    private final boolean outputUnigrams;

    /**
     * @param outputUnigrams also index each CJK character on its own, in addition to the bigrams.
     *                       Off by default: words of two or more characters - which covers
     *                       practically all Chinese and Japanese vocabulary, and all technical
     *                       terminology - are searchable without it, while it roughly doubles the
     *                       number of CJK terms. Turn it on where a single character has to be
     *                       searchable as a term of its own, for example in a parts list.
     */
    public CinnamonAnalyzer(boolean outputUnigrams) {
        this.outputUnigrams = outputUnigrams;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        StandardTokenizer tokenizer = new StandardTokenizer();
        // same cap StandardAnalyzer applies: a single unbroken "word" of a million characters is
        // split into 255-character tokens instead of being buffered whole
        tokenizer.setMaxTokenLength(StandardAnalyzer.DEFAULT_MAX_TOKEN_LENGTH);

        TokenStream tokenStream = new CJKWidthFilter(tokenizer);
        tokenStream = new LowerCaseFilter(tokenStream);
        tokenStream = new CJKBigramFilter(tokenStream,
                CJKBigramFilter.HAN | CJKBigramFilter.HIRAGANA | CJKBigramFilter.KATAKANA | CJKBigramFilter.HANGUL,
                outputUnigrams);
        return new TokenStreamComponents(tokenizer, tokenStream);
    }

    @Override
    protected TokenStream normalize(String fieldName, TokenStream in) {
        return new LowerCaseFilter(new CJKWidthFilter(in));
    }
}
