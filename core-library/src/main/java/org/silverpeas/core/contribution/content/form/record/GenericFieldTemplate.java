/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

package org.silverpeas.core.contribution.content.form.record;

import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.FormFatalException;
import org.silverpeas.core.contribution.content.form.TypeManager;
import org.silverpeas.core.util.ArrayUtil;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * A generic FieldTemplate implementation.
 */
@XmlRootElement(name = "fieldTemplate")
@XmlAccessorType(XmlAccessType.NONE)
public class GenericFieldTemplate implements FieldTemplate, Serializable, Cloneable {

  private static final long serialVersionUID = 1L;
  @XmlElement(required = true)
  private String fieldName;
  private Class fieldImpl;
  private String typeName;
  @XmlElement(required = true)
  private String displayerName = "";
  @XmlElement(name = "isMandatory", required = true, defaultValue = "false")
  private boolean mandatory;
  @XmlElement(name = "isReadOnly", required = true, defaultValue = "false")
  private boolean readOnly;
  @XmlElement(name = "isDisabled", required = true, defaultValue = "false")
  private boolean disabled;
  @XmlElement(name = "isHidden", required = true, defaultValue = "false")
  private boolean hidden;

  private String defaultLabel;
  private Map<String, String> labels = new HashMap<>();
  private Map<String, String> parameters = new HashMap<>();

  @XmlElement(name = "label")
  private List<Label> labelsObj = new ArrayList<>();
  @XmlElement(name = "parameter")
  private List<Parameter> parametersObj = new ArrayList<>();
  @XmlElement(name = "isSearchable", required = true, defaultValue = "false")
  private boolean searchable;
  private String templateName;

  @XmlElement(name = "isFacet", required = true, defaultValue = "false")
  private boolean usedAsFacet;

  @XmlElement(defaultValue = "1")
  private int maximumNumberOfOccurrences = 1;

  /**
   * Builds a GenericFieldTemplate
   */
  public GenericFieldTemplate() {
  }

  /**
   * Builds a GenericFieldTemplate from a field name and a field type name. The type name must be
   * known by the type manager. You must use the set and add methods to change any default value.
   */
  public GenericFieldTemplate(String fieldName, String typeName) throws FormException {
    this.fieldName = fieldName;
    this.typeName = typeName;

    init();
  }

  /**
   * Builds a GenericFieldTemplate from a field name and a field implementation. You must use the
   * set and add methods to change any default value.
   */
  public GenericFieldTemplate(String fieldName, Class fieldImpl) throws FormException {
    this.fieldName = fieldName;
    this.fieldImpl = fieldImpl;

    init();
  }

  /**
   * Inits and check this FieldTemplate.
   */
  private void init() throws FormException {
    if (fieldImpl == null) {
      fieldImpl = TypeManager.getInstance().getFieldImplementation(typeName);
    }

    // we build a dummy field to test if the class implements Field.
    Field dummyField = getEmptyField();

    if (typeName == null) {
      // we use this dummy field to set the type name.
      typeName = dummyField.getTypeName();
    }
  }

  /**
   * Returns the field name of the Field built on this template.
   */
  @Override
  public String getFieldName() {
    return fieldName;
  }

  /**
   * Set the field name of the Field built on this template.
   */
  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  /**
   * Returns the type name of the described field.
   */
  @Override
  @XmlElement(required = true)
  public String getTypeName() {
    return typeName;
  }

  /**
   * set the type name of the described field.
   */
  public void setTypeName(String typeName) throws FormException {
    this.typeName = typeName;
    this.fieldImpl = TypeManager.getInstance().getFieldImplementation(typeName);
  }

  /**
   * Returns the name of the FieldDisplayer to display the described field.
   */
  @Override
  public String getDisplayerName() {
    return displayerName;
  }

  /**
   * Set the name of the FieldDisplayer.
   */
  public void setDisplayerName(String displayerName) {
    this.displayerName = displayerName;
  }

  /**
   * Returns the default label of the described field.
   */
  @Override
  public String getLabel() {
    if (defaultLabel != null) {
      return defaultLabel;
    }
    return "";
  }

  /**
   * Returns the local label of the described field.
   */
  @Override
  public String getLabel(String language) {
    String label = null;
    if (getLabels() != null) {
      label = getLabels().get(language);
    }

    if (label == null) {
      return getLabel();
    } else {
      return label;
    }
  }

