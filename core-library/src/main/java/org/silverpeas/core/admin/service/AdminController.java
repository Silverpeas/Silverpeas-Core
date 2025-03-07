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
package org.silverpeas.core.admin.service;

import org.silverpeas.core.admin.ProfiledObjectId;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.component.model.PasteDetail;
import org.silverpeas.core.admin.component.model.WAComponent;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.domain.model.DomainProperty;
import org.silverpeas.core.admin.quota.exception.QuotaException;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.space.SpaceProfileInst;
import org.silverpeas.core.admin.user.model.*;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.util.expression.PrefixedNotationExpressionEngine;
import org.silverpeas.kernel.logging.SilverLogger;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.*;

import static java.util.Collections.emptyList;

/**
 * This object is used by all the admin jsp such as SpaceManagement, UserManagement, etc...
 * It provides access functions to query and modify the domains as well as the company organization
 * It should be used only by a client that has the administrator rights
 */
@Service
@Transactional
public class AdminController implements java.io.Serializable {

  private static final long serialVersionUID = -1605341557688427460L;

  @Inject
  private Administration admin;

  protected AdminController() {

  }

  public void reloadCaches() {
    admin.reloadCache();
  }

  /** Return the space Instance corresponding to the given space id */
  public SpaceInst getSpaceInstById(String sSpaceId) {

    try {
      return admin.getSpaceInstById(sSpaceId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return null;
    }
  }

  public SpaceInstLight getSpaceInstLight(String sSpaceId) {

    try {
      return admin.getSpaceInstLightById(sSpaceId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return null;
    }
  }

  public List<SpaceInstLight> getPathToComponent(String componentId) {

    try {
      return admin.getPathToComponent(componentId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return emptyList();
    }
  }

  public List<SpaceInstLight> getPathToSpace(String spaceId, boolean includeTarget) {

    try {
      return admin.getPathToSpace(spaceId, includeTarget);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return emptyList();
    }
  }

  /**
   * Return the space Instance corresponding to the given space id : FORMAT EX : 123
   */
  public String[] getUserManageableSpaceIds(String sUserId) {

    try {
      return admin.getUserManageableSpaceIds(sUserId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return ArrayUtil.emptyStringArray();
    }
  }

  /**
   * Return the space Instance corresponding to the given space id : FORMAT EX : WA123
   * If user is Admin, return all space Ids
   * @return an array of space identifiers
   **/
  public String[] getUserManageableSpaceClientIds(String sUserId) {

    try {
      UserDetail user = admin.getUserDetail(sUserId);
      if (user.isAccessAdmin() || sUserId.equals("0")) {
        return admin.getClientSpaceIds(admin.getAllSpaceIds());
      } else {
        return admin.getClientSpaceIds(admin.getUserManageableSpaceIds(sUserId));
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return ArrayUtil.emptyStringArray();
    }
  }

  /** Add the given space Instance */
  public String addSpaceInst(SpaceInst spaceInst) {

    try {
      return admin.addSpaceInst(spaceInst.getCreatorUserId(), spaceInst);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return "";
    }
  }

  /** Delete the space Instance corresponding to the given space id */
  public String deleteSpaceInstById(UserDetail user, String sSpaceInstId, boolean definitive) {

    try {
      return admin.deleteSpaceInstById(user.getId(), sSpaceInstId, definitive);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return "";
    }
  }

  /**
   * Update the space Instance corresponding to the given space name with the given SpaceInst
   */
  public String updateSpaceInst(SpaceInst spaceInstNew) {

    try {
      return admin.updateSpaceInst(spaceInstNew);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return "";
    }
  }

  /** Return all the spaces Id available in Silverpeas */
  public String[] getAllRootSpaceIds() {

    try {
      return admin.getAllRootSpaceIds();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return ArrayUtil.emptyStringArray();
    }
  }

  /** Return all the spaces Id available in Silverpeas */
  public String[] getAllSpaceIds() {

    try {
      return admin.getAllSpaceIds();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return ArrayUtil.emptyStringArray();
    }
  }

  /** Return all the spaces Id available for the given userId */
  public String[] getAllSpaceIds(String userId) {

    try {
      return admin.getAllSpaceIds(userId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return ArrayUtil.emptyStringArray();
    }
  }

  /**
   * Return all the sub spaces Id available in Silverpeas given the fatherDomainId
   */
  public String[] getAllSubSpaceIds(String sDomainFatherId) {

    try {
      return admin.getAllSubSpaceIds(sDomainFatherId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return ArrayUtil.emptyStringArray();
    }
  }

  /**
* Return all the sub spaces Id available for the given user and the given fatherDomainId
*/
  public String[] getAllSubSpaceIds(String sDomainFatherId, String userId) {
    try {
      return admin.getAllSubSpaceIds(sDomainFatherId, userId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return ArrayUtil.emptyStringArray();
    }
  }

  public void updateSpaceOrderNum(String sSpaceId, int orderNum) {

    try {
      admin.updateSpaceOrderNum(sSpaceId, orderNum);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
    }
  }

  public void indexSpace(int spaceId) {
    admin.createSpaceIndex(spaceId);
  }

  public void deleteAllSpaceIndexes() {
    admin.deleteAllSpaceIndexes();
  }

  /** Move space in the given space with the given fatherId */
  public void moveSpace(String spaceId, String fatherId) throws AdminException {

    admin.moveSpace(spaceId, fatherId);
  }

  // ----------------------------------------------

  /** Return all the components of silverpeas read in the xmlComponent directory */
  public Map<String, WAComponent> getAllComponents() {

    try {
      return admin.getAllWAComponents();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return Collections.emptyMap();
    }
  }

  /** Return the component Instance corresponding to the given component id */
  public ComponentInst getComponentInst(String sComponentId) {

    try {
      return admin.getComponentInst(sComponentId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return null;
    }
  }

  public ComponentInstLight getComponentInstLight(String sComponentId) {

    try {
      return admin.getComponentInstLight(sComponentId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return null;
    }
  }

  /** Add the given component Instance */
  public String addComponentInst(ComponentInst componentInst) throws QuotaException {

    Exception exception = null;
    try {
      return admin.addComponentInst(componentInst.getCreatorUserId(), componentInst);
    } catch (QuotaException e) {
      exception = e;
      throw e;
    } catch (Exception e) {
      exception = e;
      return "";
    } finally {
      if (exception != null) {
        SilverLogger.getLogger(this)
            .error(exception.getLocalizedMessage(), exception);
      }
    }
  }

  /** Delete the component Instance corresponding to the given component id */
  public String deleteComponentInst(UserDetail user, String sComponentId, boolean definitive) {

    try {
      return admin.deleteComponentInst(user.getId(), sComponentId, definitive);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return "";
    }
  }

  /**
   * Update the component Instance corresponding to the given space component with the given
   * ComponentInst
   */
  public String updateComponentInst(ComponentInst componentInst) {

    try {
      return admin.updateComponentInst(componentInst);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return "";
    }
  }

  /** Move the component Instance in the given space with the given componentId */
  public void moveComponentInst(String spaceId, String componentId,
      String idComponentBefore, ComponentInst[] componentInstances)
      throws AdminException {

    admin.moveComponentInst(spaceId, componentId, idComponentBefore, componentInstances);
  }

  /**
   * Return the component ids available for the current user Id in the given space id
   */
  public String[] getAvailCompoIds(String sClientSpaceId, String sUserId) {

    try {
      return admin.getAvailCompoIds(sClientSpaceId, sUserId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return ArrayUtil.emptyStringArray();
    }
  }

  public boolean isComponentAvailable(String componentId, String userId) {

    try {
      return admin.isComponentAvailableToUser(componentId, userId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return false;
    }
  }

  /**
   * Is a user can access the specified space?
   * @param userId the user id.
   * @param spaceId the space id.
   * @return true if the space is accessible - false otherwise.
   */
  public boolean isSpaceAvailable(String userId, String spaceId) {

    try {
      return admin.isSpaceAvailable(userId, spaceId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return false;
    }
  }

  public void setComponentPlace(String componentId, String idComponentBefore,
      ComponentInst[] brothersComponents) {
    try {
      admin.setComponentPlace(componentId, idComponentBefore, brothersComponents);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
    }
  }

  public void indexComponent(String componentId) {
    admin.createComponentIndex(componentId);
  }

  public void deleteAllComponentIndexes() {
    admin.deleteAllComponentIndexes();
  }

  // ----------------------------------------------
  // Space and Component Bin
  // ----------------------------------------------
  public List<SpaceInstLight> getRemovedSpaces() {
    try {
      return admin.getRemovedSpaces();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return emptyList();
    }
  }

  public List<ComponentInstLight> getRemovedComponents() {
    try {
      return admin.getRemovedComponents();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return emptyList();
    }
  }

  public void restoreSpaceFromBasket(String spaceId) {
    try {
      admin.restoreSpaceFromBasket(spaceId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
    }
  }

  public void restoreComponentFromBasket(String componentId) {
    try {
      admin.restoreComponentFromBasket(componentId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
    }
  }

  // ----------------------------------------------
  // Profile Instances related functions
  // ----------------------------------------------
  /** Return all the profiles names available for the given profile */
  public String[] getAllProfilesNames(String sComponentName) {

    try {
      return admin.getAllProfilesNames(sComponentName);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return ArrayUtil.emptyStringArray();
    }
  }

  /** Return the profile Instance corresponding to the given profile id */
  public ProfileInst getProfileInst(String sProfileId) {

    try {
      return admin.getProfileInst(sProfileId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return null;
    }
  }

  public List<ProfileInst> getProfilesByObject(ProfiledObjectId objectId, String componentId) {
    try {
      return admin.getProfilesByObject(objectId, componentId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return emptyList();
    }
  }

  public String[] getProfilesByObjectAndUserId(ProfiledObjectId objectRef, String componentId,
      String userId) {
    try {
      return admin.getProfilesByObjectAndUserId(objectRef, componentId, userId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return ArrayUtil.emptyStringArray();
    }
  }

  /** Add the given Profile Instance */
  public String addProfileInst(ProfileInst profileInst) {
    return addProfileInst(profileInst, null);
  }

  public String addProfileInst(ProfileInst profileInst, String userId) {

    try {
      return admin.addProfileInst(profileInst, userId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return "";
    }
  }

  /**
   * Delete the Profile Instance corresponding to the given Profile id.
   * @param sProfileId the identifier of the role profile.
   * @return the identifier of the deleted role profile.
   */
  public String deleteProfileInst(String sProfileId) {
    return deleteProfileInst(sProfileId, null);
  }

  public String deleteProfileInst(String sProfileId, String userId) {

    try {
      return admin.deleteProfileInst(sProfileId, userId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return "";
    }
  }

  /**
   * Update the Profile Instance corresponding to the given space Profile with the given ProfileInst
   */
  public String updateProfileInst(ProfileInst profileInst) {
    return updateProfileInst(profileInst, null);
  }

  public String updateProfileInst(ProfileInst profileInst, String userId) {

    try {
      return admin.updateProfileInst(profileInst, userId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return "";
    }
  }

  /**
   * Get the profile label from its name
   */
  public String getProfileLabelByName(String sComponentName, String sProfileName, String lang) {

    try {
      return admin.getProfileLabelFromName(sComponentName, sProfileName, lang);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(
          "cannot get profile label from component name ''{0}'' and profile name ''{1}'' ({2})",
          new String[]{sComponentName, sProfileName, e.getLocalizedMessage()}, e);
      return "";
    }
  }

  // ----------------------------------------------
  // User Profile related functions
  // ----------------------------------------------
  /**
   * All the profiles to which the user belongs
   * @return an array of profile IDs
   */
  public String[] getProfileIds(String sUserId) {

    try {
      return admin.getProfileIds(sUserId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return ArrayUtil.emptyStringArray();
    }
  }

  // ----------------------------------------------
  // GroupDetail Profile related functions
  // ----------------------------------------------
  /**
   * All the profiles to which the group belongs
   * @return an array of profile IDs
   */
  public String[] getProfileIdsOfGroup(String sGroupId) {

    try {
      return admin.getProfileIdsOfGroup(sGroupId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return ArrayUtil.emptyStringArray();
    }
  }

  /*
   * Assign rights of a user to a user
   * @param operationMode : value of {@link RightAssignationContext.MODE}
   * @param sourceUserId : the user id of the source user
   * @param targetUserId : the user id of the target user
   * @param nodeAssignRights : true if you want also to add rights to nodes
   * @param authorId : the userId of the author of this action
   */
  public void assignRightsFromUserToUser(RightAssignationContext.MODE operationMode,
      String sourceUserId, String targetUserId, boolean nodeAssignRights, String authorId)
      throws AdminException {
    admin.assignRightsFromUserToUser(operationMode, sourceUserId, targetUserId, nodeAssignRights,
        authorId);
  }

  /*
   * Assign rights of a user to a group
   * @param operationMode : value of {@link RightAssignationContext.MODE}
   * @param sourceUserId : the user id of the source user
   * @param targetGroupId : the group id of the target group
   * @param nodeAssignRights : true if you want also to add rights to nodes
   * @param authorId : the userId of the author of this action
   */
  public void assignRightsFromUserToGroup(RightAssignationContext.MODE operationMode,
      String sourceUserId, String targetGroupId, boolean nodeAssignRights, String authorId)
      throws AdminException {
    admin.assignRightsFromUserToGroup(operationMode, sourceUserId, targetGroupId, nodeAssignRights,
        authorId);
  }

  /*
   * Assign rights of a group to a user
   * @param operationMode : value of {@link RightAssignationContext.MODE}
   * @param sourceGroupId : the group id of the source group
   * @param targetUserId : the user id of the target user
   * @param nodeAssignRights : true if you want also to add rights to nodes
   * @param authorId : the userId of the author of this action
   */
  public void assignRightsFromGroupToUser(RightAssignationContext.MODE operationMode,
      String sourceGroupId, String targetUserId, boolean nodeAssignRights, String authorId)
      throws AdminException {
    admin.assignRightsFromGroupToUser(operationMode, sourceGroupId, targetUserId, nodeAssignRights,
        authorId);
  }

  /*
   * Assign rights of a group to a group
   * @param operationMode : value of {@link RightAssignationContext.MODE}
   * @param sourceGroupId : the group id of the source group
   * @param targetGroupId : the group id of the target group
   * @param nodeAssignRights : true if you want also to add rights to nodes
   * @param authorId : the userId of the author of this action
   */
  public void assignRightsFromGroupToGroup(RightAssignationContext.MODE operationMode,
      String sourceGroupId, String targetGroupId, boolean nodeAssignRights, String authorId)
      throws AdminException {
    admin
        .assignRightsFromGroupToGroup(operationMode, sourceGroupId, targetGroupId, nodeAssignRights,
            authorId);
  }

  // ----------------------------------------------
  // User related functions
  // ----------------------------------------------
  public List<GroupDetail> getDirectGroupsOfUser(String userId) {
    try {
      return admin.getDirectGroupsOfUser(userId);
    } catch (Exception e) {
      SilverLogger.getLogger(this)
          .error("cannot get direct group identifiers of userId ''{0}'' ({1})",
              new String[]{userId, e.getLocalizedMessage()}, e);
      return emptyList();
    }
  }

  /**
   * update a domain
   */
  public String updateDomain(Domain theDomain) {

    try {
      return admin.updateDomain(theDomain);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return "";
    }
  }

  /**
   * Get a domain with given id
   */
  public Domain getDomain(String domainId) {
    try {
      return admin.getDomain(domainId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error("cannot get domain from identifier ''{0}'' ({1})",
          new String[]{domainId, e.getLocalizedMessage()}, e);
      return null;
    }
  }

  /**
   * Get a domain's possible actions
   */
  public long getDomainActions(String domainId) {

    try {
      return admin.getDomainActions(domainId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return 0;
    }
  }

  /**
   * Get ALL the domain's groups
   */
  public GroupDetail[] getRootGroupsOfDomain(String domainId) {

    try {
      return admin.getRootGroupsOfDomain(domainId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return new GroupDetail[0];
    }
  }

  /**
   * Get ALL the users that are in a group or his sub groups
   */
  public UserDetail[] getAllUsersOfGroup(String groupId) {

    try {
      return admin.getAllUsersOfGroup(groupId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return new UserDetail[0];
    }
  }

  /**
   * Get ALL the domain's users
   */
  public UserDetail[] getUsersOfDomain(String domainId) {

    try {
      return admin.getUsersOfDomain(domainId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return new UserDetail[0];
    }
  }

  /**
   * Get ALL the userId of the domain
   */
  public String[] getUserIdsOfDomain(String domainId) {

    try {
      return admin.getUserIdsOfDomain(domainId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return ArrayUtil.emptyStringArray();
    }
  }

  /**
   * Get all domains declared in Silverpeas
   */
  public Domain[] getAllDomains() {

    try {
      return admin.getAllDomains();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return new Domain[0];
    }
  }

  // ----------------------------------------------
  // Space Profile related functions
  // ----------------------------------------------

  /**
   * Return the space profile Instance corresponding to the given space profile id
   */
  public SpaceProfileInst getSpaceProfileInst(String sSpaceProfileId) {

    try {
      return admin.getSpaceProfileInst(sSpaceProfileId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
        return null;
    }
  }

  /** Add the given Space Profile Instance */
  public String addSpaceProfileInst(SpaceProfileInst spaceProfileInst,
      String userId) {

    try {
      return admin.addSpaceProfileInst(spaceProfileInst, userId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return "";
    }
  }

  /**
   * Delete the Space Profile Instance corresponding to the given Space Profile id
   */
  public void deleteSpaceProfileInst(String sSpaceProfileId, String userId) {

    try {
      admin.deleteSpaceProfileInst(sSpaceProfileId, userId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
    }
  }

  /**
   * Update the Space Profile Instance corresponding to the given space Profile with the given
   * SpaceProfileInst
   */
  public String updateSpaceProfileInst(SpaceProfileInst spaceProfileInst,
      String userId) {

    try {
      return admin.updateSpaceProfileInst(spaceProfileInst, userId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return "";
    }
  }

  // ----------------------------------------------
  // Groups related functions
  // ----------------------------------------------

  public Group[] getAllSubGroups(String groupId) {
    try {
      return admin.getAllSubGroups(groupId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return new Group[0];
    }
  }

  /** Return the group name corresponding to the given group Id */
  public String getGroupName(String sGroupId) {

    try {
      return admin.getGroupName(sGroupId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error("cannot get group name from identifier ''{0}'' ({1})",
          new String[]{sGroupId, e.getLocalizedMessage()}, e);
      return "";
    }
  }

  /**
   * The spaces that can be managed by the given group
   * @return the array of space IDs
   */
  public String[] getGroupManageableSpaceIds(String sGroupId) {

    try {
      return admin.getGroupManageableSpaceIds(sGroupId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return ArrayUtil.emptyStringArray();
    }
  }

  /** Return the group profile */
  public GroupProfileInst getGroupProfile(String groupId) {

    try {
      return admin.getGroupProfileInst(groupId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return null;
    }
  }

  /** Update the GroupDetail Profile */
  public void updateGroupProfile(GroupProfileInst profile) {
    try {
      admin.updateGroupProfileInst(profile);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
    }
  }

  // ----------------------------------------------
  // Admin User Detail related functions
  // ----------------------------------------------

  /** Return the admin user detail corresponding to the given id */
  public UserDetail getUserDetail(String sId) {

    try {
      return admin.getUserDetail(sId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error("cannot get user data from identifier ''{0}'' ({1})",
          new String[]{sId, e.getLocalizedMessage()}, e);
      return null;
    }
  }

  /**
   * Return the UserFull of the user with the given Id
   */
  public UserFull getUserFull(String sUserId) {

    try {
      return admin.getUserFull(sUserId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error("cannot get full user data from identifier ''{0}'' ({1})",
          new String[]{sUserId, e.getLocalizedMessage()}, e);
      return null;
    }
  }

  public UserFull getUserFull(String domainId, String specificId) {

    try {
      return admin.getUserFull(domainId, specificId);
    } catch (Exception e) {
      SilverLogger.getLogger(this)
          .error("cannot get full user data from domainId ''{0}'' and specificId ''{1}'' ({2})",
              new String[]{domainId, specificId, e.getLocalizedMessage()}, e);
      return null;
    }
  }

  public String getUserIdByLoginAndDomain(String sLogin, String sDomainId) {

    try {
      return admin.getUserIdByLoginAndDomain(sLogin, sDomainId);
    } catch (Exception e) {
      SilverLogger.getLogger(this)
          .error("cannot get user identifier from login ''{0}'' and domainId ''{1}'' ({2})",
              new String[]{sLogin, sDomainId, e.getLocalizedMessage()}, e);
      return null;
    }
  }

  public boolean isUserByLoginAndDomainExist(String login, String domainId) {
    try {
      String userId = admin.getUserIdByLoginAndDomain(login, domainId);
      return StringUtil.isDefined(userId);
    } catch (AdminException e) {
      return false;
    }
  }

  /** Return an array of UserDetail corresponding to the given user Id array */
  public UserDetail[] getUserDetails(String[] asUserIds) {

    try {
      if (asUserIds != null) {
        return admin.getUserDetails(asUserIds);
      }
      return new UserDetail[0];
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return new UserDetail[0];
    }
  }

  /** Add the given user */
  public String addUser(UserDetail userDetail) {

    try {
      return admin.addUser(userDetail);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return "";
    }
  }

  /** Block the given user */
  public void blockUser(String userId) {

    try {
      admin.blockUser(userId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
    }
  }

  /** Unblock the given user */
  public void unblockUser(String userId) {

    try {
      admin.unblockUser(userId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
    }
  }

  /**
   * Deactivate the given user
   */
  public void deactivateUser(String userId) {

    try {
      admin.deactivateUser(userId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
    }
  }

  /**
   * Activate the given user
   */
  public void activateUser(String userId) {

    try {
      admin.activateUser(userId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
    }
  }

  public void setUserSensitiveData(String userId, boolean areSensitive) {
    try {
      admin.setUserSensitiveData(userId, areSensitive);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
    }
  }

  /** Delete the given user */
  public String deleteUser(String sUserId) {

    try {
      return admin.deleteUser(sUserId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return "";
    }
  }

  /** Restores the given user */
  public String restoreUser(String sUserId) {
    try {
      return admin.restoreUser(sUserId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return "";
    }
  }

  /** Removes the given user */
  public String removeUser(String sUserId) {
    try {
      return admin.removeUser(sUserId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return "";
    }
  }

  /** Update the given user */
  public String updateUser(UserDetail userDetail) {
    try {
      return admin.updateUser(userDetail);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return "";
    }
  }

  /**
   * Update the silverpeas specific infos of a synchronized user. For the moment : same as
   * updateUser
   */
  public String updateSynchronizedUser(UserDetail userDetail) {

    try {
      return admin.updateUser(userDetail);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return "";
    }
  }

  /** Update the given user */
  public String updateUserFull(UserFull userFull) throws AdminException {

    return admin.updateUserFull(userFull);
  }

  public String authenticate(String sKey, String sSessionId,
      boolean isAppInMaintenance) {
    try {
      return admin.identify(sKey, sSessionId, isAppInMaintenance);
    } catch (Exception e) {
      return "-1";
    }
  }

  public void indexAllUsers() {
    try {
      admin.indexAllUsers();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
    }
  }

  // ----------------------------------------------
  // Admin GroupDetail Detail related functions
  // ----------------------------------------------

  /** Return the admin group detail corresponding to the given id */
  public GroupDetail getGroupById(String sGroupId) {

    try {
      return admin.getGroup(sGroupId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return null;
    }
  }

  /** Return the groupIds from root to group */
  public List<String> getPathToGroup(String groupId) {

    try {
      return admin.getPathToGroup(groupId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return emptyList();
    }
  }

  /** Return the admin group detail corresponding to the given group Name */
  public GroupDetail getGroupByNameInDomain(String sGroupName, String sDomainFatherId) {

    try {
      return admin.getGroupByNameInDomain(sGroupName, sDomainFatherId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return null;
    }
  }

  /** Add the given group */
  public String addGroup(GroupDetail group) {

    try {
      return admin.addGroup(group);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return "";
    }
  }

  public List<GroupDetail> getRemovedGroupsInDomain(final String domainId) throws AdminException {
    return admin.getRemovedGroups(domainId);
  }

  /** Restores the given group */
  public List<GroupDetail> restoreGroupById(String groupId) {
    try {
      return admin.restoreGroup(groupId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return emptyList();
    }
  }

  /** Removes the given group */
  public List<GroupDetail> removeGroupById(String groupId) {
    try {
      return admin.removeGroup(groupId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return emptyList();
    }
  }

  /** Delete the given group */
  public List<GroupDetail> deleteGroupById(String sGroupId) {
    try {
      return admin.deleteGroupById(sGroupId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return emptyList();
    }
  }

  /** Update the given group */
  public String updateGroup(GroupDetail group) {

    try {
      return admin.updateGroup(group);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return "";
    }
  }

  public String copyGroup(GroupDetail group, String parentGroupId) {
    try {
      return admin.copyGroup(group, parentGroupId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return "";
    }
  }

  public void moveGroup(GroupDetail group, String parentGroupId) {
    try {
      admin.moveGroup(group, parentGroupId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
    }
  }

  public void indexAllGroups() {
    try {
      admin.indexAllGroups();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
    }
  }

  // //////////////////////////////////////////////////////////
  // Synchronization tools
  // //////////////////////////////////////////////////////////

  /**
   * Synchronize users and groups between cache and domain's datastore
   * @param domainId Id of domain to synchronize
   * @return String to show as the report of synchronization
   */
  public String synchronizeSilverpeasWithDomain(String domainId) {
    try {
      return admin.synchronizeSilverpeasWithDomain(domainId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return "Error has occurred";
    }
  }

  public String synchronizeUser(String userId) {
    try {
      return admin.synchronizeUser(userId, true, true);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return "";
    }
  }

  public String synchronizeImportUser(String domainId, String userLogin) {
    try {
      return admin.synchronizeImportUser(domainId, userLogin, true);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return "";
    }
  }

  public List<DomainProperty> getSpecificPropertiesToImportUsers(String domainId,
      String language) {
    try {
      return admin.getSpecificPropertiesToImportUsers(domainId, language);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return emptyList();
    }
  }

  public List<UserDetail> searchUsers(String domainId, Map<String, String> query) {
    try {
      return Arrays.asList(admin.searchUsers(domainId, query));
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return emptyList();
    }
  }

  public String synchronizeRemoveUser(String userId) {
    try {
      return admin.synchronizeRemoveUser(userId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return "";
    }
  }

  /**
   * Synchronizes the users of the group represented by the given identifier.<br>
   * Two types of synchronization are possible:
   * <ul>
   * <li>when the group is synchronized from an LDAP domain, it is synchronized directly with the
   * LDAP</li>
   * <li>otherwise, the synchronization is done from the rule defined for the group</li>
   * </ul>
   * It is no guarantee that this treatment behavior is right if it is called out of the two
   * previous cases.
   * @param groupId the identifier of the group to process.
   * @return <ul><li>in case of success, the identifier of the group (a number)</li><li>in case of
   * error about the synchronization rule expression syntax, the key of the error from {@link
   * PrefixedNotationExpressionEngine} (it can be used for bundles for example)</li><li>in case
   * of error about the ground rule syntax, the key of the error and the ground rule as string
   * both separated by '|' character (it can be used for bundles for example)</li><li>an empty
   * value otherwise</li></ul>
   */
  public String synchronizeGroup(String groupId) {
    try {
      return admin.synchronizeGroup(groupId, true);
    } catch (AdminException e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      if (e.getCause() instanceof GroupSynchronizationRule.RuleError) {
        GroupSynchronizationRule.RuleError error = (GroupSynchronizationRule.RuleError) e.getCause();
        if (error instanceof GroupSynchronizationRule.GroundRuleError) {
          return error.getHandledMessage() + "|" +
              ((GroupSynchronizationRule.GroundRuleError) error).getBaseRulePart();
        }
        return error.getHandledMessage();
      }
      return "";
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return "";
    }
  }

  public Result<String> synchronizeImportGroup(String domainId, String groupName) {
    try {
      return new Result<>(admin.synchronizeImportGroup(domainId, groupName, null, true, false));
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return new Result<>("").withError(e);
    }
  }

  public String synchronizeRemoveGroup(String groupId) {
    try {
      return admin.synchronizeRemoveGroup(groupId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
      return "";
    }
  }

  /** Removes the given user from the given group */
  public void removeUserFromGroup(String sUserId, String sGroupId) {
    try {
      admin.removeUserFromGroup(sUserId, sGroupId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
    }
  }

  /** Removes the given user from the given group */
  public void addUserInGroup(String sUserId, String sGroupId) {

    try {
      admin.addUserInGroup(sUserId, sGroupId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
    }
  }

  public String copyAndPasteComponent(PasteDetail pasteDetail)
      throws AdminException, QuotaException {
    return admin.copyAndPasteComponent(pasteDetail);
  }

  public String copyAndPasteSpace(PasteDetail pasteDetail)
      throws AdminException, QuotaException {
    return admin.copyAndPasteSpace(pasteDetail);
  }

  public boolean isDomainManagerUser(String userId, String domainId) {

    try {
      return admin.isDomainManagerUser(userId, domainId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
    }
    return false;
  }

  public List<UserDetail> getRemovedUsersInDomain(final String domainId) throws AdminException {
    return admin.getRemovedUsers(domainId);
  }

  public List<UserDetail> getDeletedUsersInDomain(final String domainId) throws AdminException {
    return admin.getNonBlankedDeletedUsers(domainId);
  }

  public List<UserDetail> getUsersWithSensitiveData(final String domainId) throws AdminException {
    return admin.getUsersWithSensitiveData(domainId);
  }

  public void blankDeletedUsers(final String targetDomainId, final List<String> userIds)
      throws AdminException {
    admin.blankDeletedUsers(targetDomainId, userIds);
  }

  public void disableDataSensitivity(final String targetDomainId,
      final List<String> userIds) throws AdminException {
    admin.disableDataSensitivity(targetDomainId, userIds);
  }

  public static class Result<T> {
    private final T value;
    private Exception exception;

    Result(final T value) {
      this.value = value;
    }

    public Optional<T> getValue() {
      return Optional.ofNullable(value);
    }

    public Optional<Exception> getException() {
      return Optional.ofNullable(exception);
    }

    Result<T> withError(final Exception error) {
      exception = error;
      return this;
    }
  }
}