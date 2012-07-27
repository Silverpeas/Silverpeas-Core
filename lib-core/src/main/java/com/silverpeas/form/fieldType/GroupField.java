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

import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.form.FormException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.OrganizationController;

/**
 * A GroupField stores a group reference.
 * @see Field
 * @see FieldDisplayer
 */
public class GroupField implements Field {

  /**
   * 
   */
  private static final long serialVersionUID = 3278935449715773819L;
  /**
   * The text field type name.
   */
  static public final String TYPE = "group";

  /**
   * Returns the type name.
   */
  public String getTypeName() {
    return TYPE;
  }

  /**
   * The no parameters constructor
   */
  public GroupField() {
  }

  /**
   * Returns the user id referenced by this field.
   */
  public String getGroupId() {
    return groupId;
  }

  /**
   * Set the group id referenced by this field.
   */
  public void setGroupId(String groupId) {
    SilverTrace.info("form", "GroupField.setGroupId",
        "root.MSG_GEN_ENTER_METHOD", "groupId = " + groupId);
    this.groupId = groupId;
  }

  /**
   * Returns true if the value is read only.
   */
  public boolean isReadOnly() {
    return false;
  }

  /**
   * Returns the string value of this field : aka the group name.
   */
  public String getValue() {
    SilverTrace.info("form", "GroupField.getValue", "root.MSG_GEN_PARAM_VALUE",
        "groupId = " + getGroupId());
    if (getGroupId() == null) {
      return null;
    }
    if (getGroupId().equals("")) {
      return "";
    }

    Group group = organizationController.getGroup(getGroupId());

    if (group == null) {
      return "group(" + getGroupId() + ")";
    }

    return group.getName();
  }

  /**
   * Returns the local value of this field. There is no local format for a group field, so the
   * language parameter is unused.
   */
  public String getValue(String language) {
    return getValue();
  }

  /**
   * Does nothing since a group reference can't be computed from a group name.
   */
  public void setValue(String value) throws FormException {
  }

  /**
   * Does nothing since a group reference can't be computed from a group name.
   */
  public void setValue(String value, String language) throws FormException {
  }

  /**
   * Always returns false since a group reference can't be computed from a group name.
   */
  public boolean acceptValue(String value) {
    return false;
  }

  /**
   * Always returns false since a group reference can't be computed from a group name.
   */
  public boolean acceptValue(String value, String language) {
    return false;
  }

  /**
   * Returns the Group referenced by this field.
   */
  public Object getObjectValue() {
    if (getGroupId() == null) {
      return null;
    }

    return organizationController.getGroup(getGroupId());
  }

  /**
   * Set user referenced by this field.
   */
  public void setObjectValue(Object value) throws FormException {
    if (value instanceof Group) {
      setGroupId(((Group) value).getId());
    } else if (value == null) {
      setGroupId("");
    } else {
      throw new FormException("GroupField.setObjectValue", "form.EXP_NOT_A_GROUP");
    }
  }

  /**
   * Returns true if the value is a String and this field isn't read only.
   */
  public boolean acceptObjectValue(Object value) {
    if (value instanceof Group) {
      return !isReadOnly();
    } else {
      return false;
    }
  }

  /**
   * Returns this field value as a normalized String : a group id
   */
  public String getStringValue() {
    return getGroupId();
  }

  /**
   * Set this field value from a normalized String : a user id
   */
  public void setStringValue(String value) {
    SilverTrace.info("form", "GroupField.setStringValue", "root.MSG_GEN_ENTER_METHOD", "value = " +
        value);
    setGroupId(value);
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
    return (getGroupId() == null);
  }

  /**
   * Set to null this field.
   * @throw FormException when the field is mandatory.
   * @throw FormException when the field is read only.
   */
  public void setNull() throws FormException {
    setGroupId(null);
  }

  /**
   * Tests equality beetwen this field and the specified field.
   */
  public boolean equals(Object o) {
    String s = getGroupId();

    if (o instanceof GroupField) {
      String t = ((GroupField) o).getGroupId();
      return ((s == null && t == null) || s.equals(t));
    } else {
      return false;
    }
  }

  /**
   * Compares this field with the specified field.
   */
  public int compareTo(Object o) {
    String s = getValue();
    if (s == null) {
      s = "";
    }

    if (o instanceof GroupField) {
      String t = ((GroupField) o).getValue();
      if (t == null) {
        t = "";
      }

      if (s.equals(t)) {
        s = getGroupId();
        if (s == null) {
          s = "";
        }
        t = ((GroupField) o).getGroupId();
        if (t == null) {
          t = "";
        }
      }
      return s.compareTo(t);
    } else {
      return -1;
    }
  }

  public int hashCode() {
    String s = getGroupId();
    return ("" + s).hashCode();
  }

  /**
   * The referenced groupId.
   */
  private String groupId = null;

  /**
   * The main access to the users set.
   */
  private static OrganizationController organizationController = new OrganizationController();

}
