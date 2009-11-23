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
package com.stratelia.silverpeas.domains.silverpeasdriver;

import java.util.ArrayList;

import com.silverpeas.util.cryptage.CryptMD5;
import com.silverpeas.util.cryptage.UnixMD5Crypt;
import com.stratelia.silverpeas.authentication.Authentication;
import com.stratelia.webactiv.beans.admin.AbstractDomainDriver;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.DomainProperty;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.UserFull;
import com.stratelia.webactiv.organization.AdminPersistenceException;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;

public class SilverpeasDriver extends AbstractDomainDriver {
  public DomainSPSchema organization = null;

  protected String m_PasswordEncryption = null;

  /**
   * Constructor
   */
  public SilverpeasDriver() {

  }

  /**
   * Virtual method that performs extra initialization from a properties file. To overload by the
   * class who need it.
   */
  public void initFromProperties(ResourceLocator rs) throws Exception {
    m_PasswordEncryption = rs.getString("database.SQLPasswordEncryption");
  }

  // when we are in a transaction the connection must not be released.
  private boolean inTransaction = false;

  /**
   * Get an domainSP schema from the pool.
   */
  public void getDomainSPSchema() throws AdminException {
    if (organization == null) {
      try {
        organization = DomainSPSchemaPool.getDomainSPSchema();
      } catch (AdminPersistenceException e) {
        throw new AdminException("SilverpeasDriver.getDomainSPSchema",
            SilverpeasException.ERROR, "admin.EX_ERR_GET_SCHEMA");
      }
    }
  }

  /**
   * Release the DomainSP schema.
   */
  public void releaseDomainSPSchema() throws AdminException {
    if (organization != null && !inTransaction) {
      DomainSPSchemaPool.releaseDomainSPSchema(organization);
      organization = null;
    }
  }

  /**
   * @param m_User
   * @return String
   */
  public String createUser(UserDetail ud) throws Exception {
    try {
      this.getDomainSPSchema();

      SPUserRow ur = new SPUserRow();
      ur.firstName = ud.getFirstName();
      ur.lastName = ud.getLastName();
      ur.title = "";
      ur.company = "";
      ur.position = "";
      ur.boss = "";
      ur.phone = "";
      ur.homePhone = "";
      ur.fax = "";
      ur.cellPhone = "";
      ur.address = "";
      ur.login = ud.getLogin();
      ur.email = ud.geteMail();
      ur.loginMail = "";
      ur.password = "";
      ur.passwordValid = false;

      this.organization.user.createUser(ur);
      return idAsString(ur.id);
    } catch (Exception e) {
      throw new AdminException("SilverpeasDriver.createUser",
          SilverpeasException.ERROR, "admin.EX_ERR_ADD_USER", ud.getFirstName()
          + " " + ud.getLastName(), e);
    } finally {
      this.releaseDomainSPSchema();
    }
  }

  /**
   * @param userId
   */
  public void deleteUser(String userId) throws Exception {
    try {
      this.getDomainSPSchema();
      this.organization.user.removeUser(idAsInt(userId));
    } catch (Exception e) {
      throw new AdminException("SilverpeasDriver.deleteUser",
          SilverpeasException.ERROR, "admin.EX_ERR_DELETE_USER", "userId : "
          + userId, e);
    } finally {
      this.releaseDomainSPSchema();
    }
  }

  /**
   * @param m_User
   */
  public void updateUserFull(UserFull ud) throws Exception {
    try {
      this.getDomainSPSchema();

      SPUserRow oldUser = this.organization.user.getUser(idAsInt(ud
          .getSpecificId()));

      SPUserRow ur = new SPUserRow();
      ur.id = idAsInt(ud.getSpecificId());
      ur.firstName = ud.getFirstName();
      ur.lastName = ud.getLastName();
      ur.login = ud.getLogin();
      ur.email = ud.geteMail();

      ur.title = ud.getValue("title");
      ur.company = ud.getValue("company");
      ur.position = ud.getValue("position");
      ur.boss = ud.getValue("boss");
      ur.phone = ud.getValue("phone");
      ur.homePhone = ud.getValue("homePhone");
      ur.fax = ud.getValue("fax");
      ur.cellPhone = ud.getValue("cellularPhone");
      ur.address = ud.getValue("address");
      ur.loginMail = "";
      if (Authentication.ENC_TYPE_UNIX.equals(m_PasswordEncryption)
          && !ud.getPassword().equals(oldUser.password)) {
        ur.password = UnixMD5Crypt.crypt(ud.getPassword());
      } else if (Authentication.ENC_TYPE_MD5.equals(m_PasswordEncryption)
          && !ud.getPassword().equals(oldUser.password)) {
        ur.password = CryptMD5.crypt(ud.getPassword());
      } else {
        ur.password = ud.getPassword();
      }
      ur.passwordValid = ud.isPasswordValid();

      this.organization.user.updateUser(ur);
    } catch (Exception e) {
      throw new AdminException("SilverpeasDriver.updateUser",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_USER", ud
          .getFirstName()
          + " " + ud.getLastName(), e);
    } finally {
      this.releaseDomainSPSchema();
    }
  }

