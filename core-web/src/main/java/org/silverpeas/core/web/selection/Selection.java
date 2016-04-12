/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.selection;

import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.StringUtil;

import java.util.Collection;
import java.util.List;

public final class Selection {

  final public static String TYPE_USERS_GROUPS = "UsersGroups";
  final public static String TYPE_SPACES_COMPONENTS = "SpacesComponents";
  final public static String TYPE_JDBC_CONNECTOR = "JdbcConnector";
  final public static String FIRST_PAGE_DEFAULT = "Default";
  final public static String FIRST_PAGE_CART = "DisplayCart";
  final public static String FIRST_PAGE_SEARCH_ELEMENT = "DisplaySearchElement";
  final public static String FIRST_PAGE_SEARCH_SET = "DisplaySearchSet";
  final public static String FIRST_PAGE_BROWSE = "DisplayBrowse";
  final public static String USER_SELECTION_PANEL_PATH = "/selection/jsp/userpanel.jsp";
  final public static String TYPE_SELECTED_SET = "Set"; //group selected
  final public static String TYPE_SELECTED_ELEMENT = "Element"; //user selected
  protected List<Domain> registeredServerDomains;
  protected String goBackURL;
  protected String cancelURL;
  protected String htmlFormName;
  protected String htmlFormElementName;
  protected String htmlFormElementId;
  protected String htmlFormElementType; //TYPE_SELECTED_SET or TYPE_SELECTED_ELEMENT
  protected String firstPage;
  protected String[] selectedSets;
  protected String[] selectedElements;
  protected boolean popupMode;
  protected boolean multiSelect;
  protected boolean setSelectable;
  protected boolean elementSelectable;
  protected String hostSpaceName;
  protected Pair<String, String> hostComponentName;
  protected Pair<String, String>[] hostPath;
  protected SelectionExtraParams extraParams;
  protected int selectedUserLimit;
  protected boolean filterOnDeactivatedState = true;

  public Selection() {
    resetAll();
  }

  public void resetAll() {
    registeredServerDomains = null;
    goBackURL = "";
    cancelURL = "";

    htmlFormName = "";
    htmlFormElementId = "";
    htmlFormElementName = "";

    firstPage = FIRST_PAGE_DEFAULT;

    selectedSets = new String[0];
    selectedElements = new String[0];

    popupMode = false;
    multiSelect = true;
    setSelectable = true;
    elementSelectable = true;

    hostSpaceName = "";
    hostComponentName = new Pair<>("", "");
    hostPath = new Pair[0];

    extraParams = null;
    selectedUserLimit = 0;
    filterOnDeactivatedState = true;
  }

  static public String getSelectionURL(String selectionType) {
    if (Selection.TYPE_USERS_GROUPS.equals(selectionType)) {
      return USER_SELECTION_PANEL_PATH;
    }
    return "/RselectionPeas/jsp/Main?SelectionType=" + selectionType;
  }

  public void setHostSpaceName(String hostSpaceName) {
    if (hostSpaceName != null) {
      this.hostSpaceName = hostSpaceName;
    } else {
      this.hostSpaceName = "";
    }
  }

  public String getHostSpaceName() {
    return hostSpaceName;
  }

  public void setHostComponentName(Pair<String, String> hostComponentName) {
    if (hostComponentName != null) {
      this.hostComponentName = hostComponentName;
    } else {
      this.hostComponentName = new Pair<>("", "");
    }
  }

  public Pair<String, String> getHostComponentName() {
    return hostComponentName;
  }

  public void setHostPath(Pair<String, String>[] hostPath) {
    if (hostPath != null) {
      this.hostPath = hostPath.clone();
    } else {
      this.hostPath = new Pair[0];
    }
  }

  public Pair<String, String>[] getHostPath() {
    return hostPath;
  }

  public String getCancelURL() {
    return cancelURL;
  }

  public void setCancelURL(String cancelURL) {
    if (cancelURL != null) {
      this.cancelURL = cancelURL;
    } else {
      this.cancelURL = "";
    }
  }

  public String getGoBackURL() {
    return goBackURL;
  }

  public void setGoBackURL(String goBackURL) {
    this.goBackURL = goBackURL;
  }

  public String getFirstPage() {
    return firstPage;
  }

  public void setFirstPage(String firstPage) {
    this.firstPage = firstPage;
  }

  public boolean isPopupMode() {
    return popupMode;
  }

