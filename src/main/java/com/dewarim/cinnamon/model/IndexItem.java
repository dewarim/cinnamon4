package com.dewarim.cinnamon.model;

import com.dewarim.cinnamon.api.Identifiable;

import java.util.Objects;

public class IndexItem implements Identifiable {
    
    private Long id;
    private String fieldName;
    private boolean forContent;
    private boolean forMetadata;
    private boolean forSysMetadata;
    private boolean multipleResults;
    private String name;
    private String searchString;
    private String indexTypeName;
    private String searchCondition;
    private boolean storeField;

    public IndexItem() {
    }

    public IndexItem(String fieldName, boolean forContent, boolean forMetadata, boolean forSysMetadata, boolean multipleResults, String name, String searchString, String indexTypeName, String searchCondition, boolean storeField) {
        this.fieldName = fieldName;
        this.forContent = forContent;
        this.forMetadata = forMetadata;
        this.forSysMetadata = forSysMetadata;
        this.multipleResults = multipleResults;
        this.name = name;
        this.searchString = searchString;
        this.indexTypeName = indexTypeName;
        this.searchCondition = searchCondition;
        this.storeField = storeField;
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

    public boolean isForContent() {
        return forContent;
    }

    public void setForContent(boolean forContent) {
        this.forContent = forContent;
    }

    public boolean isForMetadata() {
        return forMetadata;
    }

    public void setForMetadata(boolean forMetadata) {
        this.forMetadata = forMetadata;
    }

    public boolean isForSysMetadata() {
        return forSysMetadata;
    }

    public void setForSysMetadata(boolean forSysMetadata) {
        this.forSysMetadata = forSysMetadata;
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

    public String getIndexTypeName() {
        return indexTypeName;
    }

    public void setIndexTypeName(String indexTypeName) {
        this.indexTypeName = indexTypeName;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IndexItem indexItem = (IndexItem) o;
        return forContent == indexItem.forContent &&
               forMetadata == indexItem.forMetadata &&
               forSysMetadata == indexItem.forSysMetadata &&
               multipleResults == indexItem.multipleResults &&
               storeField == indexItem.storeField &&
               Objects.equals(fieldName, indexItem.fieldName) &&
               Objects.equals(name, indexItem.name) &&
               Objects.equals(searchString, indexItem.searchString) &&
               Objects.equals(indexTypeName, indexItem.indexTypeName) &&
               Objects.equals(searchCondition, indexItem.searchCondition);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "IndexItem{" +
               "id=" + id +
               ", fieldName='" + fieldName + '\'' +
               ", forContent=" + forContent +
               ", forMetadata=" + forMetadata +
               ", forSysMetadata=" + forSysMetadata +
               ", multipleResults=" + multipleResults +
               ", name='" + name + '\'' +
               ", searchString='" + searchString + '\'' +
               ", indexTypeName=" + indexTypeName +
               ", searchCondition=" + searchCondition +
               ", storeField=" + storeField +
               '}';
    }
}
