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

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.silverpeas.form.Field;
import com.silverpeas.form.FormException;

/**
 * A FileField stores an attachment reference.
 * @see Field
 * @see FieldDisplayer
 */
public class FileField implements Field {
  /**
   * The text field type name.
   */
  static public final String TYPE = TYPE_FILE;
  static public final String PARAM_NAME_SUFFIX = "$$id";

  /**
   * Returns the type name.
   */
  public String getTypeName() {
    return TYPE;
  }

  /**
   * The no parameters constructor
   */
  public FileField() {
  }

  /**
   * Returns the user id referenced by this field.
   */
  public String getAttachmentId() {
    return attachmentId;
  }

  /**
   * Set the userd id referenced by this field.
   */
  public void setAttachmentId(String attachmentId) {
    SilverTrace.info("form", "FileField.setAttachmentId",
        "root.MSG_GEN_ENTER_METHOD", "attachmentId = " + attachmentId);
    this.attachmentId = attachmentId;
  }

  /**
   * Returns true if the value is read only.
   */
  public boolean isReadOnly() {
    return false;
  }

  /**
   * Returns the string value of this field : aka the user name.
   */
  public String getValue() {
    SilverTrace.info("form", "FileField.getValue", "root.MSG_GEN_PARAM_VALUE",
        "attachmentId = " + getAttachmentId());
    return getAttachmentId();
  }

  /**
   * Returns the local value of this field. There is no local format for a user field, so the
   * language parameter is unused.
   */
  public String getValue(String language) {
    return getValue();
  }

  /**
   * Does nothing since a user reference can't be computed from a user name.
   */
  public void setValue(String value) throws FormException {
  }

  /**
   * Does nothing since a user reference can't be computed from a user name.
   */
  public void setValue(String value, String language) throws FormException {
  }

  /**
   * Always returns false since a user reference can't be computed from a user name.
   */
  public boolean acceptValue(String value) {
    return false;
  }

  /**
   * Always returns false since a user reference can't be computed from a user name.
   */
  public boolean acceptValue(String value, String language) {
    return false;
  }

  /**
   * Returns the User referenced by this field.
   */
  public Object getObjectValue() {
    return getAttachmentId();
  }

  /**
   * Set user referenced by this field.
   */
  public void setObjectValue(Object value) throws FormException {
    if (value instanceof String) {
      setAttachmentId(value.toString());
    } else if (value == null) {
      setAttachmentId("");
    } else {
      throw new FormException("FileField.setObjectValue",
          "form.EXP_NOT_AN_USER");
    }
  }

  /**
   * Returns true if the value is a String and this field isn't read only.
   */
  public boolean acceptObjectValue(Object value) {
    return (value instanceof String);
  }

  /**
   * Returns this field value as a normalized String : a user id
   */
  public String getStringValue() {
    return getAttachmentId();
  }

  /**
   * Set this field value from a normalized String : a user id
   */
  public void setStringValue(String value) {
    SilverTrace.info("form", "FileField.setStringValue",
        "root.MSG_GEN_ENTER_METHOD", "value = " + value);
    setAttachmentId(value);
  }

  /**
   * Returns true if this field isn't read only.
   */
  public boolean acceptStringValue(String value) {
    return !isReadOnly();
  }

  /**
   * Returns true if this field is not set.
   */
  public boolean isNull() {
    return (getAttachmentId() == null);
  }

  /**
   * Set to null this field.
   * @throw FormException when the field is mandatory.
   * @throw FormException when the field is read only.
   */
  public void setNull() throws FormException {
    setAttachmentId(null);
  }

  /**
   * Tests equality beetwen this field and the specified field.
   */
  public boolean equals(Object o) {
    String s = getAttachmentId();

    if (o instanceof FileField) {
      String t = ((FileField) o).getAttachmentId();
      return ((s == null && t == null) || s.equals(t));
    } else
      return false;
  }

  /**
   * Compares this field with the specified field.
   */
  public int compareTo(Object o) {
    String s = getValue();
    if (s == null)
      s = "";

    if (o instanceof FileField) {
      String t = ((FileField) o).getValue();
      if (t == null)
        t = "";

      if (s.equals(t)) {
        s = getAttachmentId();
        if (s == null)
          s = "";
        t = ((FileField) o).getAttachmentId();
        if (t == null)
          t = "";
      }

      return s.compareTo(t);
    } else
      return -1;
  }

  public int hashCode() {
    String s = getAttachmentId();
    return ("" + s).hashCode();
  }

  /**
   * The referenced userId.
   */
  private String attachmentId = null;

}