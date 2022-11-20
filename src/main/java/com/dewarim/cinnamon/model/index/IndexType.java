package com.dewarim.cinnamon.model.index;

import com.dewarim.cinnamon.application.service.index.DefaultIndexer;

import static com.dewarim.cinnamon.model.index.DataType.STRING;

public enum IndexType {

    DEFAULT_INDEXER(new DefaultIndexer(), STRING);
    final Indexer  indexer;
    final DataType dataType;

    IndexType(Indexer indexer, DataType dataType) {
        this.indexer = indexer;
        this.dataType = dataType;
    }

    public Indexer getIndexer() {
        return indexer;
    }

    public DataType getDataType() {
        return dataType;
    }
}
