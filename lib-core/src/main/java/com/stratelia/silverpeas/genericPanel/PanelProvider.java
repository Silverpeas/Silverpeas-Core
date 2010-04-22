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

/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.genericPanel;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import com.stratelia.webactiv.util.ResourceLocator;

abstract public class PanelProvider {
  protected String m_PageName = "";
  protected String m_PageSubTitle = "";
  protected String[] m_ColumnsHeader = new String[0];
  protected PanelSearchToken[] m_SearchTokens = null;

  protected ResourceLocator m_rs = null;
  protected String m_Language = "fr";

  protected String[] m_Ids = new String[0];
  protected int m_FirstDisplayed = 0;
  protected int m_NbDisplayed = GenericPanelSettings.m_ElementsByPage;

  protected Hashtable<String, PanelLine> m_ElementsCache = new Hashtable<String, PanelLine>();
  protected HashSet<String> m_SelectedElements = new HashSet<String>();

  protected boolean m_FilterValid = true;

  protected PanelMiniFilterToken[] m_MiniFilters = new PanelMiniFilterToken[0];
  protected PanelMiniFilterSelect m_SelectMiniFilter = null;

  abstract public PanelLine getElementInfos(String id);

  abstract public void refresh(String[] filters);

  public void setMiniFilter(int filterIndex, String filterValue) {
  }

  public PanelMiniFilterSelect getSelectMiniFilter() {
    return m_SelectMiniFilter;
  }

  public void setSelectMiniFilter(PanelMiniFilterSelect selectMiniFilter) {
    m_SelectMiniFilter = selectMiniFilter;
  }

  public PanelProvider() {
  }

  public void initAll(String[] selectedIds) {
    resetAllCache();
    m_SelectedElements.clear();
    if (selectedIds != null) {
      for (int i = 0; i < selectedIds.length; i++) {
        m_SelectedElements.add(selectedIds[i]);
      }
    }
    m_FirstDisplayed = 0;
  }

  public void resetAllSelected() {
    m_SelectedElements.clear();
  }

  public void resetAllCache() {
    m_ElementsCache.clear();
  }

  public void resetOneCache(String id) {
    m_ElementsCache.remove(id);
  }

  public PanelLine getCachedElement(String id) {
    PanelLine valret = null;

    if (m_ElementsCache.get(id) == null) {
      m_ElementsCache.put(id, getElementInfos(id));
      valret = m_ElementsCache.get(id);
      if (valret != null) {
        valret.m_Selected = m_SelectedElements.contains(valret.m_Id);
      }
    } else {
      valret = m_ElementsCache.get(id);
    }
    return valret;
  }

  public void setSelectedElement(String id, boolean isSelected) {
    PanelLine theElement = getCachedElement(id);
    theElement.m_Selected = isSelected;
    if (isSelected) {
      m_SelectedElements.add(id);
    } else {
      m_SelectedElements.remove(id);
    }
  }

  public void setSelectedElements(Set<String> elements) {
    for (String element : elements) {
      setSelectedElement(element, true);
    }
  }

  public void unsetSelectedElements(Set<String> elements) {
    for (String element : elements) {
      setSelectedElement(element, false);
    }
  }

  public String[] getSelectedElements() {
    return m_SelectedElements.toArray(new String[0]);
  }

  public int getSelectedNumber() {
    return m_SelectedElements.size();
  }

  public boolean isFilterValid() {
    return m_FilterValid;
  }

  public PanelSearchToken[] getSearchTokens() {
    return m_SearchTokens;
  }

  public String[] getColumnsHeader() {
    return m_ColumnsHeader;
  }

  public String getPageName() {
    return m_PageName;
  }

  public String getPageSubTitle() {
    return m_PageSubTitle;
  }

  public int getElementNumber() {
    return m_Ids.length;
  }

  public int getNbMaxDisplayed() {
    if (m_NbDisplayed == -1) {
      return getElementNumber();
    } else {
      return m_NbDisplayed;
    }
  }

  public void nextPage() {
    if ((m_NbDisplayed != -1)
        && (m_Ids.length > (m_FirstDisplayed + m_NbDisplayed))) {
      m_FirstDisplayed += m_NbDisplayed;
    }
  }

  public void previousPage() {
    if ((m_NbDisplayed != -1) && (m_FirstDisplayed > 0)) {
      if (m_FirstDisplayed >= m_NbDisplayed) {
        m_FirstDisplayed -= m_NbDisplayed;
      } else {
        m_FirstDisplayed = 0;
      }
    }
  }

  public boolean isFirstPage() {
    if (m_NbDisplayed == -1) {
      return true;
    }
    return (m_FirstDisplayed == 0);
  }

  public boolean isLastPage() {
    if (m_NbDisplayed == -1) {
      return true;
    }
    return (m_Ids.length <= (m_FirstDisplayed + m_NbDisplayed));
  }

  public PanelLine[] getPage() {
    PanelLine[] valret = new PanelLine[m_Ids.length];

    for (int i = 0; i < valret.length; i++) {
      valret[i] = getCachedElement(m_Ids[i]);
    }
    return valret;
  }

  protected void verifIndexes() {
    /*
     * if (m_Ids.length <= m_FirstDisplayed) { if (m_Ids.length > 0) { m_FirstDisplayed =
     * m_Ids.length - 1; } else { m_FirstDisplayed = 0; } }
     */
    m_FirstDisplayed = 0;
  }

  protected String getSureString(String s) {
    if (s == null) {
      return "";
    } else {
      return s;
    }
  }
}