  public void setPopupMode(boolean popupMode) {
    this.popupMode = popupMode;
  }

  /**
   * Is the set of fields with the selection could be done directly from the user panel ?
   * This can be done only if :
   * - the user panel is opened within a window popup (PopupMode = true),
   * - not with multi selection (MultiSelect = false) and
   * - the information about HTML form of the opener is provided (see the setHtmlForm kind methods).
   *
   * @return true if the user panel should modify directly the opener with the result of the
   * selection, false otherwise.
   */
  public boolean isHotSetting() {
    return StringUtil.isDefined(htmlFormName);
  }

  public boolean isMultiSelect() {
    return multiSelect;
  }

  public void setMultiSelect(boolean multiSelect) {
    this.multiSelect = multiSelect;
  }

  public boolean isSetSelectable() {
    return setSelectable;
  }

  public void setSetSelectable(boolean setSelectable) {
    this.setSelectable = setSelectable;
  }

  public boolean isElementSelectable() {
    return elementSelectable;
  }

  public void setElementSelectable(boolean elementSelectable) {
    this.elementSelectable = elementSelectable;
  }

  public String[] getSelectedElements() {
    return selectedElements;
  }

  public void setSelectedElements(String[] selectedElements) {
    if (selectedElements != null) {
      this.selectedElements = selectedElements.clone();
    } else {
      this.selectedElements = new String[0];
    }
  }

  public void setSelectedElements(Collection<String> selectedElements) {
    if (selectedElements != null) {
      setSelectedElements(selectedElements.toArray(new String[selectedElements.size()]));
    } else {
      setSelectedElements((String[]) null);
    }
  }

  public String getFirstSelectedElement() {
    if (selectedElements != null && selectedElements.length > 0 && StringUtil.isDefined(
        selectedElements[0])) {
      return selectedElements[0];
    }
    return null;
  }

  public String[] getSelectedSets() {
    return selectedSets;
  }

  public void setSelectedSets(String[] selectedSets) {
    if (selectedSets != null) {
      this.selectedSets = selectedSets.clone();
    } else {
      this.selectedSets = new String[0];
    }
  }

  public void setSelectedSets(Collection<String> selectedSets) {
    if (selectedSets != null) {
      setSelectedSets(selectedSets.toArray(new String[selectedSets.size()]));
    } else {
      setSelectedSets((String[]) null);
    }
  }

  public String getFirstSelectedSet() {
    if (selectedSets != null && selectedSets.length > 0 && StringUtil.isDefined(selectedSets[0])) {
      return selectedSets[0];
    }
    return null;
  }

  public SelectionExtraParams getExtraParams() {
    return extraParams;
  }

  public void setExtraParams(SelectionExtraParams extraParams) {
    this.extraParams = extraParams;
  }

  public String getHtmlFormElementId() {
    return htmlFormElementId;
  }

  public void setHtmlFormElementId(String formElementId) {
    htmlFormElementId = formElementId;
  }

  public String getHtmlFormElementName() {
    return htmlFormElementName;
  }

  public void setHtmlFormElementName(String formElementName) {
    htmlFormElementName = formElementName;
  }

  public String getHtmlFormName() {
    return htmlFormName;
  }

  public void setHtmlFormName(String formName) {
    htmlFormName = formName;
  }

  public String getHtmlFormElementType() { //TYPE_SELECTED_SET or TYPE_SELECTED_ELEMENT
    return htmlFormElementType;
  }

  public void setHtmlFormElementType(String formElementType) {
    htmlFormElementType = formElementType;
  }

  public int getSelectedUserLimit() {
    return selectedUserLimit;
  }

  public void setSelectedUserLimit(final int selectedUserLimit) {
    this.selectedUserLimit = selectedUserLimit;
  }

  /**
   * Gets all the domains registered on the server.
   */
  public List<Domain> getRegisteredServerDomains() {
    if (registeredServerDomains == null) {
      registeredServerDomains = CollectionUtil
          .asList(OrganizationControllerProvider.getOrganisationController().getAllDomains());
    }
    return registeredServerDomains;
  }

  public boolean isFilterOnDeactivatedState() {
    return filterOnDeactivatedState;
  }

  public void setFilterOnDeactivatedState(final boolean filterOnDeactivatedState) {
    this.filterOnDeactivatedState = filterOnDeactivatedState;
  }
}
