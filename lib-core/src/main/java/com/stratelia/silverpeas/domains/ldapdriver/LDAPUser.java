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
package com.stratelia.silverpeas.domains.ldapdriver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AbstractDomainDriver;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.DomainProperty;
import com.stratelia.webactiv.beans.admin.SynchroReport;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.UserFull;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * This class reads user infos from the LDAP DB and translate it into the
 * UserDetail format
 *
 * @author tleroi
 */

public class LDAPUser extends Object {
  LDAPSettings driverSettings = null;
  LDAPSynchroCache synchroCache = null;
  StringBuffer synchroReport = null;
  boolean synchroInProcess = false;

  protected AbstractDomainDriver driverParent = null;

  /**
   * Initialize the settings from the read ones
   *
   * @param driverSettings
   *          the settings retreived from the property file
   */
  public void init(LDAPSettings driverSettings,
      AbstractDomainDriver driverParent, LDAPSynchroCache synchroCache) {
    this.driverSettings = driverSettings;
    this.driverParent = driverParent;
    this.synchroCache = synchroCache;
  }

  /**
   * Called when Admin starts the synchronization
   */
  public void beginSynchronization() throws Exception {
    synchroReport = new StringBuffer();
    synchroInProcess = true;
  }

  /**
   * Called when Admin ends the synchronization
   */
  public String endSynchronization() throws Exception {
    synchroInProcess = false;
    return synchroReport.toString();
  }

  /**
   * Return all users found in the baseDN tree
   *
   * @param ld
   *          the LDAP connection
   * @return all founded users
   * @throws AdminException
   *           if an error occur during LDAP operations
   */
  public UserDetail[] getAllUsers(String lds, String extraFilter)
      throws AdminException {
    LDAPEntry[] theEntries = null;
    UserDetail[] usersReturned = null;
    Vector usersVector;
    int i;
    String theFilter;

    if ((extraFilter != null) && (extraFilter.length() > 0)) {
      theFilter = "(&" + extraFilter + driverSettings.getUsersFullFilter()
          + ")";
    } else {
      theFilter = driverSettings.getUsersFullFilter();
    }
    SilverTrace.info("admin", "LDAPUser.getAllUsers()",
        "root.MSG_GEN_PARAM_VALUE", "User Search : "
            + driverSettings.getLDAPUserBaseDN() + " scope : "
            + Integer.toString(driverSettings.getScope()) + " filter : "
            + theFilter);
    SynchroReport.info("LDAPUser.getAllUsers()",
        "Recherche des utilisateurs du domaine LDAP distant...", null);
    usersVector = new Vector();
    // theEntries = LDAPUtility.search1000Plus(lds,
    // driverSettings.getLDAPUserBaseDN(),driverSettings.getScope(),theFilter,driverSettings.getUsersLoginField());
    theEntries = LDAPUtility
        .search1000Plus(lds, driverSettings.getLDAPUserBaseDN(), driverSettings
            .getScope(), theFilter, driverSettings.getUsersLoginField(),
            driverSettings.getUserAttributes());
    for (i = 0; i < theEntries.length; i++) {
      SilverTrace.info("admin", "LDAPUser.getAllUsers()",
          "root.MSG_GEN_PARAM_VALUE", "User Found !!!");
      usersVector.add(translateUser(lds, theEntries[i]));
      SilverTrace.info("admin", "LDAPUser.getAllUsers()",
          "root.MSG_GEN_PARAM_VALUE", "User " + Integer.toString(i) + " : "
              + ((UserDetail) usersVector.get(i)).getLogin());
      ((UserDetail) usersVector.get(i)).traceUser();// Trace niveau Info ds
      // module 'admin' des infos
      // user courant : ID,
      // domaine, login,
      // e-mail,...
      SynchroReport.debug("LDAPUser.getAllUsers()", "Utilisateur trouvé no : "
          + Integer.toString(i) + ", login : "
          + ((UserDetail) usersVector.get(i)).getLogin() + ", "
          + ((UserDetail) usersVector.get(i)).getFirstName() + ", "
          + ((UserDetail) usersVector.get(i)).getLastName() + ", "
          + ((UserDetail) usersVector.get(i)).geteMail(), null);
    }
    SynchroReport.info("LDAPUser.getAllUsers()", "Récupération de "
        + theEntries.length + " utilisateurs du domaine LDAP distant", null);
    usersReturned = (UserDetail[]) usersVector.toArray(new UserDetail[0]);
    return usersReturned;
  }

