/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
package com.silverpeas.jobOrganizationPeas.control;

import com.silverpeas.admin.components.WAComponent;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.PairObject;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.ProfileInst;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;
import com.stratelia.webactiv.beans.admin.UserFull;
import com.stratelia.webactiv.util.GeneralPropertiesManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class declaration
 * @author Thierry Leroi
 */
public class JobOrganizationPeasSessionController extends AbstractComponentSessionController {
  // View Group or User

  private String currentUserId = null;
  private String currentGroupId = null;
  private AdminController myAdminController = null;
  private UserFull currentUser = null;
  private Group currentGroup = null;
  private String[][] currentGroups = null;
  private String[] currentSpaces = null;
  private List<String[]> currentProfiles = null;
  private Map<String, WAComponent> componentOfficialNames = getAdminController().getAllComponents();

  /**
   * Standard Session Controller Constructeur
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   * @see
   */
  public JobOrganizationPeasSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    super(
        mainSessionCtrl,
        componentContext,
        "com.silverpeas.jobOrganizationPeas.multilang.jobOrganizationPeasBundle",
        "com.silverpeas.jobOrganizationPeas.settings.jobOrganizationPeasIcons");
    setComponentRootName(URLManager.CMP_JOBORGANIZATIONPEAS);
  }

  // ####################
  // View User or Group
  private void resetCurrentArrays() {
    currentGroups = null;
    currentUser = null;
    currentGroup = null;
    currentSpaces = null;
    currentProfiles = null;
  }

  public AdminController getAdminController() {
    if (myAdminController == null) {
      myAdminController = new AdminController(getUserId());
    }
    return myAdminController;
  }

  public void setCurrentUserId(String userId) {
    if (currentUserId != null && !currentUserId.equals(userId)) {
      resetCurrentUser();
    }
    currentUserId = userId;
    resetCurrentGroup();
  }

  private void resetCurrentUser() {
    currentUserId = null;
    resetCurrentArrays();
  }

  public void setCurrentGroupId(String groupId) {
    if (currentGroupId != null && !currentGroupId.equals(groupId)) {
      resetCurrentGroup();
    }
    currentGroupId = groupId;
    resetCurrentUser();
  }

  public void resetCurrentGroup() {
    currentGroupId = null;
    resetCurrentArrays();
  }

  public String getCurrentUserId() {
    return currentUserId;
  }

  public String getCurrentGroupId() {
    return currentGroupId;
  }

  /**
   * @return an array of (id, name, number of users, description).
   */
  public String[][] getCurrentUserGroups() {
    if (currentGroups == null) {
      if (getCurrentUserId() == null) {
        return null;
      }
      String[] groupIds = getAdminController().getDirectGroupsIdsOfUser(
          getCurrentUserId());
      if (groupIds == null || groupIds.length == 0) {
        return null;
      }
      currentGroups = new String[groupIds.length][4];
      Group currentGroup = null;
      for (int iGrp = 0; iGrp < groupIds.length; iGrp++) {
        currentGroup = getOrganizationController().getGroup(groupIds[iGrp]);
        currentGroups[iGrp][0] = currentGroup.getId();
        currentGroups[iGrp][1] = currentGroup.getName();
        currentGroups[iGrp][2] = String.valueOf(currentGroup.getUserIds().length);
        currentGroups[iGrp][3] = currentGroup.getDescription();
      }
    }
    if (currentGroups == null) {
      SilverTrace.info("jobOrganizationPeas",
          "JobOrganizationPeasSessionController.getCurrentUserInfo",
          "root.MSG_GEN_PARAM_VALUE", "Groups NULL !");
    } else {
      SilverTrace.info("jobOrganizationPeas",
          "JobOrganizationPeasSessionController.getCurrentUserInfo",
          "root.MSG_GEN_PARAM_VALUE", "Groups=" + currentGroups);
    }
    return currentGroups;
  }
  
  /**
   * @return UserFull
   */
  public UserFull getCurrentUser() {
	  if(currentUser == null) {
	      if (getCurrentUserId() == null) {
	        return null;
	      }
	      currentUser = getAdminController().getUserFull(getCurrentUserId());
	  }
	  if (currentUser == null) {
	      SilverTrace.info("jobOrganizationPeas",
	          "JobOrganizationPeasSessionController.getCurrentUser",
	          "root.MSG_GEN_PARAM_VALUE", "User NULL !");
	  } else {
	      SilverTrace.info("jobOrganizationPeas",
	          "JobOrganizationPeasSessionController.getCurrentUser",
	          "root.MSG_GEN_PARAM_VALUE", "User=" + getCurrentUserId());
	  }
	  return currentUser;
  }

  /**
   * @return Group
   */
  public Group getCurrentGroup() {
	  if(currentGroup == null) {
	      if (getCurrentGroupId() == null) {
	        return null;
	      }
	      currentGroup = getAdminController().getGroupById(getCurrentGroupId());
	  }
	  if (currentGroup == null) {
	      SilverTrace.info("jobOrganizationPeas",
	          "JobOrganizationPeasSessionController.getCurrentGroup",
	          "root.MSG_GEN_PARAM_VALUE", "Group NULL !");
	  } else {
	      SilverTrace.info("jobOrganizationPeas",
	          "JobOrganizationPeasSessionController.getCurrentGroup",
	          "root.MSG_GEN_PARAM_VALUE", "Group=" + getCurrentGroupId());
	  }
	  return currentGroup;
  }

  /**
   * @return array of space names (manageable by the current group or user)
   */
  public String[] getCurrentSpaces() {
    if (currentSpaces == null) {
      String[] spaceIds = null;
      if (getCurrentGroupId() != null) {
        spaceIds = getAdminController().getGroupManageableSpaceIds(
            getCurrentGroupId());
      }
      if (getCurrentUserId() != null) {
        spaceIds = getAdminController().getUserManageableSpaceIds(
            getCurrentUserId());
      }
      if (spaceIds == null) {
        return null;
      }
      String[] spaceIdsBIS = new String[spaceIds.length];
      for (int j = 0; j < spaceIds.length; j++) {
        if ((spaceIds[j] != null) && (spaceIds[j].startsWith("WA"))) {
          spaceIdsBIS[j] = spaceIds[j];
        } else {
          spaceIdsBIS[j] = "WA" + spaceIds[j];
        }
      }
      currentSpaces = getAdminController().getSpaceNames(spaceIdsBIS);
    }
    return currentSpaces;
  }


  /**
   * 
   * @return list of (array[space name, component id, component label, component name, profile name])
   */
  public List<String[]> getCurrentProfiles() {
    if (currentProfiles == null) {
      List<String> distinctProfiles = new ArrayList<String>();
      String[] profileIds = null;
      if (getCurrentGroupId() != null) {
        profileIds = getAdminController().getProfileIdsOfGroup(
            getCurrentGroupId());
      }
      if (getCurrentUserId() != null) {
        profileIds = getAdminController().getProfileIds(getCurrentUserId());
      }
      if (profileIds == null) {
        return null;
      }  
      currentProfiles = new ArrayList<String[]>();
      ProfileInst currentProfile = null;
      ComponentInst currentComponent = null;
      List<String> spaceIds = new ArrayList<String>();
      for (String profileId : profileIds) {
        currentProfile = getAdminController().getProfileInst(profileId);
        currentComponent = getAdminController().getComponentInst(
            currentProfile.getComponentFatherId());
        String spaceId = currentComponent.getDomainFatherId();
        SpaceInstLight spaceInst = getAdminController().getSpaceInstLight(spaceId);
        if (currentComponent.getStatus() == null && !spaceInst.isPersonalSpace()) {//on n'affiche pas les composants de l'espace personnel
          String dProfile = currentComponent.getId() + currentProfile.getName();
          if (!distinctProfiles.contains(dProfile)) {
            String[] profile2Display = new String[6];
            profile2Display[1] = currentComponent.getId();
            profile2Display[2] = currentComponent.getName();
            profile2Display[3] = currentComponent.getLabel();
            profile2Display[4] = getComponentOfficialName(currentComponent.getName());
            profile2Display[5] = currentProfile.getLabel();
            if (!StringUtil.isDefined(profile2Display[5])) {
              profile2Display[5] = getAdminController().getProfileLabelfromName(
                  currentComponent.getName(), currentProfile.getName(), getLanguage());
            }
            currentProfiles.add(profile2Display);
            spaceIds.add(spaceId);
            distinctProfiles.add(dProfile);
          }
        }
      }
      String[] spaceNames = getAdminController().getSpaceNames(spaceIds.toArray(new String[spaceIds.
          size()]));
      for (int iProfile = 0; iProfile < currentProfiles.size(); iProfile++) {
        String[] profile2Display = currentProfiles.get(iProfile);
        profile2Display[0] = spaceNames[iProfile];
      }
    }
    return currentProfiles;
  }

  /**
   * @return the official component name given the internal name
   */
  private String getComponentOfficialName(String internalName) {
    try {
      WAComponent component = componentOfficialNames.get(internalName);
      if (component != null) {
        return component.getLabel().get(getLanguage());
      }
      return internalName;
    } catch (Exception e) {
      SilverTrace.info("jobOrganizationPeas",
          "JobOrganizationPeasSessionController.getComponentOfficialName",
          "root.MSG_GEN_PARAM_VALUE", "!!!!! ERROR getting official name="
          + internalName, e);
      return internalName;
    }
  }

  /*
   * Retour du initialisation userPanel un user ou (exclusif) un groupe
   */
  public String initSelectionPeas() {
    String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString(
        "ApplicationURL");
    String hostSpaceName = getString("JOP.pseudoSpace");
    String cancelUrl = m_context
        + Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
    PairObject hostComponentName = new PairObject(getString("JOP.pseudoPeas"),
        cancelUrl);
    String hostUrl = m_context + getComponentUrl() + "ViewUserOrGroup";

    Selection sel = getSelection();
    sel.resetAll();
    sel.setHostSpaceName(hostSpaceName);
    sel.setHostComponentName(hostComponentName);
    sel.setHostPath(null);

    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(cancelUrl);

    // Contraintes
    sel.setMultiSelect(false);
    sel.setPopupMode(false);
    sel.setFirstPage(Selection.FIRST_PAGE_BROWSE);
    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  /*
   * Retour du UserPanel
   */
  public void retourSelectionPeas() {
    Selection sel = getSelection();
    String id = "";

    id = sel.getFirstSelectedElement();
    if ((id != null) && (id.length() > 0)) {
      setCurrentUserId(id);
    }
    id = sel.getFirstSelectedSet();
    if ((id != null) && (id.length() > 0)) {
      setCurrentGroupId(id);
    }
  }
}
