package com.dewarim.cinnamon.application.service.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CinnamonAnalyzerUnitTest {

    private static final String HYDRAULIC_PUMP         = "液压泵";              // 液压泵
    private static final String HYDRAULIC_PUMP_FAILURE = "液压泵故障";  // 液压泵故障
    private static final String FULL_WIDTH_PART_NUMBER = "ＡＢＣ－１２３"; // ＡＢＣ－１２３
    private static final String HALF_WIDTH_KATAKANA    = "ﾊﾞｲﾄﾛ";  // ﾊﾞｲﾄﾛ

    private List<String> tokens(Analyzer analyzer, String text) throws IOException {
        List<String> result = new ArrayList<>();
        try (TokenStream tokenStream = analyzer.tokenStream("content", text)) {
            CharTermAttribute term = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                result.add(term.toString());
            }
            tokenStream.end();
        }
        return result;
    }

    private List<String> analyze(String text) throws IOException {
        try (Analyzer analyzer = new CinnamonAnalyzer(false)) {
            return tokens(analyzer, text);
        }
    }

    private List<String> analyzeWithUnigrams(String text) throws IOException {
        try (Analyzer analyzer = new CinnamonAnalyzer(true)) {
            return tokens(analyzer, text);
        }
    }

    /**
     * The whole risk of adding the CJK filters is breaking existing European content, so this is the
     * assertion that matters most: for Latin input the analyzer must produce exactly what
     * StandardAnalyzer produced before.
     */
    @Test
    public void latinTextIsTokenizedExactlyAsBefore() throws IOException {
        List<String> samples = List.of(
                "Hydraulikpumpe P/N 4711-A austauschen",
                "Replace the hydraulic pump before operating",
                "Betriebsanleitung Maßstab Größe STRASSE",
                "model XJ-900/B rev. 2.1",
                "TÜV-geprüft, Änderung vorbehalten",
                "",
                "   ",
                "single");
        try (Analyzer standard = new StandardAnalyzer();
             Analyzer cinnamon = new CinnamonAnalyzer(false)) {
            for (String text : samples) {
                assertEquals(tokens(standard, text), tokens(cinnamon, text),
                        "Latin text must tokenize exactly as StandardAnalyzer did: '" + text + "'");
            }
        }
    }

    @Test
    public void cjkTextIsIndexedAsOverlappingBigrams() throws IOException {
        // StandardTokenizer alone produced [液, 压, 泵], so the word itself was not a term
        assertEquals(List.of("液压", "压泵"), analyze(HYDRAULIC_PUMP));
        assertEquals(List.of("液压", "压泵", "泵故", "故障"),
                analyze(HYDRAULIC_PUMP_FAILURE));
    }

    @Test
    public void twoCharacterWordBecomesASingleTerm() throws IOException {
        // so even a plain TermQuery from the client finds it
        assertEquals(List.of("中文"), analyze("中文"));
    }

    @Test
    public void cjkWordsOfTwoOrMoreCharactersAreFoundWithoutUnigrams() throws IOException {
        List<String> indexed = analyze(HYDRAULIC_PUMP_FAILURE);
        // 液压 (hydraulic), 液压泵 (hydraulic pump) and 故障 (failure) all share a term with the document
        for (String query : List.of("液压", "液压泵", "故障")) {
            assertTrue(analyze(query).stream().anyMatch(indexed::contains),
                    "query '" + query + "' must share a term with the indexed document");
        }
    }

    @Test
    public void singleCharacterQueryNeedsUnigrams() throws IOException {
        String pump = "泵"; // 泵, a single character inside 液压泵故障

        List<String> withoutUnigrams = analyze(HYDRAULIC_PUMP_FAILURE);
        assertTrue(analyze(pump).stream().noneMatch(withoutUnigrams::contains),
                "without unigrams a single character is not a term of its own");

        List<String> withUnigrams = analyzeWithUnigrams(HYDRAULIC_PUMP_FAILURE);
        assertTrue(analyzeWithUnigrams(pump).stream().anyMatch(withUnigrams::contains),
                "with unigrams enabled a single character becomes findable");
        assertTrue(withUnigrams.size() > withoutUnigrams.size(),
                "unigrams add terms, which is why they are off by default");
    }

    /**
     * Documents authored in Asia routinely write part numbers in full-width characters. Without the
     * width filter a search for ABC-123 would not find a document containing ＡＢＣ－１２３.
     */
    @Test
    public void fullWidthLatinIsFoldedToAscii() throws IOException {
        assertEquals(List.of("abc", "123"), analyze(FULL_WIDTH_PART_NUMBER));
        assertEquals(analyze("ABC-123"), analyze(FULL_WIDTH_PART_NUMBER),
                "full-width and ASCII spelling of a part number must produce the same terms");
    }

    @Test
    public void halfWidthKatakanaIsNormalised() throws IOException {
        // normalised to full width, then bigrammed: バイ, イト, トロ
        assertEquals(List.of("バイ", "イト", "トロ"), analyze(HALF_WIDTH_KATAKANA));
    }

    @Test
    public void mixedScriptKeepsLatinAndBigramsCjk() throws IOException {
        assertEquals(List.of("液压", "压泵", "hydraulikpumpe", "p", "n", "4711", "a"),
                analyze(HYDRAULIC_PUMP + " Hydraulikpumpe P/N 4711-A"));
    }

    /**
     * Guards against anyone replacing this with Lucene's ready-made CJKAnalyzer: that one applies an
     * English stop-word list and would silently drop the trailing "A" of a part number, which in
     * technical documentation is usually a variant or revision designator.
     */
    @Test
    public void noStopWordsAreRemoved() throws IOException {
        String partNumber = "Hydraulikpumpe P/N 4711-A austauschen";
        assertTrue(analyze(partNumber).contains("a"), "the revision letter 'A' must survive");
        assertTrue(analyze("Replace the hydraulic pump").contains("the"), "no stop-word filtering");

        try (Analyzer stock = new CJKAnalyzer()) {
            assertTrue(!tokens(stock, partNumber).contains("a"),
                    "sanity check: the stock CJKAnalyzer really does drop it, which is why we build our own");
        }
    }

    /**
     * StandardAnalyzer caps token length at 255 characters, so a single unbroken "word" of a million
     * characters cannot be buffered whole. That bound must survive in our own analyzer.
     */
    @Test
    public void tokenLengthStaysCappedAt255() throws IOException {
        List<String> result = analyze("a".repeat(100_000));
        assertEquals(255, result.stream().mapToInt(String::length).max().orElseThrow());
        assertTrue(result.size() > 1, "an over-long token is split rather than kept whole");
    }

    @Test
    public void emojiAndCjkAreUnaffectedByTheDefaultLocale() throws IOException {
        // emoji are not cased characters, so they survive analysis unchanged
        assertEquals(List.of("💩"), analyze("💩"));
        assertEquals(List.of("report", "💩", "draft"), analyze("report 💩 draft"));
    }

    /**
     * Analysis must not depend on the server's default locale. Of all 245 languages the JVM knows,
     * only Turkish, Azeri and Lithuanian lowercase differently, and they all do it to "I" - so a
     * Turkish-locale server would otherwise index "INVOICE" as "ınvoıce" with a dotless i.
     */
    @Test
    public void analysisDoesNotDependOnTheDefaultLocale() throws IOException {
        Locale original = Locale.getDefault();
        try {
            List<String> expected;
            Locale.setDefault(Locale.ROOT);
            expected = analyze("INVOICE TITLE DIAGRAM");
            assertEquals(List.of("invoice", "title", "diagram"), expected);

            for (Locale locale : List.of(Locale.US, Locale.GERMANY, Locale.of("tr", "TR"),
                    Locale.of("az"), Locale.of("lt"))) {
                Locale.setDefault(locale);
                assertEquals(expected, analyze("INVOICE TITLE DIAGRAM"),
                        "analysis must be identical under locale " + locale);
            }
        } finally {
            Locale.setDefault(original);
        }
    }
}
