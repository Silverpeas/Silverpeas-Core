/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.form.fieldType;

import java.util.ArrayList;
import java.util.List;

import com.silverpeas.form.AbstractMultiValuableField;
import com.silverpeas.form.Field;
import com.silverpeas.form.FormException;
import com.silverpeas.form.MultiValuableField;
import com.silverpeas.form.Util;
import com.silverpeas.util.StringUtil;

/**
 * A TextField stores a text value.
 *
 * @see Field
 * @see FieldDisplayer
 */
public abstract class TextField extends AbstractMultiValuableField {

  private static final long serialVersionUID = 983277921021971664L;
  /**
   * The text field type name.
   */
  static public final String TYPE = "text";
  static public final String CONTENT_TYPE = "contentType";
  static public final String CONTENT_TYPE_INT = "int";
  static public final String CONTENT_TYPE_FLOAT = "float";
  static public final String PARAM_MAXLENGTH = "maxLength";

  /**
   * Returns the type name.
   */
  @Override
  public String getTypeName() {
    return TYPE;
  }

  /**
   * Returns the string value of this field.
   */
  @Override
  public List<String> getValues() {
    return getStringValues();
  }

  /**
   * Returns the local value of this field. There is no local format for a text field, so the
   * language parameter is unused.
   */
  @Override
  public List<String> getValues(String language) {
    return getStringValues();
  }

  /**
   * Set this field value from a local string value.
   *
   * @throw FormException when the field is readOnly.
   * @throw FormException when the string value is ill formed.
   */
  @Override
  public void setValues(List<String> values) throws FormException {
    setStringValues(values);
  }

  /**
   * Set this field value from a local string value. There is no local format for a text field, so
   * the language parameter is unused.
   *
   * @throw FormException when the field is readOnly.
   * @throw FormException when the string value is ill formed.
   */
  @Override
  public void setValues(List<String> values, String language) throws FormException {
    setStringValues(values);
  }
  
  @Override
  public void setValue(String value, String lang) throws FormException {
    List<String> values = new ArrayList<String>();
    values.add(value);
    setValues(values, lang);
  }

  /**
   * Returns true if the value isn't ill formed and this field isn't read only. Here any string
   * value is accepted unless the field is read only.
   */
  @Override
  public boolean acceptValues(List<String> values) {
    return !isReadOnly();
  }

  /**
   * Returns true if the value isn't ill formed and this field isn't read only. Here any string
   * value is accepted unless the field is read only.
   */
  @Override
  public boolean acceptStringValues(List<String> values) {
    return !isReadOnly();
  }

  /**
   * Returns true if the value isn't ill formed and this field isn't read only. Here any string
   * value is accepted unless the field is read only.
   */
  @Override
  public boolean acceptValues(List<String> values, String language) {
    return !isReadOnly();
  }

  /**
   * Returns the value of this field.
   */
  @Override
  public List getObjectValues() {
    return getStringValues();
  }

  /**
   * Set this field value.
   *
   * @throw FormException when the field is readOnly.
   * @throw FormException when the value is not a String.
   */
  @Override
  public void setObjectValues(List<Object> values) throws FormException {
    List<String> strings = new ArrayList<String>();
    for (Object object : values) {
      if (object instanceof String) {
        strings.add((String) object);
      } else if (object != null) {
        throw new FormException("TextField.setObjectValue", "form.EXP_NOT_A_STRING");
      }
    }
    if (strings.isEmpty()) {
      setNull();
    }
  }

  /**
   * Returns true if the value is a String and this field isn't read only.
   */
  @Override
  public boolean acceptObjectValues(List<Object> values) {
    List<String> strings = new ArrayList<String>();
    for (Object object : values) {
      if (object instanceof String) {
        strings.add((String) object);
      } else {
        return false;
      }
    }
    return !isReadOnly();
  }

  /**
   * Returns true if this field is not set.
   */
  @Override
  public boolean isNull() {
    return (getStringValues() == null || !StringUtil.isDefined(StringUtil.join(getStringValues(), null)));
  }

  /**
   * Set to null this field.
   *
   * @throw FormException when the field is mandatory.
   * @throw FormException when the field is read only.
   */
  @Override
  public void setNull() throws FormException {
    setStringValues(null);
  }

  /**
   * Tests equality beetwen this field and the specified field.
   */
  @Override
  public boolean equals(Object o) {
    String s = Util.list2String(getStringValues());
    if (s == null) {
      s = "";
    }

    if (o instanceof TextField) {
      String t = Util.list2String(((TextField) o).getStringValues());
      if (t == null) {
        t = "";
      }
      return s.equalsIgnoreCase(t);
    } else if (o instanceof MultiValuableField) {
      String t = Util.list2String(((MultiValuableField) o).getValues());
      if (t == null) {
        t = "";
      }
      return s.equalsIgnoreCase(t);
    } else {
      return false;
    }
  }

  /**
   * Compares this field with the specified field.
   */
  @Override
  public int compareTo(Object o) {
    String s = Util.list2String(getStringValues());
    if (s == null) {
      s = "";
    }
    if (o instanceof TextField) {
      String t = Util.list2String(((TextField) o).getStringValues());
      if (t == null) {
        t = "";
      }
      return s.compareTo(t);
    } else if (o instanceof MultiValuableField) {
      String t = Util.list2String(((MultiValuableField) o).getValues());
      if (t == null) {
        t = "";
      }
      return s.compareTo(t);
    } else {
      return -1;
    }
  }

  @Override
  public int hashCode() {
    String s = Util.list2String(getStringValues());
    return ("" + s).toLowerCase().hashCode();
  }
}
