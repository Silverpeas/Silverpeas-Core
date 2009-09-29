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

import com.stratelia.silverpeas.genericPanel.PanelLine;
import com.stratelia.silverpeas.genericPanel.PanelMiniFilterSelect;
import com.stratelia.silverpeas.genericPanel.PanelProvider;
import com.stratelia.silverpeas.genericPanel.PanelSearchToken;
import com.stratelia.silverpeas.selection.SelectionUsersGroups;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;

public class CartGroupPanel extends PanelProvider {
  protected CacheManager m_Cm = null;
  protected ResourceLocator m_Message = null;
  protected int m_what;

  SelectionUsersGroups m_SelectionExtraParams = null;

  public CartGroupPanel(String language, ResourceLocator rs, CacheManager cm,
      SelectionUsersGroups sug) {
    // Set the language
    m_Language = language;
    m_Message = GeneralPropertiesManager.getGeneralMultilang(m_Language);
    m_rs = rs;

    // Set the cache manager
    m_Cm = cm;
    m_what = CacheManager.CM_SET;

    // Set column headers
    m_ColumnsHeader = m_Cm.getColumnsNames(m_what);

    initAll(sug);
  }

  public void initAll(SelectionUsersGroups sug) {
    // m_MiniFilters = m_Cm.getPanelMiniFilters(m_what);
    setSelectMiniFilter(m_Cm.getSelectMiniFilter(m_what));

    // Set the number displayed to a new value
    m_NbDisplayed = SelectionPeasSettings.m_SetByBrowsePage;

    // Set the Selection's extra parameters
    if (sug == null) {
      m_SelectionExtraParams = new SelectionUsersGroups();
    } else {
      m_SelectionExtraParams = sug;
    }

    // Set the Page name
    m_PageName = m_rs.getString("selectionPeas.selectedGroups");
    m_PageSubTitle = "";

    // Build search tokens
    m_SearchTokens = new PanelSearchToken[1];

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
      PanelMiniFilterSelect theFilter = (PanelMiniFilterSelect) getSelectMiniFilter();
      for (int i = 0; i < m_Ids.length; i++) {
        setSelectedElement(m_Ids[i], theFilter.isSelectAllFunction());
      }
      theFilter.setSelectAllFunction(!theFilter.isSelectAllFunction());
    }
  }

  public void refresh(String[] filters) {
    PanelLine[] sortedLines = m_Cm.getSelectedLines(m_what);
    m_Ids = new String[sortedLines.length];
    for (int i = 0; i < sortedLines.length; i++) {
      m_Ids[i] = sortedLines[i].m_Id;
    }
    verifIndexes();
  }
}
