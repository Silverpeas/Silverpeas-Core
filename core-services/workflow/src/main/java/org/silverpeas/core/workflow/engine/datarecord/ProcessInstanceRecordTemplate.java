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

package org.silverpeas.core.workflow.engine.datarecord;

import java.util.HashMap;
import java.util.Iterator;

import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.workflow.api.model.Action;
import org.silverpeas.core.workflow.api.model.Column;
import org.silverpeas.core.workflow.api.model.DataFolder;
import org.silverpeas.core.workflow.api.model.Item;
import org.silverpeas.core.workflow.api.model.Presentation;
import org.silverpeas.core.workflow.api.model.ProcessModel;

/**
 * A ProcessInstanceRecordTemplate describes all the data grouped in a ProcessInstanceDataRecord.
 * The instance : instance instance.title instance.<columnName> The model : model model.label
 * model.peas-label The folder : <folderItem> The forms : form.<formName> form.<formName>.title
 * form.<formName>.<fieldItem> The actions : action.<actionName> action.<actionName>.label
 * action.<actionName>.date action.<actionName>.actor The users : participant.<participantName>
 */
public class ProcessInstanceRecordTemplate implements RecordTemplate {
  /**
   * Builds the record template of a process model.
   */
  public ProcessInstanceRecordTemplate(ProcessModel processModel, String role,
      String lang) {
    if (role == null)
      role = "";
    if (lang == null)
      lang = "";
    this.processModel = processModel;
    this.role = role;
    this.lang = lang;
    init();
  }

  /**
   * Returns all the field names of the DataRecord built on this template.
   */
  public String[] getFieldNames() {
    if (fieldNames == null) {
      fieldNames = new String[fields.size()];
      Iterator names = fields.keySet().iterator();
      String name;
      while (names.hasNext()) {
        name = (String) names.next();
        try {
          fieldNames[getFieldIndex(name)] = name;
        } catch (Exception e) {
          // can't happen : the name is a key
        }
      }
    }
    return fieldNames;
  }

  /**
   * Returns all the field templates.
   */
  public FieldTemplate[] getFieldTemplates() throws FormException {
    if (fieldTemplates == null) {
      fieldTemplates = new FieldTemplate[fields.size()];
      Iterator names = fields.keySet().iterator();
      String name;
      while (names.hasNext()) {
        name = (String) names.next();
        try {
          fieldTemplates[getFieldIndex(name)] = getFieldTemplate(name);
        } catch (Exception e) {
          // can't happen : the name is a key
        }
      }
    }
    return fieldTemplates;
  }

  /**
   * Returns the FieldTemplate of the named field.
   * @throw FormException if the field name is unknown.
   */
  public FieldTemplate getFieldTemplate(String fieldName) throws FormException {
    IndexedFieldTemplate indexed = (IndexedFieldTemplate) fields.get(fieldName);

    if (indexed == null) {
      throw new FormException("ProcessInstanceRecordTemplate",
          "form.EXP_UNKNOWN_FIELD", fieldName);
    }

    return indexed.fieldTemplate;
  }

  /**
   * Returns the FieldTemplate at the given position
   * @throw FormException if the field index is out of bound.
   */
  public FieldTemplate getFieldTemplate(int fieldIndex) throws FormException {
    if (0 <= fieldIndex && fieldIndex < fields.size()) {
      return getFieldTemplates()[fieldIndex];
    } else {
      throw new FormException("ProcessInstanceRecordTemplate",
          "form.EXP_UNKNOWN_FIELD", "" + fieldIndex);
    }
  }

  /**
   * Returns the Field index of the named field.
   * @throw FormException if the field name is unknown.
   */
  public int getFieldIndex(String fieldName) throws FormException {
    IndexedFieldTemplate indexed = (IndexedFieldTemplate) fields.get(fieldName);

    if (indexed == null) {
      throw new FormException("ProcessInstanceRecordTemplate",
          "form.EXP_UNKNOWN_FIELD", fieldName);
    }

    return indexed.index;
  }

