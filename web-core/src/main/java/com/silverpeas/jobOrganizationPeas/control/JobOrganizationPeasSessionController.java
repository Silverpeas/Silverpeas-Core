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
package com.silverpeas.jobOrganizationPeas.control;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import com.stratelia.webactiv.beans.admin.UserFull;
import com.stratelia.webactiv.beans.admin.instance.control.WAComponent;
import com.stratelia.webactiv.util.GeneralPropertiesManager;

/**
 * Class declaration
 * @author Thierry Leroi
 */
public class JobOrganizationPeasSessionController extends AbstractComponentSessionController {
  // View Group or User
  private String currentUserId = null;
  private String currentGroupId = null;
  private AdminController myAdminController = null;
  private String[][] currentInfos = null;
  private String[][] currentGroups = null;
  private String[] currentSpaces = null;
  private List currentProfiles = null;
  private Map componentOfficialNames = null;

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
    currentInfos = null;
    currentSpaces = null;
    currentProfiles = null;
  }

  private AdminController getAdminController() {
    if (myAdminController == null)
      myAdminController = new AdminController(getUserId());
    return myAdminController;
  }

  public void setCurrentUserId(String userId) {
    if (currentUserId != null && !currentUserId.equals(userId))
      resetCurrentUser();
    currentUserId = userId;
    resetCurrentGroup();
  }

  private void resetCurrentUser() {
    currentUserId = null;
    resetCurrentArrays();
  }

  public void setCurrentGroupId(String groupId) {
    if (currentGroupId != null && !currentGroupId.equals(groupId))
      resetCurrentGroup();
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
      if (getCurrentUserId() == null)
        return null;
      String[] groupIds = getAdminController().getDirectGroupsIdsOfUser(
          getCurrentUserId());
      if (groupIds == null || groupIds.length == 0)
        return null;
      currentGroups = new String[groupIds.length][4];
      Group currentGroup = null;
      for (int iGrp = 0; iGrp < groupIds.length; iGrp++) {
        currentGroup = getOrganizationController().getGroup(groupIds[iGrp]);
        currentGroups[iGrp][0] = currentGroup.getId();
        currentGroups[iGrp][1] = currentGroup.getName();
        currentGroups[iGrp][2] = String
            .valueOf(currentGroup.getUserIds().length);
        currentGroups[iGrp][3] = currentGroup.getDescription();
      }
    }
    if (currentGroups == null)
      SilverTrace.info("jobOrganizationPeas",
          "JobOrganizationPeasSessionController.getCurrentUserInfo",
          "root.MSG_GEN_PARAM_VALUE", "Groups NULL !");
    else
      SilverTrace.info("jobOrganizationPeas",
          "JobOrganizationPeasSessionController.getCurrentUserInfo",
          "root.MSG_GEN_PARAM_VALUE", "Groups=" + currentGroups);
    return currentGroups;
  }

  /**
   * @return an array of (name, value). name can be either a string or a resource key starting by
   * 'GML.' or 'JOP.'
   */
  public String[][] getCurrentUserInfos() {
    if (currentInfos == null) {
      UserFull userInfos = null;
      if (getCurrentUserId() == null)
        return null;
      userInfos = getAdminController().getUserFull(getCurrentUserId());
      String[] specificKeys = userInfos.getPropertiesNames();
      int nbStdInfos = 4;
      int nbInfos = nbStdInfos + specificKeys.length;
      currentInfos = new String[nbInfos][2];
      currentInfos[0][0] = "GML.lastName";
      currentInfos[0][1] = userInfos.getFirstName();
      currentInfos[1][0] = "GML.surname";
      currentInfos[1][1] = userInfos.getLastName();
      currentInfos[2][0] = "GML.eMail";
      currentInfos[2][1] = userInfos.geteMail();
      currentInfos[3][0] = "GML.login";
      currentInfos[3][1] = userInfos.getLogin();
      String currentKey = null;
      String currentValue = null;
      for (int iSL = nbStdInfos; iSL < currentInfos.length; iSL++) {
        currentKey = specificKeys[iSL - nbStdInfos];
        // On affiche pas le mot de passe !
        if (!currentKey.equals("password")) {
          // Label
          currentInfos[iSL][0] = userInfos.getSpecificLabel(getLanguage(),
              currentKey);
          // Valeur
          currentValue = userInfos.getValue(currentKey);
          if (currentKey.equals("passwordValid"))
            if (currentValue.equals("true"))
              currentValue = "GML.yes";
            else
              currentValue = "GML.no";
          currentInfos[iSL][1] = currentValue;
        }
      }
    }
    if (currentInfos == null)
      SilverTrace.info("jobOrganizationPeas",
          "JobOrganizationPeasSessionController.getCurrentUserInfo",
          "root.MSG_GEN_PARAM_VALUE", "User NULL !");
    else
      SilverTrace.info("jobOrganizationPeas",
          "JobOrganizationPeasSessionController.getCurrentUserInfo",
          "root.MSG_GEN_PARAM_VALUE", "User=" + currentInfos);
    return currentInfos;
  }

  /**
   * @return an array of (name, value). name can be either a string or a resource key starting by
   * 'GML.' or 'JOP.'
   */
  public String[][] getCurrentGroupInfos() {
    if (currentInfos == null) {
      Group groupInfos = null;
      if (getCurrentGroupId() == null)
        return null;
      groupInfos = getAdminController().getGroupById(getCurrentGroupId());
      currentInfos = new String[4][2];
      currentInfos[0][0] = "GML.name";
      currentInfos[0][1] = groupInfos.getName();
      currentInfos[1][0] = "GML.users";
      currentInfos[1][1] = String.valueOf(groupInfos.getUserIds().length);
      currentInfos[2][0] = "GML.description";
      currentInfos[2][1] = groupInfos.getDescription();
      currentInfos[3][0] = "JOP.parentGroup";
      String parentId = groupInfos.getSuperGroupId();
      if (parentId == null || parentId.equals(""))
        currentInfos[3][1] = "-";
      else
        currentInfos[3][1] = getAdminController().getGroupName(
            groupInfos.getSuperGroupId());
    }
    if (currentInfos == null)
      SilverTrace.info("jobOrganizationPeas",
          "JobOrganizationPeasSessionController.getCurrentGroupInfo",
          "root.MSG_GEN_PARAM_VALUE", "Infos NULL !");
    else
      SilverTrace.info("jobOrganizationPeas",
          "JobOrganizationPeasSessionController.getCurrentGroupInfo",
          "root.MSG_GEN_PARAM_VALUE", "infos=" + currentInfos);
    return currentInfos;
  }

  /**
   * @return an array of (name, value). name can be either a string or a resource key starting by
   * 'GML.' or 'JOP.'
   */
  public String[][] getCurrentInfos() {
    if (getCurrentGroupId() != null) {
      SilverTrace.debug("jobOrganizationPeas",
          "JobOrganizationPeasSessionController.getCurrentInfos",
          "root.EX_NO_MESSAGE", "Returns groupInfos");
      return getCurrentGroupInfos();
    }
    if (getCurrentUserId() != null) {
      SilverTrace.debug("jobOrganizationPeas",
          "JobOrganizationPeasSessionController.getCurrentInfos",
          "root.EX_NO_MESSAGE", "Returns userInfos");
      return getCurrentUserInfos();
    }
    SilverTrace.debug("jobOrganizationPeas",
        "JobOrganizationPeasSessionController.getCurrentInfos",
        "root.EX_NO_MESSAGE", "Returns no infos");
    return null;
  }

  /**
   * @return array of space names (manageable by the current group or user)
   */
  public String[] getCurrentSpaces() {
    if (currentSpaces == null) {
      String[] spaceIds = null;
      if (getCurrentGroupId() != null)
        spaceIds = getAdminController().getGroupManageableSpaceIds(
            getCurrentGroupId());
      if (getCurrentUserId() != null)
        spaceIds = getAdminController().getUserManageableSpaceIds(
            getCurrentUserId());
      if (spaceIds == null)
        return null;
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
   * @return a map of the official (business) names of the installed components indexed by the
   * internal names
   */
  private Map getComponentOfficialNames() {
    if (componentOfficialNames == null) {
      componentOfficialNames = getAdminController().getAllComponents();
    }
    return componentOfficialNames;
  }

  /**
   * @return the official component name given the internal name
   */
  private String getComponentOfficialName(String internalName) {
    try {
      return ((WAComponent) getComponentOfficialNames().get(internalName))
          .getLabel();
    } catch (Exception e) {
      SilverTrace.info("jobOrganizationPeas",
          "JobOrganizationPeasSessionController.getComponentOfficialName",
          "root.MSG_GEN_PARAM_VALUE", "!!!!! ERROR getting official name="
          + internalName, e);
      return internalName;
    }
  }

  /**
   * @return list of (array[space name, component label, component name, profile name])
   */
  public List getCurrentProfiles() {
    if (currentProfiles == null) {
      List distinctProfiles = new ArrayList();
      String[] profileIds = null;
      if (getCurrentGroupId() != null)
        profileIds = getAdminController().getProfileIdsOfGroup(
            getCurrentGroupId());
      if (getCurrentUserId() != null)
        profileIds = getAdminController().getProfileIds(getCurrentUserId());
      if (profileIds == null)
        return null;
      currentProfiles = new ArrayList();
      ProfileInst currentProfile = null;
      ComponentInst currentComponent = null;
      // String[] spaceIds=new String[profileIds.length];
      List spaceIds = new ArrayList();
      String[] profile2Display = null;
      for (int iProfile = 0; iProfile < profileIds.length; iProfile++) {
        currentProfile = getAdminController().getProfileInst(
            profileIds[iProfile]);
        currentComponent = getAdminController().getComponentInst(
            currentProfile.getComponentFatherId());
        if (currentComponent.getStatus() == null) {
          String dProfile = currentComponent.getId() + currentProfile.getName();
          if (!distinctProfiles.contains(dProfile)) {
            profile2Display = new String[4];
            profile2Display[1] = currentComponent.getLabel();
            profile2Display[2] = getComponentOfficialName(currentComponent
                .getName());
            profile2Display[3] = currentProfile.getLabel();
            if (!StringUtil.isDefined(profile2Display[3]))
              profile2Display[3] = getAdminController()
                  .getProfileLabelfromName(currentComponent.getName(),
                  currentProfile.getName());
            currentProfiles.add(profile2Display);

            spaceIds.add(currentComponent.getDomainFatherId());

            distinctProfiles.add(dProfile);
          }
        }

      }
      String[] spaceNames = getAdminController().getSpaceNames(
          (String[]) spaceIds.toArray(new String[spaceIds.size()]));
      for (int iProfile = 0; iProfile < currentProfiles.size(); iProfile++) {
        profile2Display = (String[]) currentProfiles.get(iProfile);
        profile2Display[0] = spaceNames[iProfile];
      }
    }
    return currentProfiles;
  }

  // ####################
  // UserPanel

  /*
   * Retour du initialisation userPanel un user ou (exclusif) un groupe
   */
  public String initSelectionPeas() {
    String m_context = GeneralPropertiesManager.getGeneralResourceLocator()
        .getString("ApplicationURL");
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
