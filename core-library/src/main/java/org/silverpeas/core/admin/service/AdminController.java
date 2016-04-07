/**
* Copyright (C) 2000 - 2013 Silverpeas
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as
* published by the Free Software Foundation, either version 3 of the
* License, or (at your option) any later version.
*
* As a special exception to the terms and conditions of version 3.0 of
* the GPL, you may redistribute this Program in connection with Free/Libre
* Open Source Software ("FLOSS") applications as described in Silverpeas's
* FLOSS exception. You should have received a copy of the text describing
* the FLOSS exception, and it is also available here:
* "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

/*
* @author Norbert CHAIX
* @version 1.0
date 14/09/2000
*/
package org.silverpeas.core.admin.service;

import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.component.model.PasteDetail;
import org.silverpeas.core.admin.component.model.WAComponent;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.domain.model.DomainProperty;
import org.silverpeas.core.admin.quota.exception.QuotaException;
import org.silverpeas.core.admin.space.SpaceAndChildren;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.space.SpaceProfileInst;
import org.silverpeas.core.admin.space.model.SpaceTemplate;
import org.silverpeas.core.admin.user.model.AdminGroupInst;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.GroupProfileInst;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This object is used by all the admin jsp such as SpaceManagement, UserManagement, etc...
 * It provides access functions to query and modify the domains as well as the company organization
 * It should be used only by a client that has the administrator rights
 */
public class AdminController implements java.io.Serializable {

  private static final long serialVersionUID = -1605341557688427460L;

  @Inject
  private Administration admin;

  protected AdminController() {

  }

  // Start the processes
  public void startServer() throws Exception {
    admin.startServer();
  }

  // ----------------------------------------------
  // Space Instances related functions
  // ----------------------------------------------
  public String getGeneralSpaceId() {

    try {
      return admin.getGeneralSpaceId();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return "";
    }
  }

  /* Return true if the given space name exists */
  public boolean isSpaceInstExist(String sClientSpaceId) {

    try {
      return admin.isSpaceInstExist(sClientSpaceId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.isSpaceInstExist",
          "admin.MSG_ERR_IS_SPACE_EXIST", e);
      return false;
    }
  }

  /** Return the space Instance corresponding to the given space id */
  public SpaceInst getSpaceInstById(String sSpaceId) {

    try {
      return admin.getSpaceInstById(sSpaceId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getSpaceInstById",
          "admin.MSG_ERR_GET_SPACE", e);
      return null;
    }
  }

  public SpaceInstLight getSpaceInstLight(String sSpaceId) {

    try {
      return admin.getSpaceInstLightById(sSpaceId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getSpaceInstLight",
          "admin.MSG_ERR_GET_SPACE", e);
      return null;
    }
  }

  public Map<String, SpaceAndChildren> getTreeView(String userId, String spaceId) {

    try {
      return admin.getTreeView(userId, spaceId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getTreeView",
          "admin.MSG_ERR_GET_SPACE", e);
      return null;
    }
  }

  public List<SpaceInstLight> getPathToComponent(String componentId) {

    try {
      return admin.getPathToComponent(componentId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getPathToComponent",
          "admin.MSG_ERR_GET_SPACE", e);
      return null;
    }
  }

