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

package org.silverpeas.core.web.panel;

import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.LocalizationBundle;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

abstract public class PanelProvider {
  protected String pageName = "";
  protected String pageSubTitle = "";
  protected String[] columnHeaders = new String[0];
  protected PanelSearchToken[] searchTokens = null;

  protected LocalizationBundle messages = null;
  protected String language = DisplayI18NHelper.getDefaultLanguage();

  protected String[] ids = new String[0];
  protected int m_FirstDisplayed = 0;
  protected int nbDisplayed = GenericPanelSettings.m_ElementsByPage;

  protected Map<String, PanelLine> elementsCache = new HashMap<>();
  protected Set<String> selectedElements = new HashSet<>();

  protected boolean m_FilterValid = true;

  protected PanelMiniFilterToken[] miniFilters = new PanelMiniFilterToken[0];
  protected PanelMiniFilterSelect selectMiniFilter = null;

  abstract public PanelLine getElementInfos(String id);

  abstract public void refresh(String[] filters);

  public void setMiniFilter(int filterIndex, String filterValue) {
  }

  public PanelMiniFilterSelect getSelectMiniFilter() {
    return selectMiniFilter;
  }

  public void setSelectMiniFilter(PanelMiniFilterSelect selectMiniFilter) {
    this.selectMiniFilter = selectMiniFilter;
  }

  public PanelProvider() {
  }

  public void initAll(String[] selectedIds) {
    resetAllCache();
    selectedElements.clear();
    if (selectedIds != null) {
      Collections.addAll(selectedElements, selectedIds);
    }
    m_FirstDisplayed = 0;
  }

  public void resetAllSelected() {
    selectedElements.clear();
  }

  public void resetAllCache() {
    elementsCache.clear();
  }

  public void resetOneCache(String id) {
    elementsCache.remove(id);
  }

  public PanelLine getCachedElement(String id) {
    PanelLine result;

    if (elementsCache.get(id) == null) {
      elementsCache.put(id, getElementInfos(id));
      result = elementsCache.get(id);
      if (result != null) {
        result.m_Selected = selectedElements.contains(result.m_Id);
      }
    } else {
      result = elementsCache.get(id);
    }
    return result;
  }

  public void setSelectedElement(String id, boolean isSelected) {
    PanelLine theElement = getCachedElement(id);
    theElement.m_Selected = isSelected;
    if (isSelected) {
      selectedElements.add(id);
    } else {
      selectedElements.remove(id);
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
    return selectedElements.toArray(new String[selectedElements.size()]);
  }

  public int getSelectedNumber() {
    return selectedElements.size();
  }

  public boolean isFilterValid() {
    return m_FilterValid;
  }

  public PanelSearchToken[] getSearchTokens() {
    return searchTokens;
  }

  public String[] getColumnsHeader() {
    return columnHeaders;
  }

  public String getPageName() {
    return pageName;
  }

  public String getPageSubTitle() {
    return pageSubTitle;
  }

  public int getElementNumber() {
    return ids.length;
  }

  public int getNbMaxDisplayed() {
    if (nbDisplayed == -1) {
      return getElementNumber();
    } else {
      return nbDisplayed;
    }
  }

  public void nextPage() {
    if ((nbDisplayed != -1)
        && (ids.length > (m_FirstDisplayed + nbDisplayed))) {
      m_FirstDisplayed += nbDisplayed;
    }
  }

  public void previousPage() {
    if ((nbDisplayed != -1) && (m_FirstDisplayed > 0)) {
      if (m_FirstDisplayed >= nbDisplayed) {
        m_FirstDisplayed -= nbDisplayed;
      } else {
        m_FirstDisplayed = 0;
      }
    }
  }

  public boolean isFirstPage() {
    return nbDisplayed == -1 || (m_FirstDisplayed == 0);
  }

  public boolean isLastPage() {
    return nbDisplayed == -1 || (ids.length <= (m_FirstDisplayed + nbDisplayed));
  }

  public PanelLine[] getPage() {
    PanelLine[] valRet = new PanelLine[ids.length];

    for (int i = 0; i < valRet.length; i++) {
      valRet[i] = getCachedElement(ids[i]);
    }
    return valRet;
  }

  protected void verifIndexes() {
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
