package com.dewarim.cinnamon.configuration;

public class LuceneConfig {

    /**
     * Where Lucene should find or create the index
     */
    private String indexPath               = "/opt/cinnamon4/cinnamon-data/index";
    private Long   millisToWaitBetweenRuns = 1000L;
    private int    maxIndexAttempts        = 1;

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

    @Override
    public String toString() {
        return "LuceneConfig{" +
                "indexPath='" + indexPath + '\'' +
                ", millisToWaitBetweenRuns=" + millisToWaitBetweenRuns +
                ", maxIndexAttempts=" + maxIndexAttempts +
                '}';
    }
}