  /**
   * Return a UserDetail object filled with the infos of the user having ID = id
   * NOTE : the DomainID and the ID are not set.
   *
   * @param ld
   *          the LDAP connection
   * @param id
   *          the user id
   * @return the user object
   * @throws AdminException
   *           if an error occur during LDAP operations or if the user is not
   *           found
   */
  public UserFull getUserFull(String lds, String id) throws AdminException
    {
        LDAPEntry   theEntry = null;

        List<String> lAttrs = new ArrayList<String>();
        String[] userAttributes = driverSettings.getUserAttributes();
        if (userAttributes != null)
        {
        	lAttrs.addAll(Arrays.asList(userAttributes));
        	if (driverParent.getMapParameters() != null)
        		lAttrs.addAll(Arrays.asList(driverParent.getMapParameters()));
        }

        SilverTrace.info("admin","LDAPUser.getUser()","root.MSG_GEN_PARAM_VALUE", "User Search : " + driverSettings.getLDAPUserBaseDN() + " scope : " + Integer.toString(driverSettings.getScope()) + " filter : " + driverSettings.getUsersIdFilter(id));
        theEntry = LDAPUtility.getFirstEntryFromSearch(lds,driverSettings.getLDAPUserBaseDN(),driverSettings.getScope(),driverSettings.getUsersIdFilter(id), lAttrs.toArray(new String[0]));
        return translateUserFull(lds,theEntry);
    }

  /**
   * Return a UserDetail object filled with the infos of the user having ID = id
   * NOTE : the DomainID and the ID are not set.
   *
   * @param ld
   *          the LDAP connection
   * @param id
   *          the user id
   * @return the user object
   * @throws AdminException
   *           if an error occur during LDAP operations or if the user is not
   *           found
   */
  public UserDetail getUser(String lds, String id) throws AdminException {
    LDAPEntry theEntry = null;

    SilverTrace.info("admin", "LDAPUser.getUser()", "root.MSG_GEN_PARAM_VALUE",
        "User Search : " + driverSettings.getLDAPUserBaseDN() + " scope : "
            + Integer.toString(driverSettings.getScope()) + " filter : "
            + driverSettings.getUsersIdFilter(id));
    theEntry = LDAPUtility.getFirstEntryFromSearch(lds, driverSettings
        .getLDAPUserBaseDN(), driverSettings.getScope(), driverSettings
        .getUsersIdFilter(id), driverSettings.getUserAttributes());
    return translateUser(lds, theEntry);
  }

  public UserDetail getUserByLogin(String lds, String loginUser)
      throws AdminException {
    LDAPEntry theEntry = null;

    SilverTrace.info("admin", "LDAPUser.getUser()", "root.MSG_GEN_PARAM_VALUE",
        "User Search : " + driverSettings.getLDAPUserBaseDN() + " scope : "
            + Integer.toString(driverSettings.getScope()) + " filter : "
            + driverSettings.getUsersLoginFilter(loginUser));
    theEntry = LDAPUtility.getFirstEntryFromSearch(lds, driverSettings
        .getLDAPUserBaseDN(), driverSettings.getScope(), driverSettings
        .getUsersLoginFilter(loginUser), driverSettings.getUserAttributes());
    return translateUser(lds, theEntry);
  }

