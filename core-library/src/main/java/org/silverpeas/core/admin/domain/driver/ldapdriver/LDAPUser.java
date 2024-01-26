/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.domain.driver.ldapdriver;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import org.silverpeas.core.admin.domain.DomainDriver;
import org.silverpeas.core.admin.domain.model.DomainProperty;
import org.silverpeas.core.admin.domain.synchro.SynchroDomainReport;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.user.UserManager;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.kernel.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.*;
import static org.silverpeas.core.SilverpeasExceptionMessages.undefined;
import static org.silverpeas.core.admin.domain.driver.ldapdriver.LDAPUtility.getFirstAttributeValue;

/**
 * This class reads user infos from the LDAP DB and translate it into the UserDetail format
 * @author tleroi
 */
public class LDAPUser {

  private static final String LDAPUSER_GET_ALL_USERS = "LDAPUser.getAllUsers()";
  private LDAPSettings driverSettings = null;
  private LDAPSynchroCache synchroCache = null;
  private StringBuilder synchroReport = null;
  private boolean synchroInProcess = false;

  private DomainDriver driverParent = null;

  /**
   * Initialize the settings from the read ones
   * @param driverSettings the settings retrieved from the property file
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
  void beginSynchronization() {
    synchroReport = new StringBuilder();
    synchroInProcess = true;
  }

  /**
   * Called when Admin ends the synchronization
   */
  String endSynchronization() {
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

    if ((extraFilter != null) && (!extraFilter.isEmpty())) {
      theFilter = "(&" + extraFilter + driverSettings.getUsersFullFilter() + ")";
    } else {
      theFilter = driverSettings.getUsersFullFilter();
    }
    SynchroDomainReport
        .debug(LDAPUSER_GET_ALL_USERS, "Recherche des utilisateurs du domaine LDAP distant...");
    List<UserDetail> ldapUsers = new ArrayList<>();
    LDAPEntry[] theEntries = LDAPUtility
        .search1000Plus(lds, driverSettings.getLDAPUserBaseDN(), driverSettings.getScope(),
            theFilter, driverSettings.getUsersLoginField(), driverSettings.getUserAttributes());
    for (i = 0; i < theEntries.length; i++) {
      ldapUsers.add(translateUser(theEntries[i]));

      ldapUsers.get(i).traceUser();// Trace niveau Info ds
      // module 'admin' des infos user courant : ID, domaine, login, e-mail,...
      SynchroDomainReport.debug(LDAPUSER_GET_ALL_USERS,
          "Utilisateur trouvé no : " + i + ", login : " + ldapUsers.get(i).getLogin() + ", " +
              ldapUsers.get(i).getFirstName() + ", " + ldapUsers.get(i).getLastName() + ", " +
              ldapUsers.get(i).getEmailAddress());
    }
    SynchroDomainReport.debug(LDAPUSER_GET_ALL_USERS,
        "Récupération de " + theEntries.length + " utilisateurs du domaine LDAP distant");
    return ldapUsers.toArray(new UserDetail[0]);
  }

  /**
   * Return a list of {@link UserFull} instance each one filled with the infos of the user having
   * ID = id
   * <p> NOTE : the DomainID and the ID are not set.</p>
   * @param lds the LDAP connection
   * @param ids list of user id
   * @return the list of {@link UserFull} instance.
   * @throws AdminException if an error occur during LDAP operations or if the user is not found
   */
  public List<UserFull> listUserFulls(String lds, Collection<String> ids, int domainId)
      throws AdminException {
    List<String> lAttrs = new ArrayList<>();
    String[] userAttributes = driverSettings.getUserAttributes();
    if (userAttributes != null && userAttributes.length > 0) {
      lAttrs.addAll(Arrays.asList(userAttributes));
      if (driverParent.getMapParameters() != null) {
        lAttrs.addAll(Arrays.asList(driverParent.getMapParameters()));
      }
    }
    final List<UserFull> users = new ArrayList<>(ids.size());
    for (final Collection<String> idBatch : CollectionUtil.split(ids)) {
      final List<LDAPEntry> entries = LDAPUtility.getEntriesFromSearch(lds,
          driverSettings.getLDAPUserBaseDN(), driverSettings.getScope(),
          driverSettings.getUsersIdFilter(idBatch), lAttrs.toArray(new String[0]));
      final Map<String, String> spUserIdByUserDN = mapSpUserIdByUserDN(lds, entries, domainId);
      for (final LDAPEntry entry : entries) {
        users.add(translateUserFull(lds, entry, spUserIdByUserDN));
      }
    }
    return users;
  }

