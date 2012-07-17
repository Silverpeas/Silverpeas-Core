/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.workflow.engine.model;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;

import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.model.AbstractDescriptor;
import com.silverpeas.workflow.api.model.ContextualDesignation;
import com.silverpeas.workflow.api.model.ContextualDesignations;
import com.silverpeas.workflow.api.model.Item;
import com.silverpeas.workflow.api.model.Parameter;
import com.silverpeas.workflow.engine.AbstractReferrableObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Class implementing the representation of the &lt;item&gt; element of a Process Model.
 **/
public class ItemImpl extends AbstractReferrableObject implements AbstractDescriptor, Item,
    Serializable {
  private static final long serialVersionUID = -888974957146029109L;
  private String name;
  private boolean computed = false;
  private ContextualDesignations labels;
  private ContextualDesignations descriptions;
  private List<Parameter> parameters;
  private String type;
  private boolean readonly;
  private String formula;
  private String mapTo;

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
    labels = new SpecificLabelListHelper();
    descriptions = new SpecificLabelListHelper();
    parameters = new ArrayList<Parameter>();
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
   * @see com.silverpeas.workflow.api.model.Item#setComputed(boolean)
   */
  @Override
  public void setComputed(boolean computed) {
    this.computed = computed;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Item#setFormula(java.lang.String)
   */
  public void setFormula(String formula) {
    this.formula = formula;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Item#setMapTo(java.lang.String)
   */
  public void setMapTo(String mapTo) {
    this.mapTo = mapTo;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Item#setName(java.lang.String)
   */
  public void setName(String name) {
    this.name = name;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Item#setReadonly(boolean)
   */
  public void setReadonly(boolean readonly) {
    this.readonly = readonly;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Item#setType(java.lang.String)
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Get description in specific language for the given role
   * @param lang description's language
   * @param role role for which the description is
   * @return wanted description as a String object. If description is not found, search description
   * with given role and default language, if not found again, return the default description in
   * given language, if not found again, return the default description in default language, if not
   * found again, return empty string.
   */
  public String getDescription(String role, String language) {
    return descriptions.getLabel(role, language);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Item#getDescriptions()
   */
  public ContextualDesignations getDescriptions() {
    return descriptions;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Item#addDescription(com.silverpeas.workflow
   * .api.model.ContextualDesignation)
   */
  public void addDescription(ContextualDesignation description) {
    descriptions.addContextualDesignation(description);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Item#iterateDescription()
   */
  public Iterator<ContextualDesignation> iterateDescription() {
    return descriptions.iterateContextualDesignation();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Item#createDesignation()
   */
  public ContextualDesignation createDesignation() {
    return labels.createContextualDesignation();
  }

  /**
   * Get label in specific language for the given role
   * @param lang label's language
   * @param role role for which the label is
   * @return wanted label as a String object. If label is not found, search label with given role
   * and default language, if not found again, return the default label in given language, if not
   * found again, return the default label in default language, if not found again, return empty
   * string.
   */
  public String getLabel(String role, String language) {
    return labels.getLabel(role, language);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Item#addLabel(com.silverpeas.workflow
   * .api.model.ContextualDesignation)
   */
  public void addLabel(ContextualDesignation label) {
    labels.addContextualDesignation(label);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Item#iterateLabel()
   */
  public Iterator<ContextualDesignation> iterateLabel() {
    return labels.iterateContextualDesignation();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Item#getLabels()
   */
  public ContextualDesignations getLabels() {
    return labels;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Item#getParameter(java.lang.String)
   */
  public Parameter getParameter(String strName) {
    Parameter reference = new ParameterImpl();
    int idx;

    reference.setName(strName);
    idx = parameters.indexOf(reference);

    if (idx >= 0)
      return parameters.get(idx);
    else
      return null;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Item#createParameter()
   */
  public Parameter createParameter() {
    return new ParameterImpl();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Item#addParameter(com.silverpeas.workflow
   * .api.model.Parameter)
   */
  public void addParameter(Parameter parameter) {
    parameters.add(parameter);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Item#iterateParameters()
   */
  public Iterator<Parameter> iterateParameter() {
    return parameters.iterator();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Item#removeParameter(java.lang.String)
   */
  public void removeParameter(String strName) throws WorkflowException {
    Parameter parameter = createParameter();

    parameter.setName(strName);

    if (parameters == null)
      return;

    if (!parameters.remove(parameter))
      throw new WorkflowException("ItemImpl.removeParameter()", //$NON-NLS-1$
          "workflowEngine.EX_PARAMETER_NOT_FOUND", // $NON-NLS-1$
          strName == null ? "<null>" //$NON-NLS-1$
              : strName);
  }

  /**
   * Get the unique key, used by equals method
   * @return unique key
   */
  public String getKey() {
    return (this.name);
  }

  public Hashtable<String, String> getKeyValuePairs() {
    Hashtable<String, String> keyValuePairs = new Hashtable<String, String>();

    if (parameters != null && parameters.size() > 0) {
      String keys = null;
      String values = null;

      for (Parameter parameter : parameters) {
        if (parameter != null && "keys".equals(parameter.getName()))
          keys = parameter.getValue();
        if (parameter != null && "values".equals(parameter.getName()))
          values = parameter.getValue();
      }

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
    }
    return keyValuePairs;
  }

  /************* Implemented methods *****************************************/
  // ~ Instance fields ////////////////////////////////////////////////////////

  private AbstractDescriptor parent;
  private boolean hasId = false;
  private int id;

  // ~ Methods ////////////////////////////////////////////////////////////////

  public void setId(int id) {
    this.id = id;
    hasId = true;
  }

  public int getId() {
    return id;
  }

  public void setParent(AbstractDescriptor parent) {
    this.parent = parent;
  }

  public AbstractDescriptor getParent() {
    return parent;
  }

  public boolean hasId() {
    return hasId;
  }
}
