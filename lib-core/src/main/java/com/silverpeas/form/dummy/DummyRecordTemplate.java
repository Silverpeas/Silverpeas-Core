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

package com.silverpeas.form.dummy;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.FormException;
import com.silverpeas.form.RecordTemplate;
import com.silverpeas.form.record.GenericDataRecord;
import com.silverpeas.util.ArrayUtil;

/**
 * A dummy record template.
 */
public class DummyRecordTemplate implements RecordTemplate {

  private DataRecord dataRecord;
  private FieldTemplate fieldTemplate;

  /**
   * A DummyRecordTemplate is empty.
   */
  public DummyRecordTemplate() {
    fieldTemplate = new DummyFieldTemplate();
    dataRecord = new DummyDataRecord();
  }

  public DummyRecordTemplate(RecordTemplate template) throws FormException {
    fieldTemplate = new DummyFieldTemplate();
    dataRecord = new GenericDataRecord(template);
  }

  /**
   * Returns all the field names of the DataRecord built on this template.
   * @return
   */
  @Override
  public String[] getFieldNames() {
    return ArrayUtil.EMPTY_STRING_ARRAY;
  }

  /**
   * Returns all the field templates.
   * @return
   */
  @Override
  public FieldTemplate[] getFieldTemplates() {
    return ArrayUtil.EMPTY_FIELD_TEMPLATE_ARRAY;
  }

  /**
   * Returns the FieldTemplate of the named field.
   * @param fieldName
   * @return
   * @throws FormException
   */
  @Override
  public FieldTemplate getFieldTemplate(String fieldName) throws FormException {
    return fieldTemplate;
  }

  /**
   * Returns the field index of the named field.
   * @param fieldName
   * @return
   * @throws FormException if the field name is unknown.
   */
  @Override
  public int getFieldIndex(String fieldName) throws FormException {
    return 0;
  }

  /**
   * Returns an empty DataRecord built on this template.
   * @return
   * @throws FormException
   */
  @Override
  public DataRecord getEmptyRecord() throws FormException {
    return dataRecord;
  }

  /**
   * Returns true if the data record is built on this template and all the constraints are ok.
   * @param record
   * @return
   */
  @Override
  public boolean checkDataRecord(DataRecord record) {
    return true;
  }
}
