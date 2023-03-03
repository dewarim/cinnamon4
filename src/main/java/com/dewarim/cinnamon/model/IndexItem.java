package com.dewarim.cinnamon.model;

import com.dewarim.cinnamon.api.Identifiable;
import com.dewarim.cinnamon.model.index.IndexType;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.Objects;

@JacksonXmlRootElement(localName = "indexItem")
@JsonPropertyOrder({"id", "name", "fieldName", "searchString", "searchCondition", "multipleResults", "indexType",
        "storeField"})
public class IndexItem implements Identifiable {

    private Long      id;
    private String    fieldName;
    private boolean   multipleResults;
    private String    name;
    private String    searchString;
    private String    searchCondition;
    private boolean   storeField;
    private IndexType indexType;

    public IndexItem() {
    }

    public IndexItem(String fieldName, boolean multipleResults, String name,
                     String searchString, String searchCondition,
                     boolean storeField, IndexType indexType) {
        this.fieldName = Objects.requireNonNull(fieldName);
        this.multipleResults = multipleResults;
        this.name = Objects.requireNonNull(name);
        this.searchString = Objects.requireNonNull(searchString);
        this.searchCondition = searchCondition;
        this.storeField = storeField;
        this.indexType = Objects.requireNonNull(indexType);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public boolean isMultipleResults() {
        return multipleResults;
    }

    public void setMultipleResults(boolean multipleResults) {
        this.multipleResults = multipleResults;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public String getSearchCondition() {
        return searchCondition;
    }

    public void setSearchCondition(String searchCondition) {
        this.searchCondition = searchCondition;
    }

    public boolean isStoreField() {
        return storeField;
    }

    public void setStoreField(boolean storeField) {
        this.storeField = storeField;
    }

    public IndexType getIndexType() {
        return indexType;
    }

    public void setIndexType(IndexType indexType) {
        this.indexType = indexType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IndexItem indexItem = (IndexItem) o;
        return multipleResults == indexItem.multipleResults &&
                storeField == indexItem.storeField &&
                Objects.equals(id, indexItem.id) &&
                Objects.equals(fieldName, indexItem.fieldName) &&
                Objects.equals(name, indexItem.name) &&
                Objects.equals(searchString, indexItem.searchString) &&
                Objects.equals(searchCondition, indexItem.searchCondition) &&
                Objects.equals(indexType, indexItem.indexType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fieldName, multipleResults, name, searchString, searchCondition, storeField, indexType);
    }

    @Override
    public String toString() {
        return "IndexItem{" +
                "id=" + id +
                ", fieldName='" + fieldName + '\'' +
                ", multipleResults=" + multipleResults +
                ", name='" + name + '\'' +
                ", searchString='" + searchString + '\'' +
                ", searchCondition='" + searchCondition + '\'' +
                ", storeField=" + storeField +
                ", indexer=" + indexType +
                '}';
    }
}
