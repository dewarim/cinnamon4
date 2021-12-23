package com.dewarim.cinnamon.model;

public class ProviderClass {

    private ProviderType providerType;
    private String       name;

    public ProviderClass() {
    }

    public ProviderClass(ProviderType providerType, String name) {
        this.providerType = providerType;
        this.name = name;
    }

    public ProviderType getProviderType() {
        return providerType;
    }

    public void setProviderType(ProviderType providerType) {
        this.providerType = providerType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "ProviderClass{" +
                "providerType=" + providerType +
                ", name='" + name + '\'' +
                '}';
    }
}
