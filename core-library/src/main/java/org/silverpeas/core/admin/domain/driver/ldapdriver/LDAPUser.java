/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.admin.domain.driver.ldapdriver;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import org.silverpeas.core.admin.domain.synchro.SynchroDomainReport;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.domain.DomainDriver;
import org.silverpeas.core.admin.domain.model.DomainProperty;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.exception.SilverpeasException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class reads user infos from the LDAP DB and translate it into the UserDetail format
 * @author tleroi
 */
public class LDAPUser {

  LDAPSettings driverSettings = null;
  LDAPSynchroCache synchroCache = null;
  private StringBuffer synchroReport = null;
  boolean synchroInProcess = false;

  protected DomainDriver driverParent = null;

  /**
   * Initialize the settings from the read ones
   * @param driverSettings the settings retreived from the property file
   */
  public void init(LDAPSettings driverSettings, DomainDriver driverParent,
      LDAPSynchroCache synchroCache) {
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
   * @param lds the LDAP connection
   * @return all founded users
   * @throws AdminException if an error occur during LDAP operations
   */
  public UserDetail[] getAllUsers(String lds, String extraFilter) throws AdminException {
    int i;
    String theFilter;

    if ((extraFilter != null) && (extraFilter.length() > 0)) {
      theFilter = "(&" + extraFilter + driverSettings.getUsersFullFilter() + ")";
    } else {
      theFilter = driverSettings.getUsersFullFilter();
    }
    SynchroDomainReport
        .info("LDAPUser.getAllUsers()", "Recherche des utilisateurs du domaine LDAP distant...");
    List<UserDetail> usersVector = new ArrayList<>();
    LDAPEntry[] theEntries = LDAPUtility
        .search1000Plus(lds, driverSettings.getLDAPUserBaseDN(), driverSettings.getScope(),
            theFilter, driverSettings.getUsersLoginField(), driverSettings.getUserAttributes());
    for (i = 0; i < theEntries.length; i++) {
      usersVector.add(translateUser(lds, theEntries[i]));

      usersVector.get(i).traceUser();// Trace niveau Info ds
      // module 'admin' des infos user courant : ID, domaine, login, e-mail,...
      SynchroDomainReport.debug("LDAPUser.getAllUsers()",
          "Utilisateur trouvé no : " + i + ", login : " + usersVector.get(i).getLogin() + ", " +
              usersVector.get(i).getFirstName() + ", " + usersVector.get(i).getLastName() + ", " +
              usersVector.get(i).geteMail());
    }
    SynchroDomainReport.info("LDAPUser.getAllUsers()",
        "Récupération de " + theEntries.length + " utilisateurs du domaine LDAP distant");
    return usersVector.toArray(new UserDetail[usersVector.size()]);
  }

  /**
   * Return a UserDetail object filled with the infos of the user having ID = id NOTE : the
   * DomainID
   * and the ID are not set.
   * @param lds the LDAP connection
   * @param id the user id
   * @return the user object
   * @throws AdminException if an error occur during LDAP operations or if the user is not found
   */
  public UserFull getUserFull(String lds, String id, int domainId) throws AdminException {
    List<String> lAttrs = new ArrayList<>();
    String[] userAttributes = driverSettings.getUserAttributes();
    if (userAttributes != null && userAttributes.length > 0) {
      lAttrs.addAll(Arrays.asList(userAttributes));
      if (driverParent.getMapParameters() != null) {
        lAttrs.addAll(Arrays.asList(driverParent.getMapParameters()));
      }
    }
    LDAPEntry theEntry = LDAPUtility
        .getFirstEntryFromSearch(lds, driverSettings.getLDAPUserBaseDN(), driverSettings.getScope(),
            driverSettings.getUsersIdFilter(id), lAttrs.toArray(new String[lAttrs.size()]));
    return translateUserFull(lds, theEntry, domainId);
  }

  /**
   * Return a UserDetail object filled with the infos of the user having ID = id NOTE : the
   * DomainID
   * and the ID are not set.
   * @param lds the LDAP connection
   * @param id the user id
   * @return the user object
   * @throws AdminException if an error occur during LDAP operations or if the user is not found
   */
  public UserDetail getUser(String lds, String id) throws AdminException {
    LDAPEntry theEntry;
    theEntry = LDAPUtility
        .getFirstEntryFromSearch(lds, driverSettings.getLDAPUserBaseDN(), driverSettings.getScope(),
            driverSettings.getUsersIdFilter(id), driverSettings.getUserAttributes());
    return translateUser(lds, theEntry);
  }

  public UserDetail getUserByLogin(String lds, String loginUser) throws AdminException {
    LDAPEntry theEntry;
    theEntry = LDAPUtility
        .getFirstEntryFromSearch(lds, driverSettings.getLDAPUserBaseDN(), driverSettings.getScope(),
            driverSettings.getUsersLoginFilter(loginUser), driverSettings.getUserAttributes());
    return translateUser(lds, theEntry);
  }

  /**
   * Centralizing the translation from LDAP user entry to Silverpeas user data.
   * @param ldapUser
   * @param silverpeasDistantUser
   */
  private void translateCommonUserData(LDAPEntry ldapUser, UserDetail silverpeasDistantUser) {
    silverpeasDistantUser.setSpecificId(
        LDAPUtility.getFirstAttributeValue(ldapUser, driverSettings.getUsersIdField()));
    silverpeasDistantUser.setLogin(
        LDAPUtility.getFirstAttributeValue(ldapUser, driverSettings.getUsersLoginField()));
    silverpeasDistantUser.setFirstName(
        LDAPUtility.getFirstAttributeValue(ldapUser, driverSettings.getUsersFirstNameField()));
    silverpeasDistantUser.setLastName(
        LDAPUtility.getFirstAttributeValue(ldapUser, driverSettings.getUsersLastNameField()));
    silverpeasDistantUser.seteMail(
        LDAPUtility.getFirstAttributeValue(ldapUser, driverSettings.getUsersEmailField()));
    silverpeasDistantUser.setAccessLevel(null); // Put the default access level (user)...

    // Trying to get the information of that the user account is disabled or not
    String userAccountControlAttribute = driverSettings.getUsersAccountControl();
    String disabledUserAccountFlag = driverSettings.getUsersDisabledAccountFlag();
    if (StringUtil.isDefined(userAccountControlAttribute) &&
        StringUtil.isDefined(disabledUserAccountFlag)) {
      String usersAccountControl =
          LDAPUtility.getFirstAttributeValue(ldapUser, userAccountControlAttribute);
      if (!StringUtil.isDefined(usersAccountControl)) {
        usersAccountControl = StringUtil.EMPTY;
      }
      if (StringUtil.isLong(usersAccountControl)) {
        if (StringUtil.isLong(disabledUserAccountFlag)) {
          long currentAccountControlFlags = Long.valueOf(usersAccountControl);
          long disabledUserAccountFlagAsLong = Long.valueOf(disabledUserAccountFlag);
          if ((currentAccountControlFlags & disabledUserAccountFlagAsLong) ==
              disabledUserAccountFlagAsLong) {
            // The account is disabled
            silverpeasDistantUser.setState(UserState.DEACTIVATED);
          } else {
            // The account is enabled
            silverpeasDistantUser.setState(UserState.VALID);
          }
        }
      } else {
        if (usersAccountControl
            .matches("(?i)(.*[ ;,|]+|)" + disabledUserAccountFlag + "([ ;,|]+.*|)")) {
          // The account is disabled
          silverpeasDistantUser.setState(UserState.DEACTIVATED);
        } else {
          // The account is enabled
          silverpeasDistantUser.setState(UserState.VALID);
        }
      }
    }
  }

  /**
   * Translate a LDAP user entry into a UserDetail structure. NOTE : the DomainID and the ID are not
   * set.
   * @param userEntry the LDAP user object
   * @return the user object
   * @throws AdminException if an error occur during LDAP operations or if there is no userEntry
   * object
   */
  private UserFull translateUserFull(String lds, LDAPEntry userEntry, int domainId)
      throws AdminException {
    UserFull userInfos = new UserFull(driverParent);
    String subUserDN;
    LDAPEntry subUserEntry;
    String[] keys = driverParent.getPropertiesNames();
    int i;
    DomainProperty curProp;

    translateCommonUserData(userEntry, userInfos);

    for (i = 0; i < keys.length; i++) {
      curProp = driverParent.getProperty(keys[i]);
      if (DomainProperty.PROPERTY_TYPE_USERID.equals(curProp.getType())) {
        subUserDN = LDAPUtility.getFirstAttributeValue(userEntry, curProp.getMapParameter());
        if (subUserDN != null && subUserDN.length() > 0) {
          try {
            subUserEntry = LDAPUtility
                .getFirstEntryFromSearch(lds, subUserDN, LDAPConnection.SCOPE_BASE,
                    driverSettings.getUsersFullFilter(), driverSettings.getUserAttributes());
          } catch (AdminException e) {
            SilverTrace.warn("admin", "LDAPUser.translateUser", "admin.EX_ERR_BOSS_NOT_FOUND",
                "subUserDN=" + subUserDN, e);
            if (synchroInProcess) {
              synchroReport.append("PB getting BOSS infos : ").append(subUserDN).append("\n");
            }
            subUserEntry = null;
          }
          if (subUserEntry != null) {
            String login = LDAPUtility
                .getFirstAttributeValue(subUserEntry, driverSettings.getUsersLoginField());
            String anotherUserId = Administration.get()
                .getUserIdByLoginAndDomain(login, String.valueOf(domainId));
            userInfos.setValue(curProp.getName(), anotherUserId);
          }
        }
      } else if (StringUtil.isDefined(curProp.getRedirectOU()) &&
          StringUtil.isDefined(curProp.getRedirectAttribute())) {
        String cn = LDAPUtility.getFirstAttributeValue(userEntry, curProp.getMapParameter());
        if (StringUtil.isDefined(cn)) {
          String baseDN = curProp.getRedirectOU();
          String filter = "(cn=" + cn + ")";
          subUserEntry = LDAPUtility
              .getFirstEntryFromSearch(lds, baseDN, LDAPConnection.SCOPE_SUB, filter,
                  driverSettings.getUserAttributes());

          if (subUserEntry != null) {
            userInfos.setValue(curProp.getName(),
                LDAPUtility.getFirstAttributeValue(subUserEntry, curProp.getRedirectAttribute()));
          }
        }
      } else {
        userInfos.setValue(curProp.getName(),
            LDAPUtility.getFirstAttributeValue(userEntry, curProp.getMapParameter()));
      }
    }
    return userInfos;
  }

  /**
   * Translate a LDAP user entry into a UserDetail structure. NOTE : the DomainID and the ID are
   * not
   * set.
   * @param userEntry the LDAP user object
   * @return the user object
   * @throws AdminException if an error occur during LDAP operations or if there is no userEntry
   * object
   */
  public UserDetail translateUser(String lds, LDAPEntry userEntry) throws AdminException {
    UserDetail userInfos = new UserDetail();

    if (userEntry == null) {
      throw new AdminException("LDAPUser.translateUser", SilverpeasException.ERROR,
          "admin.EX_ERR_LDAP_USER_ENTRY_ISNULL");
    }

    // Set the AdminUser informations
    translateCommonUserData(userEntry, userInfos);

    synchroCache.addUser(userEntry);

    return userInfos;
  }

  public AbstractLDAPTimeStamp getMaxTimeStamp(String lds, String minTimeStamp)
      throws AdminException {
    AbstractLDAPTimeStamp theTimeStamp = driverSettings.newLDAPTimeStamp(minTimeStamp);
    theTimeStamp.initFromServer(lds, driverSettings.getLDAPUserBaseDN(),
        driverSettings.getUsersFullFilter(), driverSettings.getUsersLoginField());
    return theTimeStamp;
  }

  public String[] getUserAttributes() {
    List<String> lAttrs = new ArrayList<>();
    String[] userAttributes = driverSettings.getUserAttributes();
    if (userAttributes != null) {
      lAttrs.addAll(Arrays.asList(userAttributes));
      if (driverParent.getMapParameters() != null) {
        lAttrs.addAll(Arrays.asList(driverParent.getMapParameters()));
      }
    }
    return lAttrs.toArray(new String[lAttrs.size()]);
  }

}
