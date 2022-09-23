/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.component.model;

import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Instance parameter defined for an application component. An instance parameter is a
 * parameter used to configure the behavior of an instance of the application. The description
 * of the parameter indicates how it should be rendered to the administrators, how it can be
 * valued, and, optionally, what are these possible values.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ParameterType", propOrder = { "name", "label", "order", "mandatory", "value",
    "options", "type", "size", "updatable", "help", "warning", "personalSpaceValue" })
public class Parameter {

  @XmlElement(required = true)
  protected String name;
  @XmlElement(required = true)
  @XmlJavaTypeAdapter(MultilangHashMapAdapter.class)
  protected Map<String, String> label;
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
  protected Map<String, String> help;
  @XmlJavaTypeAdapter(MultilangHashMapAdapter.class)
  protected Map<String, String> warning;
  protected String personalSpaceValue;

  public Parameter() {
  }

  /**
   * Constructs a new parameter by copying the specified one.
   * @param param the paramater to copy.
   */
  public Parameter(final Parameter param) {
    this.name = param.name;
    this.order = param.order;
    this.mandatory = param.mandatory;
    this.value = param.value;
    this.type = param.type;
    this.size = param.size;
    this.updatable = param.updatable;
    this.personalSpaceValue = param.personalSpaceValue;

    this.setHelp(new HashMap<>(param.getHelp()));
    this.setWarning(new HashMap<>(param.getWarning()));
    this.setLabel(new HashMap<>(param.getLabel()));
    if (param.options == null) {
      this.options = new ArrayList<>();
    } else {
      this.options = param.options.stream().map(Option::new).collect(Collectors.toList());
    }
  }

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
  public Map<String, String> getLabel() {
    if (label == null) {
      label = new HashMap<>();
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
  public void setLabel(Map<String, String> value) {
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
   * Gets the possible options of this parameter.
   * <p>
   * This accessor method returns a reference to the live list, not a snapshot. Therefore, any
   * modification you make to the returned list will be present inside the JAXB object. This is why
   * there is not a <CODE>set</CODE> method for the options.
   * <p>
   * For example, to add a new item, do as follows:
   * <pre>
   * getOptions().add(newItem);
   * </pre>
   * <p>
   */
  public List<Option> getOptions() {
    if (isXmlTemplate() && CollectionUtil.isEmpty(options)) {
      loadXmlTemplates();
    } else if (options == null) {
      options = new ArrayList<>();
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
  public Map<String, String> getHelp() {
    if (help == null) {
      help = new HashMap<>();
    }
    return help;
  }

  /**
   * Sets the value of the help property.
   * @param value allowed object is {@link Multilang }
   */
  public void setHelp(Map<String, String> value) {
    this.help = value;
  }

  /**
   * Gets the value of the warning property.
   * @return possible object is {@link Multilang }
   */
  public Map<String, String> getWarning() {
    if (warning == null) {
      warning = new HashMap<>();
    }
    return warning;
  }

  @SuppressWarnings("unused")
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
  public void setWarning(Map<String, String> value) {
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
  @SuppressWarnings("unused")
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

  private void loadXmlTemplates() {
    options = new ArrayList<>();
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
      SilverLogger.getLogger(this).error("Error in loading XML templates. Parameter: " + name, ex);
    }
  }

}