/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.form.record;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.silverpeas.form.Field;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.FormException;
import com.silverpeas.form.FormFatalException;
import com.silverpeas.form.TypeManager;

/**
 * A generic FieldTemplate implementation.
 */
public class GenericFieldTemplate implements FieldTemplate, Serializable {

  private static final long serialVersionUID = 1L;
  private String fieldName = null;
  private Class fieldImpl = null;
  private String typeName = null;
  private String displayerName = "";
  private boolean isMandatory = false;
  private boolean isReadOnly = false;
  private boolean isDisabled = false;
  private boolean isHidden = false;
  private String defaultLabel = null;
  private Map<String, String> labels = new HashMap<String, String>();
  private Map<String, String> parameters = new HashMap<String, String>();
  private List<Label> labelsObj = new ArrayList<Label>();
  private List<Parameter> parametersObj = new ArrayList<Parameter>();
  private boolean isSearchable = false;
  private String templateName = null;

  /**
   * Builds a GenericFieldTemplate
   */
  public GenericFieldTemplate() throws FormException {
  }

  /**
   * Builds a GenericFieldTemplate from a field name and a field type name. The type name must be
   * known by the type manager. You must use the set and add methods to change any default value.
   */
  public GenericFieldTemplate(String fieldName, String typeName)
      throws FormException {
    this.fieldName = fieldName;
    this.typeName = typeName;

    init();
  }

  /**
   * Builds a GenericFieldTemplate from a field name and a field implementation. You must use the
   * set and add methods to change any default value.
   */
  public GenericFieldTemplate(String fieldName, Class fieldImpl)
      throws FormException {
    this.fieldName = fieldName;
    this.fieldImpl = fieldImpl;

    init();
  }

