/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.web.joborganization.control;

import org.silverpeas.core.admin.component.model.WAComponent;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.web.joborganization.JobOrganizationPeasException;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.service.RightAssignationContext;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.exception.SilverpeasException;

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
  private AdminController myAdminController = ServiceProvider.getService(AdminController.class);
  private UserFull currentUser = null;
  private Group currentGroup = null;
  private String[][] currentGroups = null;
  private String[] currentSpaces = null;
  private List<String[]> currentProfiles = null;
  private Map<String, WAComponent> componentOfficialNames = getAdminController().getAllComponents();

  public static final String REPLACE_RIGHTS = "1";
  public static final String ADD_RIGHTS = "2";

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
        "org.silverpeas.jobOrganizationPeas.multilang.jobOrganizationPeasBundle",
        "org.silverpeas.jobOrganizationPeas.settings.jobOrganizationPeasIcons",
        "org.silverpeas.jobOrganizationPeas.settings.jobOrganizationPeasSettings");
    setComponentRootName(URLUtil.CMP_JOBORGANIZATIONPEAS);
  }

  public boolean isRightCopyReplaceActivated() {
    return getSettings().getBoolean("admin.profile.rights.copyReplace.activated", false);
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
    return myAdminController;
  }

  public void setCurrentUserId(String userId) {
    if (currentUserId != null && !currentUserId.equals(userId)) {
      resetCurrentUser();
    }
    currentUserId = userId;
    if (currentUserId != null) {
      resetCurrentGroup();
    }
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
    if (currentGroupId != null) {
      resetCurrentUser();
    }
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
      for (int iGrp = 0; iGrp < groupIds.length; iGrp++) {
        Group theCurrentGroup = getOrganisationController().getGroup(groupIds[iGrp]);
        currentGroups[iGrp][0] = theCurrentGroup.getId();
        currentGroups[iGrp][1] = theCurrentGroup.getName();
        currentGroups[iGrp][2] = String.valueOf(theCurrentGroup.getUserIds().length);
        currentGroups[iGrp][3] = theCurrentGroup.getDescription();
      }
    }
    return currentGroups;
  }

  /**
   * @return UserFull
   */
  public UserFull getCurrentUser() {
    if (currentUser == null) {
      if (getCurrentUserId() == null) {
        return null;
      }
      currentUser = getAdminController().getUserFull(getCurrentUserId());
    }
    return currentUser;
  }

  /**
   * @return Group
   */
  public Group getCurrentGroup() {
    if (currentGroup == null) {
      if (getCurrentGroupId() == null) {
        return null;
      }
      currentGroup = getAdminController().getGroupById(getCurrentGroupId());
    }
    return currentGroup;
  }

  /**
   * @return String
   */
  public String getCurrentSuperGroupName() {
    String currentSuperGroupName = "-";
    if (currentGroup != null) {
      String parentId = currentGroup.getSuperGroupId();
      if (StringUtil.isDefined(parentId)) {
        currentSuperGroupName = getAdminController().getGroupName(parentId);
      }
    }
    return currentSuperGroupName;
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
   * @return list of (array[space name, component id, component label, component name, profile
   * name])
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
      ProfileInst currentProfile;
      ComponentInst currentComponent;
      List<String> spaceIds = new ArrayList<String>();
      for (String profileId : profileIds) {
        currentProfile = getAdminController().getProfileInst(profileId);
        currentComponent = getAdminController().getComponentInst(
            currentProfile.getComponentFatherId());
        String spaceId = currentComponent.getDomainFatherId();
        SpaceInstLight spaceInst = getAdminController().getSpaceInstLight(spaceId);
        if (currentComponent.getStatus() == null && !spaceInst.isPersonalSpace()) {// on n'affiche
          // pas les
          // composants de
          // l'espace
          // personnel
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
      String[] spaceNames =
          getAdminController().getSpaceNames(spaceIds.toArray(new String[spaceIds.
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
      SilverTrace.error("jobOrganizationPeas",
          "JobOrganizationPeasSessionController.getComponentOfficialName",
          "root.MSG_GEN_PARAM_VALUE", "!!!!! ERROR getting official name="
          + internalName, e);
      return internalName;
    }
  }

  /*
   * UserPanel initialization : a user or (exclusive) a group
   */
  public String initSelectionUserOrGroup() {
    String m_context = URLUtil.getApplicationURL();
    String hostSpaceName = getString("JOP.pseudoSpace");
    String cancelUrl = m_context
        + Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
    Pair<String, String> hostComponentName = new Pair<>(getString("JOP.pseudoPeas"),
        cancelUrl);
    String hostUrl = m_context + getComponentUrl() + "ViewUserOrGroup";

    Selection sel = getSelection();
    sel.resetAll();
    sel.setFilterOnDeactivatedState(false);
    sel.setHostSpaceName(hostSpaceName);
    sel.setHostComponentName(hostComponentName);
    sel.setHostPath(null);

    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(cancelUrl);

    sel.setMultiSelect(false);
    sel.setPopupMode(false);
    sel.setFirstPage(Selection.FIRST_PAGE_BROWSE);
    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  /*
   * Back from UserPanel
   */
  public void backSelectionUserOrGroup() {
    Selection sel = getSelection();
    String id;

    id = sel.getFirstSelectedElement();
    setCurrentUserId(id);

    id = sel.getFirstSelectedSet();
    setCurrentGroupId(id);
  }

  /*
   * UserPanel initialization : a user or (exclusive) a group
   */
  public String initSelectionRightsUserOrGroup() {
    String hostSpaceName = getString("JOP.pseudoSpace");
    String cancelUrl = "";
    Pair<String, String> hostComponentName = new Pair<>(getString("JOP.pseudoPeas"),
        cancelUrl);
    String hostUrl = "";

    Selection sel = getSelection();
    sel.resetAll();
    sel.setFilterOnDeactivatedState(false);
    sel.setHostSpaceName(hostSpaceName);
    sel.setHostComponentName(hostComponentName);
    sel.setHostPath(null);

    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(cancelUrl);

    sel.setHtmlFormName("rightsForm");
    sel.setHtmlFormElementName("sourceRightsName");
    sel.setHtmlFormElementId("sourceRightsId");
    sel.setHtmlFormElementType("sourceRightsType");

    sel.setMultiSelect(false);
    sel.setPopupMode(true);
    sel.setFirstPage(Selection.FIRST_PAGE_BROWSE);
    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  /*
   * Assign rights to current selected user or group
   */
  public void assignRights(String choiceAssignRights, String sourceRightsId,
      String sourceRightsType, boolean nodeAssignRights) throws JobOrganizationPeasException {

    try {
      if (JobOrganizationPeasSessionController.REPLACE_RIGHTS.equals(choiceAssignRights) ||
          JobOrganizationPeasSessionController.ADD_RIGHTS.equals(choiceAssignRights)) {

        RightAssignationContext.MODE operationMode =
            JobOrganizationPeasSessionController.REPLACE_RIGHTS.equals(choiceAssignRights) ?
                RightAssignationContext.MODE.REPLACE : RightAssignationContext.MODE.COPY;

        if (Selection.TYPE_SELECTED_ELEMENT.equals(sourceRightsType)) {
          if (getCurrentUserId() != null) {
            getAdminController()
                .assignRightsFromUserToUser(operationMode, sourceRightsId, getCurrentUserId(),
                    nodeAssignRights, getUserId());
          } else if (getCurrentGroupId() != null) {
            getAdminController()
                .assignRightsFromUserToGroup(operationMode, sourceRightsId, getCurrentGroupId(),
                    nodeAssignRights, getUserId());
          }
        } else if (Selection.TYPE_SELECTED_SET.equals(sourceRightsType)) {
          if (getCurrentUserId() != null) {
            getAdminController()
                .assignRightsFromGroupToUser(operationMode, sourceRightsId, getCurrentUserId(),
                    nodeAssignRights, getUserId());
          } else if (getCurrentGroupId() != null) {
            getAdminController()
                .assignRightsFromGroupToGroup(operationMode, sourceRightsId, getCurrentGroupId(),
                    nodeAssignRights, getUserId());
          }
        }

        //force to refresh
        currentProfiles = null;
      }
    } catch (AdminException e) {
      throw new JobOrganizationPeasException("JobOrganizationPeasSessionController.assignRights",
          SilverpeasException.ERROR, "", e);
    }
  }
}
