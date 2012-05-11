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

package com.silverpeas.form.fieldType;

import java.text.ParseException;

import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.form.FormException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DateUtil;

/**
 * A TextField stores a text value.
 * @see Field
 * @see FieldDisplayer
 */
public abstract class DateField implements Field {

  private static final long serialVersionUID = -885405651541562611L;
  /**
   * The text field type name.
   */
  public static final String TYPE = "date";

  /**
   * Returns the type name.
   */
  public String getTypeName() {
    return TYPE;
  }

  public abstract boolean isReadOnly();

  public String getValue() {
    return getStringValue();
  }

  public String getValue(String language) {
    return formatClient(getStringValue(), language);
  }

  public void setValue(String value) throws FormException {
    setStringValue(value);
  }

  public void setValue(String value, String language) throws FormException {
    setStringValue(formatBD(value, language));
  }

  private String formatClient(String value, String language) {
    // SimpleDateFormat formatter = new
    // SimpleDateFormat(Util.getString("GML.dateFormat", language));
    if ((value != null) && (!value.equals(""))) {
      try {
        /*
         * Date valueBD = formatterBD.parse(value); value = formatter.format(valueBD);
         */
        value = DateUtil.getInputDate(value, language);
      } catch (ParseException pe) {
        SilverTrace.error("form", "DateField", "form.EX_CANT_PARSE_DATE",
            "typeName = [" + getTypeName() + "]", pe);
      }
    }
    return value;
  }

  private String formatBD(String newValue, String language) {
    // SimpleDateFormat formatter = new
    // SimpleDateFormat(Util.getString("GML.dateFormat", language));
    String dateBD = null;
    try {
      /*
       * Date date = null; if ((newValue != null)&&(!newValue.equals(""))) date =
       * formatter.parse(newValue); if (date != null) dateBD = formatterBD.format(date);
       */
      dateBD = DateUtil.date2SQLDate(newValue, language);
    } catch (ParseException pe) {
      SilverTrace.error("form", "DateField", "form.EX_CANT_PARSE_DATE",
          "typeName = [" + getTypeName() + "]", pe);
    }
    return dateBD;
  }

  public boolean acceptValue(String value) {
    return !isReadOnly();
  }

  public boolean acceptValue(String value, String language) {
    return !isReadOnly();
  }

  /**
   * Returns the value of this field.
   */
  public Object getObjectValue() {
    return getStringValue();
  }

  /**
   * Set this field value.
   * @throw FormException when the field is readOnly.
   * @throw FormException when the value is not a String.
   */
  public void setObjectValue(Object value) throws FormException {
    if (value instanceof String) {
      setStringValue((String) value);
    } else {
      if (value != null) {
        throw new FormException("DateField.setObjectValue",
            "form.EXP_NOT_A_STRING");
      } else {
        setNull();
      }
    }
  }

  public boolean acceptObjectValue(Object value) {
    if (value instanceof String) {
      return !isReadOnly();
    } else {
      return false;
    }
  }

  public boolean isNull() {
    return (getStringValue() == null || getStringValue().trim().equals(""));
  }

  public void setNull() throws FormException {
    setStringValue(null);
  }

  /**
   * Tests equality beetwen this field and the specified field.
   */
  public boolean equals(Object o) {
    String s = getStringValue();
    if (s == null) {
      s = "";
    }

    if (o instanceof DateField) {
      String t = ((DateField) o).getStringValue();
      if (t == null) {
        t = "";
      }

      return s.equals(t);
    } else {
      return false;
    }
  }

  /**
   * Compares this field with the specified field.
   */
  public int compareTo(Object o) {
    String s = getStringValue();
    if (s == null) {
      s = "";
    }

    if (o instanceof DateField) {
      String t = ((DateField) o).getStringValue();
      if (t == null) {
        t = "";
      }

      return s.compareTo(t);
    } else {
      return -1;
    }
  }

  public int hashCode() {
    String s = getStringValue();
    return ("" + s).hashCode();
  }

}