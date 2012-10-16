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
* FLOSS exception. You should have received a copy of the text describing
* the FLOSS exception, and it is also available here:
* "http://repository.silverpeas.com/legal/licensing"
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.stratelia.silverpeas.selectionPeas.control;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.genericPanel.GenericPanel;
import com.stratelia.silverpeas.genericPanel.PanelLine;
import com.stratelia.silverpeas.genericPanel.PanelOperation;
import com.stratelia.silverpeas.genericPanel.PanelProvider;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.selection.SelectionJdbcParams;
import com.stratelia.silverpeas.selectionPeas.*;
import com.stratelia.silverpeas.selectionPeas.jdbc.JdbcConnectorSetting;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.PairObject;
import java.util.*;

/**
* Class declaration
*
* @author
*/
public class SelectionPeasSessionController extends AbstractComponentSessionController {

  protected Selection selection = null;
  protected GenericPanel searchSetPanel = null;
  protected GenericPanel searchElementPanel = null;
  protected CacheManager cacheManager = null;
  protected List<PanelLine> panelLineList = new ArrayList<PanelLine>();
  protected Map<CacheType, BrowsePanelProvider> m_NavBrowse = new EnumMap<CacheType, BrowsePanelProvider>(
      CacheType.class);
  protected Map<CacheType, PanelProvider> m_NavCart = new EnumMap<CacheType, PanelProvider>(
      CacheType.class);
  protected String selectionType = "";