  /**
   * Return a list of {@link UserDetail} instance each one filled with the infos of the user having
   * ID = id
   * <p> NOTE : the DomainID and the ID are not set.</p>
   * @param lds the LDAP connection
   * @param ids list of user id
   * @return the list of {@link UserDetail} instance.
   * @throws AdminException if an error occur during LDAP operations or if the user is not found
   */
  public List<UserDetail> listUsers(String lds, Collection<String> ids) throws AdminException {
    final List<UserDetail> users = new ArrayList<>(ids.size());
    for (final Collection<String> idBatch : CollectionUtil.split(ids)) {
      final List<LDAPEntry> entries = LDAPUtility.getEntriesFromSearch(lds,
          driverSettings.getLDAPUserBaseDN(), driverSettings.getScope(),
          driverSettings.getUsersIdFilter(idBatch), driverSettings.getUserAttributes());
      for (final LDAPEntry entry : entries) {
        users.add(translateUser(entry));
      }
    }
    return users;
  }

  UserDetail getUserByLogin(String lds, String loginUser) throws AdminException {
    LDAPEntry theEntry;
    theEntry = LDAPUtility
        .getFirstEntryFromSearch(lds, driverSettings.getLDAPUserBaseDN(), driverSettings.getScope(),
            driverSettings.getUsersLoginFilter(loginUser), driverSettings.getUserAttributes());
    return translateUser(theEntry);
  }

  /**
   * Centralizing the translation from LDAP user entry to Silverpeas user data.
   * @param ldapUser the LDAP entry of a user
   * @param silverpeasUser the user detail in Silverpeas
   */
  private void translateCommonUserData(LDAPEntry ldapUser, UserDetail silverpeasUser) {
    setUserDetailCommonAttribute(ldapUser, silverpeasUser);

    // Trying to get the information of that the user account is disabled or not
    String userAccountControlAttribute = driverSettings.getUsersAccountControl();
    String disabledUserAccountFlag = driverSettings.getUsersDisabledAccountFlag();
    if (isRedirection(userAccountControlAttribute, disabledUserAccountFlag)) {
      String usersAccountControl = getFirstAttributeValue(ldapUser, userAccountControlAttribute);
      if (!StringUtil.isDefined(usersAccountControl)) {
        usersAccountControl = StringUtil.EMPTY;
      }
      setUserDetailState(silverpeasUser, disabledUserAccountFlag, usersAccountControl);
    }
  }

