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
package com.silverpeas.form.dummy;

import com.silverpeas.form.*;

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
  }

  /**
   * Returns all the field names of the DataRecord built on this template.
   */
  public String[] getFieldNames() {
    return new String[0];
  }

  /**
   * Returns all the field templates.
   */
  public FieldTemplate[] getFieldTemplates() {
    return new FieldTemplate[0];
  }

  /**
   * Returns the FieldTemplate of the named field.
   */
  public FieldTemplate getFieldTemplate(String fieldName) throws FormException {
    return fieldTemplate;
  }

  /**
   * Returns the field index of the named field.
   * 
   * @throw FormException if the field name is unknown.
   */
  public int getFieldIndex(String fieldName) throws FormException {
    return 0;
  }

  /**
   * Returns an empty DataRecord built on this template.
   */
  public DataRecord getEmptyRecord() throws FormException {
    return dataRecord;
  }

  /**
   * Returns true if the data record is built on this template and all the
   * constraints are ok.
   */
  public boolean checkDataRecord(DataRecord record) {
    return true;
  }

}
