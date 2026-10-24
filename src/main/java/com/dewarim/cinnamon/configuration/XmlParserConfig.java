package com.dewarim.cinnamon.configuration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Processing limits for the JDK's XML parser when parsing user supplied XML (OSD content, metadata).
 * <p>
 * The defaults are the values of JDK 25's conf/jaxp.properties. They are set explicitly on the parser
 * so they apply regardless of the jaxp.properties or system properties of the JRE Cinnamon runs on.
 * As in the JDK, a value of 0 means no limit.
 * <p>
 * See the java.xml module documentation for the meaning of each limit.
 */
public class XmlParserConfig {

    private int entityExpansionLimit        = 2500;
    private int totalEntitySizeLimit        = 100_000;
    private int maxGeneralEntitySizeLimit   = 100_000;
    private int maxParameterEntitySizeLimit = 15_000;
    private int entityReplacementLimit      = 100_000;
    private int elementAttributeLimit       = 200;
    private int maxElementDepth             = 100;
    private int maxXMLNameLimit             = 1000;

    /**
     * @return the limits as JDK parser properties ("jdk.xml.*" names), to be set on a SAX parser / XMLReader.
     */
    public Map<String, Integer> toJdkProperties() {
        Map<String, Integer> properties = new LinkedHashMap<>();
        properties.put("jdk.xml.entityExpansionLimit", entityExpansionLimit);
        properties.put("jdk.xml.totalEntitySizeLimit", totalEntitySizeLimit);
        properties.put("jdk.xml.maxGeneralEntitySizeLimit", maxGeneralEntitySizeLimit);
        properties.put("jdk.xml.maxParameterEntitySizeLimit", maxParameterEntitySizeLimit);
        properties.put("jdk.xml.entityReplacementLimit", entityReplacementLimit);
        properties.put("jdk.xml.elementAttributeLimit", elementAttributeLimit);
        properties.put("jdk.xml.maxElementDepth", maxElementDepth);
        properties.put("jdk.xml.maxXMLNameLimit", maxXMLNameLimit);
        return properties;
    }

    public int getEntityExpansionLimit() {
        return entityExpansionLimit;
    }

    public void setEntityExpansionLimit(int entityExpansionLimit) {
        this.entityExpansionLimit = entityExpansionLimit;
    }

    public int getTotalEntitySizeLimit() {
        return totalEntitySizeLimit;
    }

    public void setTotalEntitySizeLimit(int totalEntitySizeLimit) {
        this.totalEntitySizeLimit = totalEntitySizeLimit;
    }

    public int getMaxGeneralEntitySizeLimit() {
        return maxGeneralEntitySizeLimit;
    }

    public void setMaxGeneralEntitySizeLimit(int maxGeneralEntitySizeLimit) {
        this.maxGeneralEntitySizeLimit = maxGeneralEntitySizeLimit;
    }

    public int getMaxParameterEntitySizeLimit() {
        return maxParameterEntitySizeLimit;
    }

    public void setMaxParameterEntitySizeLimit(int maxParameterEntitySizeLimit) {
        this.maxParameterEntitySizeLimit = maxParameterEntitySizeLimit;
    }

    public int getEntityReplacementLimit() {
        return entityReplacementLimit;
    }

    public void setEntityReplacementLimit(int entityReplacementLimit) {
        this.entityReplacementLimit = entityReplacementLimit;
    }

    public int getElementAttributeLimit() {
        return elementAttributeLimit;
    }

    public void setElementAttributeLimit(int elementAttributeLimit) {
        this.elementAttributeLimit = elementAttributeLimit;
    }

    public int getMaxElementDepth() {
        return maxElementDepth;
    }

    public void setMaxElementDepth(int maxElementDepth) {
        this.maxElementDepth = maxElementDepth;
    }

    public int getMaxXMLNameLimit() {
        return maxXMLNameLimit;
    }

    public void setMaxXMLNameLimit(int maxXMLNameLimit) {
        this.maxXMLNameLimit = maxXMLNameLimit;
    }

    @Override
    public String toString() {
        return "XmlParserConfig{" +
                "entityExpansionLimit=" + entityExpansionLimit +
                ", totalEntitySizeLimit=" + totalEntitySizeLimit +
                ", maxGeneralEntitySizeLimit=" + maxGeneralEntitySizeLimit +
                ", maxParameterEntitySizeLimit=" + maxParameterEntitySizeLimit +
                ", entityReplacementLimit=" + entityReplacementLimit +
                ", elementAttributeLimit=" + elementAttributeLimit +
                ", maxElementDepth=" + maxElementDepth +
                ", maxXMLNameLimit=" + maxXMLNameLimit +
                '}';
    }
}
