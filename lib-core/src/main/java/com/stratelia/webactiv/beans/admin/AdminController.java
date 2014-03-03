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
package com.stratelia.webactiv.beans.admin;

import com.silverpeas.admin.components.PasteDetail;
import com.silverpeas.admin.components.WAComponent;
import com.silverpeas.admin.spaces.SpaceTemplate;
import com.silverpeas.util.ArrayUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.quota.exception.QuotaException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.stratelia.webactiv.beans.admin.AdminReference.getAdminService;
/*
This objet is used by all the admin jsp such as SpaceManagement, UserManagement, etc...
It provides access functions to query and modify the domains as well as the company organization
It should be used only by a client that has the administrator rights
*/
public class AdminController implements java.io.Serializable {

  private static final long serialVersionUID = -1605341557688427460L;
  String m_UserId = null;

  public AdminController(String sUserId) {
    m_UserId = sUserId;
  }

  // Start the processes
  public void startServer() throws Exception {
    getAdminService().startServer();
  }

  // ----------------------------------------------
  // Space Instances related functions
  // ----------------------------------------------
  public String getGeneralSpaceId() {
    SilverTrace.info("admin", "AdminController.getGeneralSpaceId",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getGeneralSpaceId();
    } catch (Exception e) {
      SilverTrace.fatal("admin", "AdminController.getGeneralSpaceId",
          "admin.MSG_FATAL_GET_GENERAL_SPACE_ID", e);
      return "";
    }
  }

  /* Return true if the given space name exists */
  public boolean isSpaceInstExist(String sClientSpaceId) {
    SilverTrace.info("admin", "AdminController.isSpaceInstExist",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().isSpaceInstExist(sClientSpaceId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.isSpaceInstExist",
          "admin.MSG_ERR_IS_SPACE_EXIST", e);
      return false;
    }
  }

  /** Return the space Instance corresponding to the given space id */
  public SpaceInst getSpaceInstById(String sSpaceId) {
    SilverTrace.info("admin", "AdminController.getSpaceInstById",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getSpaceInstById(sSpaceId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getSpaceInstById",
          "admin.MSG_ERR_GET_SPACE", e);
      return null;
    }
  }

  public SpaceInstLight getSpaceInstLight(String sSpaceId) {
    SilverTrace.info("admin", "AdminController.getSpaceInstLight",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getSpaceInstLightById(sSpaceId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getSpaceInstLight",
          "admin.MSG_ERR_GET_SPACE", e);
      return null;
    }
  }

  public Map<String, SpaceAndChildren> getTreeView(String userId, String spaceId) {
    SilverTrace.info("admin", "AdminController.getTreeView",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getTreeView(userId, spaceId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getTreeView",
          "admin.MSG_ERR_GET_SPACE", e);
      return null;
    }
  }

  public List<SpaceInstLight> getPathToComponent(String componentId) {
    SilverTrace.info("admin", "AdminController.getPathToComponent",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getPathToComponent(componentId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getPathToComponent",
          "admin.MSG_ERR_GET_SPACE", e);
      return null;
    }
  }

