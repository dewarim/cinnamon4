package com.dewarim.cinnamon.configuration;

public class LuceneConfig {

    /**
     * Where Lucene should find or create the index
     */
    private String  indexPath               = "/opt/cinnamon4/cinnamon-data/index";
    private Long    millisToWaitBetweenRuns = 1000L;
    private int     maxIndexAttempts        = 1;
    /**
     * When re-indexing, commit every n changes.
     * This prevents the server from doing a commit every 1000 objects, which slows down
     * re-indexing over large datasets.
     */
    private int     uncommittedLimit        = 10000;
    /**
     * If true, store failed index jobs in index_events table.
     */
    private boolean logFailedAttemptsToDb   = true;
    /**
     * Do not index more than maxBatchSize items (folders & osds) in one go.
     */
    private int     maxBatchSize            = 500;
    private int     threadPoolSize          = 8;
    private long    threadPoolWaitInMinutes = 5L;
    private boolean verifySearchResults     = true;
    /**
     * Maximum size of all metasets (in Unicode characters) to handle in one batch.
     * Metasets are parsed to XML and if too many are added,
     * the server may run out of memory.
     */
    private Long    maxCombinedMetasetSize  = 100_000_000L;
    /**
     * On OSD, Folder or Meta creation and update operations, wait until the item has been indexed and the
     * asynchronous index returns it during a search operation.
     * <br>
     * The search interval is millisToWaitBetweenRuns, so if you turn this feature on,
     * responses will probably be 1s slower. But it will help if you want to have an object to
     * appear in the index right away without the uncertainty of whether a search can find it.
     * <br>
     * Use case: slow server disc or slow Lucene index, so objects are unsearchable for some time
     * after changes.
     */
    private boolean waitUntilSearchable     = false;

    /**
     * How long to retry looking for an index operation to complete.
     * With the default settings, this would be 120s, which should be enough.
     */
    private int waitingRetries = 120;

    /**
     * Also index each CJK (Chinese, Japanese, Korean) character on its own, in addition to the
     * overlapping bigrams that make CJK text searchable at all.
     * <br>
     * Off by default: words of two or more characters - practically all CJK vocabulary and all
     * technical terminology - are searchable without it, while turning it on roughly doubles the
     * number of CJK terms in the index. Turn it on if a single CJK character has to be findable as a
     * term of its own, for example in a parts list.
     * <br>
     * Changing this requires a reindex to take effect for existing documents.
     */
    private boolean cjkOutputUnigrams = false;

    /**
     * Maximum number of tokens one analyzed search query clause may produce. 0 = no limit.
     * <br>
     * Generous on purpose: the Windows client builds very complex queries, and a real query never
     * comes close to this. It exists so that a hostile query cannot make the analyzer produce an
     * unbounded token stream.
     */
    private int maxQueryTokenCount = 100_000;

    /**
     * Maximum size of file content that will be read into memory for indexing, in <b>megabytes</b>.
     * 0 = no limit.
     * <br>
     * Content above the limit is <b>not</b> indexed - the object itself is still indexed with its
     * metadata, so it stays findable by name and metadata, but its content is not searchable. This is
     * a deliberate trade: indexing reads the whole file into a byte array, converts it to a String
     * and then parses it into a DOM, which needs roughly ten times the file size in heap, and up to
     * {@link #threadPoolSize} documents are processed at once. Without a limit a single large upload
     * could exhaust the heap.
     * <br>
     * Note the multiplier when choosing a value: 2048 MB of content does not need 2 GB of heap, it
     * needs far more. The effective limit is the smaller of this and
     * {@link #maxIndexableContentPercentOfHeap}.
     */
    private long maxIndexableContentSizeMB = 2048;

    /**
     * Alternative, machine-relative form of {@link #maxIndexableContentSizeMB}, as a percentage of
     * the JVM's maximum heap. 0 = not used (the default).
     * <br>
     * This is a share of the <i>heap</i> ({@code Runtime.maxMemory()}), not of physical RAM, because
     * running out of memory while indexing is a heap event: physical RAM beyond {@code -Xmx} cannot
     * help. Useful to keep one configuration sensible across machines with different heap sizes.
     * <br>
     * When both this and {@link #maxIndexableContentSizeMB} are set, the <b>smaller</b> of the two
     * applies, so the absolute value acts as a ceiling and this one as a safety net on small heaps.
     */
    private int maxIndexableContentPercentOfHeap = 0;

    public int getUncommittedLimit() {
        return uncommittedLimit;
    }

    public void setUncommittedLimit(int uncommittedLimit) {
        this.uncommittedLimit = uncommittedLimit;
    }

    public int getMaxIndexAttempts() {
        return maxIndexAttempts;
    }

    public void setMaxIndexAttempts(int maxIndexAttempts) {
        this.maxIndexAttempts = maxIndexAttempts;
    }

    public String getIndexPath() {
        return indexPath;
    }

    public void setIndexPath(String indexPath) {
        this.indexPath = indexPath;
    }