  /**
   * @param m_User
   */
  public void updateUserDetail(UserDetail ud) throws Exception {
    try {
      this.getDomainSPSchema();

      SPUserRow ur = new SPUserRow();
      ur.id = idAsInt(ud.getSpecificId());
      ur.firstName = ud.getFirstName();
      ur.lastName = ud.getLastName();
      ur.login = ud.getLogin();
      ur.email = ud.geteMail();
      this.organization.user.updateUserDetailOnly(ur);
    } catch (Exception e) {
      throw new AdminException("SilverpeasDriver.updateUser",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_USER", ud
          .getFirstName()
          + " " + ud.getLastName(), e);
    } finally {
      this.releaseDomainSPSchema();
    }
  }

  /**
   * @param userId
   * @return User
   */
  public UserDetail getUser(String userId) throws Exception {
    try {
      this.getDomainSPSchema();
      SPUserRow ur = this.organization.user.getUser(idAsInt(userId));

      UserDetail ud = new UserDetail();

      ud.setFirstName(ur.firstName);
      ud.setLastName(ur.lastName);
      ud.setLogin(ur.login);
      ud.seteMail(ur.email);

      return ud;
    } catch (Exception e) {
      throw new AdminException("SilverpeasDriver.getUser",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_USER", "userId : "
          + userId, e);
    } finally {
      this.releaseDomainSPSchema();
    }
  }

  public UserFull getUserFull(String userId) throws Exception {
    try {
      this.getDomainSPSchema();
      SPUserRow ur = this.organization.user.getUser(idAsInt(userId));

      SPUserFull uf = new SPUserFull(this);

      if (ur != null && uf != null) {
        uf.setFirstName(ur.firstName);
        uf.setLastName(ur.lastName);
        uf.setTitle(ur.title);
        uf.setCompany(ur.company);
        uf.setPosition(ur.position);
        uf.setBossId(ur.boss);
        uf.setTelephone(ur.phone);
        uf.setHomePhone(ur.homePhone);
        uf.setFax(ur.fax);
        uf.setCellularPhone(ur.cellPhone);
        uf.setAddress(ur.address);
        uf.setLogin(ur.login);
        uf.seteMail(ur.email);
        uf.setPassword(ur.password);
        uf.setPasswordValid(ur.passwordValid);
      }

      return uf;
    } catch (Exception e) {
      throw new AdminException("SilverpeasDriver.getUser",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_USER", "userId : "
          + userId, e);
    } finally {
      this.releaseDomainSPSchema();
    }
  }

