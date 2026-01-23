package com.dewarim.cinnamon.model.index;

import com.dewarim.cinnamon.model.Folder;
import com.dewarim.cinnamon.model.Format;
import com.dewarim.cinnamon.model.ObjectSystemData;

import java.util.Objects;

public class IndexJobWithDependencies {

    private final IndexJob         indexJob;
    private final IndexKey         indexKey;
    /**
     * may be null for delete jobs, but setting to null explicitly is not allowed.
     */
    private       ObjectSystemData osd;
    /**
     * may be null for delete jobs, but setting to null explicitly is not allowed.
     */
    private       Folder           folder;
    private       String           folderPath;
    private       Format           format;

    public IndexJobWithDependencies(IndexJob indexJob, IndexKey indexKey) {
        this.indexJob = indexJob;
        this.indexKey = indexKey;
    }

    public IndexJob getIndexJob() {
        return indexJob;
    }

    public IndexKey getIndexKey() {
        return indexKey;
    }

    public void setOsd(ObjectSystemData osd) {
        Objects.requireNonNull(osd);
        this.osd = osd;
    }

    public ObjectSystemData getOsd() {
        return osd;
    }

    public void setFolder(Folder folder) {
        Objects.requireNonNull(folder);
        this.folder = folder;
    }

    public Folder getFolder() {
        return folder;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    public Format getFormat() {
        return format;
    }

    @Override
    public String toString() {
        return "IndexJobWithDependencies{" +
                "indexJob=" + indexJob +
                ", indexKey=" + indexKey +
                ", osd=" + osd +
                ", folder=" + folder +
                ", folderPath='" + folderPath + '\'' +
                ", format=" + format +
                '}';
    }
}
