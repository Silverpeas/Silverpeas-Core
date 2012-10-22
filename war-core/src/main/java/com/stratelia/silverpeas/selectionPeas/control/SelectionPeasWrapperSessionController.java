/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.stratelia.silverpeas.selectionPeas.control;

import java.util.List;
import java.util.StringTokenizer;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.selection.SelectionUsersGroups;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.GeneralPropertiesManager;

/**
 * A simple wrapper to the userpanel.
 * @author Didier Wenzek
 */
public class SelectionPeasWrapperSessionController extends AbstractComponentSessionController {

  private String[] selectedUserIds;
  private String[] selectedGroupIds;
  private int selectable = SelectionUsersGroups.USER;

  /**
   * Standard Session Controller Constructeur
   * @param mainSessionCtrl The full work session.
   * @param componentContext The context of this component session.
   */
  public SelectionPeasWrapperSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "com.stratelia.silverpeas.userPanelPeas.multilang.selectionPeasBundle",
        "com.stratelia.silverpeas.userPanelPeas.settings.selectionPeasIcons");
  }

  /**
   * Returns the HTML form name whose user element must be set.
   */
  public String getFormName() {
    return formName;
  }

  /**
   * Returns the HTML input where the selected user id must be set.
   */
  public String getElementId() {
    return elementId;
  }

  /**
   * Returns the HTML input where the selected user name must be set.
   */
  public String getElementName() {
    return elementName;
  }

  /**
   * Returns the selected user (if any).
   */
  public UserDetail getSelectedUser() {
    return selectedUser;
  }

  /**
   * Returns the selected user (if any).
   */
  public UserDetail[] getSelectedUsers() {
    return selectedUsers;
  }

  /**
   * Returns the selected group (if any).
   */
  public Group getSelectedGroup() {
    return selectedGroup;
  }

  /**
   * Returns the selected groups (if any).
   */
  public Group[] getSelectedGroups() {
    return selectedGroups;
  }

  /**
   * Set the HTML form name whose user element must be set.
   */
  public void setFormName(String formName) {
    this.formName = formName;
  }

  /**
   * Set the HTML input where the selected user id must be set.
   */
  public void setElementId(String elementId) {
    this.elementId = elementId;
  }

  /**
   * Set the HTML input where the selected user name must be set.
   */
  public void setElementName(String elementName) {
    this.elementName = elementName;
  }

  /**
   * Set the selected user (if any).
   */
  public void setSelectedUserId(String selectedUserId) {
    selectedUser = null;
    this.selectedUserIds = new String[] { selectedUserId };
  }

  public void setSelectedUserIds(String selectedUserIds) {
    selectedUsers = null;
    if (StringUtil.isDefined(selectedUserIds)) {
      StringTokenizer tokenizer = new StringTokenizer(selectedUserIds, ",");
      this.selectedUserIds = new String[tokenizer.countTokens()];
      int i = 0;
      while (tokenizer.hasMoreTokens()) {
        this.selectedUserIds[i++] = tokenizer.nextToken();
      }
    } else {
      this.selectedUserIds = null;
    }
  }

  /**
   * Init the user panel.
   */
  public String initSelectionPeas(boolean multiple, String instanceId, List<String> roles) {
    String m_context = URLManager.getApplicationURL();
    String hostUrl = m_context + "/RselectionPeasWrapper/jsp/close";

    Selection sel = getSelection();
    sel.resetAll();
    sel.setHostSpaceName(null);
    sel.setHostComponentName(null);
    sel.setHostPath(null);

    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(hostUrl);

    // Contraintes
    sel.setMultiSelect(multiple);
    sel.setPopupMode(false);
    sel.setSetSelectable(isGroupSelectable());
    sel.setElementSelectable(isUserSelectable());
    sel.setFirstPage(Selection.FIRST_PAGE_BROWSE);

    if (StringUtil.isDefined(instanceId)) {
      SelectionUsersGroups sug = new SelectionUsersGroups();
      sug.setComponentId(instanceId);
      if (roles != null && !roles.isEmpty()) {
        sug.setProfileNames(roles);
      }
      sel.setExtraParams(sug);
    }

    // Initialisation des éléments sélectionnés
    sel.setSelectedElements(selectedUserIds);
    sel.setSelectedSets(selectedGroupIds);

    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  /**
   * Reads the selection made with the user panel.
   */
  public void getSelectionPeasSelection() {
    Selection sel = getSelection();

    if (isGroupSelectable()) {
      if (sel.isMultiSelect()) {
        String[] ids = sel.getSelectedSets();
        selectedGroups = organizationController.getGroups(ids);
      } else {
        String id = sel.getFirstSelectedSet();
        if (StringUtil.isDefined(id)) {
          selectedGroup = organizationController.getGroup(id);
        }
      }
    } else {
      if (sel.isMultiSelect()) {
        String[] ids = sel.getSelectedElements();
        selectedUsers = organizationController.getUserDetails(ids);
      } else {
        String id = sel.getFirstSelectedElement();
        if (StringUtil.isDefined(id)) {
          selectedUser = organizationController.getUserDetail(id);
        }
      }
    }
  }

  public void setSelectable(int selectable) {
    this.selectable = selectable;
  }

  public void setSelectable(String selectable) {
    if (StringUtil.isDefined(selectable) && StringUtil.isInteger(selectable)) {
      setSelectable(Integer.parseInt(selectable));
    } else {
      setSelectable(SelectionUsersGroups.USER);
    }
  }

  public boolean isUserSelectable() {
    return SelectionUsersGroups.USER == selectable;
  }

  public boolean isGroupSelectable() {
    return SelectionUsersGroups.GROUP == selectable;
  }

  public void setSelectedGroupId(String selectedId) {
    selectedGroup = null;
    this.selectedGroupIds = new String[] { selectedId };
  }

  public void setSelectedGroupIds(String selectedIds) {
    selectedGroups = null;
    if (StringUtil.isDefined(selectedIds)) {
      StringTokenizer tokenizer = new StringTokenizer(selectedIds, ",");
      this.selectedGroupIds = new String[tokenizer.countTokens()];
      int i = 0;
      while (tokenizer.hasMoreTokens()) {
        this.selectedGroupIds[i++] = tokenizer.nextToken();
      }
    } else {
      this.selectedGroupIds = null;
    }
  }

  /**
   * A private OrganizationController.
   */
  static private OrganizationController organizationController = new OrganizationController();

  /**
   * The HTML form name whose user element must be set.
   */
  private String formName = null;

  /**
   * The HTML input where the selected user id must be set.
   */
  private String elementId = null;

  /**
   * The HTML input where the selected user name must be set.
   */
  private String elementName = null;

  /**
   * The selected user (if any).
   */
  private UserDetail selectedUser = null;

  /**
   * The selected users (if any).
   */
  private UserDetail[] selectedUsers = null;

  /**
   * The selected user (if any).
   */
  private Group selectedGroup = null;

  /**
   * The selected users (if any).
   */
  private Group[] selectedGroups = null;

}