/**
 * Copyright (C) 2000 - 2011 Silverpeas
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

package com.silverpeas.form.fieldType;

import com.silverpeas.form.Field;
import com.silverpeas.form.FormException;

public class JdbcRefField implements Field {

  private static final long serialVersionUID = -2738403979429471532L;

  static public final String TYPE = "jdbcRef";

  private String value = "";

  public JdbcRefField() {

  }

  public String getTypeName() {
    return TYPE;
  }

  public boolean acceptObjectValue(Object value) {
    return false;
  }

  public boolean acceptStringValue(String value) {
    return false;
  }

  public boolean acceptValue(String value) {
    return false;
  }

  public boolean acceptValue(String value, String lang) {
    return false;
  }

  public Object getObjectValue() {
    return getStringValue();
  }

  public String getStringValue() {
    return value;
  }

  public void setStringValue(String value) {
    this.value = value;
  }

  public String getValue() {
    return getStringValue();
  }

  public String getValue(String lang) {
    return getStringValue();
  }

  public boolean isNull() {
    return (getStringValue() == null || getStringValue().trim().equals(""));
  }

  public void setNull() throws FormException {
    setStringValue(null);
  }

  public void setObjectValue(Object value) throws FormException {
    if (value instanceof String) {
      setStringValue((String) value);
    } else {
      if (value != null) {
        throw new FormException("JdbcRefField.setObjectValue",
            "form.EXP_NOT_A_STRING");
      } else {
        setNull();
      }
    }
  }

  public void setValue(String value) throws FormException {
    setStringValue(value);
  }

  public void setValue(String value, String lang) throws FormException {
    setStringValue(value);
  }

  public int compareTo(Object o) {
    String s = getStringValue();
    if (s == null) {
      s = "";
    }
    if (o instanceof JdbcRefField) {
      String t = ((JdbcRefField) o).getStringValue();
      if (t == null) {
        t = "";
      }
      return s.compareTo(t);
    } else if (o instanceof Field) {
      String t = ((Field) o).getValue("");
      if (t == null) {
        t = "";
      }
      return s.compareTo(t);
    } else {
      return -1;
    }
  }

}