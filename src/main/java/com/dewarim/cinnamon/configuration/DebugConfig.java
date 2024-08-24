package com.dewarim.cinnamon.configuration;

public class DebugConfig {

    private String  debugFolderPath = "/tmp/cinnamon-debug";
    private boolean debugEnabled    = true;

    public String getDebugFolderPath() {
        return debugFolderPath;
    }

    public void setDebugFolderPath(String debugFolderPath) {
        this.debugFolderPath = debugFolderPath;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }

    @Override
    public String toString() {
        return "DebugConfig{" +
                "debugFolderPath='" + debugFolderPath + '\'' +
                ", debugEnabled=" + debugEnabled +
                '}';
    }
}