  /**
   * Inits and check this FieldTemplate.
   */
  private void init() throws FormException {
    if (fieldImpl == null) {
      fieldImpl = TypeManager.getFieldImplementation(typeName);
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
  public String getTypeName() {
    return typeName;
  }

  /**
   * set the type name of the described field.
   */
  public void setTypeName(String typeName) throws FormException {
    this.typeName = typeName;
    this.fieldImpl = TypeManager.getFieldImplementation(typeName);
  }

  /**
   * Returns the name of the FieldDisplayer to display the described field.
   */
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
  public String getLabel() {
    if (defaultLabel == null)
      return ""/* fieldName */;
    else
      return defaultLabel;
  }

  /**
   * Returns the local label of the described field.
   */
  public String getLabel(String language) {
    String label = null;
    if (getLabels() != null)
      label = (String) getLabels().get(language);

    if (label == null)
      return getLabel();
    else
      return label;
  }

  /**
   * Returns a local parameter of the described field.
   */
  public String getParameter(String name, String language) {
    String parameter = null;
    if (getParameters(language) != null) {
      parameter = (String) getParameters(language).get(name);
    }

    if (parameter == null)
      return "";
    else
      return parameter;
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
    if (language == null || language.trim().equals(""))
      setLabel(label);
    else {
      if (labels == null)
        labels = new HashMap<String, String>();
      labels.put(language, label);
    }
  }

  /**
   * Adds a local parameter.
   */
  public void addParameter(String name, String value) {
    if (parameters == null)
      parameters = new HashMap<String, String>();
    parameters.put(name, value);
  }

  /**
   * Returns true when the described field must have a value.
   */
  public boolean isMandatory() {
    return isMandatory;
  }

  /**
   * Set or unset the isMandatory flag
   */
  public void setMandatory(boolean isMandatory) {
    this.isMandatory = isMandatory;
  }

  /**
   * Set or unset the isMandatory flag
   */
  public void setMandatory(Boolean isMandatory) {
    this.isMandatory = isMandatory.booleanValue();
  }

  /**
   * Returns true when the described field can't be updated.
   */
  public boolean isReadOnly() {
    return isReadOnly;
  }

  /**
   * Set or unset the isReadOnly flag
   */
  public void setReadOnly(boolean isReadOnly) {
    this.isReadOnly = isReadOnly;
  }

  /**
   * Returns true when the described field must be disabled.
   */
  public boolean isDisabled() {
    return isDisabled;
  }

  /**
   * Set or unset the isDisabled flag
   */
  public void setDisabled(boolean isDisabled) {
    this.isDisabled = isDisabled;
  }

  /**
   * Returns true when the described field must be hidden.
   */
  public boolean isHidden() {
    return isHidden;
  }

  /**
   * Set or unset the isHidden flag
   */
  public void setHidden(boolean isHidden) {
    this.isHidden = isHidden;
  }

  public Map<String, String> getParameters(String language) {
    // if ((parameters == null) || (parameters.size()==0)) {
    Iterator<Parameter> parametersIter = parametersObj.iterator();

    while (parametersIter.hasNext()) {
      Parameter parameter = parametersIter.next();
      addParameter(parameter.getName(), parameter.getValue(language));
    }
    // }
    return parameters;
  }

  public Hashtable<String, String> getKeyValuePairs(String language) {
    Hashtable<String, String> keyValuePairs = new Hashtable<String, String>();
    Map<String, String> parameters = getParameters(language);

    if (parameters == null)
      return keyValuePairs;

    String keys = parameters.get("keys");
    String values = parameters.get("values");
    if (keys != null && values != null) {
      StringTokenizer kTokenizer = new StringTokenizer(keys, "##");
      StringTokenizer vTokenizer = new StringTokenizer(values, "##");
      while (kTokenizer.hasMoreTokens()) {
        String key = kTokenizer.nextToken();
        String value = vTokenizer.nextToken();
        keyValuePairs.put(key, value);
      }
    } else if (keys != null && values == null) {
      StringTokenizer kTokenizer = new StringTokenizer(keys, "##");
      while (kTokenizer.hasMoreTokens()) {
        String key = kTokenizer.nextToken();
        keyValuePairs.put(key, key);
      }
    } else if (keys == null && values != null) {
      StringTokenizer vTokenizer = new StringTokenizer(values, "##");
      while (vTokenizer.hasMoreTokens()) {
        String value = vTokenizer.nextToken();
        keyValuePairs.put(value, value);
      }
    }
    return keyValuePairs;
  }

  public Map<String, String> getLabels() {
    if (labels == null || labels.size() == 0) {
      Iterator<Label> labelsIter = labelsObj.iterator();
      while (labelsIter.hasNext()) {
        Label label = labelsIter.next();
        addLabel(label.getLabel(), label.getLanguage());
      }
    }
    return labels;
  }

  /**
   * Returns an empty Field built on this template.
   */
  public Field getEmptyField() throws FormException {
    try {
      Class[] noParameterClass = new Class[0];
      Constructor constructor = fieldImpl.getConstructor(noParameterClass);
      Object[] noParameter = new Object[0];
      Field field = (Field) constructor.newInstance(noParameter);

      return field;
    } catch (NoSuchMethodException e) {
      throw new FormFatalException("TypeManager",
          "form.EXP_MISSING_EMPTY_CONSTRUCTOR", fieldImpl.getName());
    } catch (ClassCastException e) {
      throw new FormFatalException("TypeManager", "form.EXP_NOT_A_FIELD",
          fieldImpl.getName());
    } catch (Exception e) {
      throw new FormFatalException("TypeManager",
          "form.EXP_FIELD_CONSTRUCTION_FAILED", fieldImpl.getName());
    }
  }

  public boolean equals(Object obj) {
    try {
      if (obj instanceof String) {
        return obj.equals(this.getFieldName());
      } else if (obj instanceof GenericFieldTemplate)
        return ((GenericFieldTemplate) obj).getFieldName().equals(
            this.getFieldName());
      else
        return false;
    } catch (Exception e) {
      return false;
    }
  }

  public int hashCode() {
    return this.getFieldName().hashCode();
  }

  public String[] getLanguages() {
    if (labels == null)
      return new String[0];

    List<String> langs = new ArrayList<String>();
    Iterator<String> iter = labels.keySet().iterator();

    while (iter.hasNext()) {
      String lang = iter.next();
      langs.add(labels.get(lang));
    }

    return (String[]) langs.toArray(new String[0]);
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
  public List<Parameter> getParametersObj() {
    return parametersObj;
  }

  /**
   * Used by the castor xml mapping.
   */
  public void setParametersObj(List<Parameter> parametersObj) {
    this.parametersObj = parametersObj;
  }

  public boolean isSearchable() {
    return isSearchable;
  }

  public void setSearchable(boolean searchable) {
    isSearchable = searchable;
  }

  public String getTemplateName() {
    return templateName;
  }

  public void setTemplateName(String templateName) {
    this.templateName = templateName;
  }
  
  @Override
  public GenericFieldTemplate clone() {
    GenericFieldTemplate clone = null;
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
    } catch (FormException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return clone;
  }



}