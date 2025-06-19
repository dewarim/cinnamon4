package com.dewarim.cinnamon.model.index;

import com.dewarim.cinnamon.application.service.index.*;

import static com.dewarim.cinnamon.model.index.DataType.*;

public enum IndexType {

    BOOLEAN_INDEXER(new BooleanIndexer(), BOOLEAN),
    COMPLETE_STRING_INDEXER(new CompleteStringIndexer(), STRING),
    DEFAULT_INDEXER(new DefaultIndexer(), TEXT),
    DESCENDING_STRING_INDEXER(new DescendingStringIndexer(), STRING),
    ELEMENT_NAME_INDEXER(new ElementNameIndexer(), STRING),
    DATE_INDEXER(new DateIndexer(), DATE),
    DATE_TIME_INDEXER(new DateTimeIndexer(), DATE_TIME),
    INTEGER_INDEXER(new IntegerIndexer(), INTEGER),
    ATTRIBUTE_STRING_INDEXER(new AttributeStringIndexer(), STRING)
    ;

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
