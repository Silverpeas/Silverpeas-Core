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
import com.silverpeas.form.Util;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;

import org.silverpeas.core.admin.OrganisationController;
import org.silverpeas.core.admin.OrganisationControllerFactory;

/**
 * A UserField stores a user reference.
 *
 * @see Field
 * @see com.silverpeas.form.FieldDisplayer
 */
public class UserField extends AbstractMultiValuableField {

  private static final long serialVersionUID = -861888647155176647L;
  /**
   * The text field type name.
   */
  static public final String TYPE = "user";

  /**
   * Returns the type name.
   */
  @Override
  public String getTypeName() {
    return TYPE;
  }

  /**
   * The no parameters constructor
   */
  public UserField() {
  }

  /**
   * Returns the user id referenced by this field.
   */
  public List<String> getUserIds() {
    return userIds;
  }

  /**
   * Set the userd id referenced by this field.
   */
  public void setUserIds(List<String> userIds) {
    SilverTrace.info("form", "UserField.setUserId",
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
  @Override
  public List<String> getValues() {
    String theUserIds = StringUtil.join(getUserIds(), ", ");
    SilverTrace.info("form", "UserField.getValue", "root.MSG_GEN_PARAM_VALUE",
        "userIds = " + theUserIds);
    if (!StringUtil.isDefined(theUserIds)) {
      return getUserIds();
    }

    OrganisationController oc = OrganisationControllerFactory.getOrganisationController();
    List<String> userNames = new ArrayList<String>();
    for (String userId : getUserIds()) {
      UserDetail user = oc.getUserDetail(userId);
      if (user == null) {
        userNames.add("user(" + userId + ")");
      } else {
        userNames.add(user.getDisplayedName());
      }
    }
    return userNames;
  }

  /**
   * Returns the local value of this field. There is no local format for a user field, so the
   * language parameter is unused.
   */
  @Override
  public List<String> getValues(String language) {
    return getValues();
  }

  /**
   * Does nothing since a user reference can't be computed from a user name.
   */
  @Override
  public void setValues(List<String> values) throws FormException {
  }

  /**
   * Does nothing since a user reference can't be computed from a user name.
   */
  @Override
  public void setValues(List<String> values, String language) throws FormException {
  }

  /**
   * Always returns false since a user reference can't be computed from a user name.
   */
  @Override
  public boolean acceptValues(List<String> values) {
    return false;
  }

  /**
   * Always returns false since a user reference can't be computed from a user name.
   */
  @Override
  public boolean acceptValues(List<String> values, String language) {
    return false;
  }

  /**
   * Returns the User referenced by this field.
   */
  @Override
  public List<Object> getObjectValues() {
    if (getUserIds() == null) {
      return null;
    }
    List<Object> users = new ArrayList<Object>();
    OrganisationController oc = OrganisationControllerFactory.getOrganisationController();
    for (String userId : getUserIds()) {
      if (StringUtil.isDefined(userId)) {
        users.add(oc.getUserDetail(userId));
      }
    }
    return users;
  }

  /**
   * Set user referenced by this field.
   */
  @Override
  public void setObjectValues(List<Object> values) throws FormException {
    List<String> userIds = new ArrayList<String>();
    for (Object value : values) {
      if (value instanceof UserDetail) {
        userIds.add(((UserDetail) value).getId());
      } else if (value == null) {
        userIds.add("");
      } else {
        throw new FormException("UserField.setObjectValue", "form.EXP_NOT_AN_USER");
      }
    }
    setUserIds(userIds);
  }

  /**
   * Returns true if the value is a String and this field isn't read only.
   */
  @Override
  public boolean acceptObjectValues(List<Object> values) {
    for (Object value : values) {
      if (value instanceof UserDetail) {
        // do nothing
      } else {
        return false;
      }
    }
    return !isReadOnly();
  }

  /**
   * Returns this field value as a normalized String : a user id
   */
  @Override
  public List<String> getStringValues() {
    return getUserIds();
  }

  /**
   * Set this field value from a normalized String : a user id
   */
  @Override
  public void setStringValues(List<String> values) {
    SilverTrace.info("form", "UserField.setStringValue",
        "root.MSG_GEN_ENTER_METHOD", "values = " + StringUtil.join(values, ","));
    setUserIds(values);
  }

  /**
   * Returns true if this field isn't read only.
   */
  @Override
  public boolean acceptStringValues(List<String> values) {
    return !isReadOnly();
  }

  /**
   * Returns true if this field is not set.
   */
  @Override
  public boolean isNull() {
    return (getUserIds() == null);
  }

  /**
   * Set to null this field.
   *
   * @throw FormException when the field is mandatory.
   * @throw FormException when the field is read only.
   */
  @Override
  public void setNull() throws FormException {
    setUserIds(null);
  }

  /**
   * Tests equality between this field and the specified field.
   */
  @Override
  public boolean equals(Object o) {
    String s = Util.list2String(getUserIds());
    if (s == null) {
      s = "";
    }
    if (o instanceof UserField) {
      String t = Util.list2String(((UserField) o).getUserIds());
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
  @Override
  public int compareTo(Object o) {
    String s = Util.list2String(getValues());
    if (s == null) {
      s = "";
    }
    if (o instanceof UserField) {
      String t = Util.list2String(((UserField) o).getValues());
      if (t == null) {
        t = "";
      }

      if (s.equals(t)) {
        s = Util.list2String(getUserIds());
        if (s == null) {
          s = "";
        }
        t = Util.list2String(((UserField) o).getUserIds());
        if (t == null) {
          t = "";
        }
      }
      return s.compareTo(t);
    } else {
      return -1;
    }
  }

  @Override
  public int hashCode() {
    String s = Util.list2String(getUserIds());
    return ("" + s).hashCode();
  }
  /**
   * The referenced userId.
   */
  private List<String> userIds = null;
}
