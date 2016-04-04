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

package org.silverpeas.web.selection.control;

import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.core.web.selection.SelectionUsersGroups;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.UserDetail;

import java.util.List;
import java.util.StringTokenizer;

/**
 * A simple wrapper to the userpanel.
 * @author Didier Wenzek
 */
public class SelectionPeasWrapperSessionController extends AbstractComponentSessionController {

  private String domainIdFilter = "";
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
        "org.silverpeas.userPanelPeas.multilang.selectionPeasBundle",
        "org.silverpeas.userPanelPeas.settings.selectionPeasIcons");
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
   * Gets the identifier of the domain on which the user selection must be filtered.
   * @return an identifier of domain as string, empty if none.
   */
  public String getDomainIdFilter() {
    return domainIdFilter;
  }

  /**
   * Sets the identifier of the domain in order to filter user selection on it.
   * @param domainIdFilter the identifier of domain as string.
   */
  public void setDomainIdFilter(final String domainIdFilter) {
    this.domainIdFilter = StringUtil.defaultStringIfNotDefined(domainIdFilter);
  }

  /**
   * Init the user panel.
   */
  public String initSelectionPeas(boolean multiple, String instanceId, List<String> roles) {
    String m_context = URLUtil.getApplicationURL();
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

    SelectionUsersGroups sug = new SelectionUsersGroups();
    if (StringUtil.isDefined(instanceId)) {
      sug.setComponentId(instanceId);
      if (roles != null && !roles.isEmpty()) {
        sug.setProfileNames(roles);
      }
      sel.setExtraParams(sug);
    }
    if (StringUtil.isDefined(getDomainIdFilter()) &&
        !Domain.MIXED_DOMAIN_ID.equals(getDomainIdFilter())) {
      sug.setDomainId(getDomainIdFilter());
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
        selectedGroups = OrganizationControllerProvider.getOrganisationController().getGroups(ids);
      } else {
        String id = sel.getFirstSelectedSet();
        if (StringUtil.isDefined(id)) {
          selectedGroup = OrganizationControllerProvider.getOrganisationController().getGroup(id);
        }
      }
    } else {
      if (sel.isMultiSelect()) {
        String[] ids = sel.getSelectedElements();
        selectedUsers = OrganizationControllerProvider.getOrganisationController().getUserDetails(ids);
      } else {
        String id = sel.getFirstSelectedElement();
        if (StringUtil.isDefined(id)) {
          selectedUser = OrganizationControllerProvider.getOrganisationController().getUserDetail(id);
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