  public List<SpaceInstLight> getPathToSpace(String spaceId, boolean includeTarget) {
    SilverTrace.info("admin", "AdminController.getPathToSpace",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getPathToSpace(spaceId, includeTarget);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getPathToSpace",
          "admin.MSG_ERR_GET_SPACE", e);
      return null;
    }
  }

  /** Return the space Instance corresponding to the given space id */
  public String[] getUserManageableSpaceRootIds(String sUserId) {
    SilverTrace.info("admin", "AdminController.getUserManageableSpaceRootIds",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getUserManageableSpaceRootIds(sUserId);
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
    SilverTrace.info("admin", "AdminController.getUserManageableSubSpaceIds",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getUserManageableSubSpaceIds(sUserId, sParentSpace);
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
    SilverTrace.info("admin", "AdminController.getUserManageableSpaceIds",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getUserManageableSpaceIds(sUserId);
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
    SilverTrace.info("admin", "AdminController.getUserManageableSpaceIds",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      UserDetail user = getAdminService().getUserDetail(sUserId);
      if (user.isAccessAdmin() || sUserId.equals("0")) {
        return getAdminService().getClientSpaceIds(getAdminService().getAllSpaceIds());
      } else {
        return getAdminService().getClientSpaceIds(getAdminService().getUserManageableSpaceIds(sUserId));
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
    SilverTrace.info("admin", "AdminController.addSpaceInst",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().addSpaceInst(m_UserId, spaceInst);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.addSpaceInst", "admin.MSG_ERR_ADD_SPACE", e);
      return "";
    }
  }

  /** Delete the space Instance corresponding to the given space id */
  public String deleteSpaceInstById(String sSpaceInstId, boolean definitive) {
    SilverTrace.info("admin", "AdminController.deleteSpaceInstById",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().deleteSpaceInstById(m_UserId, sSpaceInstId, definitive);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.deleteSpaceInstById", "admin.MSG_ERR_DELETE_SPACE", e);
      return "";
    }
  }

  /**
* Update the space Instance corresponding to the given space name wuth the given SpaceInst
*/
  public String updateSpaceInst(SpaceInst spaceInstNew) {
    SilverTrace.info("admin", "AdminController.updateSpaceInst", "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().updateSpaceInst(spaceInstNew);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.updateSpaceInst", "admin.MSG_ERR_UPDATE_SPACE", e);
      return "";
    }
  }

  public Map<String, SpaceTemplate> getAllSpaceTemplates() {
    return getAdminService().getAllSpaceTemplates();
  }

  public SpaceInst getSpaceInstFromTemplate(String templateName) {
    return getAdminService().getSpaceInstFromTemplate(templateName);
  }

  /** Return all the spaces Id available in webactiv */
  public String[] getAllRootSpaceIds() {
    SilverTrace.info("admin", "AdminController.getAllSpaceIds", "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getAllRootSpaceIds();
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getAllSpaceIds", "admin.MSG_ERR_GET_ALL_SPACE_IDS",
          e);
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }
  }

  /** Return all the spaces Id available in webactiv */
  public String[] getAllSpaceIds() {
    SilverTrace.info("admin", "AdminController.getAllSpaceIds",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getAllSpaceIds();
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getAllSpaceIds",
          "admin.MSG_ERR_GET_ALL_SPACE_IDS", e);
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }
  }

  /** Return all the spaces Id available for the given userId */
  public String[] getAllSpaceIds(String userId) {
    SilverTrace.info("admin", "AdminController.getAllSpaceIds",
        "root.MSG_GEN_ENTER_METHOD", "userId = " + userId);
    try {
      return getAdminService().getAllSpaceIds(userId);
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
    SilverTrace.info("admin", "AdminController.getAllSubSpaceIds",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getAllSubSpaceIds(sDomainFatherId);
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
    SilverTrace.info("admin", "AdminController.getAllSubSpaceIds",
        "root.MSG_GEN_ENTER_METHOD", "sDomainFatherId = " + sDomainFatherId
        + ", userId = " + userId);
    try {
      return getAdminService().getAllSubSpaceIds(sDomainFatherId, userId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getAllSubSpaceIds",
          "admin.MSG_ERR_GET_SUBSPACE_IDS", e);
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }
  }

  /** Return the the spaces name corresponding to the given space ids */
  public String[] getSpaceNames(String[] asSpaceIds) {
    SilverTrace.info("admin", "AdminController.getSpaceNames",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getSpaceNames(asSpaceIds);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getSpaceNames",
          "admin.MSG_ERR_GET_SPACE_NAMES", e);
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }
  }

  public void updateSpaceOrderNum(String sSpaceId, int orderNum) {
    SilverTrace.info("admin", "AdminController.updateSpaceOrderNum",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      getAdminService().updateSpaceOrderNum(sSpaceId, orderNum);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.updateSpaceOrderNum",
          "admin.MSG_ERR_UPDATE_SPACE", e);
    }
  }

  public void indexSpace(int spaceId) {
    getAdminService().createSpaceIndex(spaceId);
  }

  /** Move space in the given space with the given fatherId */
  public void moveSpace(String spaceId, String fatherId) throws AdminException {
    SilverTrace.info("admin", "AdminController.moveSpace",
        "root.MSG_GEN_ENTER_METHOD", "moving "+spaceId+" in space "+fatherId);
    getAdminService().moveSpace(spaceId, fatherId);
  }

  // ----------------------------------------------
  // Component Instances related functions
  // ----------------------------------------------
  /** Return all the components names available in webactiv */
  public Map<String, String> getAllComponentsNames() {
    SilverTrace.info("admin", "AdminController.getAllComponentsNames",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getAllComponentsNames();
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getAllComponentsNames",
          "admin.MSG_ERR_GET_ALL_COMPONENT_NAMES", e);
      return new HashMap<String, String>();
    }
  }

  /** Return all the components of silverpeas read in the xmlComponent directory */
  public Map<String, WAComponent> getAllComponents() {
    SilverTrace.info("admin", "AdminController.getAllComponents",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getAllComponents();
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getAllComponents",
          "admin.MSG_ERR_GET_ALL_COMPONENTS", e);
      return new HashMap<String, WAComponent>();
    }
  }

  /** Return the component Instance corresponding to the given component id */
  public ComponentInst getComponentInst(String sComponentId) {
    SilverTrace.info("admin", "AdminController.getComponentInst",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getComponentInst(sComponentId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getComponentInst",
          "admin.MSG_ERR_GET_COMPONENT", e);
      return null;
    }
  }

  public ComponentInstLight getComponentInstLight(String sComponentId) {
    SilverTrace.info("admin", "AdminController.getComponentInstLight",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getComponentInstLight(sComponentId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getComponentInstLight",
          "admin.MSG_ERR_GET_COMPONENT", e);
      return null;
    }
  }

  /** Add the given component Instance */
  public String addComponentInst(ComponentInst componentInst) throws QuotaException {
    SilverTrace.info("admin", "AdminController.addComponentInst", "root.MSG_GEN_ENTER_METHOD");
    Exception exceptionCatched = null;
    try {
      return getAdminService().addComponentInst(m_UserId, componentInst);
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
    SilverTrace.info("admin", "AdminController.addComponentInst", "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().addComponentInst(userId, componentInst);
    } catch (QuotaException e) {
      throw e;
    } catch (Exception e) {
      SilverTrace.error(
        "admin", "AdminController.addComponentInst", "admin.MSG_ERR_ADD_COMPONENT", e);
      return "";
    }
  }

  /** Delete the component Instance corresponding to the given component id */
  public String deleteComponentInst(String sComponentId, boolean definitive) {
    SilverTrace.info("admin", "AdminController.deleteComponentInst",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().deleteComponentInst(m_UserId, sComponentId, definitive);
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
    SilverTrace.info("admin", "AdminController.updateComponentInst",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().updateComponentInst(componentInst);
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
    SilverTrace.info("admin", "AdminController.moveComponentInst",
        "root.MSG_GEN_ENTER_METHOD", "moving "+componentId+" in space "+spaceId);
    getAdminService().moveComponentInst(spaceId, componentId, idComponentBefore,
        componentInsts);
  }

  /**
* Return the component ids available for the cuurent user Id in the given space id
*/
  public String[] getAvailCompoIds(String sClientSpaceId, String sUserId) {
    SilverTrace.info("admin", "AdminController.getAvailCompoIds",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getAvailCompoIds(sClientSpaceId, sUserId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getAvailCompoIds",
          "admin.MSG_ERR_GET_USER_AVAILABLE_COMPONENT_IDS", e);
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }
  }

  public boolean isComponentAvailable(String componentId, String userId) {
    SilverTrace.info("admin", "AdminController.isComponentAvailable",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().isComponentAvailable(componentId, userId);
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
    SilverTrace.info("admin", "AdminController.isSpaceAvailable",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().isSpaceAvailable(userId, spaceId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.isSpaceAvailable",
          "admin.MSG_ERR_GET_USER_AVAILABLE_COMPONENT", e);
      return false;
    }
  }

  public void updateComponentOrderNum(String sComponentId, int orderNum) {
    SilverTrace.info("admin", "AdminController.updateComponentOrderNum",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      getAdminService().updateComponentOrderNum(sComponentId, orderNum);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.updateComponentOrderNum",
          "admin.MSG_ERR_UPDATE_COMPONENT", e);
    }
  }

  public void indexComponent(String componentId) {
    getAdminService().createComponentIndex(componentId);
  }

  // ----------------------------------------------
  // Space and Component Bin
  // ----------------------------------------------
  public List<SpaceInstLight> getRemovedSpaces() {
    try {
      return getAdminService().getRemovedSpaces();
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getRemovedSpaces",
          "admin.MSG_ERR_GET_REMOVED_SPACES", e);
      return null;
    }
  }

  public List<ComponentInstLight> getRemovedComponents() {
    try {
      return getAdminService().getRemovedComponents();
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getRemovedComponents",
          "admin.MSG_ERR_GET_REMOVED_COMPONENTS", e);
      return null;
    }
  }

  public void restoreSpaceFromBasket(String spaceId) {
    try {
      getAdminService().restoreSpaceFromBasket(spaceId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.restoreSpaceFromBasket",
          "admin.MSG_ERR_GET_RESTORE_SPACE_FROM_BASKET", e);
    }
  }

  public void restoreComponentFromBasket(String componentId) {
    try {
      getAdminService().restoreComponentFromBasket(componentId);
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
    SilverTrace.info("admin", "AdminController.getAllProfilesNames",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getAllProfilesNames(sComponentName);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getAllProfilesNames",
          "admin.MSG_ERR_GET_ALL_PROFILE_NAMES", e);
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }
  }

  /** Return the profile Instance corresponding to the given profile id */
  public ProfileInst getProfileInst(String sProfileId) {
    SilverTrace.info("admin", "AdminController.getProfileInst",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getProfileInst(sProfileId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getProfileInst",
          "admin.MSG_ERR_GET_PROFILE", e);
      return null;
    }
  }

  public List<ProfileInst> getProfilesByObject(String objectId, String objectType,
      String componentId) {
    SilverTrace.info("admin", "AdminController.getProfilesByObject",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getProfilesByObject(objectId, objectType, componentId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getProfilesByObject",
          "admin.MSG_ERR_GET_PROFILE", e);
      return null;
    }
  }

  public String[] getProfilesByObjectAndUserId(int objectId, String objectType,
      String componentId, String userId) {
    SilverTrace.info("admin", "AdminController.getProfilesByObjectAndUserId",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getProfilesByObjectAndUserId(objectId, objectType,
          componentId, userId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.isObjectAvailable",
          "admin.MSG_ERR_GET_PROFILE", e);
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }
  }

  public boolean isObjectAvailable(int objectId, String objectType,
      String componentId, String userId) {
    SilverTrace.info("admin", "AdminController.isObjectAvailable",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().isObjectAvailable(componentId, objectId, objectType,
          userId);
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
    SilverTrace.info("admin", "AdminController.addProfileInst",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().addProfileInst(profileInst, userId);
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
    SilverTrace.info("admin", "AdminController.deleteProfileInst",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().deleteProfileInst(sProfileId, userId);
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
    SilverTrace.info("admin", "AdminController.updateProfileInst",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().updateProfileInst(profileInst, userId);
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
    SilverTrace.info("admin", "AdminController.getProfileLabelfromName",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getProfileLabelfromName(sComponentName, sProfileName, lang);
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
  // JCC 10/04/2002 ajout de getProfileIds
  /**
* All the profiles to which the user belongs
* @return an array of profile IDs
*/
  public String[] getProfileIds(String sUserId) {
    SilverTrace.info("admin", "AdminController.getProfileIds",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getProfileIds(sUserId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getProfileIds",
          "admin.MSG_ERR_GET_USERPROFILE", e);
      return null;
    }
  }

  // ----------------------------------------------
  // Group Profile related functions
  // ----------------------------------------------
  // JCC 10/04/2002 ajout de getProfileIdsOfGroup
  /**
* All the profiles to which the group belongs
* @return an array of profile IDs
*/
  public String[] getProfileIdsOfGroup(String sGroupId) {
    SilverTrace.info("admin", "AdminController.getProfileIdsOfGroup",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getProfileIdsOfGroup(sGroupId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getProfileIdsOfGroup",
          "admin.MSG_ERR_GET_USERPROFILE", e);
      return null;
    }
  }

  // ----------------------------------------------
  // User related functions
  // ----------------------------------------------
  public String[] getDirectGroupsIdsOfUser(String userId) {
    SilverTrace.info("admin", "AdminController.getDirectGroupsIdsOfUser",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getDirectGroupsIdsOfUser(userId);
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
    SilverTrace.info("admin", "AdminController.addDomain",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().addDomain(theDomain);
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
    SilverTrace.info("admin", "AdminController.updateDomain",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().updateDomain(theDomain);
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
    SilverTrace.info("admin", "AdminController.removeDomain",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().removeDomain(domainId);
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
    SilverTrace.info("admin", "AdminController.getDomain",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getDomain(domainId);
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
    SilverTrace.info("admin", "AdminController.getDomainActions",
        "root.MSG_GEN_ENTER_METHOD", "domainID = " + domainId);
    try {
      return getAdminService().getDomainActions(domainId);
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
    SilverTrace.info("admin", "AdminController.getRootGroupsOfDomain",
        "root.MSG_GEN_ENTER_METHOD", "domainID = " + domainId);
    try {
      return getAdminService().getRootGroupsOfDomain(domainId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getRootGroupsOfDomain",
          "admin.MSG_ERR_GET_ALL_DOMAINS", e);
      return ArrayUtil.EMPTY_GROUP_ARRAY;
    }
  }

  /**
* Get ALL Group Ids for the domain's groups
*/
  public String[] getRootGroupIdsOfDomain(String domainId) {
    SilverTrace.info("admin", "AdminController.getRootGroupIdsOfDomain",
        "root.MSG_GEN_ENTER_METHOD", "domainID = " + domainId);
    try {
      return getAdminService().getRootGroupIdsOfDomain(domainId);
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
    SilverTrace.info("admin", "AdminController.getAllUsersOfGroup",
        "root.MSG_GEN_ENTER_METHOD", "groupId = " + groupId);
    try {
      return getAdminService().getAllUsersOfGroup(groupId);
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
    SilverTrace.info("admin", "AdminController.getUsersOfDomain",
        "root.MSG_GEN_ENTER_METHOD", "domainID = " + domainId);
    try {
      return getAdminService().getUsersOfDomain(domainId);
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
    SilverTrace.info("admin", "AdminController.getUserIdsOfDomain",
        "root.MSG_GEN_ENTER_METHOD", "domainID = " + domainId);
    try {
      return getAdminService().getUserIdsOfDomain(domainId);
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
    SilverTrace.info("admin", "AdminController.getUsersNumberOfDomain",
        "root.MSG_GEN_ENTER_METHOD", "domainID = " + domainId);
    try {
      return getAdminService().getUsersNumberOfDomain(domainId);
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
    SilverTrace.info("admin", "AdminController.getAllDomains",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getAllDomains();
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
    SilverTrace.info("admin", "AdminController.getSpaceProfileInst",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getSpaceProfileInst(sSpaceProfileId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getSpaceProfileInst",
          "admin.MSG_ERR_GET_SPACE_PROFILE", e);
      return null;
    }
  }

  /** Add the given Space Profile Instance */
  public String addSpaceProfileInst(SpaceProfileInst spaceProfileInst,
      String userId) {
    SilverTrace.info("admin", "AdminController.addSpaceProfileInst",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().addSpaceProfileInst(spaceProfileInst, userId);
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
    SilverTrace.info("admin", "AdminController.deleteSpaceProfileInst",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().deleteSpaceProfileInst(sSpaceProfileId, userId);
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
    SilverTrace.info("admin", "AdminController.updateSpaceProfileInst",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().updateSpaceProfileInst(spaceProfileInst, userId);
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
    SilverTrace.info("admin", "AdminController.getAllGroupsIds",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getAllGroupIds();
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
    SilverTrace.info("admin", "AdminController.getAllRootGroupsIds",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getAllRootGroupIds();
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
    SilverTrace.info("admin", "AdminController.getDirectSubgroupIds",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getAllSubGroupIds(groupId);
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
    SilverTrace.info("admin", "AdminController.getAllSubGroupIdsRecursively",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getAllSubGroupIdsRecursively(groupId);
    } catch (Exception e) {
      SilverTrace.error("admin",
          "AdminController.getAllSubGroupIdsRecursively",
          "admin.MSG_ERR_GET_ALL_GROUP_IDS", e);
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }
  }

  /** Return all the group names corresponding to the given group Ids */
  public String[] getGroupNames(String[] asGroupIds) {
    SilverTrace.info("admin", "AdminController.getGroupNames",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getGroupNames(asGroupIds);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getGroupNames",
          "admin.MSG_ERR_GET_GROUP_NAMES", e);
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }
  }

  /** Return the group name corresponding to the given group Id */
  public String getGroupName(String sGroupId) {
    SilverTrace.info("admin", "AdminController.getGroupName",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getGroupName(sGroupId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getGroupName",
          "admin.MSG_ERR_GET_GROUP_NAME", "group id: " + sGroupId, e);
      return "";
    }
  }

  /** Return all the user ids available in webactiv */
  public String[] getAllUsersIds() {
    SilverTrace.info("admin", "AdminController.getAllUsersIds",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getAllUsersIds();
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
    SilverTrace.info("admin", "AdminController.getGroupManageableSpaceIds",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getGroupManageableSpaceIds(sGroupId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getGroupManageableSpaceIds",
          "admin.MSG_ERR_GET_SPACE", e);
      return null;
    }
  }

  /** Return the group profile */
  public GroupProfileInst getGroupProfile(String groupId) {
    SilverTrace.info("admin", "AdminController.getGroupProfile",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getGroupProfileInst(groupId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getGroupProfile",
          "admin.MSG_ERR_GET_GROUP_PROFILE", e);
      return null;
    }
  }

  /** Delete the Group Profile */
  public String deleteGroupProfile(String groupId) {
    SilverTrace.info("admin", "AdminController.deleteGroupProfile",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().deleteGroupProfileInst(groupId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.deleteGroupProfile",
          "admin.MSG_ERR_DELETE_GROUP_PROFILE", e);
      return "";
    }
  }

  /** Update the Group Profile */
  public String updateGroupProfile(GroupProfileInst profile) {
    SilverTrace.info("admin", "AdminController.updateGroupProfile",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().updateGroupProfileInst(profile);
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
    SilverTrace.info("admin", "AdminController.getDAPIGeneralAdminId",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getDAPIGeneralAdminId();
    } catch (Exception e) {
      SilverTrace.fatal("admin", "AdminController.getDAPIGeneralAdminId",
          "admin.MSG_FATAL_GET_GENERAL_ADMIN_ID", e);
      return null;
    }
  }

  // ----------------------------------------------
  // Admin User Detail related functions
  // ----------------------------------------------
  /** Return the admin user detail corresponding to the given id */
  public UserDetail getUserDetail(String sId) {
    SilverTrace.info("admin", "AdminController.getUserDetail",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getUserDetail(sId);
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
    SilverTrace.info("admin", "AdminController.getUserFull",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getUserFull(sUserId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getUserFull", "admin.EX_ERR_GET_USER_DETAIL",
          "user Id : '" + sUserId + "'", e);
      return null;
    }
  }

  public UserFull getUserFull(String domainId, String specificId) {
    SilverTrace.info("admin", "AdminController.getUserFull",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getUserFull(domainId, specificId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getUserFull",
          "admin.EX_ERR_GET_USER_DETAIL", "specificId = " + specificId, e);
      return null;
    }
  }

  public String getUserIdByLoginAndDomain(String sLogin, String sDomainId) {
    SilverTrace.info("admin", "AdminController.getUserIdByLoginAndDomain",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getUserIdByLoginAndDomain(sLogin, sDomainId);
    } catch (Exception e) {
      SilverTrace.warn("admin", "AdminController.getUserIdByLoginAndDomain",
          "admin.EX_ERR_GET_USER_DETAIL", "sLogin : '" + sLogin + "' Domain = " + sDomainId, e);
      return null;
    }
  }

  /** Return an array of UserDetail corresponding to the given user Id array */
  public UserDetail[] getUserDetails(String[] asUserIds) {
    SilverTrace.info("admin", "AdminController.getUserDetails",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      if (asUserIds != null) {
        return getAdminService().getUserDetails(asUserIds);
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
    SilverTrace.info("admin", "AdminController.addUser",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().addUser(userDetail);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.addUser",
          "admin.EX_ERR_ADD_USER", e);
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
      getAdminService().userAcceptsTermsOfService(userId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.userAcceptsTermsOfService",
          "admin.EX_ERR_UPDATE_USER_TOS_ACCEPTANCE_DATE", e);
    }
  }

  /** Block the given user */
  public void blockUser(String userId) {
    SilverTrace.info("admin", "AdminController.blockUser", "root.MSG_GEN_ENTER_METHOD");
    try {
      getAdminService().blockUser(userId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.blockUser", "admin.EX_ERR_BLOCK_USER", e);
    }
  }

  /** Unblock the given user */
  public void unblockUser(String userId) {
    SilverTrace.info("admin", "AdminController.unblockUser", "root.MSG_GEN_ENTER_METHOD");
    try {
      getAdminService().unblockUser(userId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.unblockUser", "admin.EX_ERR_UNBLOCK_USER", e);
    }
  }

  /** Delete the given user */
  public String deleteUser(String sUserId) {
    SilverTrace.info("admin", "AdminController.deleteUser",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().deleteUser(sUserId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.deleteUser",
          "admin.EX_ERR_DELETE_USER", e);
      return "";
    }
  }

  /** Update the given user */
  public String updateUser(UserDetail userDetail) {
    SilverTrace.info("admin", "AdminController.updateUser",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().updateUser(userDetail);
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
    SilverTrace.info("admin", "AdminController.updateSynchronizedUser",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().updateUser(userDetail);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.updateSynchronizedUser",
          "admin.EX_ERR_UPDATE_USER", e);
      return "";
    }
  }

  /** Update the given user */
  public String updateUserFull(UserFull userFull) throws AdminException {
    SilverTrace.info("admin", "AdminController.updateUserFull", "root.MSG_GEN_ENTER_METHOD");
    return getAdminService().updateUserFull(userFull);
  }

  public String authenticate(String sKey, String sSessionId,
      boolean isAppInMaintenance) {
    try {
      return getAdminService().authenticate(sKey, sSessionId, isAppInMaintenance);
    } catch (Exception e) {
      return "-1";
    }
  }

  public void indexUsers(String domainId) {
    try {
      getAdminService().indexUsers(domainId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.indexUsers",
          "admin.CANT_INDEX_USERS", "domainId = " + domainId, e);
    }
  }

  public void indexAllUsers() {
    try {
      getAdminService().indexAllUsers();
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
    SilverTrace.info("admin", "AdminController.getAllGroupIds",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getAllGroupIds();
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getAllGroupIds",
          "admin.EX_ERR_GET_ALL_GROUP_IDS", e);
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }
  }

  /** Return true if the group with the given name */
  public boolean isGroupExist(String sName) {
    SilverTrace.info("admin", "AdminController.isGroupExist",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().isGroupExist(sName);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.isGroupExist",
          "admin.EX_ERR_IS_GROUP_EXIST", e);
      return false;
    }
  }

  /** Return the admin group detail corresponding to the given id */
  public Group getGroupById(String sGroupId) {
    SilverTrace.info("admin", "AdminController.getGroupById",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getGroup(sGroupId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getGroupById",
          "admin.EX_ERR_GET_GROUP", e);
      return null;
    }
  }

  /** Return the groupIds from root to group */
  public List<String> getPathToGroup(String groupId) {
    SilverTrace.info("admin", "AdminController.getPathToGroup",
        "root.MSG_GEN_ENTER_METHOD", "groupId =" + groupId);
    try {
      return getAdminService().getPathToGroup(groupId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getPathToGroup",
          "admin.EX_ERR_GET_GROUP", e);
      return null;
    }
  }

  /** Return the admin group detail corresponding to the given group Name */
  public Group getGroupByNameInDomain(String sGroupName, String sDomainFatherId) {
    SilverTrace.info("admin", "AdminController.getGroupByNameInDomain",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getGroupByNameInDomain(sGroupName, sDomainFatherId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getGroupByNameInDomain",
          "admin.EX_ERR_GET_GROUP", e);
      return null;
    }
  }

  /** Add the given group */
  public String addGroup(Group group) {
    SilverTrace.info("admin", "AdminController.addGroup",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().addGroup(group);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.addGroup",
          "admin.EX_ERR_ADD_GROUP", e);
      return "";
    }
  }

  /** Delete the given group */
  public String deleteGroupById(String sGroupId) {
    SilverTrace.info("admin", "AdminController.deleteGroupById",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().deleteGroupById(sGroupId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.deleteGroupById",
          "admin.EX_ERR_DELETE_GROUP", e);
      return "";
    }
  }

  /** Update the given group */
  public String updateGroup(Group group) {
    SilverTrace.info("admin", "AdminController.updateGroup",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().updateGroup(group);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.updateGroup",
          "admin.EX_ERR_UPDATE_GROUP", e);
      return "";
    }
  }

  public AdminGroupInst[] getAdminOrganization() {
    SilverTrace.info("admin", "AdminController.getAdminOrganization",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getAdminOrganization();
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.getAdminOrganization",
          "admin.EX_ERR_GET_ADMIN_ORGANIZATION", e);
      return null;
    }
  }

  public void indexGroups(String domainId) {
    try {
      getAdminService().indexGroups(domainId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.indexGroups",
          "admin.CANT_INDEX_GROUPS", "domainId = " + domainId, e);
    }
  }

  public void indexAllGroups() {
    try {
      getAdminService().indexAllGroups();
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.indexAllGroups",
          "admin.CANT_INDEX_ALL_USERS", e);
    }
  }

  // ----------------------------------------------
  // Exploitation related functions
  // ----------------------------------------------
  public UserLog[] getUserConnected() {
    SilverTrace.info("admin", "AdminController.getUserConnected",
        "root.MSG_GEN_ENTER_METHOD");
    return getAdminService().getUserConnected();
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
    SilverTrace.info("admin",
        "AdminController.synchronizeSilverpeasWithDomain",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().synchronizeSilverpeasWithDomain(domainId);
    } catch (Exception e) {
      SilverTrace.error("admin",
          "AdminController.synchronizeSilverpeasWithDomain",
          "admin.MSG_ERR_SYNCHRONIZE_DOMAIN", e);
      return "Error has occurred";
    }
  }

  public String synchronizeUser(String userId) {
    SilverTrace.info("admin", "AdminController.synchronizeUser",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().synchronizeUser(userId, true);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.synchronizeUser",
          "admin.MSG_ERR_SYNCHRONIZE_USER", e);
      return "";
    }
  }

  public String synchronizeImportUser(String domainId, String userLogin) {
    SilverTrace.info("admin", "AdminController.synchronizeImportUser",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().synchronizeImportUser(domainId, userLogin, true);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.synchronizeImportUser",
          "admin.MSG_ERR_SYNCHRONIZE_USER", e);
      return "";
    }
  }

  public List<DomainProperty> getSpecificPropertiesToImportUsers(String domainId,
      String language) {
    SilverTrace.info("admin",
        "AdminController.getSpecificPropertiesToImportUsers",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().getSpecificPropertiesToImportUsers(domainId, language);
    } catch (Exception e) {
      SilverTrace.error("admin",
          "AdminController.getSpecificPropertiesToImportUsers",
          "admin.MSG_ERR_SYNCHRONIZE_USER", e);
      return null;
    }
  }

  public List<UserDetail> searchUsers(String domainId, Map<String, String> query) {
    SilverTrace.info("admin", "AdminController.searchUsers",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return Arrays.asList(getAdminService().searchUsers(domainId, query));
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.searchUsers",
          "admin.MSG_ERR_SYNCHRONIZE_USER", e);
      return new ArrayList<UserDetail>();
    }
  }

  public String synchronizeRemoveUser(String userId) {
    SilverTrace.info("admin", "AdminController.synchronizeRemoveUser",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().synchronizeRemoveUser(userId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.synchronizeRemoveUser",
          "admin.MSG_ERR_SYNCHRONIZE_USER", e);
      return "";
    }
  }

  public String synchronizeGroup(String groupId) {
    SilverTrace.info("admin", "AdminController.synchronizeGroup",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().synchronizeGroup(groupId, true);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.synchronizeGroup",
          "admin.MSG_ERR_SYNCHRONIZE_GROUP", e);
      return "";
    }
  }

  public String synchronizeImportGroup(String domainId, String groupName) {
    SilverTrace.info("admin", "AdminController.synchronizeImportGroup",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().synchronizeImportGroup(domainId, groupName, null, true,
          false);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.synchronizeImportGroup",
          "admin.MSG_ERR_SYNCHRONIZE_GROUP", e);
      return "";
    }
  }

  public String synchronizeRemoveGroup(String groupId) {
    SilverTrace.info("admin", "AdminController.synchronizeRemoveGroup",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getAdminService().synchronizeRemoveGroup(groupId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.synchronizeRemoveGroup",
          "admin.MSG_ERR_SYNCHRONIZE_GROUP", e);
      return "";
    }
  }

  public void resetAllDBConnections(boolean isScheduled) {
    // SilverTrace.info("admin", "AdminController.resetAllDBConnections",
    // "root.MSG_GEN_ENTER_METHOD");
    try {
      getAdminService().resetAllDBConnections(isScheduled);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.resetAllDBConnections",
          "admin.MSG_ERR_SYNCHRONIZE_GROUP", e);
    }
  }

  /** Removes the given user from the given group */
  public void removeUserFromGroup(String sUserId, String sGroupId) {
    SilverTrace.info("admin", "AdminController.removeUserFromGroup",
        "root.MSG_GEN_ENTER_METHOD", "userId = " + sUserId + ", groupId = " + sGroupId);
    try {
      getAdminService().removeUserFromGroup(sUserId, sGroupId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.removeUserFromGroup",
          "admin.EX_ERR_REMOVE_USER_FROM_GROUP", e);
    }
  }

  /** Removes the given user from the given group */
  public void addUserInGroup(String sUserId, String sGroupId) {
    SilverTrace.info("admin", "AdminController.addUserInGroup",
        "root.MSG_GEN_ENTER_METHOD", "userId = " + sUserId + ", groupId = " + sGroupId);
    try {
      getAdminService().addUserInGroup(sUserId, sGroupId);
    } catch (Exception e) {
      SilverTrace.error("admin", "AdminController.addUserInGroup",
          "admin.EX_ERR_ADD_USER_IN_GROUP", e);
    }
  }

  public void reloadAdminCache() {
    getAdminService().reloadCache();
  }

  public String copyAndPasteComponent(PasteDetail pasteDetail)
      throws AdminException, QuotaException {
    return getAdminService().copyAndPasteComponent(pasteDetail);
  }

  public String copyAndPasteSpace(PasteDetail pasteDetail)
      throws AdminException, QuotaException {
    return getAdminService().copyAndPasteSpace(pasteDetail);
  }

}