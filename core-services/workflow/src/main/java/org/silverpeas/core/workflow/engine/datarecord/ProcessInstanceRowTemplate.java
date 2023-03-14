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
import org.silverpeas.core.workflow.api.model.Column;
import org.silverpeas.core.workflow.api.model.Item;
import org.silverpeas.core.workflow.api.model.Presentation;
import org.silverpeas.core.workflow.api.model.ProcessModel;

/**
 * ProcessInstanceRowTemplate.
 */
public class ProcessInstanceRowTemplate extends ProcessInstanceTemplate {

  private static final String PROCESS_INSTANCE_RECORD_TEMPLATE = "ProcessInstanceRecordTemplate";
  private static final String FORM_EXP_UNKNOWN_FIELD = "form.EXP_UNKNOWN_FIELD";

  /**
   * Builds the record template of the process instance rows.
   */
  public ProcessInstanceRowTemplate(ProcessModel processModel, String role,
      String lang) {
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

  @Override
  public boolean checkDataRecord(DataRecord record) {
    if (record instanceof ProcessInstanceRowRecord) {
      ProcessInstanceRowRecord rowRecord = (ProcessInstanceRowRecord) record;
      return this == rowRecord.template;
    } else {
      return false;
    }
  }

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
    addField(new TitleTemplate("title", role, lang));
    addField(new StateTemplate("instance.state", processModel, role, lang));

    Presentation presentation = processModel.getPresentation();
    if (presentation != null) {
      Column[] columns = presentation.getColumns(role);
      Item item;
      for (int i = 0; columns != null && i < columns.length; i++) {
        item = columns[i].getItem();
        if (item != null) {
          addField(new ItemTemplate(item.getName(), item, role, lang));
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
