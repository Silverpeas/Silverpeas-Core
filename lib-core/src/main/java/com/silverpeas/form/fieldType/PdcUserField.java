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

import com.stratelia.silverpeas.silvertrace.*;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.silverpeas.form.Field;
import com.silverpeas.form.FormException;

/**
 * A PdcUserField stores a the users references
 * @see Field
 * @see FieldDisplayer
 */
public class PdcUserField implements Field {

  private static final long serialVersionUID = -1365851085995310180L;

  /**
   * The text field type name.
   */
  static public final String TYPE = "pdcUser";

  /**
   * Returns the type name.
   */
  public String getTypeName() {
    return TYPE;
  }

  /**
   * The no parameters constructor
   */
  public PdcUserField() {
  }

  /**
   * Returns the users ids referenced by this field. (userCardId,userCardId,userCardId, ...)
   */
  public String getUserCardIds() {
    return userCardIds;
  }

  /**
   * Set the userCardIds referenced by this field.
   */
  public void setUserCardIds(String userCardIds) {
    this.userCardIds = userCardIds;
  }

  /**
   * Returns true if the value is read only.
   */
  public boolean isReadOnly() {
    return false;
  }

  /**
   * Returns the string value of this field : the user names (FirstName LastName,FirstName
   * LastName,FirstName LastName, ...)
   */
  public String getValue() {
    String theUserCardIds = getUserCardIds(); // userCardId-userId,userCardId-userId
    // ....

    if (theUserCardIds == null)
      return null;

    if (theUserCardIds.equals(""))
      return "";

    try {

      theUserCardIds += ",";
      String userCardIdUserId = null;
      int index = -1;
      String userCardId = null;
      String userId = null;
      UserDetail user = null;
      StringBuffer names = new StringBuffer("");
      int begin = 0;
      int end = 0;

      end = theUserCardIds.indexOf(',', begin);
      while (end != -1) {
        userCardIdUserId = theUserCardIds.substring(begin, end); // userCardId-userId
        index = userCardIdUserId.indexOf("-");
        userCardId = userCardIdUserId.substring(0, index);
        userId = userCardIdUserId.substring(index + 1);

        user = organizationController.getUserDetail(userId);
        if (user == null)
          names.append("userCardId(" + userCardId + ")");
        else
          names.append(user.getFirstName() + " " + user.getLastName());
        names.append(",");
        begin = end + 1;
        end = theUserCardIds.indexOf(',', begin);
      }

      if (!names.toString().equals("")) {
        names = names.deleteCharAt(names.length() - 1);
      }

      return names.toString();
    } catch (Exception e) {
      SilverTrace.error("form", "PdcUserField.getValue",
          "root.MSG_GEN_PARAM_VALUE", "", e);
      return null;
    }
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
   * Returns the userCardIds referenced by this field.
   */
  public Object getObjectValue() {
    if (getUserCardIds() == null)
      return null;

    return getUserCardIds();
  }

  /**
   * Set userCardIds referenced by this field.
   */
  public void setObjectValue(Object value) throws FormException {
    if (value instanceof String) {
      setUserCardIds((String) value);
    } else {
      throw new FormException("PdcUserField.setObjectValue",
          "form.EXP_NOT_AN_USER");
    }
  }

  /**
   * Returns true if the value is a String and this field isn't read only.
   */
  public boolean acceptObjectValue(Object value) {
    if (value instanceof String)
      return !isReadOnly();
    else
      return false;
  }

  /**
   * Returns this field value as a normalized String : a user id
   */
  public String getStringValue() {
    return getUserCardIds();
  }

  /**
   * Set this field value from a normalized String : a user id
   */
  public void setStringValue(String value) {
    setUserCardIds(value);
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
    return (getUserCardIds() == null);
  }

  /**
   * Set to null this field.
   * @throw FormException when the field is mandatory.
   * @throw FormException when the field is read only.
   */
  public void setNull() throws FormException {
    setUserCardIds(null);
  }

  /**
   * Tests equality beetwen this field and the specified field.
   */
  public boolean equals(Object o) {
    String s = getUserCardIds();

    if (o instanceof PdcUserField) {
      String t = ((PdcUserField) o).getUserCardIds();
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

    if (o instanceof PdcUserField) {
      String t = ((PdcUserField) o).getValue();
      if (t == null)
        t = "";

      if (s.equals(t)) {
        s = getUserCardIds();
        if (s == null)
          s = "";
        t = ((PdcUserField) o).getUserCardIds();
        if (t == null)
          t = "";
      }

      return s.compareTo(t);
    } else
      return -1;
  }

  public int hashCode() {
    String s = getUserCardIds();
    return ("" + s).hashCode();
  }

  /**
   * The referenced userCardIds.
   */
  private String userCardIds = null;

  /**
   * The main access to the users set.
   */
  private static OrganizationController organizationController = new OrganizationController();

}
