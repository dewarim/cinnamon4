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
    /**
     * Maximum size of all metasets (in Unicode characters) to handle in one batch.
     * Metasets are parsed to XML and if too many are added,
     * the server may run out of memory.
     */
    private Long    maxCombinedMetasetSize  = 100_000_000L;

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
                ", maxCombinedMetasetSize=" + maxCombinedMetasetSize +
                '}';
    }

    public void setMaxCombinedMetasetSize(Long maxCombinedMetasetSize) {
        this.maxCombinedMetasetSize = maxCombinedMetasetSize;
    }

    public Long getMaxCombinedMetasetSize() {
        return maxCombinedMetasetSize;
    }
}
