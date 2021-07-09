//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.11.09 at 03:29:54 PM CST 
//

package com.webank.wecube.platform.core.service.plugin.xml.register;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

/**
 * <p>
 * Java class for inputParameterType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="inputParameterType">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *       &lt;attribute name="datatype" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="mappingType" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="mappingSystemVariableName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="required" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="mappingEntityExpression" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="sensitiveData" type="{http://www.w3.org/2001/XMLSchema}string" default="N" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "inputParameterType", propOrder = { "value" })
public class InputParameterType {

    @XmlValue
    protected String value;
    @XmlAttribute(name = "datatype", required = true)
    protected String datatype;
    @XmlAttribute(name = "mappingType")
    protected String mappingType;
    @XmlAttribute(name = "mappingSystemVariableName")
    protected String mappingSystemVariableName;
    @XmlAttribute(name = "required", required = true)
    protected String required;
    @XmlAttribute(name = "mappingEntityExpression")
    protected String mappingEntityExpression;
    @XmlAttribute(name = "sensitiveData")
    protected String sensitiveData;
    @XmlAttribute(name = "description")
    protected String description;

    @XmlAttribute(name = "mappingValue")
    protected String mappingValue;

    /**
     * Gets the value of the value property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the datatype property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getDatatype() {
        return datatype;
    }

    /**
     * Sets the value of the datatype property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setDatatype(String value) {
        this.datatype = value;
    }

    /**
     * Gets the value of the mappingType property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getMappingType() {
        return mappingType;
    }

    /**
     * Sets the value of the mappingType property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setMappingType(String value) {
        this.mappingType = value;
    }

    /**
     * Gets the value of the mappingSystemVariableName property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getMappingSystemVariableName() {
        return mappingSystemVariableName;
    }

    /**
     * Sets the value of the mappingSystemVariableName property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setMappingSystemVariableName(String value) {
        this.mappingSystemVariableName = value;
    }

    /**
     * Gets the value of the required property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getRequired() {
        return required;
    }

    /**
     * Sets the value of the required property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setRequired(String value) {
        this.required = value;
    }

    /**
     * Gets the value of the mappingEntityExpression property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getMappingEntityExpression() {
        return mappingEntityExpression;
    }

    /**
     * Sets the value of the mappingEntityExpression property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setMappingEntityExpression(String value) {
        this.mappingEntityExpression = value;
    }

    /**
     * Gets the value of the sensitiveData property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getSensitiveData() {
        if (sensitiveData == null) {
            return "N";
        } else {
            return sensitiveData;
        }
    }

    /**
     * Sets the value of the sensitiveData property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setSensitiveData(String value) {
        this.sensitiveData = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMappingValue() {
        return mappingValue;
    }

    public void setMappingValue(String mappingValue) {
        this.mappingValue = mappingValue;
    }

}
