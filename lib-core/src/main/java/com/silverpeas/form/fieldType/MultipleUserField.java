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
 * FLOSS exception.  You should have recieved a copy of the text describing
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.StringTokenizer;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.silverpeas.form.Field;
import com.silverpeas.form.FormException;

/**
 * A UserField stores user references.
 * @see Field
 * @see FieldDisplayer
 */
public class MultipleUserField implements Field {

  private static final long serialVersionUID = 1412147782354556460L;
  /**
   * The text field type name.
   */
  static public final String TYPE = "multipleUser";
  static public final String PARAM_NAME_SUFFIX = "$$ids";

  /**
   * Returns the type name.
   */
  public String getTypeName() {
    return TYPE;
  }

  /**
   * The no parameters constructor
   */
  public MultipleUserField() {
  }

  /**
   * Returns the user id referenced by this field.
   */
  public String[] getUserIds() {
    return userIds;
  }

  /**
   * Set the userd id referenced by this field.
   */
  public void setUserIds(String[] userIds) {
    SilverTrace.info("form", "MultipleUserField.setUserIds",
        "root.MSG_GEN_ENTER_METHOD", "userIds = " + userIds);
    this.userIds = userIds;
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
    SilverTrace.info("form", "MultipleUserField.getValue",
        "root.MSG_GEN_PARAM_VALUE", "userIds = " + getUserIds());
    if (getUserIds() == null)
      return null;
    if (getUserIds().length == 0)
      return "";

    StringBuffer value = new StringBuffer();
    UserDetail[] users = organizationController.getUserDetails(getUserIds());
    for (int i = 0; i < users.length; i++) {
      if (i > 0) {
        value.append("\n");
      }
      if (users[i] == null) {
        value.append("user(").append(userIds[i]).append(")");
      } else {
        value.append(users[i].getFirstName()).append(" ").append(
            users[i].getLastName());
      }
    }

    return value.toString();
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
    if ((getUserIds() == null) || (getUserIds().length == 0))
      return null;

    return organizationController.getUserDetails(getUserIds());
  }

  /**
   * Set user referenced by this field.
   */
  public void setObjectValue(Object value) throws FormException {
    if (value instanceof UserDetail[]) {
      UserDetail[] values = (UserDetail[]) value;
      String[] userIds = new String[values.length];
      for (int i = 0; i < values.length; i++) {
        userIds[i] = (values[i] == null) ? "" : values[i].getId();
      }
      setUserIds(userIds);
    } else if (value == null) {
      setUserIds(new String[0]);
    } else {
      throw new FormException("MultipleUserField.setObjectValue",
          "form.EXP_NOT_AN_USERS_ARRAY");
    }
  }

  /**
   * Returns true if the value is a String and this field isn't read only.
   */
  public boolean acceptObjectValue(Object value) {
    if (value instanceof UserDetail[])
      return !isReadOnly();
    else
      return false;
  }

  /**
   * Returns this field value as a normalized String : a user id
   */
  public String getStringValue() {
    String[] userIds = getUserIds();
    if (userIds == null) {
      return "";
    }

    StringBuffer values = new StringBuffer();
    for (int i = 0; i < userIds.length; i++) {
      if (i > 0) {
        values.append(",");
      }
      values.append(userIds[i]);
    }
    return values.toString();
  }

  /**
   * Set this field value from a normalized String : a user id
   */
  public void setStringValue(String value) {
    SilverTrace.info("form", "MultipleUserField.setStringValue",
        "root.MSG_GEN_ENTER_METHOD", "value = " + value);
    if (value == null) {
      setUserIds(null);
    } else {
      StringTokenizer tokenizer = new StringTokenizer(value, ",");
      Collection<String> userIds = new ArrayList<String>();
      while (tokenizer.hasMoreTokens()) {
        userIds.add(tokenizer.nextToken());
      }
      setUserIds(userIds.toArray(new String[0]));
    }
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
    return (getUserIds() == null);
  }

  /**
   * Set to null this field.
   * @throw FormException when the field is mandatory.
   * @throw FormException when the field is read only.
   */
  public void setNull() throws FormException {
    setUserIds(null);
  }

  /**
   * Tests equality beetwen this field and the specified field.
   */
  public boolean equals(Object o) {
    String[] usersMine = getUserIds();

    if (o instanceof MultipleUserField) {
      String[] usersYours = ((MultipleUserField) o).getUserIds();

      if (usersMine.length != usersYours.length) {
        return false;
      } else {
        Arrays.sort(usersMine);
        Arrays.sort(usersYours);
      }

      for (int i = 0; i < usersMine.length; i++) {
        if (!usersMine[i].equals(usersYours[i])) {
          return false;
        }
      }

      return true;
    } else
      return false;
  }

  /**
   * Compares this field with the specified field.
   */
  public int compareTo(Object o) {
    // this is nonsense to compare arrays
    return 0;
  }

  public int hashCode() {
    String[] s = getUserIds();
    return ("" + s).hashCode();
  }

  /**
   * The referenced userId.
   */
  private String[] userIds = null;

  /**
   * The main access to the users set.
   */
  private static OrganizationController organizationController = new OrganizationController();

}
