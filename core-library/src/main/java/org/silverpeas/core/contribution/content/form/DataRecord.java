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

import java.io.Serializable;
import java.util.Map;

/**
 * A DataRecord is the interface used by all the form components to exchange, display and save a set
 * of named and typed fields which are unknown at compile time but defined by a silverpeas end user
 * in a workflow process model or a publication model. A DataRecord is modelized by a RecordTemplate
 * giving all the fields names and types. A DataRecord is built, managed and saved by a RecordSet.
 * @see Field
 * @see RecordSet
 * @see RecordTemplate
 */
public interface DataRecord extends Serializable {
  /**
   * Returns the data record id. This id is unique within the RecordSet from witch this DataRecord
   * has been extracted. This id is null when the DataRecord has been built from a RecordTemplate
   * but not yet inserted in a recordTemplate.
   */
  public String getId();

  /**
   * Gives an id to the data record. This id must be set before the record is inserted in a
   * RecordSet.
   */
  public void setId(String externalId);

  /**
   * Return true if this record has not been inserted in a RecordSet.
   */
  public boolean isNew();

  /**
   * Returns the named field.
   * @throws FormException when the fieldName is unknown.
   */
  public Field getField(String fieldName) throws FormException;

  public Field getField(String fieldName, int occurrence);

  /**
   * Returns the field at the index position in the record.
   * @throws FormException when the fieldIndex is unknown.
   */
  public Field getField(int fieldIndex) throws FormException;

  public String[] getFieldNames();

  public String getLanguage();

  public void setLanguage(String language);

  public Map<String, String> getValues(String language);

}
