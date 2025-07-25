/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.contribution.content.form.record;

import org.silverpeas.core.contribution.content.form.*;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.kernel.util.Pair;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.lang.reflect.Constructor;
import java.util.*;

/**
 * A generic FieldTemplate implementation.
 */
@XmlRootElement(name = "fieldTemplate")
@XmlAccessorType(XmlAccessType.NONE)
public class GenericFieldTemplate implements FieldTemplate {

  private static final long serialVersionUID = 1L;
  @XmlElement(required = true)
  private String fieldName;
  private Class<? extends Field> fieldImpl;
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
  public GenericFieldTemplate(String fieldName, Class<? extends Field> fieldImpl) throws FormException {
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

    return Objects.requireNonNullElse(parameter, "");
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
    if (language == null || language.trim().isEmpty()) {
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

  @Override
  public FieldValuesTemplate getFieldValuesTemplate(String language) {
    var keyValuePairs = computeKeyValuePairs(language);
    var valuesTemplate = new FieldValuesTemplate(language);
    keyValuePairs.forEach(p -> valuesTemplate.withAsValue(p.getFirst(), p.getSecond()));
    return valuesTemplate;
  }

  private List<Pair<String, String>> computeKeyValuePairs(String language) {
    Map<String, String> theParameters = getParameters(language);
    if (theParameters == null) {
      return Collections.emptyList();
    }
    String keys = theParameters.get("keys");
    String values = theParameters.get("values");
    return computeOrderedKeyValuePairs(keys, values);
  }

  /**
   * Computes from the specified field values a dictionary of key -> value in which the key is the
   * unique identifier of a value (fetched from the specified keys parameter) and value is the
   * localized renderable label of the value (fetched from the specified values parameter). The
   * identifiers of * the field values are encoded into the specified single String {@code key} and
   * all the localized * labels of the field values are encoded into the specified single String
   * {@code values}. For * each key in the given keys parameter must match a value label in the
   * given values parameter.
   *
   * @param keys the identifiers of a field value.
   * @param values the localized renderable label of a field value.
   * @return a dictionary of field values such as the key is the value identifier and the value is
   * the renderable label of the value. Be caution the order of the field values as encoded into the
   * specified keys and values parameters is lost in the dictionary by the nature of the {@link Map}
   * itself.
   */
  public static Map<String, String> computeKeyValuePairs(String keys, String values) {
    return computeOrderedKeyValuePairs(keys, values).stream()
        .collect(HashMap::new,
            (m, kv) -> m.put(kv.getFirst(), kv.getSecond()),
            HashMap::putAll);
  }

  /**
   * Computes from the specified field values a list of pairs of key/label in which the key is the
   * unique identifier of a value and the label the localized label of the value. The identifiers of
   * the field values are encoded into the specified single String {@code key} and all the localized
   * labels of the field values are encoded into the specified single String {@code values}. For
   * each key in the given keys parameter must match a value label in the given values parameter.
   *
   * @param keys the identifiers of a field value.
   * @param values the localized labels of a field value.
   * @return a list of key/values pairs. The list is ordered in the order the keys and values are
   * encoded into the keys and values parameters.
   */
  public static List<Pair<String, String>> computeOrderedKeyValuePairs(String keys, String values) {
    List<Pair<String, String>> keyValuePairs = new ArrayList<>();
    if (keys != null && values != null) {
      var decodedKeys = Parameter.decode(keys);
      var decodedValues = Parameter.decode(values);
      for (int i = 0; i < decodedKeys.size(); i++) {
        keyValuePairs.add(Pair.of(decodedKeys.get(i), decodedValues.get(i)));
      }
    } else if (keys != null) {
      Parameter.decode(keys).forEach(k -> keyValuePairs.add(Pair.of(k, k)));
    } else if (values != null) {
      Parameter.decode(values).forEach(v -> keyValuePairs.add(Pair.of(v, v)));
    }
    return keyValuePairs;
  }

  public Map<String, String> getLabels() {
    if (labels == null || labels.isEmpty()) {
      for (Label label : labelsObj) {
        addLabel(label.getName(), label.getLanguage());
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
  public Field getEmptyField(int occurrence) throws FormException {
    try {
      Constructor<? extends Field> constructor = fieldImpl.getConstructor();
      Field field = constructor.newInstance();
      field.setName(fieldName);
      field.setOccurrence(occurrence);
      return field;
    } catch (Exception e) {
      throw new FormFatalException(e);
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
      return ArrayUtil.emptyStringArray();
    }

    return labels.keySet().stream().map(labels::get).toArray(String[]::new);
  }

  /**
   * Used by the XML mapping.
   */
  public List<Label> getLabelsObj() {
    return labelsObj;
  }

  /**
   * Used by the XML mapping.
   */
  public void setLabelsObj(List<Label> labelsObj) {
    this.labelsObj = labelsObj;
  }

  /**
   * Used by the XML mapping.
   */
  @Override
  public List<Parameter> getParameters() {
    return parametersObj;
  }

  /**
   * Used by the XML mapping.
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

  public GenericFieldTemplate copy() {
    GenericFieldTemplate copy;
    try {
      copy = new GenericFieldTemplate();
      copy.setDisabled(this.isDisabled());
      copy.setDisplayerName(this.getDisplayerName());
      copy.setFieldName(this.getFieldName());
      copy.setHidden(this.isHidden());
      copy.setLabel(this.getLabel());
      copy.setLabelsObj(this.getLabelsObj());
      copy.setMandatory(this.isMandatory());
      copy.setParametersObj(this.getParameters());
      copy.setReadOnly(this.isReadOnly());
      copy.setSearchable(this.isSearchable());
      copy.setTemplateName(this.getTemplateName());
      copy.setTypeName(this.getTypeName());
      copy.setUsedAsFacet(isUsedAsFacet());
      copy.setMaximumNumberOfOccurrences(getMaximumNumberOfOccurrences());
    } catch (FormException e) {
      throw new SilverpeasRuntimeException(e);
    }
    return copy;
  }

  @Override
  public int getMaximumNumberOfOccurrences() {
    return maximumNumberOfOccurrences;
  }

  public void setMaximumNumberOfOccurrences(int nb) {
    // If the occurrence number is strictly lower than 1, the value is forced to 1.
    // This avoids potential technical errors in different uses of the formula.
    maximumNumberOfOccurrences = Math.max(nb, 1);
  }

  @Override
  public boolean isRepeatable() {
    return maximumNumberOfOccurrences > 1;
  }

}