    public Long getMillisToWaitBetweenRuns() {
        return millisToWaitBetweenRuns;
    }

    public void setMillisToWaitBetweenRuns(Long millisToWaitBetweenRuns) {
        this.millisToWaitBetweenRuns = millisToWaitBetweenRuns;
    }

    public boolean isLogFailedAttemptsToDb() {
        return logFailedAttemptsToDb;
    }

    public void setLogFailedAttemptsToDb(boolean logFailedAttemptsToDb) {
        this.logFailedAttemptsToDb = logFailedAttemptsToDb;
    }

    public void setMaxBatchSize(int maxBatchSize) {
        this.maxBatchSize = maxBatchSize;
    }

    public int getMaxBatchSize() {
        return maxBatchSize;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    public long getThreadPoolWaitInMinutes() {
        return threadPoolWaitInMinutes;
    }

    public void setThreadPoolWaitInMinutes(long threadPoolWaitInMinutes) {
        this.threadPoolWaitInMinutes = threadPoolWaitInMinutes;
    }

    public boolean isVerifySearchResults() {
        return verifySearchResults;
    }

    public void setVerifySearchResults(boolean verifySearchResults) {
        this.verifySearchResults = verifySearchResults;
    }

    public void setMaxCombinedMetasetSize(Long maxCombinedMetasetSize) {
        this.maxCombinedMetasetSize = maxCombinedMetasetSize;
    }

    public Long getMaxCombinedMetasetSize() {
        return maxCombinedMetasetSize;
    }

    @Override
    public String toString() {
        return "LuceneConfig{" +
                "indexPath='" + indexPath + '\'' +
                ", millisToWaitBetweenRuns=" + millisToWaitBetweenRuns +
                ", maxIndexAttempts=" + maxIndexAttempts +
                ", uncommittedLimit=" + uncommittedLimit +
                ", logFailedAttemptsToDb=" + logFailedAttemptsToDb +
                ", maxBatchSize=" + maxBatchSize +
                ", threadPoolSize=" + threadPoolSize +
                ", threadPoolWaitInMinutes=" + threadPoolWaitInMinutes +
                ", verifySearchResults=" + verifySearchResults +
                ", maxCombinedMetasetSize=" + maxCombinedMetasetSize +
                ", waitUntilSearchable=" + waitUntilSearchable +
                ", cjkOutputUnigrams=" + cjkOutputUnigrams +
                ", maxQueryTokenCount=" + maxQueryTokenCount +
                ", maxIndexableContentSizeMB=" + maxIndexableContentSizeMB +
                ", maxIndexableContentPercentOfHeap=" + maxIndexableContentPercentOfHeap +
                '}';
    }

    public boolean isWaitUntilSearchable() {
        return waitUntilSearchable;
    }

    public void setWaitUntilSearchable(boolean waitUntilSearchable) {
        this.waitUntilSearchable = waitUntilSearchable;
    }

    public int getWaitingRetries() {
        return waitingRetries;
    }

    public void setWaitingRetries(int waitingRetries) {
        this.waitingRetries = waitingRetries;
    }

    public boolean isCjkOutputUnigrams() {
        return cjkOutputUnigrams;
    }

    public void setCjkOutputUnigrams(boolean cjkOutputUnigrams) {
        this.cjkOutputUnigrams = cjkOutputUnigrams;
    }

    public int getMaxQueryTokenCount() {
        return maxQueryTokenCount;
    }

    public void setMaxQueryTokenCount(int maxQueryTokenCount) {
        this.maxQueryTokenCount = maxQueryTokenCount;
    }

    public long getMaxIndexableContentSizeMB() {
        return maxIndexableContentSizeMB;
    }

    public void setMaxIndexableContentSizeMB(long maxIndexableContentSizeMB) {
        this.maxIndexableContentSizeMB = maxIndexableContentSizeMB;
    }

    public int getMaxIndexableContentPercentOfHeap() {
        return maxIndexableContentPercentOfHeap;
    }

    public void setMaxIndexableContentPercentOfHeap(int maxIndexableContentPercentOfHeap) {
        this.maxIndexableContentPercentOfHeap = maxIndexableContentPercentOfHeap;
    }

    /**
     * The effective content size limit in bytes: the smaller of the absolute megabyte value and the
     * percentage of maximum heap, ignoring whichever of the two is not set.
     *
     * @return the limit in bytes, or 0 if content size is not limited at all
     */
    public long getMaxIndexableContentSizeInBytes() {
        long absolute = maxIndexableContentSizeMB > 0 ? maxIndexableContentSizeMB * 1024L * 1024L : 0L;
        long relative = maxIndexableContentPercentOfHeap > 0
                ? Runtime.getRuntime().maxMemory() / 100L * maxIndexableContentPercentOfHeap
                : 0L;
        if (absolute == 0L) {
            return relative;
        }
        if (relative == 0L) {
            return absolute;
        }
        return Math.min(absolute, relative);
    }
}
