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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.DataRecordUtil;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.contribution.content.form.record.GenericFieldTemplate;
import org.silverpeas.core.contribution.content.form.record.GenericRecordTemplate;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.model.ContextualDesignation;
import org.silverpeas.core.workflow.api.model.ContextualDesignations;
import org.silverpeas.core.workflow.api.model.Form;
import org.silverpeas.core.workflow.api.model.Input;
import org.silverpeas.core.workflow.api.model.Item;
import org.silverpeas.core.workflow.api.model.Parameter;
import org.silverpeas.kernel.annotation.NonNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class implementing the representation of the &lt;form&gt; element of a Process Model.
 */
@XmlRootElement(name = "form")
@XmlAccessorType(XmlAccessType.NONE)
public class FormImpl implements Form, Serializable {

  @XmlAttribute
  @XmlID
  private String name;
  @XmlAttribute
  private String role;
  @XmlAttribute
  private String htmlFileName;
  @XmlElement(name = "title", type = SpecificLabel.class)
  private List<ContextualDesignation> titles;
  @XmlElement(name = "input", type = ItemRef.class)
  private List<Input> inputList;

  /**
   * Constructor
   */
  public FormImpl() {
    reset();
  }

  /**
   * reset attributes
   */
  private void reset() {
    titles = new ArrayList<>();
    inputList = new Vector<>();
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public String getRole() {
    return role;
  }

  @Override
  public String getHTMLFileName() {
    return this.htmlFileName;
  }

  @Override
  public void setHTMLFileName(String htmlFileName) {
    this.htmlFileName = htmlFileName;
  }

  @Override
  public Input[] getInputs() {
    if (inputList == null) {
      return new Input[0];
    }
    return inputList.toArray(new Input[0]);
  }

  @Override
  public Input getInput(int idx) {
    return inputList.get(idx);
  }

  @Override
  public Input getInput(Input reference) {
    int idx = inputList.indexOf(reference);

    if (idx >= 0) {
      return inputList.get(idx);
    } else {
      return null;
    }
  }

  @Override
  public void addInput(Input input) {
    inputList.add(input);
  }

  @Override
  public Iterator<Input> iterateInput() {
    return inputList.iterator();
  }

  @Override
  public Input createInput() {
    return new ItemRef();
  }

  @Override
  public void removeInput(int idx) {
    inputList.remove(idx);
  }

  @Override
  public ContextualDesignations getTitles() {
    return new SpecificLabelListHelper(titles);
  }

  @Override
  public String getTitle(String role, String language) {
    return getTitles().getLabel(role, language);
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public void setRole(String role) {
    this.role = role;
  }

  @Override
  public RecordTemplate toRecordTemplate(String role, String lang)
      throws WorkflowException {
    return toRecordTemplate(role, lang, false);
  }

  @Override
  public DataRecord getDefaultRecord(String role, String lang, DataRecord data)
      throws WorkflowException {
    try {
      String fieldName;
      String value;
      DataRecord defaultRecord = this.toRecordTemplate(role, lang).getEmptyRecord();

      if (inputList == null) {
        return defaultRecord;
      }

      // Add all fields description in the RecordTemplate
      int count = 0;
      for (Input input : inputList) {
        // Get the input
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
    } catch (FormException e) {
      throw new WorkflowException(this.getClass().getName(), "Can't get default record", e);
    }
  }

  @Override
  public RecordTemplate toRecordTemplate(String role, String lang,
      boolean readOnly) throws WorkflowException {
    GenericRecordTemplate rt = new GenericRecordTemplate();
    if (inputList == null) {
      return rt;
    }

    try {
      // Add all fields description in the RecordTemplate
      int count = 0;
      for (Input input : inputList) {
        // Get the item definition
        ItemImpl item = (ItemImpl) input.getItem();
        if (item == null) {
          item = new ItemImpl();
          item.setName("label#" + count);
          item.setType("text");
          count++;
        }

        // create a new FieldTemplate and set attributes
        GenericFieldTemplate ft = createNewFieldTemplate(input, item, readOnly);

        setRoleInFieldTemplate(ft, input, item, role, lang);

        // add parameters
        addParametersInFieldTemplate(ft, item);

        // add the new FieldTemplate in RecordTemplate
        rt.addFieldTemplate(ft);
      }

      return rt;
    } catch (FormException fe) {
      throw new WorkflowException("FormImpl.toRecordTemplate()",
          "workflowEngine.EX_ERR_BUILD_FIELD_TEMPLATE", fe);
    }
  }

  private static void addParametersInFieldTemplate(GenericFieldTemplate ft, ItemImpl item) {
    Iterator<Parameter> parameters = item.iterateParameter();
    Parameter param;
    while (parameters.hasNext()) {
      param = parameters.next();
      if (param != null) {
        ft.addParameter(param.getName(), param.getValue());
      }
    }
  }

  private static void setRoleInFieldTemplate(GenericFieldTemplate ft, Input input, ItemImpl item,
      String role, String lang) {
    String label;
    if (role != null && lang != null) {
      label = input.getLabel(role, lang);
      if (label == null || label.isEmpty()) {
        ft.addLabel(item.getLabel(role, lang), lang);
      } else {
        ft.addLabel(label, lang);
      }
    }
  }

  @NonNull
  private GenericFieldTemplate createNewFieldTemplate(Input input, ItemImpl item, boolean readOnly)
      throws FormException {
    GenericFieldTemplate ft = new GenericFieldTemplate(item.getName(), item.getType());
    if (readOnly) {
      ft.setReadOnly(true);
    } else {
      ft.setReadOnly(input.isReadonly());
    }
    ft.setMandatory(input.isMandatory());
    ft.setTemplateName("form:" + name);
    if (input.getDisplayerName() != null
        && !input.getDisplayerName().isEmpty()) {
      ft.setDisplayerName(input.getDisplayerName());
    }
    return ft;
  }

}