  /**
   * Translate a LDAP user entry into a UserDetail structure. NOTE : the
   * DomainID and the ID are not set.
   *
   * @param userEntry
   *          the LDAP user object
   * @return the user object
   * @throws AdminException
   *           if an error occur during LDAP operations or if there is no
   *           userEntry object
   */
  private UserFull translateUserFull(String lds, LDAPEntry userEntry)
      throws AdminException {
    UserFull userInfos = new UserFull(driverParent);
    String subUserDN = null;
    LDAPEntry subUserEntry = null;
    String[] keys = driverParent.getPropertiesNames();
    int i;
    DomainProperty curProp;

    userInfos.setSpecificId(LDAPUtility.getFirstAttributeValue(userEntry,
        driverSettings.getUsersIdField()));
    userInfos.setLogin(LDAPUtility.getFirstAttributeValue(userEntry,
        driverSettings.getUsersLoginField()));
    userInfos.setFirstName(LDAPUtility.getFirstAttributeValue(userEntry,
        driverSettings.getUsersFirstNameField()));
    userInfos.setLastName(LDAPUtility.getFirstAttributeValue(userEntry,
        driverSettings.getUsersLastNameField()));
    userInfos.seteMail(LDAPUtility.getFirstAttributeValue(userEntry,
        driverSettings.getUsersEmailField()));
    userInfos.setAccessLevel(null); // Put the default access level (user)...

    for (i = 0; i < keys.length; i++) {
      curProp = driverParent.getProperty(keys[i]);
      if (curProp.getType() == DomainProperty.PROPERTY_TYPE_USERID) {
        subUserDN = LDAPUtility.getFirstAttributeValue(userEntry, curProp
            .getMapParameter());
        if (subUserDN != null && subUserDN.length() > 0) {
          try {
            subUserEntry = LDAPUtility.getFirstEntryFromSearch(lds, subUserDN,
                LDAPConnection.SCOPE_BASE, driverSettings.getUsersFullFilter(),
                driverSettings.getUserAttributes());
          } catch (AdminException e) {
            SilverTrace.warn("admin", "LDAPUser.translateUser",
                "admin.EX_ERR_BOSS_NOT_FOUND", "subUserDN=" + subUserDN, e);
            if (synchroInProcess) {
              synchroReport.append("PB getting BOSS infos : " + subUserDN
                  + "\n");
            }
            subUserEntry = null;
          }
          if (subUserEntry != null) {
            userInfos.setValue(curProp.getName(), LDAPUtility
                .getFirstAttributeValue(subUserEntry, driverSettings
                    .getUsersFirstNameField())
                + " "
                + LDAPUtility.getFirstAttributeValue(subUserEntry,
                    driverSettings.getUsersLastNameField()));
          }
        }
      } else if (StringUtil.isDefined(curProp.getRedirectOU())
          && StringUtil.isDefined(curProp.getRedirectAttribute())) {
        String cn = LDAPUtility.getFirstAttributeValue(userEntry, curProp
            .getMapParameter());
        if (StringUtil.isDefined(cn)) {
          // String dn = "cn="+cn+","+curProp.getRedirectOU();
          String baseDN = curProp.getRedirectOU();
          String filter = "(cn=" + cn + ")";
          subUserEntry = LDAPUtility.getFirstEntryFromSearch(lds, baseDN,
              LDAPConnection.SCOPE_SUB, filter, driverSettings
                  .getUserAttributes());

          if (subUserEntry != null) {
            userInfos.setValue(curProp.getName(), LDAPUtility
                .getFirstAttributeValue(subUserEntry, curProp
                    .getRedirectAttribute()));
          }
        }
      } else {
        userInfos.setValue(curProp.getName(), LDAPUtility
            .getFirstAttributeValue(userEntry, curProp.getMapParameter()));
      }
    }
    return userInfos;
  }

  /**
   * Translate a LDAP user entry into a UserDetail structure. NOTE : the
   * DomainID and the ID are not set.
   *
   * @param userEntry
   *          the LDAP user object
   * @return the user object
   * @throws AdminException
   *           if an error occur during LDAP operations or if there is no
   *           userEntry object
   */
  public UserDetail translateUser(String lds, LDAPEntry userEntry)
      throws AdminException {
    UserDetail userInfos = new UserDetail();

    if (userEntry == null) {
      throw new AdminException("LDAPUser.translateUser",
          SilverpeasException.ERROR, "admin.EX_ERR_LDAP_USER_ENTRY_ISNULL");
    }

    // Set the AdminUser informations
    userInfos.setSpecificId(LDAPUtility.getFirstAttributeValue(userEntry,
        driverSettings.getUsersIdField()));
    userInfos.setLogin(LDAPUtility.getFirstAttributeValue(userEntry,
        driverSettings.getUsersLoginField()));
    userInfos.setFirstName(LDAPUtility.getFirstAttributeValue(userEntry,
        driverSettings.getUsersFirstNameField()));
    userInfos.setLastName(LDAPUtility.getFirstAttributeValue(userEntry,
        driverSettings.getUsersLastNameField()));
    userInfos.seteMail(LDAPUtility.getFirstAttributeValue(userEntry,
        driverSettings.getUsersEmailField()));
    userInfos.setAccessLevel(null); // Put the default access level (user)...

    synchroCache.addUser(userEntry);

    return userInfos;
  }

  public AbstractLDAPTimeStamp getMaxTimeStamp(String lds, String minTimeStamp)
      throws AdminException {
    AbstractLDAPTimeStamp theTimeStamp = driverSettings
        .newLDAPTimeStamp(minTimeStamp);
    theTimeStamp.initFromServer(lds, driverSettings.getLDAPUserBaseDN(),
        driverSettings.getUsersFullFilter(), driverSettings
            .getUsersLoginField());
    return theTimeStamp;
  }

}
