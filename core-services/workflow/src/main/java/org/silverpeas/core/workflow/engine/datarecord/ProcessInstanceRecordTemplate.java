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
package org.silverpeas.core.workflow.engine.datarecord;

import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.workflow.api.model.Action;
import org.silverpeas.core.workflow.api.model.Column;
import org.silverpeas.core.workflow.api.model.DataFolder;
import org.silverpeas.core.workflow.api.model.Item;
import org.silverpeas.core.workflow.api.model.Presentation;
import org.silverpeas.core.workflow.api.model.ProcessModel;

/**
 * A ProcessInstanceRecordTemplate describes all the data grouped in a
 * {@link ProcessInstanceDataRecord}.
 * @see ProcessInstanceDataRecord
 */
public class ProcessInstanceRecordTemplate extends ProcessInstanceTemplate {

  private static final String PROCESS_INSTANCE_RECORD_TEMPLATE = "ProcessInstanceRecordTemplate";
  private static final String FORM_EXP_UNKNOWN_FIELD = "form.EXP_UNKNOWN_FIELD";
  private static final String ACTION = "action.";

  /**
   * Builds the record template of a process model.
   * @param processModel the model of a workflow process.
   * @param role the role a user plays in the workflow process.
   * @param lang the ISO-631 code of a language in which the textual data in the process are.
   */
  public ProcessInstanceRecordTemplate(ProcessModel processModel, String role,
      String lang) {
    if (role == null) {
      role = "";
    }
    if (lang == null) {
      lang = "";
    }
    this.processModel = processModel;
    this.role = role;
    this.lang = lang;
    init();
  }

  @Override
  public FieldTemplate getFieldTemplate(String fieldName) throws FormException {
    IndexedFieldTemplate indexed = fields.get(fieldName);

    if (indexed == null) {
      throw new FormException(PROCESS_INSTANCE_RECORD_TEMPLATE, FORM_EXP_UNKNOWN_FIELD, fieldName);
    }

    return indexed.fieldTemplate;
  }

  @Override
  public FieldTemplate getFieldTemplate(int fieldIndex) throws FormException {
    if (0 <= fieldIndex && fieldIndex < fields.size()) {
      return getFieldTemplates()[fieldIndex];
    } else {
      throw new FormException(PROCESS_INSTANCE_RECORD_TEMPLATE,
          FORM_EXP_UNKNOWN_FIELD, "" + fieldIndex);
    }
  }

  @Override
  public int getFieldIndex(String fieldName) throws FormException {
    IndexedFieldTemplate indexed = fields.get(fieldName);

    if (indexed == null) {
      throw new FormException(PROCESS_INSTANCE_RECORD_TEMPLATE, FORM_EXP_UNKNOWN_FIELD, fieldName);
    }

    return indexed.index;
  }

  @Override
  public DataRecord getEmptyRecord() throws FormException {
    throw new FormException("workflowEngine", "workflowEngine.EXP_ILLEGAL_CALL");
  }

  /**
   * Returns true if the data record is built on this template.
   */
  @Override
  public boolean checkDataRecord(DataRecord record) {
    if (record instanceof ProcessInstanceDataRecord) {
      ProcessInstanceDataRecord instanceRecord = (ProcessInstanceDataRecord) record;
      return this == instanceRecord.template;
    } else {
      return false;
    }
  }

  /**
   * Builds a Field[] with the correct size().
   */
  @Override
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
   * Inits the fields
   */
  private void init() {
    // The instance : instance
    // instance.title
    // instance.state

    addField(new TitleTemplate("instance", role, lang));
    addField(new TitleTemplate("instance.title", role, lang));
    addField(new StateTemplate("instance.state", processModel, role, lang));

    // The instance columns : instance.<columnName>

    Presentation presentation = processModel.getPresentation();
    addInstanceFields(presentation);

    // The model : model
    // model.label
    // model.peas-label

    // to write

    // The folder : folder.<folderItem>

    Item[] items = processModel.getDataFolder().getItems();
    addFolderFields(items);

    // The forms : form.<formName>
    // form.<formName>.title
    // form.<formName>.<fieldItem>

    // to write

    // The actions : action.<actionName>
    // action.<actionName>.label
    // action.<actionName>.date
    // action.<actionName>.actor

    Action[] actions = processModel.getActions();
    addActionFields(actions);

    // The users : participant.<participantName>

    // to write
  }

  private void addActionFields(final Action[] actions) {
    DataFolder userInfos = processModel.getUserInfos();
    Item[] userItems = null;
    if (userInfos != null) {
      userItems = userInfos.getItems();
    }

    for (Action action : actions) {
      addField(new ActionLabelTemplate(ACTION + action.getName(),
          action, role, lang));
      addField(new ActionLabelTemplate(ACTION + action.getName()
          + ".label", action, role, lang));
      addField(new ActionDateTemplate(ACTION + action.getName()
          + ".date", action, lang));
      addField(new ActionActorTemplate(ACTION + action.getName()
          + ".actor", action, lang));

      if (userItems != null) {
        for (Item userItem : userItems) {
          if (userItem != null) {
            addField(
                new UserInfoTemplate(ACTION + action.getName() + ".actor." + userItem.getName(),
                    userItem, role, lang));
          }
        }
      }
    }
  }

  private void addFolderFields(final Item[] items) {
    for (Item item : items) {
      if (item != null) {
        addField(new ItemTemplate("folder." + item.getName(), item,
            role, lang));
      }
    }
  }

  private void addInstanceFields(final Presentation presentation) {
    if (presentation != null) {
      Column[] columns = presentation.getColumns(role);
      for (Column column : columns) {
        Item item = column.getItem();
        if (item != null) {
          addField(new ItemTemplate("instance." + item.getName(), item, role,
              lang));
        }
      }
    }
  }

  /**
   * Adds the given fieldTemplate.
   */
  private void addField(FieldTemplate fieldTemplate) {
    fields.put(fieldTemplate.getFieldName(), new IndexedFieldTemplate(fields
        .size(), fieldTemplate));

  }
}
