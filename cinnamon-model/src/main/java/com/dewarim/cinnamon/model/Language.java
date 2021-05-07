package com.dewarim.cinnamon.model;

import com.dewarim.cinnamon.api.Identifiable;

import java.util.Objects;

public class Language implements Identifiable {
    
    private Long id;
    private String isoCode;

    public Language() {
    }

    public Language(Long id, String isoCode) {
        this.id = id;
        this.isoCode = isoCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIsoCode() {
        return isoCode;
    }

    public void setIsoCode(String isoCode) {
        this.isoCode = isoCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Language that = (Language) o;
        return Objects.equals(isoCode, that.isoCode);
    }

    @Override
    public int hashCode() {

        return Objects.hash(isoCode);
    }

    @Override
    public String toString() {
        return "UiLanguage{" +
               "id=" + id +
               ", isoCode='" + isoCode + '\'' +
               '}';
    }
}
