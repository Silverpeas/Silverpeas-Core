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

package com.stratelia.silverpeas.selection;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.util.PairObject;

public class Selection {
  final public static String TYPE_USERS_GROUPS = "UsersGroups";
  final public static String TYPE_SPACES_COMPONENTS = "SpacesComponents";
  final public static String TYPE_JDBC_CONNECTOR = "JdbcConnector";

  final public static String FIRST_PAGE_DEFAULT = "Default";
  final public static String FIRST_PAGE_CART = "DisplayCart";
  final public static String FIRST_PAGE_SEARCH_ELEMENT = "DisplaySearchElement";
  final public static String FIRST_PAGE_SEARCH_SET = "DisplaySearchSet";
  final public static String FIRST_PAGE_BROWSE = "DisplayBrowse";

  protected String goBackURL;
  protected String cancelURL;

  protected String htmlFormName;
  protected String htmlFormElementName;
  protected String htmlFormElementId;

  protected String firstPage;

  protected String[] selectedSets;
  protected String[] selectedElements;

  protected boolean popupMode;
  protected boolean multiSelect;
  protected boolean setSelectable;
  protected boolean elementSelectable;

  protected String hostSpaceName;
  protected PairObject hostComponentName;
  protected PairObject[] hostPath;

  protected SelectionExtraParams extraParams;

  public Selection() {
    resetAll();
  }

  public void resetAll() {
    goBackURL = "";
    cancelURL = "";

    firstPage = FIRST_PAGE_DEFAULT;

    selectedSets = new String[0];
    selectedElements = new String[0];

    popupMode = false;
    multiSelect = true;
    setSelectable = true;
    elementSelectable = true;

    hostSpaceName = "";
    hostComponentName = new PairObject("", "");
    hostPath = new PairObject[0];

    extraParams = null;
  }

  static public String getSelectionURL(String selectionType) {
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

  public void setHostComponentName(PairObject hostComponentName) {
    if (hostComponentName != null) {
      this.hostComponentName = hostComponentName;
    } else {
      this.hostComponentName = new PairObject("", "");
    }
  }

  public PairObject getHostComponentName() {
    return hostComponentName;
  }

  public void setHostPath(PairObject[] hostPath) {
    if (hostPath != null) {
      this.hostPath = hostPath;
    } else {
      this.hostPath = new PairObject[0];
    }
  }

  public PairObject[] getHostPath() {
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
      this.selectedElements = selectedElements;
    } else {
      this.selectedElements = new String[0];
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
      this.selectedSets = selectedSets;
    } else {
      this.selectedSets = new String[0];
    }
  }

  public String getFirstSelectedSet() {
    if (selectedSets != null && selectedSets.length > 0 && StringUtil.isDefined(selectedSets[0])) {
     return selectedSets [0];
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
}