  /**
* Standard Session Controller Constructor
*
* @param mainSessionCtrl The user's profile
* @param componentContext The component's profile
* @see
*/
  public SelectionPeasSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "com.stratelia.silverpeas.selectionPeas.multilang.selectionPeasBundle",
        "com.stratelia.silverpeas.selectionPeas.settings.selectionPeasIcons");
    setComponentRootName(URLManager.CMP_SELECTIONPEAS);
  }

  public String getSelectionType() {
    return selectionType;
  }

  public void initSC(String selectionType) {
    SilverTrace.info("selectionPeas", "SelectionPeasSessionController.initSC",
        "root.MSG_GEN_PARAM_VALUE", "SelectionType=" + selectionType);
    searchSetPanel = null;
    searchElementPanel = null;
    selection = getSelection();
    m_NavBrowse.remove(CacheType.CM_SET);
    m_NavBrowse.remove(CacheType.CM_ELEMENT);
    panelLineList.clear();
    this.selectionType = selectionType;

    if (Selection.TYPE_USERS_GROUPS.equals(this.selectionType)) {
      cacheManager = new CacheManagerUsersGroups(getLanguage(), getMultilang(),
          getIcon(), selection, getUserDetail());
    } else if (Selection.TYPE_JDBC_CONNECTOR.equals(this.selectionType)) {
      selection.setMultiSelect(false);
      selection.setPopupMode(true);
      selection.setHostComponentName(new PairObject("Sélection d'un élément", null));
      selection.setHostPath(null);
      selection.setElementSelectable(false);
      cacheManager = new CacheManagerJdbcConnector(getLanguage(), getMultilang(), getIcon(),
          selection);
    }
    // Preset Ids
    cacheManager.setSelected(CacheType.CM_ELEMENT, selection.getSelectedElements(), true);
    cacheManager.setSelected(CacheType.CM_SET, selection.getSelectedSets(), true);
  }

  public void updateJdbcParameters(JdbcConnectorSetting jdbcSetting, String tableName,
      String columnsNames, String formIndex, String fieldsNames) {
    SelectionJdbcParams selectionJdbcParams = new SelectionJdbcParams(
        jdbcSetting.getDriverClassName(), jdbcSetting.getUrl(), jdbcSetting.getLogin(),
        jdbcSetting.getPassword(), tableName, columnsNames, formIndex, fieldsNames);
    selection.setExtraParams(selectionJdbcParams);
  }

  public boolean isMultiSelect() {
    return selection.isMultiSelect();
  }

  public String getZoomToSetURL() {
    return URLManager.getApplicationURL() + getComponentUrl() + "ZoomToSetInfos";
  }

  public String getZoomToElementURL() {
    return URLManager.getApplicationURL() + getComponentUrl() + "ZoomToElementInfos";
  }

  public String getStartingFunction() {
    String theStartingPage;
    if (!(Selection.FIRST_PAGE_DEFAULT.equalsIgnoreCase(SelectionPeasSettings.firstPage))) {
      theStartingPage = SelectionPeasSettings.firstPage;
    } else {
      if (Selection.FIRST_PAGE_DEFAULT.equalsIgnoreCase(selection.getFirstPage())) {
        theStartingPage = SelectionPeasSettings.defaultPage;
      } else {
        theStartingPage = selection.getFirstPage();
      }
    }
    if (Selection.FIRST_PAGE_SEARCH_ELEMENT.equalsIgnoreCase(theStartingPage)
        && (!selection.isElementSelectable())) {
      if (selection.isSetSelectable()) {
        theStartingPage = Selection.FIRST_PAGE_SEARCH_SET;
      } else {
        theStartingPage = Selection.FIRST_PAGE_BROWSE;
      }
    }
    if (Selection.FIRST_PAGE_SEARCH_SET.equalsIgnoreCase(theStartingPage)
        && (!selection.isSetSelectable())) {
      if (selection.isElementSelectable()) {
        theStartingPage = Selection.FIRST_PAGE_SEARCH_ELEMENT;
      } else {
        theStartingPage = Selection.FIRST_PAGE_BROWSE;
      }
    }
    if (Selection.FIRST_PAGE_CART.equalsIgnoreCase(theStartingPage)
        && (!selection.isMultiSelect())) {
      if (selection.isSetSelectable()) {
        theStartingPage = Selection.FIRST_PAGE_SEARCH_SET;
      } else if (selection.isElementSelectable()) {
        theStartingPage = Selection.FIRST_PAGE_SEARCH_ELEMENT;
      } else {
        theStartingPage = Selection.FIRST_PAGE_BROWSE;
      }
    }
    return theStartingPage;
  }

  public String getSearchSet() {
    if (searchSetPanel == null) {
      searchSetPanel = new GenericPanel();
      searchSetPanel.setCancelURL(URLManager.getApplicationURL() + getComponentUrl() + "Cancel");
      searchSetPanel.setGoBackURL(URLManager.getApplicationURL() + getComponentUrl()
          + "ReturnSearchSet");
      searchSetPanel.setZoomToItemURL(getZoomToSetURL());
      searchSetPanel.setPopupMode(false);
      searchSetPanel.setMultiSelect(selection.isMultiSelect());
      searchSetPanel.setSelectable(selection.isSetSelectable());
      searchSetPanel.setZoomToItemInPopup(true);
      searchSetPanel.setPanelOperations(getOperations("DisplaySearchSet"));
      searchSetPanel.setPanelProvider(cacheManager.getSearchPanelProvider(CacheType.CM_SET,
          selection.getExtraParams()));
      searchSetPanel.setHostSpaceName(selection.getHostSpaceName());
      if (selection.isPopupMode()) {
        PairObject po = null;
        PairObject[] emptyapo = null;

        if (selection.getHostComponentName() != null) {
          po = new PairObject(selection.getHostComponentName().getFirst(), "");
        }
        searchSetPanel.setHostComponentName(po);
        PairObject[] apo = selection.getHostPath();
        if (apo != null) {
          emptyapo = new PairObject[apo.length];
          for (int i = 0; i < apo.length; i++) {
            if (apo[i] != null) {
              emptyapo[i] = new PairObject(apo[i].getFirst(), "");
            } else {
              emptyapo[i] = null;
            }
          }
        }
        searchSetPanel.setHostPath(emptyapo);
      } else {
        searchSetPanel.setHostComponentName(selection.getHostComponentName());
        searchSetPanel.setHostPath(selection.getHostPath());
      }
      setGenericPanel("SearchSet", searchSetPanel);
    }
    return GenericPanel.getGenericPanelURL("SearchSet");
  }

  public String returnSearchSet() {
    GenericPanel gp = getGenericPanel("SearchSet");
    String theOperation = gp.getSelectedOperation();
    if (GenericPanel.OPERATION_VALIDATE.equals(theOperation)) {
      return "Validate";
    }
    return theOperation;
  }

  public String getSearchElement() {
    if (searchElementPanel == null) {
      searchElementPanel = new GenericPanel();
      searchElementPanel.setCancelURL(URLManager.getApplicationURL() + getComponentUrl()
          + "Cancel");
      searchElementPanel.setGoBackURL(URLManager.getApplicationURL() + getComponentUrl()
          + "ReturnSearchElement");
      searchElementPanel.setZoomToItemURL(getZoomToElementURL());
      searchElementPanel.setPopupMode(false);
      searchElementPanel.setMultiSelect(selection.isMultiSelect());
      searchElementPanel.setSelectable(selection.isElementSelectable());
      searchElementPanel.setZoomToItemInPopup(true);
      searchElementPanel
          .setPanelOperations(getOperations("DisplaySearchElement"));
      searchElementPanel.setPanelProvider(cacheManager.getSearchPanelProvider(
          CacheType.CM_ELEMENT, selection.getExtraParams()));
      searchElementPanel.setHostSpaceName(selection.getHostSpaceName());
      if (selection.isPopupMode()) {
        PairObject po = null;
        PairObject[] emptyapo = null;

        if (selection.getHostComponentName() != null) {
          po = new PairObject(selection.getHostComponentName().getFirst(), "");
        }
        searchElementPanel.setHostComponentName(po);
        PairObject[] apo = selection.getHostPath();
        if (apo != null) {
          emptyapo = new PairObject[apo.length];
          for (int i = 0; i < apo.length; i++) {
            if (apo[i] != null) {
              emptyapo[i] = new PairObject(apo[i].getFirst(), "");
            } else {
              emptyapo[i] = null;
            }
          }
        }
        searchElementPanel.setHostPath(emptyapo);
      } else {
        searchElementPanel.setHostComponentName(selection.getHostComponentName());
        searchElementPanel.setHostPath(selection.getHostPath());
      }
      setGenericPanel("SearchElement", searchElementPanel);
    }
    return GenericPanel.getGenericPanelURL("SearchElement");
  }

  public String returnSearchElement() {
    GenericPanel gp = getGenericPanel("SearchElement");
    String theOperation = gp.getSelectedOperation();
    if (GenericPanel.OPERATION_VALIDATE.equals(theOperation)) {
      return "Validate";
    }
    return theOperation;
  }

  public String getGoBackURL() {
    return getSureString(selection.getGoBackURL());
  }

  public String getCancelURL() {
    return getSureString(selection.getCancelURL());
  }

  public boolean isPopup() {
    return selection.isPopupMode();
  }

  public boolean isSetSelectable() {
    return selection.isSetSelectable();
  }

  public boolean isElementSelectable() {
    return selection.isElementSelectable();
  }

  public PairObject getHostComponentName() {
    return selection.getHostComponentName();
  }

  public String getHostSpaceName() {
    return selection.getHostSpaceName();
  }

  public PairObject[] getHostPath() {
    return selection.getHostPath();
  }

  public PanelLine[] getSetPath() {
    return panelLineList.toArray(new PanelLine[panelLineList.size()]);
  }

  public String[][] getInfos(CacheType what, String theId) {
    return cacheManager.getContentInfos(what, theId);
  }

  public String getContentText(CacheType what) {
    return cacheManager.getContentText(what);
  }

  public String[] getContentColumns(CacheType what) {
    return cacheManager.getContentColumnsNames(what);
  }

  public String[][] getContent(CacheType what, String theId) {
    return cacheManager.getContentLines(what, theId);
  }

  public PanelOperation[] getOperations(String currentFunction) {
    List<PanelOperation> panelOperationList = new ArrayList<PanelOperation>();

    panelOperationList.add(cacheManager.getPanelOperation("DisplayBrowse"));
    if (selection.isElementSelectable()) {
      panelOperationList.add(cacheManager.getPanelOperation("DisplaySearchElement"));
    }
    if (selection.isSetSelectable()) {
      panelOperationList.add(cacheManager.getPanelOperation("DisplaySearchSet"));
    }
    if (selection.isMultiSelect()) {
      panelOperationList.add(new PanelOperation(getString("selectionPeas.helpCart"),
          URLManager.getApplicationURL() + getIcon().getString("selectionPeas.showPanier"),
          "DisplayCart"));
    }
    if ("DisplayCart".equals(currentFunction)) {
      panelOperationList.add(new PanelOperation("", "", ""));
      panelOperationList.add(new PanelOperation(getString("selectionPeas.removeFromCart"),
          URLManager.getApplicationURL() + getIcon().getString("selectionPeas.selectDelete"),
          "RemoveSelectedFromCart",
          getString("selectionPeas.confirmRemoveFromCart")));
      panelOperationList.add(new PanelOperation(getString("selectionPeas.removeAllFromCart"),
          URLManager.getApplicationURL() + getIcon().getString("selectionPeas.allDelete"),
          "RemoveAllFromCart",
          getString("selectionPeas.confirmRemoveFromCart")));
    }
    return panelOperationList.toArray(new PanelOperation[panelOperationList.size()]);
  }

  public void validate() {
    selection.setSelectedSets(cacheManager.getSelectedIds(CacheType.CM_SET));
    selection.setSelectedElements(cacheManager.getSelectedIds(CacheType.CM_ELEMENT));
  }

  protected String getSureString(String s) {
    if (s == null) {
      return "";
    }
    return s;
  }

  public void initBrowse() {
    if (!m_NavBrowse.containsKey(CacheType.CM_SET)) {
      m_NavBrowse.put(CacheType.CM_SET, cacheManager.getBrowsePanelProvider(
          CacheType.CM_SET, selection.getExtraParams()));
    }
    if (!m_NavBrowse.containsKey(CacheType.CM_ELEMENT)) {
      m_NavBrowse.put(CacheType.CM_ELEMENT, cacheManager.getBrowsePanelProvider(
          CacheType.CM_ELEMENT, selection.getExtraParams()));
    }
  }

  public String getSelectedNumber() {
    return Integer.toString(m_NavBrowse.get(CacheType.CM_SET).getSelectedNumber()
        + m_NavBrowse.get(CacheType.CM_ELEMENT).getSelectedNumber());
  }

  public String getText(CacheType what) {
    return m_NavBrowse.get(what).getPageName();
  }

  public boolean[] getNavigation(CacheType what) {
    return new boolean[]{!m_NavBrowse.get(what).isFirstPage(), !m_NavBrowse.get(what).isLastPage()};
  }

  public void setSelected(CacheType what, Set<String> selectedSets, Set<String> unselectedSets) {
    m_NavBrowse.get(what).setSelectedElements(selectedSets);
    m_NavBrowse.get(what).unsetSelectedElements(unselectedSets);
  }

  public void setOneSelected(CacheType what, String selected) {
    cacheManager.unselectAll();
    cacheManager.setSelected(what, selected, true);
  }

  public String[] getColumnsHeader(CacheType what) {
    return m_NavBrowse.get(what).getColumnsHeader();
  }

  public PanelLine[] getPage(CacheType what) {
    return m_NavBrowse.get(what).getPage();
  }

  public String getMiniFilterString(CacheType what) {
    return m_NavBrowse.get(what).getSelectMiniFilter().getHTMLDisplay();
  }

  public void setMiniFilter(String theValue, String theFilter) {
    CacheType what = CacheType.extractValue(theFilter.substring(1, 2));
    m_NavBrowse.get(what).setMiniFilter(Integer.parseInt(theFilter.substring(3)), theValue);
  }

  public void setParentSet(String parentSetId) {
    int parc = 0;

    m_NavBrowse.get(CacheType.CM_SET).setNewParentSet(parentSetId);
    m_NavBrowse.get(CacheType.CM_ELEMENT).setNewParentSet(parentSetId);
    if (!StringUtil.isDefined(parentSetId) || "-1".equals(parentSetId)) {
      panelLineList.clear();
    } else {
      while ((parc < panelLineList.size())
          && (!parentSetId.equals(panelLineList.get(parc).m_Id))) {
        parc++;
      }
      if (parc < panelLineList.size()) {
        parc++;
        while (parc < panelLineList.size()) { // Group found -> go back to it
          panelLineList.remove(parc);
        }
      } else {
        panelLineList.add(cacheManager.getInfos(CacheType.CM_SET, parentSetId));
      }
    }
  }

  public void initCart() {
    m_NavCart.put(CacheType.CM_SET, cacheManager.getCartPanelProvider(
        CacheType.CM_SET, selection.getExtraParams()));
    m_NavCart.put(CacheType.CM_ELEMENT, cacheManager.getCartPanelProvider(
        CacheType.CM_ELEMENT, selection.getExtraParams()));
  }

  public String getCartSelectedNumber() {
    return Integer.toString(m_NavCart.get(CacheType.CM_SET).getSelectedNumber()
        + m_NavCart.get(CacheType.CM_ELEMENT).getSelectedNumber());
  }

  public String getCartText(CacheType what) {
    return m_NavCart.get(what).getPageName();
  }

  public String[] getCartColumnsHeader(CacheType what) {
    return m_NavCart.get(what).getColumnsHeader();
  }

  public boolean[] getCartNavigation(CacheType what) {
    return new boolean[]{!m_NavCart.get(what).isFirstPage(), !m_NavCart.get(what).isLastPage()};
  }

  public void setCartSelected(CacheType what, Set<String> selectedSets,
      Set<String> unselectedSets) {
    m_NavCart.get(what).setSelectedElements(selectedSets);
    m_NavCart.get(what).unsetSelectedElements(unselectedSets);
  }

  public PanelLine[] getCartPage(CacheType what) {
    return m_NavCart.get(what).getPage();
  }

  public String getCartMiniFilterString(CacheType what) {
    return m_NavCart.get(what).getSelectMiniFilter().getHTMLDisplay();
  }

  public void setCartMiniFilter(String theValue, String theFilter) {
    CacheType what = CacheType.extractValue(theFilter.substring(1, 2));
    m_NavCart.get(what).setMiniFilter(Integer.parseInt(theFilter.substring(3)), theValue);
  }

  public void removeAllFromCart() {
    cacheManager.unselectAll();
  }

  public void removeSelectedFromCart() {
    String[] sel = m_NavCart.get(CacheType.CM_SET).getSelectedElements();
    for (int i = 0; i < sel.length; i++) {
      cacheManager.setSelected(CacheType.CM_SET, sel[i], false);
    }
    sel = m_NavCart.get(CacheType.CM_ELEMENT).getSelectedElements();
    for (int i = 0; i < sel.length; i++) {
      cacheManager.setSelected(CacheType.CM_ELEMENT, sel[i], false);
    }
  }

}