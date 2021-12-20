package com.dewarim.cinnamon.api.lifecycle;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JacksonXmlRootElement(localName = "config")
public class LifecycleStateConfig {

    @JacksonXmlElementWrapper(localName = "properties")
    @JacksonXmlProperty(localName = "property")
    private List<NameValue> properties = new ArrayList<>();


    @JacksonXmlElementWrapper(localName = "nextStates")
    @JacksonXmlProperty(localName = "nextState")
    private List<String> nextStates = new ArrayList<>();

    public List<String> getPropertyValues(String propertyName){
        return properties.stream()
                .filter(nameValue -> nameValue.getName().equals(propertyName))
                .map(NameValue::getValue)
                .collect(Collectors.toList());
    }

    public List<String> getNextStates() {
        return nextStates;
    }

    public void setNextStates(List<String> nextStates) {
        this.nextStates = nextStates;
    }

    public List<NameValue> getProperties() {
        return properties;
    }

    public void setProperties(List<NameValue> properties) {
        this.properties = properties;
    }

    public static class NameValue{
        private String name;
        private String value;

        public NameValue() {
        }

        public NameValue(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

}
