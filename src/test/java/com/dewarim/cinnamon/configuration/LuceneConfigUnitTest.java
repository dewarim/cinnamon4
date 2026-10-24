package com.dewarim.cinnamon.configuration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The effective indexable content size is the smaller of an absolute megabyte value and a percentage
 * of maximum heap, so that one configuration stays sensible on machines with different heap sizes.
 */
public class LuceneConfigUnitTest {

    private static final long MB = 1024L * 1024L;

    @Test
    public void defaultIsTwoGigabytesAbsoluteWithNoHeapPercentage() {
        LuceneConfig config = new LuceneConfig();
        assertEquals(2048, config.getMaxIndexableContentSizeMB());
        assertEquals(0, config.getMaxIndexableContentPercentOfHeap());
        assertEquals(2048 * MB, config.getMaxIndexableContentSizeInBytes());
    }

    @Test
    public void absoluteValueIsConvertedFromMegabytesToBytes() {
        LuceneConfig config = new LuceneConfig();
        config.setMaxIndexableContentSizeMB(10);
        assertEquals(10 * MB, config.getMaxIndexableContentSizeInBytes());
    }

    @Test
    public void zeroMeansNoLimitAtAll() {
        LuceneConfig config = new LuceneConfig();
        config.setMaxIndexableContentSizeMB(0);
        config.setMaxIndexableContentPercentOfHeap(0);
        assertEquals(0, config.getMaxIndexableContentSizeInBytes(), "0 = unlimited");
    }

    @Test
    public void heapPercentageIsUsedWhenNoAbsoluteValueIsSet() {
        LuceneConfig config = new LuceneConfig();
        config.setMaxIndexableContentSizeMB(0);
        config.setMaxIndexableContentPercentOfHeap(50);

        long expected = Runtime.getRuntime().maxMemory() / 100L * 50;
        assertEquals(expected, config.getMaxIndexableContentSizeInBytes());
        assertTrue(expected > 0, "sanity: the test JVM reports a maximum heap");
    }

    @Test
    public void theSmallerOfTheTwoLimitsApplies() {
        long maxHeap = Runtime.getRuntime().maxMemory();

        // a huge absolute value, so the heap percentage is the binding constraint
        LuceneConfig heapBound = new LuceneConfig();
        heapBound.setMaxIndexableContentSizeMB(1024L * 1024L); // 1 TB
        heapBound.setMaxIndexableContentPercentOfHeap(10);
        assertEquals(maxHeap / 100L * 10, heapBound.getMaxIndexableContentSizeInBytes(),
                "the heap percentage must win when it is smaller");

        // a tiny absolute value, so it is the binding constraint
        LuceneConfig absoluteBound = new LuceneConfig();
        absoluteBound.setMaxIndexableContentSizeMB(1);
        absoluteBound.setMaxIndexableContentPercentOfHeap(100);
        assertEquals(MB, absoluteBound.getMaxIndexableContentSizeInBytes(),
                "the absolute value must win when it is smaller");
    }

    @Test
    public void maxQueryTokenCountIsLimitedByDefault() {
        // it used to be wired to Integer.MAX_VALUE, which limited nothing
        LuceneConfig config = new LuceneConfig();
        assertEquals(100_000, config.getMaxQueryTokenCount());
        assertTrue(config.getMaxQueryTokenCount() < Integer.MAX_VALUE,
                "the default must actually be a limit");
    }
}
