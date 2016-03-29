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
package org.silverpeas.core.contribution.content.form.field;

import org.silverpeas.core.contribution.content.form.AbstractField;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FormException;
import org.owasp.encoder.Encode;

public class JdbcRefField extends AbstractField {

  private static final long serialVersionUID = -2738403979429471532L;
  static public final String TYPE = "jdbcRef";
  private String value = "";

  public JdbcRefField() {
  }

  @Override
  public String getTypeName() {
    return TYPE;
  }

  @Override
  public boolean acceptObjectValue(Object value) {
    return false;
  }

  @Override
  public boolean acceptStringValue(String value) {
    return false;
  }

  @Override
  public boolean acceptValue(String value) {
    return false;
  }

  @Override
  public boolean acceptValue(String value, String lang) {
    return false;
  }

  @Override
  public Object getObjectValue() {
    return getStringValue();
  }

  @Override
  public String getStringValue() {
    return value;
  }

  @Override
  public void setStringValue(String value) {
    this.value = Encode.forHtml(value);
  }

  @Override
  public String getValue() {
    return getStringValue();
  }

  @Override
  public String getValue(String lang) {
    return getStringValue();
  }

  @Override
  public boolean isNull() {
    return (getStringValue() == null || getStringValue().trim().equals(""));
  }

  @Override
  public void setNull() throws FormException {
    setStringValue(null);
  }

  @Override
  public void setObjectValue(Object value) throws FormException {
    if (value instanceof String) {
      setStringValue((String) value);
    } else {
      if (value != null) {
        throw new FormException("JdbcRefField.setObjectValue", "form.EXP_NOT_A_STRING");
      } else {
        setNull();
      }
    }
  }

  @Override
  public void setValue(String value) throws FormException {
    setStringValue(value);
  }

  @Override
  public void setValue(String value, String lang) throws FormException {
    setStringValue(value);
  }

  @Override
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
