package com.dewarim.cinnamon.configuration;

public class LuceneConfig {

    /**
     * Where Lucene should find or create the index
     */
    private String indexPath               = "/opt/cinnamon4/cinnamon-data/index";
    private Long   millisToWaitBetweenRuns = 1000L;

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
}
