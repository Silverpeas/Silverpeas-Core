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
package org.silverpeas.core.contribution.content.form;

import java.io.Serializable;

/**
 * A RecordTemplate defines the schema of a {@link DataRecord} by defining the fields by their names
 * and types.
 * @see DataRecord
 */
public interface RecordTemplate extends Serializable {
  /**
   * Gets all the field names of the {@link DataRecord}s built on this template.
   * @return an array with the name of the fields defined by this template.
   */
  String[] getFieldNames();

  /**
   * Gets all the fields defined by this template.
   * @return an array of {@link FieldTemplate} instances, each of them being a template of a
   * {@link DataRecord} field.
   * @throws FormException is an error occurs while getting the template of the fields.
   */
  FieldTemplate[] getFieldTemplates() throws FormException;

  /**
   * Gets the {@link FieldTemplate} modelling the specified named field.
   * @param fieldName the name of a field
   * @return a {@link FieldTemplate} instance or null if there is no such template.
   * @throws FormException if the template of the specified named field cannot be got.
   * @apiNote the {@link FormException} can be thrown if the template of the named field cannot be
   * found instead of returning null. It depends of the implementation of the concrete class.
   */
  FieldTemplate getFieldTemplate(String fieldName) throws FormException;

  /**
   * Gets the index in this template of of the named field.
   * @param fieldName the name of a field.
   * @return the index of the named field in this template or -1 if no template of this field can be
   * found.
   * @throws FormException if the template of the specified named field cannot be got.
   * @apiNote the {@link FormException} can be thrown if the template of the named field cannot be
   * found instead of returning -1. It depends of the implementation of the concrete class.
   */
  int getFieldIndex(String fieldName) throws FormException;

  /**
   * Gets an empty DataRecord built on this template.
   * @return an empty {@link DataRecord} instance.
   */
  DataRecord getEmptyRecord() throws FormException;

  /**
   * Checks the specified {@link DataRecord} instance matches this template.
   * @param record a {@link DataRecord} object.
   * @return true if the data record is built on this template and all the constraints are
   * satisfied.
   */
  boolean checkDataRecord(DataRecord record);
}
