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
import org.silverpeas.core.contribution.content.form.FieldDisplayer;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.util.StringUtil;

/**
 * A UserField stores a user reference.
 *
 * @see Field
 * @see FieldDisplayer
 */
public class UserField extends AbstractField {

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
  public String getUserId() {
    return userId;
  }

  /**
   * Set the user id referenced by this field.
   */
  public void setUserId(String userId) {

    this.userId = userId;
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
  public String getValue() {
    String theUserId = getUserId();

    if (!StringUtil.isDefined(theUserId)) {
      return theUserId;
    }

    UserDetail user = OrganizationControllerProvider.getOrganisationController().getUserDetail(
        getUserId());
    if (user == null) {
      return "user(" + getUserId() + ")";
    }

    return user.getDisplayedName();
  }

  /**
   * Returns the local value of this field. There is no local format for a user field, so the
   * language parameter is unused.
   */
  @Override
  public String getValue(String language) {
    return getValue();
  }

  /**
   * Does nothing since a user reference can't be computed from a user name.
   */
  @Override
  public void setValue(String value) throws FormException {
  }

  /**
   * Does nothing since a user reference can't be computed from a user name.
   */
  @Override
  public void setValue(String value, String language) throws FormException {
  }

  /**
   * Always returns false since a user reference can't be computed from a user name.
   */
  @Override
  public boolean acceptValue(String value) {
    return false;
  }

  /**
   * Always returns false since a user reference can't be computed from a user name.
   */
  @Override
  public boolean acceptValue(String value, String language) {
    return false;
  }

  /**
   * Returns the User referenced by this field.
   */
  @Override
  public Object getObjectValue() {
    if (getUserId() == null) {
      return null;
    }
    return OrganizationControllerProvider.getOrganisationController().getUserDetail(getUserId());
  }

  /**
   * Set user referenced by this field.
   */
  @Override
  public void setObjectValue(Object value) throws FormException {
    if (value instanceof UserDetail) {
      setUserId(((UserDetail) value).getId());
    } else if (value == null) {
      setUserId("");
    } else {
      throw new FormException("UserField.setObjectValue",
          "form.EXP_NOT_AN_USER");
    }
  }

  /**
   * Returns true if the value is a String and this field isn't read only.
   */
  @Override
  public boolean acceptObjectValue(Object value) {
    return value instanceof UserDetail && !isReadOnly();
  }

  /**
   * Returns this field value as a normalized String : a user id
   */
  @Override
  public String getStringValue() {
    return getUserId();
  }

  /**
   * Set this field value from a normalized String : a user id
   */
  @Override
  public void setStringValue(String value) {

    setUserId(value);
  }

  /**
   * Returns true if this field isn't read only.
   */
  @Override
  public boolean acceptStringValue(String value) {
    return !isReadOnly();
  }

  /**
   * Returns true if this field is not set.
   */
  @Override
  public boolean isNull() {
    return (getUserId() == null);
  }

  /**
   * Set to null this field.
   *
   * @throws FormException when the field is mandatory or when the field is read only.
   */
  @Override
  public void setNull() throws FormException {
    setUserId(null);
  }

  /**
   * Tests equality between this field and the specified field.
   */
  @Override
  public boolean equals(Object o) {
    String s = getUserId();
    if (s == null) {
      s = "";
    }
    if (o instanceof UserField) {
      String t = ((UserField) o).getUserId();
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
    String s = getValue();
    if (s == null) {
      s = "";
    }
    if (o instanceof UserField) {
      String t = ((UserField) o).getValue();
      if (t == null) {
        t = "";
      }

      if (s.equals(t)) {
        s = getUserId();
        if (s == null) {
          s = "";
        }
        t = ((UserField) o).getUserId();
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
    String s = getUserId();
    return ("" + s).hashCode();
  }
  /**
   * The referenced userId.
   */
  private String userId;
}