  /**
   * Returns a local parameter of the described field.
   */
  public String getParameter(String name, String language) {
    String parameter = null;
    if (getParameters(language) != null) {
      parameter = getParameters(language).get(name);
    }

    if (parameter == null) {
      return "";
    } else {
      return parameter;
    }
  }

  /**
   * Set the default label.
   */
  public void setLabel(String label) {
    defaultLabel = label;
  }

  /**
   * Adds a local label.
   */
  public void addLabel(String label, String language) {
    if (language == null || language.trim().equals("")) {
      setLabel(label);
    } else {
      if (labels == null) {
        labels = new HashMap<>();
      }
      labels.put(language, label);
    }
  }

  /**
   * Adds a local parameter.
   */
  public void addParameter(String name, String value) {
    if (parameters == null) {
      parameters = new HashMap<>();
    }
    parameters.put(name, value);
  }

  /**
   * Returns true when the described field must have a value.
   */
  @Override
  public boolean isMandatory() {
    return mandatory;
  }

  /**
   * Set or unset the isMandatory flag
   */
  public void setMandatory(boolean isMandatory) {
    this.mandatory = isMandatory;
  }

  /**
   * Set or unset the isMandatory flag
   */
  public void setMandatory(Boolean isMandatory) {
    this.mandatory = isMandatory;
  }

  /**
   * Returns true when the described field can't be updated.
   */
  @Override
  public boolean isReadOnly() {
    return readOnly;
  }

  /**
   * Set or unset the isReadOnly flag
   */
  public void setReadOnly(boolean isReadOnly) {
    this.readOnly = isReadOnly;
  }

  /**
   * Returns true when the described field must be disabled.
   */
  @Override
  public boolean isDisabled() {
    return disabled;
  }

  /**
   * Set or unset the isDisabled flag
   */
  public void setDisabled(boolean isDisabled) {
    this.disabled = isDisabled;
  }

  /**
   * Returns true when the described field must be hidden.
   */
  @Override
  public boolean isHidden() {
    return hidden;
  }

  /**
   * Set or unset the isHidden flag
   */
  public void setHidden(boolean isHidden) {
    this.hidden = isHidden;
  }

  @Override
  public Map<String, String> getParameters(String language) {
    for (Parameter parameter : parametersObj) {
      addParameter(parameter.getName(), parameter.getValue(language));
    }
    return parameters;
  }

  public Map<String, String> getKeyValuePairs(String language) {
    Map<String, String> keyValuePairs = new HashMap<>();
    Map<String, String> theParameters = getParameters(language);

    if (theParameters == null) {
      return keyValuePairs;
    }

    String keys = theParameters.get("keys");
    String values = theParameters.get("values");
    if (keys != null && values != null) {
      StringTokenizer kTokenizer = new StringTokenizer(keys, "##");
      StringTokenizer vTokenizer = new StringTokenizer(values, "##");
      while (kTokenizer.hasMoreTokens()) {
        String key = kTokenizer.nextToken();
        String value = vTokenizer.nextToken();
        keyValuePairs.put(key, value);
      }
    } else if (keys != null) {
      StringTokenizer kTokenizer = new StringTokenizer(keys, "##");
      while (kTokenizer.hasMoreTokens()) {
        String key = kTokenizer.nextToken();
        keyValuePairs.put(key, key);
      }
    } else if (values != null) {
      StringTokenizer vTokenizer = new StringTokenizer(values, "##");
      while (vTokenizer.hasMoreTokens()) {
        String value = vTokenizer.nextToken();
        keyValuePairs.put(value, value);
      }
    }
    return keyValuePairs;
  }

  public Map<String, String> getLabels() {
    if (labels == null || labels.isEmpty()) {
      for (Label label : labelsObj) {
        addLabel(label.getLabel(), label.getLanguage());
      }
    }
    return labels;
  }

