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
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.selectionPeas;

import java.util.Set;

import com.stratelia.silverpeas.genericPanel.PanelLine;
import com.stratelia.silverpeas.genericPanel.PanelMiniFilterSelect;
import com.stratelia.silverpeas.genericPanel.PanelProvider;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;

abstract public class BrowsePanelProvider extends PanelProvider {
  protected CacheManager m_Cm = null;
  protected ResourceLocator m_Message = null;
  protected OrganizationController m_oc = new OrganizationController();
  protected String m_ParentGroupId = "";
  protected String m_ParentGroupName = "";
  protected int m_what;

  public BrowsePanelProvider(String language, ResourceLocator rs,
      CacheManager cm, int what) {
    // Set the language
    m_Language = language;
    m_Message = GeneralPropertiesManager.getGeneralMultilang(m_Language);
    m_rs = rs;

    // Set the cache manager
    m_Cm = cm;
    m_what = what;

    // Set column headers
    m_ColumnsHeader = m_Cm.getColumnsNames(m_what);
  }

  abstract public void setNewParentSet(String newSetId);

  // OVERWRITE THIS FUNCTION : The cache is already managed by CacheManager
  public PanelLine getCachedElement(String id) {
    return getElementInfos(id);
  }

  public PanelLine getElementInfos(String id) {
    return m_Cm.getInfos(m_what, id);
  }

  // OVERWRITE THIS FUNCTION : The cache is already managed by CacheManager
  public void setSelectedElements(Set<String> elements) {
    for (String element : elements) {
      m_Cm.setSelected(m_what, element, true);
    }
  }

  public void unsetSelectedElements(Set<String> elements) {
    for (String element : elements) {
      m_Cm.setSelected(m_what, element, false);
    }
  }

  // OVERWRITE THIS FUNCTION : The cache is already managed by CacheManager
  public void setSelectedElement(String id, boolean isSelected) {
    m_Cm.setSelected(m_what, id, isSelected);
  }

  // OVERWRITE THIS FUNCTION : The cache is already managed by CacheManager
  public String[] getSelectedElements() {
    return m_Cm.getSelectedIds(m_what);
  }

  // OVERWRITE THIS FUNCTION : The cache is already managed by CacheManager
  public void setMiniFilter(int filterIndex, String filterValue) {
    // Select case for all
    if (filterIndex == 999) {
      PanelMiniFilterSelect theFilter = (PanelMiniFilterSelect) getSelectMiniFilter();
      m_Cm.setSelected(m_what, m_Ids, theFilter.isSelectAllFunction());
      theFilter.setSelectAllFunction(!theFilter.isSelectAllFunction());
    }
  }

  public String[] getColumnsHeader() {
    String[] valret;

    if ((m_MiniFilters == null) || (m_MiniFilters.length <= 0)) {
      return m_ColumnsHeader;
    }
    valret = new String[m_ColumnsHeader.length];
    for (int i = 0; i < m_ColumnsHeader.length; i++) {
      if ((m_MiniFilters.length > i) && (m_MiniFilters[i] != null)) {
        valret[i] = m_ColumnsHeader[i] + m_MiniFilters[i].getHTMLDisplay();
      } else {
        valret[i] = m_ColumnsHeader[i];
      }
    }
    return valret;
  }

  // OVERWRITE THIS FUNCTION : The cache is already managed by CacheManager
  public int getSelectedNumber() {
    return m_Cm.getSelectedNumber(m_what);
  }
}
