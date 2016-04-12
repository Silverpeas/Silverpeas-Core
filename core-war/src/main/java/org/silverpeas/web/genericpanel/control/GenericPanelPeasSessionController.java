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

/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

package org.silverpeas.web.genericpanel.control;

import java.util.Set;

import org.silverpeas.core.web.panel.GenericPanel;
import org.silverpeas.core.web.panel.PanelLine;
import org.silverpeas.core.web.panel.PanelOperation;
import org.silverpeas.core.web.panel.PanelProvider;
import org.silverpeas.core.web.panel.PanelSearchToken;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.Pair;

/**
 * Class declaration
 * @author
 */
public class GenericPanelPeasSessionController extends AbstractComponentSessionController {
  GenericPanel m_Panel = null;
  PanelProvider m_Nav = null;

  /**
   * Standard Session Controller Constructeur
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   * @see
   */
  public GenericPanelPeasSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    super(
        mainSessionCtrl,
        componentContext,
        "org.silverpeas.genericPanelPeas.multilang.genericPanelPeasBundle",
        "org.silverpeas.genericPanelPeas.settings.genericPanelPeasIcons");
    setComponentRootName(URLUtil.CMP_GENERICPANELPEAS);
  }

  public void initSC(String panelKey) {
    m_Panel = getGenericPanel(panelKey);
    m_Nav = m_Panel.getPanelProvider();
    m_Nav.initAll(m_Panel.getSelectedElements());
  }

  public void nextUserPage() {
    m_Nav.nextPage();
  }

  public void previousUserPage() {
    m_Nav.previousPage();
  }

  public void setFilters(String[] filters) {
    m_Nav.refresh(filters);
  }

  public PanelSearchToken[] getSearchTokens() {
    return m_Nav.getSearchTokens();
  }

  public String getSearchUsersNumber() {
    return Integer.toString(m_Nav.getElementNumber());
  }

  public String getPageName() {
    return m_Nav.getPageName();
  }

  public String getPageSubTitle() {
    return m_Nav.getPageSubTitle();
  }

  public boolean[] getPageNavigation() {
    boolean[] valret = new boolean[2];
    valret[0] = (!m_Nav.isFirstPage());
    valret[1] = (!m_Nav.isLastPage());
    return valret;
  }

  public PanelOperation[] getPanelOperations() {
    return m_Panel.getPanelOperations();
  }

  public boolean isSelectable() {
    return m_Panel.isSelectable();
  }

  public boolean isFilterValid() {
    return m_Nav.isFilterValid();
  }

  public boolean isMultiSelect() {
    return m_Panel.isMultiSelect();
  }

  public boolean isZoomToItemValid() {
    return ((m_Panel.getZoomToItemURL() != null) && (m_Panel.getZoomToItemURL()
        .length() > 0));
  }

  public String getSelectedNumber() {
    return Integer.toString(m_Nav.getSelectedNumber());
  }

  public void setSelectedUsers(String operation) {
    m_Panel.setSelectedElements(m_Nav.getSelectedElements());
    m_Panel.setSelectedOperation(operation);
  }

  public void setSelectedUser(String id, String operation) {
    if ((id != null) && (id.length() > 0)) {
      m_Nav.resetAllSelected();
      m_Nav.setSelectedElement(id, true);
      String[] selectedIds = new String[1];
      selectedIds[0] = id;
      m_Panel.setSelectedElements(selectedIds);
    }
    m_Panel.setSelectedOperation(operation);
  }

  public void setMiniFilter(String theValue, String theFilter) {
    m_Nav.setMiniFilter(Integer.parseInt(theFilter.substring(3)), theValue);
  }

  public String getMiniFilterString() {
    if (m_Nav.getSelectMiniFilter() != null) {
      return m_Nav.getSelectMiniFilter().getHTMLDisplay();
    } else {
      return "";
    }
  }

  public boolean isPopupMode() {
    return m_Panel.isPopupMode();
  }

  public String getGoBackURL() {
    return getSureString(m_Panel.getGoBackURL());
  }

  public String getCancelURL() {
    return getSureString(m_Panel.getCancelURL());
  }

  public String getZoomToItemURL() {
    return getSureString(m_Panel.getZoomToItemURL());
  }

  public String[] getColumnsHeader() {
    return m_Nav.getColumnsHeader();
  }

  public Pair getHostComponentName() {
    return m_Panel.getHostComponentName();
  }

  public String getHostSpaceName() {
    return m_Panel.getHostSpaceName();
  }

  public Pair[] getHostPath() {
    return m_Panel.getHostPath();
  }

  public PanelLine[] getPage() {
    return m_Nav.getPage();
  }

  public int getNbMaxDisplayed() {
    return m_Nav.getNbMaxDisplayed();
  }

  public void setSelectedElements(Set<String> elements) {
    m_Nav.setSelectedElements(elements);
  }

  protected String getSureString(String s) {
    if (s == null) {
      return "";
    } else {
      return s;
    }
  }
}
