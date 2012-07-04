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
import java.util.Iterator;
import java.util.Vector;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.DataRecordUtil;
import com.silverpeas.form.Field;
import com.silverpeas.form.FormException;
import com.silverpeas.form.RecordTemplate;
import com.silverpeas.form.record.GenericFieldTemplate;
import com.silverpeas.form.record.GenericRecordTemplate;
import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.model.AbstractDescriptor;
import com.silverpeas.workflow.api.model.ContextualDesignation;
import com.silverpeas.workflow.api.model.ContextualDesignations;
import com.silverpeas.workflow.api.model.Form;
import com.silverpeas.workflow.api.model.Input;
import com.silverpeas.workflow.api.model.Item;
import com.silverpeas.workflow.api.model.Parameter;
import com.silverpeas.workflow.engine.AbstractReferrableObject;

/**
 * Class implementing the representation of the &lt;form&gt; element of a Process Model.
 */
public class FormImpl extends AbstractReferrableObject implements Form, AbstractDescriptor,
    Serializable {
  private static final long serialVersionUID = 1721380617586523435L;
  private String name;
  private String role;
  private String HTMLFileName;
  private ContextualDesignations titles; // object storing the titles
  private Vector<Input> inputList;

  // ~ Instance fields related to AbstractDescriptor
  // ////////////////////////////////////////////////////////

  private AbstractDescriptor parent;
  private boolean hasId = false;
  private int id;

  /**
   * Constructor
   */
  public FormImpl() {
    reset();
  }

  /**
   * Constructor
   */
  public FormImpl(String name) {
    this();
    this.name = name;
  }

  /**
   * reset attributes
   */
  private void reset() {
    titles = new SpecificLabelListHelper();
    inputList = new Vector<Input>();
  }

  /**
   * Get the name of this form
   * @return form's name
   */
  public String getName() {
    return this.name;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Form#getRole()
   */
  public String getRole() {
    return role;
  }

  /**
   * Get the name of HTML file to show this form if no HTML file is defined, XMLForm will be used to
   * create the form
   * @return form's name
   */
  public String getHTMLFileName() {
    return this.HTMLFileName;
  }

  /**
   * Set the name of HTML file to show this form if no HTML file is defined, XMLForm will be used to
   * display the form
   * @return form's name
   */
  public void setHTMLFileName(String HTMLFileName) {
    this.HTMLFileName = HTMLFileName;
  }

  /**
   * Get the inputs
   * @return the inputs as an array
   */
  public Input[] getInputs() {
    if (inputList == null) {
      return null;
    }
    return (Input[]) inputList.toArray(new ItemRef[0]);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Form#getInput(int)
   */
  public Input getInput(int idx) {
    return (Input) inputList.get(idx);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Form#getInput(com.silverpeas.workflow .api.model.Input)
   */
  public Input getInput(Input reference) {
    int idx = inputList.indexOf(reference);

    if (idx >= 0) {
      return (Input) inputList.get(idx);
    } else {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Form#addInput(com.silverpeas.workflow .api.model.Input)
   */
  public void addInput(Input input) {
    inputList.add(input);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Form#iterateInput()
   */
  public Iterator<Input> iterateInput() {
    return inputList.iterator();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Form#createInput()
   */
  public Input createInput() {
    return new ItemRef();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Form#removeInput(int)
   */
  public void removeInput(int idx) throws WorkflowException {
    inputList.remove(idx);
  }

  // //////////////////
  // titles
  // //////////////////

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Form#getTitles()
   */
  public ContextualDesignations getTitles() {
    return titles;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Form#getTitle(java.lang.String, java.lang.String)
   */
  public String getTitle(String role, String language) {
    return titles.getLabel(role, language);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Form#addTitle(com.silverpeas.workflow
   * .api.model.ContextualDesignation)
   */
  public void addTitle(ContextualDesignation title) {
    titles.addContextualDesignation(title);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Form#iterateTitle()
   */
  public Iterator<ContextualDesignation> iterateTitle() {
    return titles.iterateContextualDesignation();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Form#createDesignation()
   */
  public ContextualDesignation createDesignation() {
    return titles.createContextualDesignation();
  }

  /**
   * Set the name of this form
   * @param name form's name
   **/
  public void setName(String name) {
    this.name = name;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Form#setRole(java.lang.String)
   */
  public void setRole(String role) {
    this.role = role;
  }

  /**
   * Get the unique key, used by equals method
   * @return unique key
   */
  public String getKey() {
    return (this.name);
  }

  /**
   * Converts this object in a RecordTemplate object
   * @return the resulting RecordTemplate
   */
  public RecordTemplate toRecordTemplate(String role, String lang)
      throws WorkflowException {
    return toRecordTemplate(role, lang, false);
  }

  /**
   * Converts this object in a DataRecord object
   * @return the resulting DataRecord object with the default values set
   */
  public DataRecord getDefaultRecord(String role, String lang, DataRecord data)
      throws FormException, WorkflowException {
    String fieldName = "";
    String value = "";
    DataRecord defaultRecord = this.toRecordTemplate(role, lang)
        .getEmptyRecord();

    if (inputList == null) {
      return defaultRecord;
    }

    // Add all fields description in the RecordTemplate
    int count = 0;
    for (int i = 0; i < inputList.size(); i++) {
      // Get the input
      Input input = (Input) inputList.get(i);

      // Get the item name referenced by this input
      Item item = input.getItem();
      if (item == null) {
        fieldName = "label#" + count;
        count++;
      } else {
        fieldName = item.getName();
      }
      // Compute the default value
      value = input.getValue();
      if (data != null) {
        value = DataRecordUtil.applySubstitution(value, data, lang);
      }

      // Set the field value
      Field field = defaultRecord.getField(fieldName);
      if (field != null) {
        field.setStringValue(value);
      }
    }

    return defaultRecord;
  }

  /**
   * Converts this object in a RecordTemplate object
   * @return the resulting RecordTemplate
   */
  public RecordTemplate toRecordTemplate(String role, String lang,
      boolean readOnly) throws WorkflowException {
    GenericRecordTemplate rt = new GenericRecordTemplate();
    String label = "";

    if (inputList == null)
      return rt;

    int count = 0;

    try {
      // Add all fields description in the RecordTemplate
      for (int i = 0; i < inputList.size(); i++) {
        // Get the item definition
        Input input = (Input) inputList.get(i);
        ItemImpl item = (ItemImpl) input.getItem();
        if (item == null) {
          item = new ItemImpl();
          item.setName("label#" + count);
          item.setType("text");
          count++;
        }

        // create a new FieldTemplate and set attributes
        GenericFieldTemplate ft = new GenericFieldTemplate(item.getName(), item
            .getType());
        if (readOnly) {
          ft.setReadOnly(true);
        } else {
          ft.setReadOnly(input.isReadonly());
        }
        ft.setMandatory(input.isMandatory());
        if (input.getDisplayerName() != null
            && input.getDisplayerName().length() > 0) {
          ft.setDisplayerName(input.getDisplayerName());
        }

        if (role != null && lang != null) {
          label = input.getLabel(role, lang);
          if (label == null || label.length() == 0) {
            ft.addLabel(item.getLabel(role, lang), lang);
          } else {
            ft.addLabel(label, lang);
          }
        }

        // add parameters
        Iterator<Parameter> parameters = item.iterateParameter();
        Parameter param = null;
        while (parameters.hasNext()) {
          param = parameters.next();
          if (param != null) {
            ft.addParameter(param.getName(), param.getValue());
          }
        }

        // add the new FieldTemplate in RecordTemplate
        rt.addFieldTemplate(ft);
      }

      return rt;
    } catch (FormException fe) {
      throw new WorkflowException("FormImpl.toRecordTemplate()",
          "workflowEngine.EX_ERR_BUILD_FIELD_TEMPLATE", fe);
    }
  }

  /************* Implemented methods *****************************************/
  // ~ Methods ////////////////////////////////////////////////////////////////

  /*
	 * 
	 */
  public void setId(int id) {
    this.id = id;
    hasId = true;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.AbstractDescriptor#getId()
   */
  public int getId() {
    return id;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.AbstractDescriptor#setParent(com.silverpeas
   * .workflow.api.model.AbstractDescriptor)
   */
  public void setParent(AbstractDescriptor parent) {
    this.parent = parent;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.AbstractDescriptor#getParent()
   */
  public AbstractDescriptor getParent() {
    return parent;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.AbstractDescriptor#hasId()
   */
  public boolean hasId() {
    return hasId;
  }
}
