package com.dewarim.cinnamon.model.index;

import com.dewarim.cinnamon.model.Folder;
import com.dewarim.cinnamon.model.Format;
import com.dewarim.cinnamon.model.ObjectSystemData;

public class IndexJobWithDependencies {

    private final IndexJob         indexJob;
    private final IndexKey         indexKey;
    private       ObjectSystemData osd;
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
        this.osd = osd;
    }

    public ObjectSystemData getOsd() {
        return osd;
    }

    public void setFolder(Folder folder) {
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
}
