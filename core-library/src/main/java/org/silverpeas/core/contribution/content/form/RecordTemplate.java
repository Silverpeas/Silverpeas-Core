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

package org.silverpeas.core.contribution.content.form;

/**
 * A RecordTemplate describes DataRecord and gives the field names and type informations.
 * @see DataRecord
 */
public interface RecordTemplate {
  /**
   * Returns all the field names of the DataRecord built on this template.
   */
  String[] getFieldNames();

  /**
   * Returns all the field templates.
   */
  FieldTemplate[] getFieldTemplates() throws FormException;

  /**
   * Returns the FieldTemplate of the named field.
   * @throws FormException if the field name is unknown.
   */
  FieldTemplate getFieldTemplate(String fieldName) throws FormException;

  /**
   * Returns the Field index of the named field.
   * @throws FormException if the field name is unknown.
   */
  int getFieldIndex(String fieldName) throws FormException;

  /**
   * Returns an empty DataRecord built on this template.
   */
  DataRecord getEmptyRecord() throws FormException;

  /**
   * Returns true if the data record is built on this template and all the constraints are ok.
   */
  boolean checkDataRecord(DataRecord record);
}
