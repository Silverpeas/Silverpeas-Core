/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.joborganization.control;

import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.component.model.LocalizedComponent;
import org.silverpeas.core.admin.component.model.LocalizedProfile;
import org.silverpeas.core.admin.component.model.WAComponent;
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.RightAssignationContext;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.web.joborganization.JobOrganizationPeasException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Class declaration
 * @author Thierry Leroi
 */
public class JobOrganizationPeasSessionController extends AbstractComponentSessionController {

  private String currentUserId = null;
  private String currentGroupId = null;
  private AdminController myAdminController = ServiceProvider.getService(AdminController.class);
  private UserFull currentUser = null;
  private Group currentGroup = null;
  private List<Group> currentGroups = null;
  private String[] currentSpaces = null;
  private List<String[]> currentProfiles = null;
  private Map<String, WAComponent> componentOfficialNames = getAdminController().getAllComponents();

  public static final String REPLACE_RIGHTS = "1";
  public static final String ADD_RIGHTS = "2";

  /**
   * Standard Session Controller Constructeur
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
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

  private AdminController getAdminController() {
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

  private void setCurrentGroupId(String groupId) {
    if (currentGroupId != null && !currentGroupId.equals(groupId)) {
      resetCurrentGroup();
    }
    currentGroupId = groupId;
    if (currentGroupId != null) {
      resetCurrentUser();
    }
  }

  private void resetCurrentGroup() {
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
  @SuppressWarnings("unchecked")
  public List<Group> getCurrentUserGroups() {
    if (currentGroups == null) {
      if (getCurrentUserId() == null) {
        return Collections.emptyList();
      }
      currentGroups = (List) getAdminController().getDirectGroupsOfUser(getCurrentUserId());
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
        return new String[0];
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
      List<String> distinctProfiles = new ArrayList<>();
      String[] profileIds = null;
      if (getCurrentGroupId() != null) {
        profileIds = getAdminController().getProfileIdsOfGroup(
            getCurrentGroupId());
      } else if (getCurrentUserId() != null) {
        profileIds = getAdminController().getProfileIds(getCurrentUserId());
      }
      if (profileIds == null) {
        return Collections.emptyList();
      }
      currentProfiles = new ArrayList<>();
      List<String> spaceIds = new ArrayList<>();
      for (String profileId : profileIds) {
        ProfileInst currentProfile = getAdminController().getProfileInst(profileId);
        ComponentInstLight currentComponent = getAdminController().getComponentInstLight(
            currentProfile.getComponentFatherId());
        String spaceId = currentComponent.getDomainFatherId();
        SpaceInstLight spaceInst = getAdminController().getSpaceInstLight(spaceId);
        if (currentComponent.getStatus() == null && !spaceInst.isPersonalSpace()) {
          // Personal components are not displayed
          String dProfile = currentComponent.getId() + currentProfile.getName();
          if (!distinctProfiles.contains(dProfile)) {
            currentProfiles.add(getProfileToDisplay(currentComponent, currentProfile));
            spaceIds.add(spaceId);
            distinctProfiles.add(dProfile);
          }
        }
      }
      String[] spaceNames =
          getAdminController().getSpaceNames(spaceIds.toArray(new String[spaceIds.size()]));
      for (int iProfile = 0; iProfile < currentProfiles.size(); iProfile++) {
        String[] profile2Display = currentProfiles.get(iProfile);
        profile2Display[0] = spaceNames[iProfile];
      }
    }
    return currentProfiles;
  }

  private String[] getProfileToDisplay(ComponentInstLight component, ProfileInst profile) {
    String[] profile2Display = new String[6];
    profile2Display[1] = component.getId();
    profile2Display[2] = component.getName();
    profile2Display[3] = component.getLabel();
    LocalizedComponent localizedComponent = getLocalizedComponent(component.getName());
    if (localizedComponent != null) {
      profile2Display[4] = localizedComponent.getLabel();
      LocalizedProfile localizedProfile = localizedComponent.getProfile(profile.getName());
      if (localizedProfile != null) {
        profile2Display[5] = localizedProfile.getLabel();
      }
    }
    return profile2Display;
  }

  private LocalizedComponent getLocalizedComponent(String name) {
    WAComponent component = componentOfficialNames.get(name);
    if (component != null) {
      return new LocalizedComponent(component, getLanguage());
    }
    return null;
  }

  /*
   * UserPanel initialization : a user or (exclusive) a group
   */
  public String initSelectionUserOrGroup() {
    String mContext = URLUtil.getApplicationURL();
    String hostSpaceName = getString("JOP.pseudoSpace");
    String cancelUrl = mContext
        + Selection.getSelectionURL();
    Pair<String, String> hostComponentName = new Pair<>(getString("JOP.pseudoPeas"),
        cancelUrl);
    String hostUrl = mContext + getComponentUrl() + "ViewUserOrGroup";

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
    return Selection.getSelectionURL();
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
    return Selection.getSelectionURL();
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
