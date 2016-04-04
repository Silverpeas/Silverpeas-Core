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

package org.silverpeas.core.web.selection;

import org.silverpeas.core.web.panel.PanelLine;
import org.silverpeas.core.web.panel.PanelMiniFilterSelect;
import org.silverpeas.core.web.panel.PanelProvider;
import org.silverpeas.core.web.panel.PanelSearchToken;
import org.silverpeas.core.util.LocalizationBundle;

public class CartGroupPanel extends PanelProvider {
  protected CacheManager m_Cm = null;
  protected CacheType m_what;

  SelectionUsersGroups m_SelectionExtraParams = null;

  public CartGroupPanel(String language, LocalizationBundle rs, CacheManager cm,
      SelectionUsersGroups sug) {
    // Set the language
    this.language = language;
    messages = rs;

    // Set the cache manager
    m_Cm = cm;
    m_what = CacheType.CM_SET;

    // Set column headers
    columnHeaders = m_Cm.getColumnsNames(m_what);

    initAll(sug);
  }

  public void initAll(SelectionUsersGroups sug) {
    // miniFilters = cacheManager.getPanelMiniFilters(m_what);
    setSelectMiniFilter(m_Cm.getSelectMiniFilter(m_what));

    // Set the number displayed to a new value
    nbDisplayed = SelectionPeasSettings.setByBrowsePage;

    // Set the Selection's extra parameters
    if (sug == null) {
      m_SelectionExtraParams = new SelectionUsersGroups();
    } else {
      m_SelectionExtraParams = sug;
    }

    // Set the Page name
    pageName = messages.getString("selectionPeas.selectedGroups");
    pageSubTitle = "";

    // Build search tokens
    searchTokens = new PanelSearchToken[1];

    // Set filters and get Ids
    refresh(null);
  }

  public PanelLine getElementInfos(String id) {
    PanelLine pl = m_Cm.getInfos(m_what, id);

    if (pl == null) {
      return null;
    } else {
      return new PanelLine(pl.m_Id, pl.m_Values, pl.m_HighLight);
    }
  }

  public void setMiniFilter(int filterIndex, String filterValue) {
    // Select case for all
    if (filterIndex == 999) {
      PanelMiniFilterSelect theFilter = getSelectMiniFilter();
      for (String id : ids) {
        setSelectedElement(id, theFilter.isSelectAllFunction());
      }
      theFilter.setSelectAllFunction(!theFilter.isSelectAllFunction());
    }
  }

  public void refresh(String[] filters) {
    PanelLine[] sortedLines = m_Cm.getSelectedLines(m_what);
    ids = new String[sortedLines.length];
    for (int i = 0; i < sortedLines.length; i++) {
      ids[i] = sortedLines[i].m_Id;
    }
    verifIndexes();
  }
}