  /**
   * @return User[]
   */
  public UserDetail[] getAllUsers() throws Exception {
    UserDetail[] uds = null;

    try {
      this.getDomainSPSchema();
      SPUserRow[] urs = this.organization.user.getAllUsers();

      uds = new UserDetail[urs.length];
      for (int nI = 0; nI < urs.length; nI++) {
        UserDetail ud = new UserDetail();
        ud.setFirstName(urs[nI].firstName);
        ud.setLastName(urs[nI].lastName);
        ud.setLogin(urs[nI].login);
        ud.seteMail(urs[nI].email);
        ud.setSpecificId(idAsString(urs[nI].id));

        uds[nI] = ud;
      }

      return uds;
    } catch (Exception e) {
      throw new AdminException("SilverpeasDriver.getAllUsers",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_ALL_USERS", e);
    } finally {
      this.releaseDomainSPSchema();
    }
  }

  public UserDetail[] getUsersBySpecificProperty(String propertyName,
      String propertyValue) throws Exception {
    DomainProperty property = getProperty(propertyName);
    if (property == null) {
      // This property is not defined in this domain
      return null;
    } else {
      try {
        this.getDomainSPSchema();
        SPUserRow sample = new SPUserRow();
        sample.firstName = null;
        sample.lastName = null;
        sample.phone = null;
        sample.homePhone = null;
        sample.cellPhone = null;
        sample.fax = null;
        sample.address = null;
        sample.title = null;
        sample.company = null;
        sample.position = null;
        sample.email = null;
        sample.loginMail = null;

        if (propertyName.equalsIgnoreCase("title"))
          sample.title = propertyValue;
        else if (propertyName.equalsIgnoreCase("company"))
          sample.company = propertyValue;
        else if (propertyName.equalsIgnoreCase("position"))
          sample.position = propertyValue;
        else if (propertyName.equalsIgnoreCase("phone"))
          sample.phone = propertyValue;
        else if (propertyName.equalsIgnoreCase("homePhone"))
          sample.homePhone = propertyValue;
        else if (propertyName.equalsIgnoreCase("fax"))
          sample.fax = propertyValue;
        else if (propertyName.equalsIgnoreCase("cellularPhone"))
          sample.cellPhone = propertyValue;
        else if (propertyName.equalsIgnoreCase("address"))
          sample.address = propertyValue;

        SPUserRow[] users = this.organization.user.getAllMatchingUsers(sample);

        UserDetail[] userDetails = new UserDetail[users.length];
        UserDetail userDetail = null;
        for (int u = 0; users != null && u < users.length; u++) {
          userDetail = new UserDetail();
          userDetail.setSpecificId(Integer.toString(users[u].id));
          userDetails[u] = userDetail;
        }

        return userDetails;
      } catch (Exception e) {
        throw new AdminException("SQLDriver.getUsersBySpecificProperty",
            SilverpeasException.ERROR, "admin.EX_ERR_GET_USERS", e);
      } finally {
        this.releaseDomainSPSchema();
      }
    }
  }

  /**
   * @param m_Group
   * @return String
   */
  public String createGroup(Group m_Group) throws Exception {
    try {
      this.getDomainSPSchema();

      SPGroupRow gr = new SPGroupRow();

      gr.superGroupId = idAsInt(m_Group.getSuperGroupId());
      gr.name = m_Group.getName();
      gr.description = m_Group.getDescription();

      this.organization.group.createGroup(gr);

      // Add the users in the group
      String[] asUserIds = m_Group.getUserIds();
      for (int nI = 0; nI < asUserIds.length; nI++)
        if (asUserIds[nI] != null && asUserIds[nI].length() > 0)
          this.addUserInGroup(asUserIds[nI], idAsString(gr.id));

      return idAsString(gr.id);
    } catch (Exception e) {
      throw new AdminException("SilverpeasDriver.createGroup",
          SilverpeasException.ERROR, "admin.EX_ERR_ADD_GROUP", m_Group
          .getName(), e);
    } finally {
      this.releaseDomainSPSchema();
    }
  }

  /**
   * @param groupId
   */
  public void deleteGroup(String groupId) throws Exception {
    try {
      this.getDomainSPSchema();
      this.organization.group.removeGroup(idAsInt(groupId));
    } catch (Exception e) {
      throw new AdminException("SilverpeasDriver.deleteGroup",
          SilverpeasException.ERROR, "admin.EX_ERR_DELETE_GROUP", "groupId : "
          + groupId, e);
    } finally {
      this.releaseDomainSPSchema();
    }
  }

  /**
   * @param m_Group
   */
  public void updateGroup(Group group) throws Exception {
    ArrayList alRemUsers = new ArrayList();
    ArrayList alAddUsers = new ArrayList();

    try {
      if (group == null || group.getName().length() == 0
          || group.getId().length() == 0)
        throw new AdminException("SilverpeasDriver.updateGroup",
            SilverpeasException.ERROR, "admin.EX_ERR_INVALID_GROUP");

      this.getDomainSPSchema();

      // Update the group node
      SPGroupRow gr = Group2GroupRow(group);
      this.organization.group.updateGroup(gr);

      // Update the users if necessary
      String[] asOldUsersId = this.organization.user
          .getDirectUserIdsOfGroup(gr.id);

      // Compute the remove users list
      String[] asNewUsersId = group.getUserIds();
      for (int nI = 0; nI < asOldUsersId.length; nI++) {
        boolean bFound = false;
        for (int nJ = 0; nJ < asNewUsersId.length; nJ++)
          if (asOldUsersId[nI].equals(asNewUsersId[nJ]))
            bFound = true;

        if (!bFound)
          alRemUsers.add(asOldUsersId[nI]);
      }

      // Compute the add users list
      for (int nI = 0; nI < asNewUsersId.length; nI++) {
        boolean bFound = false;
        for (int nJ = 0; nJ < asOldUsersId.length; nJ++)
          if (asNewUsersId[nI].equals(asOldUsersId[nJ]))
            bFound = true;

        if (!bFound)
          alAddUsers.add(asNewUsersId[nI]);
      }

      // Remove the users that are not in this group anymore
      for (int nI = 0; nI < alRemUsers.size(); nI++)
        this.organization.group.removeUserFromGroup(idAsInt((String) alRemUsers
            .get(nI)), gr.id);

      // Add the new users of the group
      for (int nI = 0; nI < alAddUsers.size(); nI++)
        this.organization.group.addUserInGroup(idAsInt((String) alAddUsers
            .get(nI)), gr.id);
    } catch (Exception e) {
      throw new AdminException("SilverpeasDriver.updateGroup",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_GROUP", "groupId : "
          + group.getSpecificId(), e);
    } finally {
      this.releaseDomainSPSchema();
    }
  }

  /**
   * @param groupId
   * @return Group
   */
  public Group getGroup(String groupId) throws Exception {
    try {
      this.getDomainSPSchema();
      SPGroupRow gr = this.organization.group.getGroup(idAsInt(groupId));

      Group group = new Group();
      group.setSpecificId(idAsString(gr.id));
      group.setName(gr.name);
      group.setDescription(gr.description);

      // Get the father id
      gr = this.organization.group.getSuperGroup(idAsInt(groupId));
      if (gr != null)
        group.setSuperGroupId(idAsString(gr.id));

      // Get the selected users for this group
      String[] asUsersId = this.organization.user
          .getDirectUserIdsOfGroup(idAsInt(groupId));
      if (asUsersId != null)
        group.setUserIds(asUsersId);

      return group;
    } catch (Exception e) {
      throw new AdminException("SilverpeasDriver.getGroup",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_GROUP", "groupId : "
          + groupId, e);
    } finally {
      this.releaseDomainSPSchema();
    }
  }

  /**
   * @param groupId
   * @return Group[]
   */
  public Group[] getGroups(String groupId) throws Exception {
    try {
      this.getDomainSPSchema();
      SPGroupRow[] grs = this.organization.group
          .getDirectSubGroups(idAsInt(groupId));

      Group[] groups = new Group[grs.length];
      for (int nI = 0; nI < grs.length; nI++) {
        Group group = new Group();
        group.setSpecificId(idAsString(grs[nI].id));
        group.setName(grs[nI].name);
        group.setDescription(grs[nI].description);

        // Get the selected users for this group
        String[] asUsersId = this.organization.user
            .getDirectUserIdsOfGroup(grs[nI].id);
        if (asUsersId != null)
          group.setUserIds(asUsersId);

        groups[nI] = group;
      }

      return groups;
    } catch (Exception e) {
      throw new AdminException("SilverpeasDriver.getGroups",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_GROUPS",
          "father group id : " + groupId, e);
    } finally {
      this.releaseDomainSPSchema();
    }
  }

  /**
   * @return Group[]
   */
  public Group[] getAllGroups() throws Exception {
    try {
      this.getDomainSPSchema();
      SPGroupRow[] grs = this.organization.group.getAllGroups();

      Group[] groups = new Group[grs.length];
      for (int nI = 0; nI < grs.length; nI++) {
        Group group = new Group();
        group.setSpecificId(idAsString(grs[nI].id));
        group.setName(grs[nI].name);
        group.setDescription(grs[nI].description);

        // Get the father id
        SPGroupRow gr = this.organization.group.getSuperGroup(grs[nI].id);
        if (gr != null)
          group.setSuperGroupId(idAsString(gr.id));

        // Get the selected users for this group
        String[] asUsersId = this.organization.user
            .getDirectUserIdsOfGroup(grs[nI].id);
        if (asUsersId != null)
          group.setUserIds(asUsersId);

        groups[nI] = group;
      }

      return groups;
    } catch (Exception e) {
      throw new AdminException("SilverpeasDriver.getAllGroups",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_ALL_GROUPS", e);
    } finally {
      this.releaseDomainSPSchema();
    }
  }

  /**
   * @return Group[]
   */
  public Group[] getAllRootGroups() throws Exception {
    try {
      this.getDomainSPSchema();
      SPGroupRow[] grs = this.organization.group.getAllRootGroups();

      Group[] groups = new Group[grs.length];
      for (int nI = 0; nI < grs.length; nI++) {
        Group group = new Group();
        group.setSpecificId(idAsString(grs[nI].id));
        group.setName(grs[nI].name);
        group.setDescription(grs[nI].description);

        // Get the father id
        SPGroupRow gr = this.organization.group.getSuperGroup(grs[nI].id);
        if (gr != null)
          group.setSuperGroupId(idAsString(gr.id));

        // Get the selected users for this group
        String[] asUsersId = this.organization.user
            .getDirectUserIdsOfGroup(grs[nI].id);
        if (asUsersId != null)
          group.setUserIds(asUsersId);

        groups[nI] = group;
      }

      return groups;
    } catch (Exception e) {
      throw new AdminException("SilverpeasDriver.getAllRootGroups",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_ALL_ROOT_GROUPS", e);
    } finally {
      this.releaseDomainSPSchema();
    }
  }

  /**
   * @param userId
   * @param groupId
   */
  public void addUserInGroup(String userId, String groupId) throws Exception {
    try {
      this.getDomainSPSchema();
      this.organization.group.addUserInGroup(idAsInt(userId), idAsInt(groupId));
    } catch (Exception e) {
      throw new AdminException("SilverpeasDriver.addUserInGroup",
          SilverpeasException.ERROR, "admin.EX_ERR_ADD_USER_IN_GROUP",
          "userId : '" + userId + "', groupId : '" + groupId + "'", e);
    } finally {
      this.releaseDomainSPSchema();
    }
  }

  /**
   * @param userId
   * @param groupId
   */
  public void removeUserFromGroup(String userId, String groupId)
      throws Exception {
    try {
      this.getDomainSPSchema();
      this.organization.group.removeUserFromGroup(idAsInt(userId),
          idAsInt(groupId));
    } catch (Exception e) {
      throw new AdminException("SilverpeasDriver.removeUserFromGroup",
          SilverpeasException.ERROR, "admin.EX_ERR_REMOVE_USER_FROM_GROUP",
          "userId : '" + userId + "', groupId : '" + groupId + "'", e);
    } finally {
      this.releaseDomainSPSchema();
    }
  }

  /**
   * Start a new transaction
   */
  public void startTransaction(boolean bAutoCommit) throws Exception {
    getDomainSPSchema();
    inTransaction = true;
  }

  /**
   * Commit transaction
   */
  public void commit() throws Exception {
    try {
      organization.commit();
      inTransaction = false;
      releaseDomainSPSchema();
    } catch (AdminPersistenceException e) {
      throw new AdminException("SilverpeasDriver.commit",
          SilverpeasException.ERROR, "root.EX_ERR_COMMIT", e);
    }
  }

  /**
   * Rollback transaction
   */
  public void rollback() throws Exception {
    try {
      organization.rollback();
      inTransaction = false;
      releaseDomainSPSchema();
    } catch (AdminPersistenceException e) {
      throw new AdminException("SilverpeasDriver.rollback",
          SilverpeasException.ERROR, "admin.EX_ERR_ROLLBACK", e);
    }
  }

  /**
   * Convert Group to SPGroupRow
   */
  private SPGroupRow Group2GroupRow(Group group) {
    SPGroupRow gr = new SPGroupRow();
    gr.id = idAsInt(group.getSpecificId());
    gr.superGroupId = idAsInt(group.getSuperGroupId());
    gr.name = group.getName();
    gr.description = group.getDescription();

    return gr;
  }
}
