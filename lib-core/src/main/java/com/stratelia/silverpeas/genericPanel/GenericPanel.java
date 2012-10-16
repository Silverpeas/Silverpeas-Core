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

/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.genericPanel;

import com.silverpeas.util.ArrayUtil;
import com.stratelia.silverpeas.util.PairObject;

public class GenericPanel {
  final static public String OPERATION_VALIDATE = "GENERICPANELVALIDATE";

  protected String m_goBackURL;
  protected String m_cancelURL;
  protected String m_zoomToItemURL;

  protected String[] m_selectedElements;
  protected PanelProvider m_panelProvider;
  protected boolean m_popupMode;
  protected boolean m_multiSelect;
  protected boolean m_selectable;
  protected boolean m_zoomToItemInPopup;

  protected PanelOperation[] m_panelOperations;
  protected String m_selectedOperation;

  protected String m_hostSpaceName;
  protected PairObject m_hostComponentName;
  protected PairObject[] m_hostPath;

  public GenericPanel() {
    resetAll();
  }

  public void resetAll() {
    m_goBackURL = "";
    m_cancelURL = "";
    m_zoomToItemURL = null;
    m_selectedElements = ArrayUtil.EMPTY_STRING_ARRAY;
    m_panelOperations = new PanelOperation[0];
    m_panelProvider = null;
    m_popupMode = false;
    m_multiSelect = false;
    m_selectable = true;
    m_zoomToItemInPopup = false;
    m_selectedOperation = "";
    m_hostSpaceName = "";
    m_hostComponentName = new PairObject("", "");
    m_hostPath = new PairObject[0];
  }

  static public String getGenericPanelURL(String panelKey) {
    return "/RgenericPanelPeas/jsp/Main?PanelKey=" + panelKey;
  }

  public void setHostSpaceName(String hostSpaceName) {
    m_hostSpaceName = hostSpaceName;
  }

  public String getHostSpaceName() {
    return m_hostSpaceName;
  }

  public void setHostComponentName(PairObject hostComponentName) {
    m_hostComponentName = hostComponentName;
  }

  public PairObject getHostComponentName() {
    return m_hostComponentName;
  }

  public void setHostPath(PairObject[] hostPath) {
    m_hostPath = hostPath;
  }

  public PairObject[] getHostPath() {
    return m_hostPath;
  }

  public String getCancelURL() {
    return m_cancelURL;
  }

  public void setCancelURL(String cancelURL) {
    m_cancelURL = cancelURL;
  }

  public String getGoBackURL() {
    return m_goBackURL;
  }

  public void setGoBackURL(String goBackURL) {
    m_goBackURL = goBackURL;
  }

  // WARNING : ZoomToItem must not contains any extra parameter !!!! It will be
  // called with elementId parameter
  public String getZoomToItemURL() {
    return m_zoomToItemURL;
  }

  public void setZoomToItemURL(String zoomToItemURL) {
    m_zoomToItemURL = zoomToItemURL;
  }

  public PanelProvider getPanelProvider() {
    return m_panelProvider;
  }

  public void setPanelProvider(PanelProvider panelProvider) {
    m_panelProvider = panelProvider;
  }

  public boolean isPopupMode() {
    return m_popupMode;
  }

  public void setPopupMode(boolean popupMode) {
    m_popupMode = popupMode;
  }

  public boolean isMultiSelect() {
    return m_multiSelect;
  }

  public void setMultiSelect(boolean multiSelect) {
    m_multiSelect = multiSelect;
  }

  public boolean isSelectable() {
    return m_selectable;
  }

  public void setSelectable(boolean selectable) {
    m_selectable = selectable;
  }

  public boolean isZoomToItemInPopup() {
    return m_zoomToItemInPopup;
  }

  public void setZoomToItemInPopup(boolean zoomToItemInPopup) {
    m_zoomToItemInPopup = zoomToItemInPopup;
  }

  public PanelOperation[] getPanelOperations() {
    return m_panelOperations;
  }

  public void setPanelOperations(PanelOperation[] panelOperations) {
    m_panelOperations = panelOperations;
  }

  public String getSelectedOperation() {
    return m_selectedOperation;
  }

  public void setSelectedOperation(String selectedOperation) {
    m_selectedOperation = selectedOperation;
  }

  public String[] getSelectedElements() {
    return m_selectedElements;
  }

  public void setSelectedElements(String[] selectedElements) {
    m_selectedElements = selectedElements;
  }
}