  public List<SpaceInstLight> getPathToSpace(String spaceId, boolean includeTarget) {

    try {
      return admin.getPathToSpace(spaceId, includeTarget);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getPathToSpace",
          "admin.MSG_ERR_GET_SPACE", e);
      return null;
    }
  }

  /** Return the space Instance corresponding to the given space id */
  public String[] getUserManageableSpaceRootIds(String sUserId) {

    try {
      return admin.getUserManageableSpaceRootIds(sUserId);
    } catch (Exception e) {
      SilverTrace.error("admin",
          "AdminController.getUserManageableSpaceRootIds",
          "admin.MSG_ERR_GET_SPACE", e);
      return null;
    }
  }

  /** Return the space Instance corresponding to the given space id */
  public String[] getUserManageableSubSpaceIds(String sUserId,
      String sParentSpace) {

    try {
      return admin.getUserManageableSubSpaceIds(sUserId, sParentSpace);
    } catch (Exception e) {
      SilverTrace.error("admin",
          "AdminController.getUserManageableSubSpaceIds",
          "admin.MSG_ERR_GET_SPACE", e);
      return null;
    }
  }

  /**
* Return the space Instance corresponding to the given space id : FORMAT EX : 123
*/
  public String[] getUserManageableSpaceIds(String sUserId) {

    try {
      return admin.getUserManageableSpaceIds(sUserId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getUserManageableSpaceIds",
          "admin.MSG_ERR_GET_SPACE", e);
      return null;
    }
  }

  /**
* Return the space Instance corresponding to the given space id : FORMAT EX : WA123
*/
  /** If user is Admin, return all space Ids */
  public String[] getUserManageableSpaceClientIds(String sUserId) {

    try {
      UserDetail user = admin.getUserDetail(sUserId);
      if (user.isAccessAdmin() || sUserId.equals("0")) {
        return admin.getClientSpaceIds(admin.getAllSpaceIds());
      } else {
        return admin.getClientSpaceIds(admin.getUserManageableSpaceIds(sUserId));
      }
    } catch (Exception e) {
      SilverTrace.error("admin",
          "AdminController.getUserManageableSpaceClientIds",
          "admin.MSG_ERR_GET_SPACE", e);
      return null;
    }
  }

  /** Add the given space Instance */
  public String addSpaceInst(SpaceInst spaceInst) {

    try {
      return admin.addSpaceInst(spaceInst.getCreatorUserId(), spaceInst);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.addSpaceInst", "admin.MSG_ERR_ADD_SPACE", e);
      return "";
    }
  }

  /** Delete the space Instance corresponding to the given space id */
  public String deleteSpaceInstById(UserDetail user, String sSpaceInstId, boolean definitive) {

    try {
      return admin.deleteSpaceInstById(user.getId(), sSpaceInstId, definitive);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.deleteSpaceInstById", "admin.MSG_ERR_DELETE_SPACE", e);
      return "";
    }
  }

  /**
* Update the space Instance corresponding to the given space name wuth the given SpaceInst
*/
  public String updateSpaceInst(SpaceInst spaceInstNew) {

    try {
      return admin.updateSpaceInst(spaceInstNew);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.updateSpaceInst", "admin.MSG_ERR_UPDATE_SPACE", e);
      return "";
    }
  }

  public Map<String, SpaceTemplate> getAllSpaceTemplates() {
    return admin.getAllSpaceTemplates();
  }

  public SpaceInst getSpaceInstFromTemplate(String templateName) {
    return admin.getSpaceInstFromTemplate(templateName);
  }

  /** Return all the spaces Id available in webactiv */
  public String[] getAllRootSpaceIds() {

    try {
      return admin.getAllRootSpaceIds();
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getAllSpaceIds", "admin.MSG_ERR_GET_ALL_SPACE_IDS",
          e);
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }
  }

  /** Return all the spaces Id available in webactiv */
  public String[] getAllSpaceIds() {

    try {
      return admin.getAllSpaceIds();
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getAllSpaceIds",
          "admin.MSG_ERR_GET_ALL_SPACE_IDS", e);
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }
  }

  /** Return all the spaces Id available for the given userId */
  public String[] getAllSpaceIds(String userId) {

    try {
      return admin.getAllSpaceIds(userId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getAllSpaceIds",
          "admin.MSG_ERR_GET_ALL_SPACE_IDS", e);
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }
  }

  /**
* Return all the sub spaces Id available in webactiv given the fatherDomainId
*/
  public String[] getAllSubSpaceIds(String sDomainFatherId) {

    try {
      return admin.getAllSubSpaceIds(sDomainFatherId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getAllSubSpaceIds",
          "admin.MSG_ERR_GET_SUBSPACE_IDS", e);
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }
  }

  /**
* Return all the sub spaces Id available for the given user and the given fatherDomainId
*/
  public String[] getAllSubSpaceIds(String sDomainFatherId, String userId) {
    try {
      return admin.getAllSubSpaceIds(sDomainFatherId, userId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getAllSubSpaceIds",
          "admin.MSG_ERR_GET_SUBSPACE_IDS", e);
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }
  }

  /** Return the the spaces name corresponding to the given space ids */
  public String[] getSpaceNames(String[] asSpaceIds) {

    try {
      return admin.getSpaceNames(asSpaceIds);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getSpaceNames",
          "admin.MSG_ERR_GET_SPACE_NAMES", e);
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }
  }

  public void updateSpaceOrderNum(String sSpaceId, int orderNum) {

    try {
      admin.updateSpaceOrderNum(sSpaceId, orderNum);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.updateSpaceOrderNum",
          "admin.MSG_ERR_UPDATE_SPACE", e);
    }
  }

  public void indexSpace(int spaceId) {
    admin.createSpaceIndex(spaceId);
  }

  /** Move space in the given space with the given fatherId */
  public void moveSpace(String spaceId, String fatherId) throws AdminException {

    admin.moveSpace(spaceId, fatherId);
  }

  // ----------------------------------------------

  /** Return all the components of silverpeas read in the xmlComponent directory */
  public Map<String, WAComponent> getAllComponents() {

    try {
      return admin.getAllComponents();
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getAllComponents",
          "admin.MSG_ERR_GET_ALL_COMPONENTS", e);
      return new HashMap<String, WAComponent>();
    }
  }

  /** Return the component Instance corresponding to the given component id */
  public ComponentInst getComponentInst(String sComponentId) {

    try {
      return admin.getComponentInst(sComponentId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getComponentInst",
          "admin.MSG_ERR_GET_COMPONENT", e);
      return null;
    }
  }

  public ComponentInstLight getComponentInstLight(String sComponentId) {

    try {
      return admin.getComponentInstLight(sComponentId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getComponentInstLight",
          "admin.MSG_ERR_GET_COMPONENT", e);
      return null;
    }
  }

  /** Add the given component Instance */
  public String addComponentInst(ComponentInst componentInst) throws QuotaException {

    Exception exceptionCatched = null;
    try {
      return admin.addComponentInst(componentInst.getCreatorUserId(), componentInst);
    } catch (QuotaException e) {
      exceptionCatched = e;
      throw e;
    } catch (Exception e) {
      exceptionCatched = e;
      return "";
    } finally {
      if (exceptionCatched != null) {
        SilverTrace.error("admin", "AdminController.addComponentInst",
            "admin.MSG_ERR_ADD_COMPONENT", exceptionCatched);
      }
    }
  }

  /**
* @param componentInst The component instance to add.
* @param userId The id of the user who becomes the instance's creator.
* @return The id of the new component instance.
*/
  public String addComponentInst(ComponentInst componentInst, String userId) throws QuotaException {

    try {
      return admin.addComponentInst(userId, componentInst);
    } catch (QuotaException e) {
      throw e;
    } catch (Exception e) {
      SilverTrace.error(
        "admin", "AdminController.addComponentInst", "admin.MSG_ERR_ADD_COMPONENT", e);
      return "";
    }
  }

  /** Delete the component Instance corresponding to the given component id */
  public String deleteComponentInst(UserDetail user, String sComponentId, boolean definitive) {

    try {
      return admin.deleteComponentInst(user.getId(), sComponentId, definitive);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.deleteComponentInst",
          "admin.MSG_ERR_DELETE_COMPONENT", e);
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
      SilverTrace.error("admin", "AdminController.updateComponentInst",
          "admin.MSG_ERR_UPDATE_COMPONENT", e);
      return "";
    }
  }

  /** Move the component Instance in the given space with the given componentId */
  public void moveComponentInst(String spaceId, String componentId,
      String idComponentBefore, ComponentInst[] componentInsts)
      throws AdminException {

    admin.moveComponentInst(spaceId, componentId, idComponentBefore, componentInsts);
  }

  /**
* Return the component ids available for the cuurent user Id in the given space id
*/
  public String[] getAvailCompoIds(String sClientSpaceId, String sUserId) {

    try {
      return admin.getAvailCompoIds(sClientSpaceId, sUserId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getAvailCompoIds",
          "admin.MSG_ERR_GET_USER_AVAILABLE_COMPONENT_IDS", e);
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }
  }

  public boolean isComponentAvailable(String componentId, String userId) {

    try {
      return admin.isComponentAvailable(componentId, userId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.isComponentAvailable",
          "admin.MSG_ERR_GET_USER_AVAILABLE_COMPONENT", e);
      return false;
    }
  }

  /**
* Indcates if a user can access the specified space.
* @param userId the user id.
* @param spaceId the space id.
* @return true if the space is accessible - false otherwise.
*/
  public boolean isSpaceAvailable(String userId, String spaceId) {

    try {
      return admin.isSpaceAvailable(userId, spaceId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.isSpaceAvailable",
          "admin.MSG_ERR_GET_USER_AVAILABLE_COMPONENT", e);
      return false;
    }
  }

  public void updateComponentOrderNum(String sComponentId, int orderNum) {

    try {
      admin.updateComponentOrderNum(sComponentId, orderNum);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.updateComponentOrderNum",
          "admin.MSG_ERR_UPDATE_COMPONENT", e);
    }
  }

  public void indexComponent(String componentId) {
    admin.createComponentIndex(componentId);
  }

  // ----------------------------------------------
  // Space and Component Bin
  // ----------------------------------------------
  public List<SpaceInstLight> getRemovedSpaces() {
    try {
      return admin.getRemovedSpaces();
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getRemovedSpaces",
          "admin.MSG_ERR_GET_REMOVED_SPACES", e);
      return null;
    }
  }

  public List<ComponentInstLight> getRemovedComponents() {
    try {
      return admin.getRemovedComponents();
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getRemovedComponents",
          "admin.MSG_ERR_GET_REMOVED_COMPONENTS", e);
      return null;
    }
  }

  public void restoreSpaceFromBasket(String spaceId) {
    try {
      admin.restoreSpaceFromBasket(spaceId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.restoreSpaceFromBasket",
          "admin.MSG_ERR_GET_RESTORE_SPACE_FROM_BASKET", e);
    }
  }

  public void restoreComponentFromBasket(String componentId) {
    try {
      admin.restoreComponentFromBasket(componentId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.restoreComponentFromBasket",
          "admin.MSG_ERR_GET_RESTORE_COMPONENT_FROM_BASKET", e);
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
      SilverTrace.error("admin", "AdminController.getAllProfilesNames",
          "admin.MSG_ERR_GET_ALL_PROFILE_NAMES", e);
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }
  }

  /** Return the profile Instance corresponding to the given profile id */
  public ProfileInst getProfileInst(String sProfileId) {

    try {
      return admin.getProfileInst(sProfileId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getProfileInst",
          "admin.MSG_ERR_GET_PROFILE", e);
      return null;
    }
  }

  public List<ProfileInst> getProfilesByObject(String objectId, String objectType,
      String componentId) {

    try {
      return admin.getProfilesByObject(objectId, objectType, componentId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getProfilesByObject",
          "admin.MSG_ERR_GET_PROFILE", e);
      return null;
    }
  }

  public String[] getProfilesByObjectAndUserId(int objectId, String objectType,
      String componentId, String userId) {

    try {
      return admin.getProfilesByObjectAndUserId(objectId, objectType, componentId, userId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.isObjectAvailable",
          "admin.MSG_ERR_GET_PROFILE", e);
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }
  }

  public boolean isObjectAvailable(int objectId, String objectType,
      String componentId, String userId) {

    try {
      return admin.isObjectAvailable(componentId, objectId, objectType, userId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.isObjectAvailable",
          "admin.MSG_ERR_GET_PROFILE", e);
      return false;
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
      SilverTrace.error("admin", "AdminController.addProfileInst",
          "admin.MSG_ERR_ADD_PROFILE", e);
      return "";
    }
  }

  /**
* Delete the Profile Instance corresponding to the given Profile id.
* @param sProfileId
* @return
*/
  public String deleteProfileInst(String sProfileId) {
    return deleteProfileInst(sProfileId, null);
  }

  public String deleteProfileInst(String sProfileId, String userId) {

    try {
      return admin.deleteProfileInst(sProfileId, userId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.deleteProfileInst",
          "admin.MSG_ERR_DELETE_PROFILE", e);
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
      SilverTrace.error("admin", "AdminController.updateProfileInst",
          "admin.MSG_ERR_UPDATE_PROFILE", e);
      return "";
    }
  }

  /**
* Get the profile label from its name
*/
  public String getProfileLabelfromName(String sComponentName, String sProfileName, String lang) {

    try {
      return admin.getProfileLabelfromName(sComponentName, sProfileName, lang);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getProfileLabelfromName",
          "admin.MSG_ERR_GET_PROFILE_LABEL_FROM_NAME", "component name: "
          + sComponentName + ", profile name: " + sProfileName, e);
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
      SilverTrace.error("admin", "AdminController.getProfileIds",
          "admin.MSG_ERR_GET_USERPROFILE", e);
      return null;
    }
  }

  // ----------------------------------------------
  // Group Profile related functions
  // ----------------------------------------------
  /**
* All the profiles to which the group belongs
* @return an array of profile IDs
*/
  public String[] getProfileIdsOfGroup(String sGroupId) {

    try {
      return admin.getProfileIdsOfGroup(sGroupId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getProfileIdsOfGroup",
          "admin.MSG_ERR_GET_USERPROFILE", e);
      return null;
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
    SilverTrace
        .info("admin", "AdminController.assignRightsFromUserToUser", "root.MSG_GEN_ENTER_METHOD");
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
    SilverTrace
        .info("admin", "AdminController.assignRightsFromUserToGroup", "root.MSG_GEN_ENTER_METHOD");
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
    SilverTrace
        .info("admin", "AdminController.assignRightsFromGroupToUser", "root.MSG_GEN_ENTER_METHOD");
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
    SilverTrace
        .info("admin", "AdminController.assignRightsFromGroupToGroup", "root.MSG_GEN_ENTER_METHOD");
    admin
        .assignRightsFromGroupToGroup(operationMode, sourceGroupId, targetGroupId, nodeAssignRights,
            authorId);
  }

  // ----------------------------------------------
  // User related functions
  // ----------------------------------------------
  public String[] getDirectGroupsIdsOfUser(String userId) {

    try {
      return admin.getDirectGroupsIdsOfUser(userId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getDirectGroupsIdsOfUser",
          "admin.MSG_ERR_GET_DOMAIN", "user id: " + userId, e);
      return null;
    }
  }

  /**
* Add a new domain
*/
  public String addDomain(Domain theDomain) {

    try {
      return admin.addDomain(theDomain);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.addDomain",
          "admin.MSG_ERR_ADD_DOMAIN", e);
      return "";
    }
  }

  /**
* update a domain
*/
  public String updateDomain(Domain theDomain) {

    try {
      return admin.updateDomain(theDomain);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.updateDomain",
          "admin.EX_ERR_UPDATE_DOMAIN", e);
      return "";
    }
  }

  /**
* Remove a domain
*/
  public String removeDomain(String domainId) {

    try {
      return admin.removeDomain(domainId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.removeDomain",
          "admin.MSG_ERR_DELETE_DOMAIN", e);
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
      SilverTrace.error("admin", "AdminController.getDomain",
          "admin.MSG_ERR_GET_DOMAIN", "domain id: " + domainId, e);
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
      SilverTrace.error("admin", "AdminController.getDomainActions",
          "admin.MSG_ERR_GET_ALL_DOMAINS", e);
      return 0;
    }
  }

  /**
* Get ALL the domain's groups
*/
  public Group[] getRootGroupsOfDomain(String domainId) {

    try {
      return admin.getRootGroupsOfDomain(domainId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getRootGroupsOfDomain",
          "admin.MSG_ERR_GET_ALL_DOMAINS", e);
      return new Group[0];
    }
  }

  /**
* Get ALL Group Ids for the domain's groups
*/
  public String[] getRootGroupIdsOfDomain(String domainId) {

    try {
      return admin.getRootGroupIdsOfDomain(domainId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getRootGroupIdsOfDomain",
          "admin.MSG_ERR_GET_ALL_DOMAINS", e);
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }
  }

  /**
* Get ALL the users that are in a group or his sub groups
*/
  public UserDetail[] getAllUsersOfGroup(String groupId) {

    try {
      return admin.getAllUsersOfGroup(groupId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getAllUsersOfGroup",
          "admin.MSG_ERR_GET_ALL_DOMAINS", e);
      return ArrayUtil.EMPTY_USER_DETAIL_ARRAY;
    }
  }

  /**
* Get ALL the domain's users
*/
  public UserDetail[] getUsersOfDomain(String domainId) {

    try {
      return admin.getUsersOfDomain(domainId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getUsersOfDomain",
          "admin.MSG_ERR_GET_ALL_DOMAINS", e);
      return ArrayUtil.EMPTY_USER_DETAIL_ARRAY;
    }
  }

  /**
* Get ALL the userId of the domain
*/
  public String[] getUserIdsOfDomain(String domainId) {

    try {
      return admin.getUserIdsOfDomain(domainId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getUserIdsOfDomain",
          "admin.MSG_ERR_GET_ALL_DOMAINS", e);
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }
  }

  /**
* Get number of the domain's users
*/
  public int getUsersNumberOfDomain(String domainId) {

    try {
      return admin.getUsersNumberOfDomain(domainId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getUsersNumberOfDomain",
          "admin.MSG_ERR_GET_ALL_DOMAINS", e);
      return 0;
    }
  }

  /**
* Get all domains declared in Silverpeas
*/
  public Domain[] getAllDomains() {

    try {
      return admin.getAllDomains();
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getAllDomains",
          "admin.MSG_ERR_GET_ALL_DOMAINS", e);
      return null;
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
      SilverTrace.error("admin", "AdminController.getSpaceProfileInst",
       "admin.MSG_ERR_GET_SPACE_PROFILE", e);
        return null;
    }
  }

  /** Add the given Space Profile Instance */
  public String addSpaceProfileInst(SpaceProfileInst spaceProfileInst,
      String userId) {

    try {
      return admin.addSpaceProfileInst(spaceProfileInst, userId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.addSpaceProfileInst",
          "admin.MSG_ERR_ADD_SPACE_PROFILE", e);
      return "";
    }
  }

  /**
* Delete the Space Profile Instance corresponding to the given Space Profile id
*/
  public String deleteSpaceProfileInst(String sSpaceProfileId, String userId) {

    try {
      return admin.deleteSpaceProfileInst(sSpaceProfileId, userId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.deleteSpaceProfileInst",
          "admin.MSG_ERR_DELETE_SPACE_PROFILE", e);
      return "";
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
      SilverTrace.error("admin", "AdminController.updateSpaceProfileInst",
          "admin.MSG_ERR_UPDATE_SPACE_PROFILE", e);
      return "";
    }
  }

  // ----------------------------------------------
  // Groups related functions
  // ----------------------------------------------
  /** Return all the groups ids available in webactiv */
  public String[] getAllGroupsIds() {

    try {
      return admin.getAllGroupIds();
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getAllGroupIds",
          "admin.MSG_ERR_GET_ALL_GROUP_IDS", e);
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }
  }

  /**
* @return all the root groups ids available
*/
  public String[] getAllRootGroupIds() {

    try {
      return admin.getAllRootGroupIds();
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getAllRootGroupsIds",
          "admin.MSG_ERR_GET_ALL_GROUP_IDS", e);
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }
  }

  /**
* @return all the direct subgroups ids of a given group
*/
  public String[] getAllSubGroupIds(String groupId) {

    try {
      return admin.getAllSubGroupIds(groupId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getDirectSubgroupIds",
          "admin.MSG_ERR_GET_ALL_GROUP_IDS", e);
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }
  }

  /**
* @return all subgroups ids of a given group
*/
  public String[] getAllSubGroupIdsRecursively(String groupId) {

    try {
      return admin.getAllSubGroupIdsRecursively(groupId);
    } catch (Exception e) {
      SilverTrace.error("admin",
          "AdminController.getAllSubGroupIdsRecursively",
          "admin.MSG_ERR_GET_ALL_GROUP_IDS", e);
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }
  }

  /** Return all the group names corresponding to the given group Ids */
  public String[] getGroupNames(String[] asGroupIds) {

    try {
      return admin.getGroupNames(asGroupIds);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getGroupNames",
          "admin.MSG_ERR_GET_GROUP_NAMES", e);
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }
  }

  /** Return the group name corresponding to the given group Id */
  public String getGroupName(String sGroupId) {

    try {
      return admin.getGroupName(sGroupId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getGroupName",
          "admin.MSG_ERR_GET_GROUP_NAME", "group id: " + sGroupId, e);
      return "";
    }
  }

  /** Return all the user ids available in webactiv */
  public String[] getAllUsersIds() {

    try {
      return admin.getAllUsersIds();
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getAllUsersIds",
          "admin.MSG_ERR_GET_ALL_USER_IDS", e);
      return null;
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
      SilverTrace.error("admin", "AdminController.getGroupManageableSpaceIds",
          "admin.MSG_ERR_GET_SPACE", e);
      return null;
    }
  }

  /** Return the group profile */
  public GroupProfileInst getGroupProfile(String groupId) {

    try {
      return admin.getGroupProfileInst(groupId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getGroupProfile",
          "admin.MSG_ERR_GET_GROUP_PROFILE", e);
      return null;
    }
  }

  /** Delete the Group Profile */
  public String deleteGroupProfile(String groupId) {

    try {
      return admin.deleteGroupProfileInst(groupId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.deleteGroupProfile",
          "admin.MSG_ERR_DELETE_GROUP_PROFILE", e);
      return "";
    }
  }

  /** Update the Group Profile */
  public String updateGroupProfile(GroupProfileInst profile) {

    try {
      return admin.updateGroupProfileInst(profile);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.updateGroupProfile",
          "admin.MSG_ERR_UPDATE_GROUP_PROFILE", e);
      return "";
    }
  }

  // ----------------------------------------------
  // General Admin ID related functions
  // ----------------------------------------------
  /** Return the general admin id */
  public String getDAPIGeneralAdminId() {

    try {
      return admin.getDAPIGeneralAdminId();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return null;
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
      SilverTrace.warn("admin", "AdminController.getUserDetail",
          "admin.EX_ERR_GET_USER_DETAIL", "user id: " + sId, e);
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
      SilverTrace.error("admin", "AdminController.getUserFull", "admin.EX_ERR_GET_USER_DETAIL",
          "user Id : '" + sUserId + "'", e);
      return null;
    }
  }

  public UserFull getUserFull(String domainId, String specificId) {

    try {
      return admin.getUserFull(domainId, specificId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getUserFull", "admin.EX_ERR_GET_USER_DETAIL",
          "specificId = " + specificId, e);
      return null;
    }
  }

  public String getUserIdByLoginAndDomain(String sLogin, String sDomainId) {

    try {
      return admin.getUserIdByLoginAndDomain(sLogin, sDomainId);
    } catch (Exception e) {
      SilverTrace.warn("admin", "AdminController.getUserIdByLoginAndDomain",
          "admin.EX_ERR_GET_USER_DETAIL", "sLogin : '" + sLogin + "' Domain = " + sDomainId, e);
      return null;
    }
  }

  /** Return an array of UserDetail corresponding to the given user Id array */
  public UserDetail[] getUserDetails(String[] asUserIds) {

    try {
      if (asUserIds != null) {
        return admin.getUserDetails(asUserIds);
      }
      return ArrayUtil.EMPTY_USER_DETAIL_ARRAY;
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getUserDetails", "admin.EX_ERR_GET_USER_DETAILS",
          e);
      return null;
    }
  }

  /** Add the given user */
  public String addUser(UserDetail userDetail) {

    try {
      return admin.addUser(userDetail);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.addUser", "admin.EX_ERR_ADD_USER", e);
      return "";
    }
  }

  /**
   * Updates the acceptance date of a user from its id.
   */
  public void userAcceptsTermsOfService(String userId) {
    SilverTrace
        .info("admin", "AdminController.userAcceptsTermsOfService", "root.MSG_GEN_ENTER_METHOD");
    try {
      admin.userAcceptsTermsOfService(userId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.userAcceptsTermsOfService",
          "admin.EX_ERR_UPDATE_USER_TOS_ACCEPTANCE_DATE", e);
    }
  }

  /** Block the given user */
  public void blockUser(String userId) {

    try {
      admin.blockUser(userId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.blockUser", "admin.EX_ERR_BLOCK_USER", e);
    }
  }

  /** Unblock the given user */
  public void unblockUser(String userId) {

    try {
      admin.unblockUser(userId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.unblockUser", "admin.EX_ERR_UNBLOCK_USER", e);
    }
  }

  /**
   * Deactivate the given user
   */
  public void deactivateUser(String userId) {

    try {
      admin.deactivateUser(userId);
    } catch (Exception e) {
      SilverTrace
          .error("admin", "AdminController.deactivateUser", "admin.EX_ERR_DEACTIVATE_USER", e);
    }
  }

  /**
   * Activate the given user
   */
  public void activateUser(String userId) {

    try {
      admin.activateUser(userId);
    } catch (Exception e) {
      SilverTrace
          .error("admin", "AdminController.activateUser", "admin.EX_ERR_UNDEACTIVATE_USER", e);
    }
  }

  /** Delete the given user */
  public String deleteUser(String sUserId) {

    try {
      return admin.deleteUser(sUserId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.deleteUser",
          "admin.EX_ERR_DELETE_USER", e);
      return "";
    }
  }

  /** Update the given user */
  public String updateUser(UserDetail userDetail) {

    try {
      return admin.updateUser(userDetail);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.updateUser",
          "admin.EX_ERR_UPDATE_USER", e);
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
      SilverTrace.error("admin", "AdminController.updateSynchronizedUser",
          "admin.EX_ERR_UPDATE_USER", e);
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

  public void indexUsers(String domainId) {
    try {
      admin.indexUsers(domainId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.indexUsers",
          "admin.CANT_INDEX_USERS", "domainId = " + domainId, e);
    }
  }

  public void indexAllUsers() {
    try {
      admin.indexAllUsers();
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.indexAllUsers",
          "admin.CANT_INDEX_ALL_USERS", e);
    }
  }

  // ----------------------------------------------
  // Admin Group Detail related functions
  // ----------------------------------------------
  /** Return all the groups Id available in webactiv */
  public String[] getAllGroupIds() {

    try {
      return admin.getAllGroupIds();
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getAllGroupIds",
          "admin.EX_ERR_GET_ALL_GROUP_IDS", e);
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }
  }

  /** Return true if the group with the given name */
  public boolean isGroupExist(String sName) {

    try {
      return admin.isGroupExist(sName);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.isGroupExist",
          "admin.EX_ERR_IS_GROUP_EXIST", e);
      return false;
    }
  }

  /** Return the admin group detail corresponding to the given id */
  public Group getGroupById(String sGroupId) {

    try {
      return admin.getGroup(sGroupId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getGroupById",
          "admin.EX_ERR_GET_GROUP", e);
      return null;
    }
  }

  /** Return the groupIds from root to group */
  public List<String> getPathToGroup(String groupId) {

    try {
      return admin.getPathToGroup(groupId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getPathToGroup",
          "admin.EX_ERR_GET_GROUP", e);
      return null;
    }
  }

  /** Return the admin group detail corresponding to the given group Name */
  public Group getGroupByNameInDomain(String sGroupName, String sDomainFatherId) {

    try {
      return admin.getGroupByNameInDomain(sGroupName, sDomainFatherId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getGroupByNameInDomain",
          "admin.EX_ERR_GET_GROUP", e);
      return null;
    }
  }

  /** Add the given group */
  public String addGroup(Group group) {

    try {
      return admin.addGroup(group);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.addGroup",
          "admin.EX_ERR_ADD_GROUP", e);
      return "";
    }
  }

  /** Delete the given group */
  public String deleteGroupById(String sGroupId) {

    try {
      return admin.deleteGroupById(sGroupId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.deleteGroupById",
          "admin.EX_ERR_DELETE_GROUP", e);
      return "";
    }
  }

  /** Update the given group */
  public String updateGroup(Group group) {

    try {
      return admin.updateGroup(group);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.updateGroup",
          "admin.EX_ERR_UPDATE_GROUP", e);
      return "";
    }
  }

  public AdminGroupInst[] getAdminOrganization() {

    try {
      return admin.getAdminOrganization();
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getAdminOrganization",
          "admin.EX_ERR_GET_ADMIN_ORGANIZATION", e);
      return null;
    }
  }

  public void indexGroups(String domainId) {
    try {
      admin.indexGroups(domainId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.indexGroups",
          "admin.CANT_INDEX_GROUPS", "domainId = " + domainId, e);
    }
  }

  public void indexAllGroups() {
    try {
      admin.indexAllGroups();
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.indexAllGroups",
          "admin.CANT_INDEX_ALL_USERS", e);
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
      SilverTrace.error("admin",
          "AdminController.synchronizeSilverpeasWithDomain",
          "admin.MSG_ERR_SYNCHRONIZE_DOMAIN", e);
      return "Error has occurred";
    }
  }

  public String synchronizeUser(String userId) {

    try {
      return admin.synchronizeUser(userId, true);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.synchronizeUser",
          "admin.MSG_ERR_SYNCHRONIZE_USER", e);
      return "";
    }
  }

  public String synchronizeImportUser(String domainId, String userLogin) {

    try {
      return admin.synchronizeImportUser(domainId, userLogin, true);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.synchronizeImportUser",
          "admin.MSG_ERR_SYNCHRONIZE_USER", e);
      return "";
    }
  }

  public List<DomainProperty> getSpecificPropertiesToImportUsers(String domainId,
      String language) {
    try {
      return admin.getSpecificPropertiesToImportUsers(domainId, language);
    } catch (Exception e) {
      SilverTrace.error("admin",
          "AdminController.getSpecificPropertiesToImportUsers",
          "admin.MSG_ERR_SYNCHRONIZE_USER", e);
      return null;
    }
  }

  public List<UserDetail> searchUsers(String domainId, Map<String, String> query) {

    try {
      return Arrays.asList(admin.searchUsers(domainId, query));
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.searchUsers",
          "admin.MSG_ERR_SYNCHRONIZE_USER", e);
      return new ArrayList<UserDetail>();
    }
  }

  public String synchronizeRemoveUser(String userId) {

    try {
      return admin.synchronizeRemoveUser(userId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.synchronizeRemoveUser",
          "admin.MSG_ERR_SYNCHRONIZE_USER", e);
      return "";
    }
  }

  public String synchronizeGroup(String groupId) {

    try {
      return admin.synchronizeGroup(groupId, true);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.synchronizeGroup",
          "admin.MSG_ERR_SYNCHRONIZE_GROUP", e);
      return "";
    }
  }

  public String synchronizeImportGroup(String domainId, String groupName) {

    try {
      return admin.synchronizeImportGroup(domainId, groupName, null, true, false);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.synchronizeImportGroup",
          "admin.MSG_ERR_SYNCHRONIZE_GROUP", e);
      return "";
    }
  }

  public String synchronizeRemoveGroup(String groupId) {

    try {
      return admin.synchronizeRemoveGroup(groupId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.synchronizeRemoveGroup",
          "admin.MSG_ERR_SYNCHRONIZE_GROUP", e);
      return "";
    }
  }

  public void resetAllDBConnections(boolean isScheduled) {
    //
    try {
      admin.resetAllDBConnections(isScheduled);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.resetAllDBConnections",
          "admin.MSG_ERR_SYNCHRONIZE_GROUP", e);
    }
  }

  /** Removes the given user from the given group */
  public void removeUserFromGroup(String sUserId, String sGroupId) {

    try {
      admin.removeUserFromGroup(sUserId, sGroupId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.removeUserFromGroup",
          "admin.EX_ERR_REMOVE_USER_FROM_GROUP", e);
    }
  }

  /** Removes the given user from the given group */
  public void addUserInGroup(String sUserId, String sGroupId) {

    try {
      admin.addUserInGroup(sUserId, sGroupId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.addUserInGroup",
          "admin.EX_ERR_ADD_USER_IN_GROUP", e);
    }
  }

  public void reloadAdminCache() {
    admin.reloadCache();
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
      SilverTrace.error("admin", "AdminController.isDomainManagerUser",
          "Error inside admin service", e);
    }
    return false;
  }

}