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
import java.util.Collection;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPSearchConstraints;
import com.novell.ldap.LDAPSearchResults;
import com.silverpeas.form.Field;
import com.silverpeas.form.FormException;
import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.beans.admin.OrganizationController;

/**
 * A LdapField stores a value of ldap field.
 * 
 * @see Field
 * @see FieldDisplayer
 */
public class LdapField extends TextField {
  /**
   * The ldap field type name.
   */
  static public final String TYPE = "ldap";

  /**
   * The ldap field dynamic variable login.
   */
  static public final String VARIABLE_LOGIN = "$$login";

  /**
   * The ldap field dynamic variable login for regex.
   */
  static private final String VARIABLE_REGEX_LOGIN = "\\$\\$login";

  private String value = "";

  public LdapField() {
  }

  /**
   * Returns the type name.
   */
  public String getTypeName() {
    return TYPE;
  }

  /**
   * Returns the string value of this field.
   */
  public String getStringValue() {
    return value;
  }

  /**
   * Set the string value of this field.
   */
  public void setStringValue(String value) {
    this.value = value;
  }

  /**
   * Returns true if the value is read only.
   */
  public boolean isReadOnly() {
    return false;
  }

  public LDAPConnection connectLdap(String host, String port)
      throws FormException {
    LDAPConnection ldapConnection = null;
    if (!StringUtil.isDefined(host) || !StringUtil.isDefined(port)) {
      throw new FormException("LdapField.connectLdap",
          "form.EX_CANT_CONNECT_LDAP");
    }

    try {
      ldapConnection = new LDAPConnection();
      int portInt = new Integer(port).intValue();
      ldapConnection.connect(host, portInt);

      return ldapConnection;
    } catch (Exception e) {
      throw new FormException("LdapField.connectLdap",
          "form.EX_CANT_CONNECT_LDAP", e);
    }
  }

  public void disconnectLdap(LDAPConnection connection) throws FormException {
    try {
      if (connection != null && connection.isConnected()) {
        connection.disconnect();
      }
    } catch (Exception e) {
      throw new FormException("LdapField.disconnectLdap",
          "form.EX_CANT_DISCONNECT_LDAP", e);
    }
    connection = null;
  }

  public void bindLdap(LDAPConnection ldapConnection, String version,
      String distinguishedName, byte[] password) throws FormException {
    if (!StringUtil.isDefined(version)
        || !StringUtil.isDefined(distinguishedName)) {
      throw new FormException("LdapField.bindLdap", "form.EX_CANT_BIND_LDAP");
    }

    try {
      int versionInt = new Integer(version).intValue();
      ldapConnection.bind(versionInt, distinguishedName, password);

    } catch (Exception e) {
      throw new FormException("LdapField.bindLdap", "form.EX_CANT_BIND_LDAP", e);
    }
  }

  public void setConstraintLdap(LDAPConnection ldapConnection,
      String maxResultDisplayed) throws FormException {

    try {
      int maxResultDisplayedInt = new Integer(maxResultDisplayed).intValue();
      LDAPSearchConstraints ldapConstraint = new LDAPSearchConstraints();
      ldapConstraint.setMaxResults(maxResultDisplayedInt);
      ldapConnection.setConstraints(ldapConstraint);
    } catch (Exception e) {
      throw new FormException("LdapField.setConstraintLdap",
          "form.EX_CANT_SET_CONSTRAINT_LDAP", e);
    }
  }

  public Collection searchLdap(LDAPConnection ldapConnection, String baseDn,
      String scope, String filter, String attribute, boolean typesOnly,
      String currentUserId) throws FormException {
    Collection listRes = new ArrayList();

    LDAPSearchResults searchResult = null;

    // parsing filter -> dynamic variable
    if (filter.indexOf(VARIABLE_LOGIN) != -1) {
      try {
        String valueLogin = organizationController.getUserDetail(currentUserId)
            .getLogin();
        filter = filter.replaceAll(VARIABLE_REGEX_LOGIN, valueLogin);
      } catch (Exception e) {
        throw new FormException("LdapField.searchLdap",
            "form.EX_CANT_SEARCH_LDAP", "Can't get login of the currentUser", e);
      }
    }

    String[] tabSearchAttribute = null;
    try {
      int scopeInt = new Integer(scope).intValue();
      if (StringUtil.isDefined(attribute)) {
        tabSearchAttribute = new String[1];
        tabSearchAttribute[0] = attribute;
      }
      searchResult = ldapConnection.search(baseDn, scopeInt, filter,
          tabSearchAttribute, typesOnly);
    } catch (Exception e) {
      throw new FormException("LdapField.searchLdap",
          "form.EX_CANT_SEARCH_LDAP", e);
    }

    if (searchResult != null) {
      LDAPEntry entry;
      int nbReaded = 0;
      LDAPAttribute ldapAttribute;
      String value;
      try {
        while (searchResult.hasMore()
            && ldapConnection.getSearchConstraints().getMaxResults() > nbReaded) {
          entry = (LDAPEntry) searchResult.next();

          if (tabSearchAttribute != null) {
            ldapAttribute = entry.getAttribute(tabSearchAttribute[0]);
            value = ldapAttribute.getStringValue();
          } else {
            value = entry.getDN();
          }

          nbReaded++;
          listRes.add(value);

        }
      } catch (LDAPException e) {
        throw new FormException("LdapField.searchLdap",
            "form.EX_CANT_SEARCH_LDAP", e);
      }
    }
    return listRes;
  }

  /**
   * The main access to the users set.
   */
  private static OrganizationController organizationController = new OrganizationController();
}
