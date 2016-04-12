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

package org.silverpeas.core.contribution.content.form.dummy;

import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.field.TextFieldImpl;
import org.silverpeas.core.util.ArrayUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * A dummy DataRecord .
 */
public class DummyDataRecord implements DataRecord {

  private static final long serialVersionUID = -3169937693303520239L;
  private Field field;

  public DummyDataRecord() {
    field = new TextFieldImpl();
  }

  public DummyDataRecord(Field field) {
    this.field = field;
  }

  /**
   * Returns the data record id.
   */
  @Override
  public String getId() {
    return "id";
  }

  /**
   * Gives an id to the data record.
   */
  @Override
  public void setId(String externalId) {
  }

  /**
   * Return true if this record has not been inserted in a RecordSet.
   */
  @Override
  public boolean isNew() {
    return true;
  }

  /**
   * Returns the named field.
   */
  @Override
  public Field getField(String fieldName) {
    return field;
  }

  @Override
  public Field getField(String fieldName, int occurrence) {
    return field;
  }

  /**
   * Returns the field at the index position in the record.
   */
  @Override
  public Field getField(int fieldIndex) {
    return field;
  }

  @Override
  public String[] getFieldNames() {
    return ArrayUtil.EMPTY_STRING_ARRAY;
  }

  @Override
  public String getLanguage() {
    return null;
  }

  @Override
  public void setLanguage(String language) {
  }

  @Override
  public Map<String, String> getValues(String language) {
    return new HashMap<>();
  }
}