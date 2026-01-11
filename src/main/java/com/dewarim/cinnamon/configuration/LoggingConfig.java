package com.dewarim.cinnamon.configuration;

/**
 *
 */
public class LoggingConfig {

    private boolean logErrorsToDatabase = true;
    private int     truncateLogAfterRow = 1000;
    private int     truncateTableBy     = 100;

    public LoggingConfig() {
    }

    public boolean isLogErrorsToDatabase() {
        return logErrorsToDatabase;
    }

    public void setLogErrorsToDatabase(boolean logErrorsToDatabase) {
        this.logErrorsToDatabase = logErrorsToDatabase;
    }

    public int getTruncateLogAfterRow() {
        return truncateLogAfterRow;
    }

    public void setTruncateLogAfterRow(int truncateLogAfterRow) {
        this.truncateLogAfterRow = truncateLogAfterRow;
    }

    public int getTruncateTableBy() {
        return truncateTableBy;
    }

    public void setTruncateTableBy(int truncateTableBy) {
        this.truncateTableBy = truncateTableBy;
    }

    @Override
    public String toString() {
        return "LoggingConfig{" +
                "logErrorsToDatabase=" + logErrorsToDatabase +
                ", truncateLogAfterRow=" + truncateLogAfterRow +
                ", truncateTableBy=" + truncateTableBy +
                '}';
    }
}