  /**
   * Returns an empty Field built on this template.
   */
  @Override
  public Field getEmptyField() throws FormException {
    return getEmptyField(0);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Field getEmptyField(int occurrence) throws FormException {
    try {
      Class[] noParameterClass = ArrayUtil.EMPTY_CLASS_ARRAY;
      Constructor constructor = fieldImpl.getConstructor(noParameterClass);
      Object[] noParameter = ArrayUtil.EMPTY_OBJECT_ARRAY;
      Field field = (Field) constructor.newInstance(noParameter);
      field.setName(fieldName);
      field.setOccurrence(occurrence);
      return field;
    } catch (NoSuchMethodException e) {
      throw new FormFatalException("TypeManager",
          "form.EXP_MISSING_EMPTY_CONSTRUCTOR", fieldImpl.getName(), e);
    } catch (ClassCastException e) {
      throw new FormFatalException("TypeManager", "form.EXP_NOT_A_FIELD",
          fieldImpl.getName(), e);
    } catch (Exception e) {
      throw new FormFatalException("TypeManager",
          "form.EXP_FIELD_CONSTRUCTION_FAILED", fieldImpl.getName(), e);
    }
  }

  @Override
  public boolean equals(Object obj) {
    try {
      if (obj instanceof String) {
        return obj.equals(this.getFieldName());
      } else if (obj instanceof GenericFieldTemplate) {
        return ((GenericFieldTemplate) obj).getFieldName().equals(
            this.getFieldName());
      }
      return false;
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return this.getFieldName().hashCode();
  }

  @Override
  public String[] getLanguages() {
    if (labels == null) {
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }

    return labels.keySet().stream().map(labels::get).toArray(String[]::new);
  }

  /**
   * Used by the castor xml mapping.
   */
  public List<Label> getLabelsObj() {
    return labelsObj;
  }

  /**
   * Used by the castor xml mapping.
   */
  public void setLabelsObj(List<Label> labelsObj) {
    this.labelsObj = labelsObj;
  }

  /**
   * Used by the castor xml mapping.
   */
  @Override
  public List<Parameter> getParametersObj() {
    return parametersObj;
  }

  /**
   * Used by the castor xml mapping.
   */
  public void setParametersObj(List<Parameter> parametersObj) {
    this.parametersObj = parametersObj;
  }

  @Override
  public boolean isSearchable() {
    return searchable;
  }

  public void setSearchable(boolean searchable) {
    this.searchable = searchable;
  }

  @Override
  public String getTemplateName() {
    return templateName;
  }

  public void setTemplateName(String templateName) {
    this.templateName = templateName;
  }

  @Override
  public boolean isUsedAsFacet() {
    return usedAsFacet;
  }

  public void setUsedAsFacet(boolean usedAsFacet) {
    this.usedAsFacet = usedAsFacet;
  }

  @SuppressWarnings({"CloneDoesntDeclareCloneNotSupportedException", "CloneDoesntCallSuperClone"})
  @Override
  public GenericFieldTemplate clone() {
    GenericFieldTemplate clone;
    try {
      clone = new GenericFieldTemplate();
      clone.setDisabled(this.isDisabled());
      clone.setDisplayerName(this.getDisplayerName());
      clone.setFieldName(this.getFieldName());
      clone.setHidden(this.isHidden());
      clone.setLabel(this.getLabel());
      clone.setLabelsObj(this.getLabelsObj());
      clone.setMandatory(this.isMandatory());
      clone.setParametersObj(this.getParametersObj());
      clone.setReadOnly(this.isReadOnly());
      clone.setSearchable(this.isSearchable());
      clone.setTemplateName(this.getTemplateName());
      clone.setTypeName(this.getTypeName());
      clone.setUsedAsFacet(isUsedAsFacet());
      clone.setMaximumNumberOfOccurrences(getMaximumNumberOfOccurrences());
    } catch (FormException e) {
      throw new RuntimeException(e);
    }
    return clone;
  }

  @Override
  public int getMaximumNumberOfOccurrences() {
    return maximumNumberOfOccurrences;
  }

  public void setMaximumNumberOfOccurrences(int nb) {
    // If the occurrence number is stricly lower than 1, the value is forced to 1.
    // This avoids potential technical errors in different uses of the formular.
    if (nb > 1) {
      maximumNumberOfOccurrences = nb;
    } else {
      maximumNumberOfOccurrences = 1;
    }
  }

  @Override
  public boolean isRepeatable() {
    return maximumNumberOfOccurrences > 1;
  }

}