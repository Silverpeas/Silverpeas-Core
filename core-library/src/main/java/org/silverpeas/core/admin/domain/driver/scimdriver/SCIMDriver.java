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
package org.silverpeas.core.admin.domain.driver.scimdriver;

import org.silverpeas.core.admin.domain.AbstractDomainDriver;
import org.silverpeas.core.admin.domain.DomainServiceProvider;
import org.silverpeas.core.admin.domain.quota.UserDomainQuotaKey;
import org.silverpeas.core.admin.quota.exception.QuotaException;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.user.UserManager;
import org.silverpeas.core.admin.user.model.GroupDetail;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.kernel.bundle.SettingBundle;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.singleton;
import static org.silverpeas.core.admin.domain.DomainDriver.ActionConstants.ACTION_MASK_RO_LISTENER;
import static org.silverpeas.core.admin.domain.DomainDriver.ActionConstants.ACTION_UPDATE_USER;
import static org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery.unique;

@SuppressWarnings("unused")
public class SCIMDriver extends AbstractDomainDriver {

  private final UserManager userManager;

  public static final String GROUP = "group";

  /**
   * Constructor
   */
  protected SCIMDriver() {
    userManager = ServiceProvider.getService(UserManager.class);
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.admin.domain.AbstractDomainDriver#getGroupMemberGroupIds
   * (java.lang.String)
   */
  @Override
  public String[] getGroupMemberGroupIds(String groupId) {
    // In this driver, do nothing
    return ArrayUtil.emptyStringArray();
  }

  /**
   * Virtual method that performs extra initialization from a properties file. To overload by the
   * class who need it.
   */
  @Override
  public void initFromProperties(SettingBundle rs) {
    // In this driver, do nothing
  }

  /**
   * Gets all the actions this driver supports.
   * @return a bit mask identifying the supported actions.
   */
  @Override
  public long getDriverActions() {
    return ACTION_MASK_RO_LISTENER | ACTION_UPDATE_USER;
  }

  @Override
  public UserDetail importUser(String userLogin) {
    // In this driver, do nothing
    return null;
  }

  @Override
  public void removeUser(String userId) {
    // In this driver, do nothing
  }

  @Override
  public UserDetail synchroUser(String userId) {
    // In this driver, do nothing
    return null;
  }

  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public String createUser(UserDetail ud) throws AdminException {
    try {
      DomainServiceProvider.getUserDomainQuotaService().verify(UserDomainQuotaKey.from(ud));
    } catch (QuotaException qe) {
      throw new AdminException(qe.getMessage(), qe);
    }
    // In this driver, for now, nothing is specially saved into domain persistence returning the
    // external id (given by SCIM client).
    return ud.getSpecificId();
  }


  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public void resetPassword(UserDetail user, String password) {
    // In this driver, do nothing
  }

  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public void resetEncryptedPassword(UserDetail user, String encryptedPassword) {
    // In this driver, do nothing
  }


  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public void deleteUser(String userId) {
    // In this driver, do nothing
  }

  /**
   * Update the data inside domain &lt;DOMAIN_NAME&gt;_user table
   * @param uf a UserFull object
   */
  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public void updateUserFull(UserFull uf) {
    // In this driver, do nothing
  }

  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public void updateUserDetail(UserDetail ud) {
    // In this driver, do nothing
  }

  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public UserDetail getUser(String specificId) throws AdminException {
    return unique(listUsers(singleton(specificId)));
  }

  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public List<UserDetail> listUsers(final Collection<String> specificIds) throws AdminException {
    final List<UserDetail> users = userManager
        .getUsersBySpecificIdsAndDomainId(specificIds, String.valueOf(getDomainId()));
    if (users.size() != specificIds.size()) {
      throw new AdminException("not enough or too many users referenced to same identifier");
    }
    return users;
  }

  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public UserFull getUserFull(String id) throws AdminException {
    return unique(listUserFulls(singleton(id)));
  }

  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public List<UserFull> listUserFulls(final Collection<String> specificIds) throws AdminException {
    return listUsers(specificIds).stream()
        // In this driver, for now, nothing is specially saved into domain persistence
        .map(u -> new UserFull(null, u))
        .collect(Collectors.toList());
  }

  @Override
  public String[] getUserMemberGroupIds(String specificId) {
    // In this driver, do nothing
    return new String[0];
  }

  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public UserDetail[] getAllUsers() throws AdminException {
    // In this driver, returning silverpeas data
    return userManager.getAllUsersInDomain(String.valueOf(getDomainId()), false);
  }

  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public UserDetail[] getUsersBySpecificProperty(String propertyName, String propertyValue) {
    // In this driver, do nothing
    return new UserDetail[0];
  }

  @Override
  public UserDetail[] getUsersByQuery(Map<String, String> query) {
    // In this driver, do nothing
    return new UserDetail[0];
  }

  @Override
  public GroupDetail importGroup(String groupName) {
    // In this driver, do nothing
    return null;
  }

  @Override
  public void removeGroup(String groupId) {
    // In this driver, do nothing
  }

  @Override
  public GroupDetail synchroGroup(String groupId) {
    // In this driver, do nothing
    return null;
  }

  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public String createGroup(GroupDetail group) {
    // In this driver, do nothing
    return null;
  }

  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public void deleteGroup(String groupId) {
    // In this driver, do nothing
  }

  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public void updateGroup(GroupDetail group) {
    // In this driver, do nothing
  }

  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public GroupDetail getGroup(String specificId) {
    // In this driver, do nothing
    return null;
  }

  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public GroupDetail getGroupByName(String groupName) {
    // In this driver, do nothing
    return null;
  }

  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public GroupDetail[] getGroups(String groupId) {
    // In this driver, do nothing
    return new GroupDetail[0];
  }

  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public GroupDetail[] getAllGroups() {
    // In this driver, do nothing
    return new GroupDetail[0];
  }

  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public GroupDetail[] getAllRootGroups() {
    // In this driver, do nothing
    return new GroupDetail[0];
  }

  @Transactional(Transactional.TxType.MANDATORY)
  public void addUserInGroup(String userId, String groupId) {
    // In this driver, do nothing
  }

  @Transactional(Transactional.TxType.MANDATORY)
  public void removeUserFromGroup(String userId, String groupId) {
    // In this driver, do nothing
  }

  @Override
  public List<String> getUserAttributes() {
    return Collections.emptyList();
  }
}