  /**
   * Throws an illegal call exception, since an empty DataRecord can't be built from this template.
   */
  public DataRecord getEmptyRecord() throws FormException {
    throw new FormException("workflowEngine", "workflowEngine.EXP_ILLEGAL_CALL");
  }

  /**
   * Returns true if the data record is built on this template.
   */
  public boolean checkDataRecord(DataRecord record) {
    if (record instanceof ProcessInstanceDataRecord) {
      ProcessInstanceDataRecord instanceRecord = (ProcessInstanceDataRecord) record;
      return this == instanceRecord.template;
    } else
      return false;
  }

  /**
   * Builds a Field[] with the correct size().
   */
  public Field[] buildFieldsArray() {
    return new Field[fields.size()];
  }

  /**
   * The process model.
   */
  private final ProcessModel processModel;

  /**
   * The role giving this view of the process.
   */
  private final String role;

  /**
   * The lang used for this view of the process.
   */
  private final String lang;

  /**
   * The field names.
   */
  private String[] fieldNames = null;

  /**
   * The field templates.
   */
  private FieldTemplate[] fieldTemplates = null;

  /**
   * The map (fieldName -> IndexedFieldTemplate).
   */
  private HashMap fields = new HashMap();

  /**
   * Inits the fields
   */
  private void init() {
    // The instance : instance
    // instance.title
    // instance.state

    addField(new TitleTemplate("instance", processModel, role, lang));
    addField(new TitleTemplate("instance.title", processModel, role, lang));
    addField(new StateTemplate("instance.state", processModel, role, lang));

    // The instance columns : instance.<columnName>

    Presentation presentation = processModel.getPresentation();
    if (presentation != null) {
      Column[] columns = presentation.getColumns(role);
      Item item;
      for (int i = 0; columns != null && i < columns.length; i++) {
        item = columns[i].getItem();
        if (item != null) {
          addField(new ItemTemplate("instance." + item.getName(), item, role,
              lang));
        }
      }
    }

    // The model : model
    // model.label
    // model.peas-label

    // xoxox a ecrire

    // The folder : folder.<folderItem>

    Item[] items = processModel.getDataFolder().getItems();

    for (int i = 0; items != null && i < items.length; i++) {
      if (items[i] != null) {
        addField(new ItemTemplate("folder." + items[i].getName(), items[i],
            role, lang));
      }
    }

    // The forms : form.<formName>
    // form.<formName>.title
    // form.<formName>.<fieldItem>

    // xoxoxa ecrire

    // The actions : action.<actionName>
    // action.<actionName>.label
    // action.<actionName>.date
    // action.<actionName>.actor

    Action[] actions = processModel.getActions();
    DataFolder userInfos = processModel.getUserInfos();
    Item[] userItems = null;
    if (userInfos != null) {
      userItems = userInfos.getItems();
    }

    for (int i = 0; actions != null && i < actions.length; i++) {
      addField(new ActionLabelTemplate("action." + actions[i].getName(),
          actions[i], role, lang));
      addField(new ActionLabelTemplate("action." + actions[i].getName()
          + ".label", actions[i], role, lang));
      addField(new ActionDateTemplate("action." + actions[i].getName()
          + ".date", actions[i], role, lang));
      addField(new ActionActorTemplate("action." + actions[i].getName()
          + ".actor", actions[i], role, lang));

      for (int j = 0; userItems != null && j < userItems.length; j++) {
        if (userItems[j] != null) {
          addField(new UserInfoTemplate("action." + actions[i].getName()
              + ".actor." + userItems[j].getName(), userItems[j], role, lang));
        }
      }

    }

    // The users : participant.<participantName>

    // xoxox a ecrire
  }

  /**
   * Adds the given fieldTemplate.
   */
  private void addField(FieldTemplate fieldTemplate) {
    fields.put(fieldTemplate.getFieldName(), new IndexedFieldTemplate(fields
        .size(), fieldTemplate));

  }
}
