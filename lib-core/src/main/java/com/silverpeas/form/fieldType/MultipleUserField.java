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

import com.google.common.base.Joiner;
import com.silverpeas.form.Field;
import com.silverpeas.form.FormException;
import com.silverpeas.util.ArrayUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.StringTokenizer;

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

  /**
   * Returns the type name.
   * @return
   */
  @Override
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
   * @return
   */
  public String[] getUserIds() {
    return userIds;
  }

  /**
   * Set the user ids referenced by this field.
   * @param currentUserIds
   */
  public void setUserIds(String[] currentUserIds) {
    SilverTrace.info("form", "MultipleUserField.setUserIds",
        "root.MSG_GEN_ENTER_METHOD", "userIds = " + Arrays.toString(currentUserIds));
    if (currentUserIds != null) {
      this.userIds = Arrays.copyOf(currentUserIds, currentUserIds.length);
    } else {
      this.userIds = null;
    }
  }

  /**
   * Returns true if the value is read only.
   * @return
   */
  public boolean isReadOnly() {
    return false;
  }

  /**
   * Returns the string value of this field : aka the user name.
   * @return
   */
  @Override
  public String getValue() {
    SilverTrace.info("form", "MultipleUserField.getValue",
        "root.MSG_GEN_PARAM_VALUE", "userIds = " + Arrays.toString(getUserIds()));
    if (this.userIds == null) {
      return null;
    }
    if (this.userIds.length == 0) {
      return "";
    }

    StringBuilder value = new StringBuilder();
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
   * @param language
   * @return
   */
  @Override
  public String getValue(String language) {
    return getValue();
  }

  /**
   * Does nothing since a user reference can't be computed from a user name.
   * @param value
   * @throws FormException
   */
  @Override
  public void setValue(String value) throws FormException {
  }

  /**
   * Does nothing since a user reference can't be computed from a user name.
   * @param value
   * @param language
   * @throws FormException
   */
  @Override
  public void setValue(String value, String language) throws FormException {
  }

  /**
   * Always returns false since a user reference can't be computed from a user name.
   * @param value
   * @return
   */
  @Override
  public boolean acceptValue(String value) {
    return false;
  }

  /**
   * Always returns false since a user reference can't be computed from a user name.
   * @param value
   * @param language
   * @return
   */
  @Override
  public boolean acceptValue(String value, String language) {
    return false;
  }

  /**
   * Returns the User referenced by this field.
   * @return
   */
  @Override
  public Object getObjectValue() {
    if (this.userIds == null || this.userIds.length == 0) {
      return null;
    }
    return organizationController.getUserDetails(getUserIds());
  }

  /**
   * Set user referenced by this field.
   * @param value
   * @throws FormException
   */
  @Override
  public void setObjectValue(Object value) throws FormException {
    if (value instanceof UserDetail[]) {
      UserDetail[] values = (UserDetail[]) value;
      this.userIds = new String[values.length];
      for (int i = 0; i < values.length; i++) {
        userIds[i] = (values[i] == null) ? "" : values[i].getId();
      }
    } else if (value == null) {
      this.userIds = ArrayUtil.EMPTY_STRING_ARRAY;
    } else {
      throw new FormException("MultipleUserField.setObjectValue", "form.EXP_NOT_AN_USERS_ARRAY");
    }
  }

  /**
   * Returns true if the value is a String and this field isn't read only.
   * @param value
   * @return
   */
  @Override
  public boolean acceptObjectValue(Object value) {
    if (value instanceof UserDetail[]) {
      return !isReadOnly();
    }
    return false;
  }

  /**
   * Returns this field value as a normalized String : a user id
   * @return
   */
  @Override
  public String getStringValue() {
    if (userIds == null) {
      return "";
    }
    Joiner joiner = Joiner.on(",").skipNulls();
    return joiner.join(userIds);
  }

  /**
   * Set this field value from a normalized String : a user id
   * @param value
   */
  @Override
  public void setStringValue(String value) {
    SilverTrace.info("form", "MultipleUserField.setStringValue",
        "root.MSG_GEN_ENTER_METHOD", "value = " + value);
    if (value == null) {
      setUserIds(null);
    } else {
      StringTokenizer tokenizer = new StringTokenizer(value, ",");
      Collection<String> extractedIds = new ArrayList<String>();
      while (tokenizer.hasMoreTokens()) {
        extractedIds.add(tokenizer.nextToken());
      }
      setUserIds(extractedIds.toArray(new String[extractedIds.size()]));
    }
  }

  /**
   * Returns true if this field isn't read only.
   * @param value
   * @return
   */
  @Override
  public boolean acceptStringValue(String value) {
    return !isReadOnly();
  }

  /**
   * Returns true if this field is not set.
   * @return
   */
  @Override
  public boolean isNull() {
    return this.userIds == null;
  }

  /**
   * Set to null this field.
   * @throws FormException when the field is read only or when the field is mandatory.
   */
  @Override
  public void setNull() throws FormException {
    this.userIds = null;
  }

  /**
   * Tests equality beetwen this field and the specified field.
   * @param o
   * @return
   */
  @Override
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
    } else {
      return false;
    }
  }

  /**
   * Compares this field with the specified field. This is nonsense to compare arrays.
   * @param o
   * @return
   */
  @Override
  public int compareTo(Object o) {
    return 0;
  }

  @Override
  public int hashCode() {
    return ("" + Arrays.toString(this.userIds)).hashCode();
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
