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

package com.stratelia.silverpeas.selectionPeas.control;

import java.util.StringTokenizer;

import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.selection.SelectionUsersGroups;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.GeneralPropertiesManager;

/**
 * A simple wrapper to the userpanel.
 * @author Didier Wenzek
 */
public class SelectionPeasWrapperSessionController extends AbstractComponentSessionController {

  private String[] selectedUserIds;

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
    if ((selectedUserIds != null) && (selectedUserIds.length() > 0)) {
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
  public String initSelectionPeas(boolean multiple, String instanceId) {
    String m_context = GeneralPropertiesManager.getGeneralResourceLocator()
        .getString("ApplicationURL");
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
    sel.setSetSelectable(false);
    sel.setFirstPage(Selection.FIRST_PAGE_BROWSE);

    if (instanceId != null) {
      SelectionUsersGroups sug = new SelectionUsersGroups();
      sug.setComponentId(instanceId);
      sel.setExtraParams(sug);
    }

    // Initialisation des éléments sélectionnés
    sel.setSelectedElements(selectedUserIds);

    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  /**
   * Reads the selection made with the user panel.
   */
  public void getSelectionPeasSelection() {
    Selection sel = getSelection();

    if (sel.isMultiSelect()) {
      String[] ids = sel.getSelectedElements();
      selectedUsers = organizationController.getUserDetails(ids);
    } else {
      String id = sel.getFirstSelectedElement();
      if ((id != null) && (id.length() > 0)) {
        selectedUser = organizationController.getUserDetail(id);
      }
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

}
