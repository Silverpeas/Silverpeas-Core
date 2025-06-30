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
package org.silverpeas.core.workflow.engine.model;

import java.io.Serializable;
import java.util.*;

import org.silverpeas.core.contribution.content.form.record.GenericFieldTemplate;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.model.ContextualDesignation;
import org.silverpeas.core.workflow.api.model.ContextualDesignations;
import org.silverpeas.core.workflow.api.model.Item;
import org.silverpeas.core.workflow.api.model.Parameter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class implementing the representation of the &lt;item&gt; element of a Process Model.
 */
@XmlRootElement(name = "item")
@XmlAccessorType(XmlAccessType.NONE)
public class ItemImpl implements Item, Serializable {

  @XmlID
  @XmlAttribute
  private String name;
  @XmlAttribute
  private boolean computed = false;
  @XmlElement(name = "label", type = SpecificLabel.class)
  private List<ContextualDesignation> labels;
  @XmlElement(name = "description", type = SpecificLabel.class)
  private List<ContextualDesignation> descriptions;
  @XmlElement
  private String type;
  @XmlElement
  private boolean readonly;
  @XmlElement
  private String formula;
  @XmlAttribute
  private String mapTo;
  @XmlElement(name = "param", type = ParameterImpl.class)
  private List<Parameter> parameters;

  /**
   * Constructor
   */
  public ItemImpl() {
    reset();
  }

  /**
   * reset attributes
   */
  private void reset() {
    labels = new ArrayList<>();
    descriptions = new ArrayList<>();
    parameters = new ArrayList<>();
  }

  /**
   * Get value of computed attribute
   * @return true if item must be computed
   */
  @Override
  public boolean isComputed() {
    return this.computed;
  }

  /**
   * Get formula to use if item must be computed
   * @return formula of type 'action.Validation.actor'
   */
  @Override
  public String getFormula() {
    return this.formula;
  }

  /**
   * Get the full user field name, to which this item is map
   * @return full user field name
   */
  @Override
  public String getMapTo() {
    return this.mapTo;
  }

  /**
   * Get the name of this item
   * @return item's name
   */
  @Override
  public String getName() {
    return this.name;
  }

  /**
   * Get value of readOnly attribute
   * @return true if item must be readonly
   */
  @Override
  public boolean isReadonly() {
    return this.readonly;
  }

  /**
   * Get the type of this item
   * @return item's type (text for text field)
   */
  @Override
  public String getType() {
    return this.type;
  }

  /*
   * (non-Javadoc)
   * @see Item#setComputed(boolean)
   */
  @Override
  public void setComputed(boolean computed) {
    this.computed = computed;
  }

  /*
   * (non-Javadoc)
   * @see Item#setFormula(java.lang.String)
   */
  public void setFormula(String formula) {
    this.formula = formula;
  }

  /*
   * (non-Javadoc)
   * @see Item#setMapTo(java.lang.String)
   */
  public void setMapTo(String mapTo) {
    this.mapTo = mapTo;
  }

  /*
   * (non-Javadoc)
   * @see Item#setName(java.lang.String)
   */
  public void setName(String name) {
    this.name = name;
  }

  /*
   * (non-Javadoc)
   * @see Item#setReadonly(boolean)
   */
  public void setReadonly(boolean readonly) {
    this.readonly = readonly;
  }

  /*
   * (non-Javadoc)
   * @see Item#setType(java.lang.String)
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Get description in specific language for the given role
   * @param role role for which the description is
   * @param language description's language
   * @return wanted description as a String object. If description is not found, search description
   * with given role and default language, if not found again, return the default description in
   * given language, if not found again, return the default description in default language, if not
   * found again, return empty string.
   */
  public String getDescription(String role, String language) {
    return getDescriptions().getLabel(role, language);
  }

  /*
   * (non-Javadoc)
   * @see Item#getDescriptions()
   */
  public ContextualDesignations getDescriptions() {
    return new SpecificLabelListHelper(descriptions);
  }

  /**
   * Get label in specific language for the given role
   * @param role role for which the label is
   * @param language label's language
   * @return wanted label as a String object. If label is not found, search label with given role
   * and default language, if not found again, return the default label in given language, if not
   * found again, return the default label in default language, if not found again, return empty
   * string.
   */
  public String getLabel(String role, String language) {
    return getLabels().getLabel(role, language);
  }

  /*
   * (non-Javadoc)
   * @see Item#getLabels()
   */
  public ContextualDesignations getLabels() {
    return new SpecificLabelListHelper(labels);
  }

  /*
   * (non-Javadoc)
   * @see Item#getParameter(java.lang.String)
   */
  public Parameter getParameter(String strName) {
    Parameter reference = new ParameterImpl();
    int idx;

    reference.setName(strName);
    idx = parameters.indexOf(reference);

    if (idx >= 0) {
      return parameters.get(idx);
    } else {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * @see Item#createParameter()
   */
  public Parameter createParameter() {
    return new ParameterImpl();
  }

  /*
   * (non-Javadoc)
   * @see Item#addParameter(com.silverpeas.workflow
   * .api.model.Parameter)
   */
  public void addParameter(Parameter parameter) {
    parameters.add(parameter);
  }

  /*
   * (non-Javadoc)
   * @see Item#iterateParameters()
   */
  public Iterator<Parameter> iterateParameter() {
    return parameters.iterator();
  }

  /*
   * (non-Javadoc)
   * @see Item#removeParameter(java.lang.String)
   */
  public void removeParameter(String strName) throws WorkflowException {
    Parameter parameter = createParameter();

    parameter.setName(strName);

    if (parameters == null) {
      return;
    }

    if (!parameters.remove(parameter)) {
      throw new WorkflowException("ItemImpl.removeParameter()", //$NON-NLS-1$
          "workflowEngine.EX_PARAMETER_NOT_FOUND", // $NON-NLS-1$
          strName == null ? "<null>" //$NON-NLS-1$
              : strName);
    }
  }

  public Map<String, String> getKeyValuePairs() {
    if (parameters != null && !parameters.isEmpty()) {
      String keys = null;
      String values = null;

      for (Parameter parameter : parameters) {
        if (parameter != null && "keys".equals(parameter.getName())) {
          keys = parameter.getValue();
        }
        if (parameter != null && "values".equals(parameter.getName())) {
          values = parameter.getValue();
        }
      }
      return GenericFieldTemplate.computeKeyValuePairs(keys, values);
    } else {
      return Collections.emptyMap();
    }
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ItemImpl)) {
      return false;
    }

    final ItemImpl item = (ItemImpl) o;

    return name.equals(item.name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }
}
