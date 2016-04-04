/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.admin.component.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.silvertrace.SilverTrace;

/**
 * <p>
 * Java class for ParameterType complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name=&quot;ParameterType&quot;&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element name=&quot;name&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot;/&gt;
 *         &lt;element name=&quot;label&quot; type=&quot;{http://silverpeas.org/xml/ns/component}multilang&quot;/&gt;
 *         &lt;element name=&quot;order&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}int&quot;/&gt;
 *         &lt;element name=&quot;mandatory&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}boolean&quot;/&gt;
 *         &lt;element name=&quot;value&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot;/&gt;
 *         &lt;element name=&quot;options&quot; maxOccurs=&quot;unbounded&quot; minOccurs=&quot;0&quot;&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name=&quot;option&quot; type=&quot;{http://silverpeas.org/xml/ns/component}ParameterOptionType&quot; maxOccurs=&quot;unbounded&quot; minOccurs=&quot;0&quot;/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name=&quot;type&quot;&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}string&quot;&gt;
 *               &lt;enumeration value=&quot;checkbox&quot;/&gt;
 *               &lt;enumeration value=&quot;select&quot;/&gt;
 *               &lt;enumeration value=&quot;xmltemplates&quot;/&gt;
 *               &lt;enumeration value=&quot;text&quot;/&gt;
 *               &lt;enumeration value=&quot;radio&quot;/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name=&quot;size&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}int&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;element name=&quot;updatable&quot;&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}string&quot;&gt;
 *               &lt;enumeration value=&quot;always&quot;/&gt;
 *               &lt;enumeration value=&quot;creation&quot;/&gt;
 *               &lt;enumeration value=&quot;never&quot;/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name=&quot;help&quot; type=&quot;{http://silverpeas.org/xml/ns/component}multilang&quot;/&gt;
 *         &lt;element name=&quot;personalSpaceValue&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; minOccurs=&quot;0&quot;/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ParameterType", propOrder = { "name", "label", "order", "mandatory", "value",
    "options", "type", "size", "updatable", "help", "warning", "personalSpaceValue" })
public class Parameter implements Cloneable {

  @XmlElement(required = true)
  protected String name;
  @XmlElement(required = true)
  @XmlJavaTypeAdapter(MultilangHashMapAdapter.class)
  protected HashMap<String, String> label;
  protected int order;
  protected boolean mandatory;
  @XmlElement(required = true)
  protected String value;
  @XmlElementWrapper(name = "options")
  @XmlElement(name = "option")
  protected List<Option> options;
  @XmlElement(required = true)
  protected String type;
  protected Integer size;
  @XmlElement(required = true)
  protected String updatable;
  @XmlElement(required = true)
  @XmlJavaTypeAdapter(MultilangHashMapAdapter.class)
  protected HashMap<String, String> help;
  @XmlJavaTypeAdapter(MultilangHashMapAdapter.class)
  protected HashMap<String, String> warning;
  protected String personalSpaceValue;

  /**
   * Gets the value of the name property.
   * @return possible object is {@link String }
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the value of the name property.
   * @param value allowed object is {@link String }
   */
  public void setName(String value) {
    this.name = value;
  }

  /**
   * Gets the value of the label property.
   * @return possible object is {@link Multilang }
   */
  public HashMap<String, String> getLabel() {
    if (label == null) {
      label = new HashMap<String, String>();
    }
    return label;
  }

  public String getLabel(String lang) {
    if (getLabel().containsKey(lang)) {
      return getLabel().get(lang);
    }
    return getLabel().get(DisplayI18NHelper.getDefaultLanguage());
  }

  /**
   * Sets the value of the label property.
   * @param value allowed object is {@link Multilang }
   */
  public void setLabel(HashMap<String, String> value) {
    this.label = value;
  }

  /**
   * Gets the value of the order property.
   */
  public int getOrder() {
    return order;
  }

  /**
   * Sets the value of the order property.
   */
  public void setOrder(int value) {
    this.order = value;
  }

  /**
   * Gets the value of the mandatory property.
   */
  public boolean isMandatory() {
    return mandatory;
  }

  /**
   * Sets the value of the mandatory property.
   */
  public void setMandatory(boolean value) {
    this.mandatory = value;
  }

  /**
   * Gets the value of the value property.
   * @return possible object is {@link String }
   */
  public String getValue() {
    return value;
  }

  /**
   * Sets the value of the value property.
   * @param value allowed object is {@link String }
   */
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * Gets the value of the options property.
   * <p>
   * This accessor method returns a reference to the live list, not a snapshot. Therefore any
   * modification you make to the returned list will be present inside the JAXB object. This is why
   * there is not a <CODE>set</CODE> method for the options property.
   * <p>
   * For example, to add a new item, do as follows:
   *
   * <pre>
   * getOptions().add(newItem);
   * </pre>
   * <p>
   * Objects of the following type(s) are allowed in the list {@link Parameter.Options }
   */
  public List<Option> getOptions() {
    if (isXmlTemplate()) {
      loadXmlTemplates();
    } else if (options == null) {
      options = new ArrayList<Option>();
    }
    return this.options;
  }

  public void setOptions(List<Option> options) {
    this.options = options;
  }

  /**
   * Gets the value of the type property.
   * @return possible object is {@link String }
   */
  public String getType() {
    return type;
  }

  /**
   * Sets the value of the type property.
   * @param value allowed object is {@link String }
   */
  public void setType(String value) {
    this.type = value;
  }

  /**
   * Gets the value of the size property.
   * @return possible object is {@link Integer }
   */
  public Integer getSize() {
    return size;
  }

  /**
   * Sets the value of the size property.
   * @param value allowed object is {@link Integer }
   */
  public void setSize(Integer value) {
    this.size = value;
  }

  /**
   * Gets the value of the updatable property.
   * @return possible object is {@link String }
   */
  public String getUpdatable() {
    return updatable;
  }

  /**
   * Sets the value of the updatable property.
   * @param value allowed object is {@link String }
   */
  public void setUpdatable(String value) {
    this.updatable = value;
  }

  public String getHelp(String lang) {
    if (getHelp().containsKey(lang)) {
      return getHelp().get(lang);
    }
    return getHelp().get(DisplayI18NHelper.getDefaultLanguage());
  }

  /**
   * Gets the value of the help property.
   * @return possible object is {@link Multilang }
   */
  public HashMap<String, String> getHelp() {
    if (help == null) {
      help = new HashMap<String, String>();
    }
    return help;
  }

  /**
   * Sets the value of the help property.
   * @param value allowed object is {@link Multilang }
   */
  public void setHelp(HashMap<String, String> value) {
    this.help = value;
  }

  /**
   * Gets the value of the warning property.
   * @return possible object is {@link Multilang }
   */
  public HashMap<String, String> getWarning() {
    if (warning == null) {
      warning = new HashMap<String, String>();
    }
    return warning;
  }

  public String getWarning(String lang) {
    if (getWarning().containsKey(lang)) {
      return getWarning().get(lang);
    }
    return getWarning().get(DisplayI18NHelper.getDefaultLanguage());
  }

  /**
   * Sets the value of the warning property.
   * @param value allowed object is {@link Multilang }
   */
  public void setWarning(HashMap<String, String> value) {
    this.warning = value;
  }

  /**
   * Gets the value of the personalSpaceValue property.
   * @return possible object is {@link String }
   */
  public String getPersonalSpaceValue() {
    return personalSpaceValue;
  }

  /**
   * Sets the value of the personalSpaceValue property.
   * @param value allowed object is {@link String }
   */
  public void setPersonalSpaceValue(String value) {
    this.personalSpaceValue = value;
  }

  public boolean isVisible() {
    return !isNeverUpdatable() && !isHidden();
  }

  public boolean isHidden() {
    return ParameterUpdatableType.hidden == ParameterUpdatableType.valueOf(getUpdatable());
  }

  public boolean isUpdatableOnCreationOnly() {
    return ParameterUpdatableType.creation == ParameterUpdatableType.valueOf(getUpdatable());
  }

  public boolean isAlwaysUpdatable() {
    return ParameterUpdatableType.always == ParameterUpdatableType.valueOf(getUpdatable());
  }

  public boolean isNeverUpdatable() {
    return ParameterUpdatableType.never == ParameterUpdatableType.valueOf(getUpdatable());
  }

  public boolean isText() {
    return ParameterInputType.text == ParameterInputType.valueOf(getType());
  }

  public boolean isCheckbox() {
    return ParameterInputType.checkbox == ParameterInputType.valueOf(getType());
  }

  public boolean isRadio() {
    return ParameterInputType.radio == ParameterInputType.valueOf(getType());
  }

  public boolean isSelect() {
    return ParameterInputType.select == ParameterInputType.valueOf(getType());
  }

  public boolean isXmlTemplate() {
    return ParameterInputType.xmltemplates == ParameterInputType.valueOf(getType());
  }

  @Override
  @SuppressWarnings("unchecked")
  public Parameter clone() {
    Parameter param = new Parameter();
    param.setHelp((HashMap<String, String>) getHelp().clone());
    param.setWarning((HashMap<String, String>) getWarning().clone());
    param.setLabel((HashMap<String, String>) getLabel().clone());
    param.setMandatory(mandatory);
    param.setName(name);
    if (options == null) {
      param.setOptions(new ArrayList<Option>());
    } else {
      List<Option> newOptions = new ArrayList<Option>(options.size());
      for (Option option : options) {
        newOptions.add(option.clone());
      }
      param.setOptions(newOptions);
    }
    param.setOrder(order);
    param.setPersonalSpaceValue(personalSpaceValue);
    param.setSize(size);
    param.setType(type);
    param.setUpdatable(updatable);
    param.setValue(value);
    return param;
  }

  private void loadXmlTemplates() {
    options = new ArrayList<Option>();
    try {
      List<PublicationTemplate> templates = PublicationTemplateManager.getInstance().
          getPublicationTemplates(true);
      for (PublicationTemplate template : templates) {
        Option option = new Option();
        for (String lang : DisplayI18NHelper.getLanguages()) {
          option.getName().put(lang, template.getName());
        }
        option.setValue(template.getFileName());
        options.add(option);
      }
    } catch (PublicationTemplateException ex) {
      SilverTrace.error("admin", "Parameters.loadXmlTemplates",
          "root.EX_IGNORED", "ParameterName=" + name, ex);
    }
  }

}