  private void setUserDetailState(final UserDetail silverpeasDistantUser,
      final String disabledUserAccountFlag, final String usersAccountControl) {
    if (StringUtil.isLong(usersAccountControl)) {
      if (StringUtil.isLong(disabledUserAccountFlag)) {
        long currentAccountControlFlags = Long.parseLong(usersAccountControl);
        long disabledUserAccountFlagAsLong = Long.parseLong(disabledUserAccountFlag);
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

  private void setUserDetailCommonAttribute(final LDAPEntry ldapUser,
      final UserDetail silverpeasDistantUser) {
    silverpeasDistantUser.setSpecificId(
        getFirstAttributeValue(ldapUser, driverSettings.getUsersIdField()));
    silverpeasDistantUser.setLogin(
        getFirstAttributeValue(ldapUser, driverSettings.getUsersLoginField()));
    silverpeasDistantUser.setFirstName(
        getFirstAttributeValue(ldapUser, driverSettings.getUsersFirstNameField()));
    silverpeasDistantUser.setLastName(
        getFirstAttributeValue(ldapUser, driverSettings.getUsersLastNameField()));
    silverpeasDistantUser.setEmailAddress(
        getFirstAttributeValue(ldapUser, driverSettings.getUsersEmailField()));

    silverpeasDistantUser.setAccessLevel(null); // Put the default access level (user)...
  }

  /**
   * Translate a LDAP user entry into a UserDetail structure. NOTE : the DomainID and the ID are not
   * set.
   * @param userEntry the LDAP user object
   * @param spUserIdByUserDN map of silverpeas user id indexed by their corresponding user DN.
   * The map MUST have been initialized from USERID attributes of {@link LDAPEntry} instances.
   * @return the user object
   * @throws AdminException if an error occur during LDAP operations or if there is no userEntry
   * object
   */
  private UserFull translateUserFull(String lds, LDAPEntry userEntry,
      final Map<String, String> spUserIdByUserDN) throws AdminException {
    final UserFull userInfos = new UserFull(driverParent);
    final String[] keys = driverParent.getPropertiesNames();
    translateCommonUserData(userEntry, userInfos);
    for (final String key : keys) {
      final DomainProperty curProp = driverParent.getProperty(key);
      if (DomainProperty.PROPERTY_TYPE_USERID.equals(curProp.getType())) {
        final String subUserDN = getFirstAttributeValue(userEntry, curProp.getMapParameter());
        if (subUserDN != null && !subUserDN.isEmpty()) {
          setUserProperty(userInfos, curProp, subUserDN, spUserIdByUserDN);
        }
      } else if (isRedirection(curProp.getRedirectOU(), curProp.getRedirectAttribute())) {
        setRedirectedProperty(lds, userEntry, curProp, userInfos);
      } else {
        userInfos.setValue(curProp.getName(),
            getFirstAttributeValue(userEntry, curProp.getMapParameter()));
      }
    }
    return userInfos;
  }

  private Map<String, String> mapSpUserIdByUserDN(String lds, List<LDAPEntry> userEntries,
      int domainId) throws AdminException {
    final List<String> parameters = Stream.of(driverParent.getPropertiesNames())
        .map(driverParent::getProperty)
        .filter(p -> DomainProperty.PROPERTY_TYPE_USERID.equals(p.getType()))
        .map(DomainProperty::getMapParameter)
        .collect(toList());
    final Set<String> bossDNs = userEntries.stream()
        .flatMap(e -> parameters.stream()
            .map(p -> {
              final String subUserDN = getFirstAttributeValue(e, p);
              if (subUserDN != null && !subUserDN.isEmpty()) {
                return subUserDN;
              }
              return null;
            })
            .filter(Objects::nonNull))
        .map(d -> d.substring(0, d.indexOf(",")))
        .collect(toSet());
    if (!bossDNs.isEmpty()) {
      final String usersIdField = driverSettings.getUsersIdField();
      Stream<LDAPEntry> entries = Stream.empty();
      for (final Collection<String> dnBatch : CollectionUtil.split(bossDNs, 1)) {
        entries = Stream.concat(entries, LDAPUtility.getEntriesFromSearch(lds,
            driverSettings.getLDAPUserBaseDN(), driverSettings.getScope(),
            driverSettings.getUsersManualFilter(dnBatch), new String[]{usersIdField}).stream());
      }
      final Map<String, String> dnBySpecificId = entries
          .collect(toMap(e -> getFirstAttributeValue(e, usersIdField), LDAPEntry::getDN));
      if (!dnBySpecificId.isEmpty()) {
        return UserManager.get()
            .getUsersBySpecificIdsAndDomainId(dnBySpecificId.keySet(), String.valueOf(domainId))
            .stream()
            .collect(toMap(u -> dnBySpecificId.get(u.getSpecificId()), UserDetail::getId));
      }
    }
    return Map.of();
  }

  private void setUserProperty(final UserFull userInfos, final DomainProperty curProp,
      final String subUserDN, final Map<String, String> spUserIdByUserDN) {
    final String userId = spUserIdByUserDN.get(subUserDN);
    if (userId != null) {
      userInfos.setValue(curProp.getName(), userId);
    } else if (synchroInProcess) {
      synchroReport.append(format("PB getting '%s' data: %s%n", curProp.getName(), subUserDN));
    }
  }

  private void setRedirectedProperty(final String lds, final LDAPEntry userEntry,
      final DomainProperty curProp, final UserFull userInfos) throws AdminException {
    final LDAPEntry subUserEntry;
    String cn = getFirstAttributeValue(userEntry, curProp.getMapParameter());
    if (StringUtil.isDefined(cn)) {
      String baseDN = curProp.getRedirectOU();
      String filter = "(cn=" + cn + ")";
      subUserEntry =
          LDAPUtility.getFirstEntryFromSearch(lds, baseDN, LDAPConnection.SCOPE_SUB, filter,
              driverSettings.getUserAttributes());

      if (subUserEntry != null) {
        userInfos.setValue(curProp.getName(),
            getFirstAttributeValue(subUserEntry, curProp.getRedirectAttribute()));
      }
    }
  }

  private boolean isRedirection(final String redirectOU, final String redirectAttribute) {
    return StringUtil.isDefined(redirectOU) && StringUtil.isDefined(redirectAttribute);
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
  private UserDetail translateUser(LDAPEntry userEntry) throws AdminException {
    UserDetail userInfos = new UserDetail();

    if (userEntry == null) {
      throw new AdminException(undefined("LDAP user entry"));
    }

    // Set the AdminUser information
    translateCommonUserData(userEntry, userInfos);

    synchroCache.addUser(userEntry);

    return userInfos;
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
    return lAttrs.toArray(new String[0]);
  }

}
