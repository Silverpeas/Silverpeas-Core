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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.silverpeas.selectionPeas;

import java.util.Set;

import com.stratelia.silverpeas.genericPanel.PanelLine;
import com.stratelia.silverpeas.genericPanel.PanelMiniFilterSelect;
import com.stratelia.silverpeas.genericPanel.PanelProvider;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;

abstract public class BrowsePanelProvider extends PanelProvider {

  protected CacheManager cacheManager = null;
  protected ResourceLocator resource = null;
  protected OrganizationController organizationCOntroller = new OrganizationController();
  protected String parentGroupId = "";
  protected String parentGroupName = "";
  protected CacheType m_what;

  public BrowsePanelProvider(String language, ResourceLocator rs, CacheManager cm, CacheType what) {
    // Set the language
    this.language = language;
    resource = GeneralPropertiesManager.getGeneralMultilang(this.language);
    resourceLocator = rs;

    // Set the cache manager
    cacheManager = cm;
    m_what = what;

    // Set column headers
    columnHeaders = cacheManager.getColumnsNames(m_what);
  }

  abstract public void setNewParentSet(String newSetId);

  // OVERWRITE THIS FUNCTION : The cache is already managed by CacheManager
  public PanelLine getCachedElement(String id) {
    return getElementInfos(id);
  }

  public PanelLine getElementInfos(String id) {
    return cacheManager.getInfos(m_what, id);
  }

  // OVERWRITE THIS FUNCTION : The cache is already managed by CacheManager
  public void setSelectedElements(Set<String> elements) {
    for (String element : elements) {
      cacheManager.setSelected(m_what, element, true);
    }
  }

  public void unsetSelectedElements(Set<String> elements) {
    for (String element : elements) {
      cacheManager.setSelected(m_what, element, false);
    }
  }

  // OVERWRITE THIS FUNCTION : The cache is already managed by CacheManager
  public void setSelectedElement(String id, boolean isSelected) {
    cacheManager.setSelected(m_what, id, isSelected);
  }

  // OVERWRITE THIS FUNCTION : The cache is already managed by CacheManager
  public String[] getSelectedElements() {
    return cacheManager.getSelectedIds(m_what);
  }

  // OVERWRITE THIS FUNCTION : The cache is already managed by CacheManager
  public void setMiniFilter(int filterIndex, String filterValue) {
    // Select case for all
    if (filterIndex == 999) {
      PanelMiniFilterSelect theFilter = getSelectMiniFilter();
      cacheManager.setSelected(m_what, ids, theFilter.isSelectAllFunction());
      theFilter.setSelectAllFunction(!theFilter.isSelectAllFunction());
    }
  }

  public String[] getColumnsHeader() {
    String[] valret;

    if ((miniFilters == null) || (miniFilters.length <= 0)) {
      return columnHeaders;
    }
    valret = new String[columnHeaders.length];
    for (int i = 0; i < columnHeaders.length; i++) {
      if ((miniFilters.length > i) && (miniFilters[i] != null)) {
        valret[i] = columnHeaders[i] + miniFilters[i].getHTMLDisplay();
      } else {
        valret[i] = columnHeaders[i];
      }
    }
    return valret;
  }

  // OVERWRITE THIS FUNCTION : The cache is already managed by CacheManager
  public int getSelectedNumber() {
    return cacheManager.getSelectedNumber(m_what);
  }
}
