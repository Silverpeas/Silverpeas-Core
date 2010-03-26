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
import com.stratelia.silverpeas.genericPanel.PanelMiniFilterEdit;
import com.stratelia.silverpeas.genericPanel.PanelSearchToken;
import com.stratelia.silverpeas.selection.SelectionUsersGroups;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.ResourceLocator;

public class BrowseUserPanel extends BrowsePanelProvider {
  protected static final int FILTER_LASTNAME = 1;

  SelectionUsersGroups m_SelectionExtraParams = null;

  public BrowseUserPanel(String language, ResourceLocator rs, CacheManager cm,
      SelectionUsersGroups sug) {
    super(language, rs, cm, CacheManager.CM_ELEMENT);
    initAll(sug);
  }

  public void setNewParentSet(String newSetId) {
    m_ParentGroupId = newSetId;
    if ((m_ParentGroupId == null) || (m_ParentGroupId.length() <= 0)) {
      m_ParentGroupName = "";
    } else {
      PanelLine pl = m_Cm.getLineFromId(CacheManager.CM_SET, m_ParentGroupId);
      m_ParentGroupName = pl.m_Values[0];
    }
    refresh(null);
  }

  public void initAll(SelectionUsersGroups sug) {
    m_MiniFilters = m_Cm.getPanelMiniFilters(m_what);
    setSelectMiniFilter(m_Cm.getSelectMiniFilter(m_what));

    // Set the number displayed to a new value
    m_NbDisplayed = SelectionPeasSettings.m_ElementByBrowsePage;

    // Set the Selection's extra parameters
    if (sug == null) {
      m_SelectionExtraParams = new SelectionUsersGroups();
    } else {
      m_SelectionExtraParams = sug;
    }

    // Set the Page name
    m_PageName = m_rs.getString("selectionPeas.usersAll");
    m_PageSubTitle = m_rs.getString("selectionPeas.searchUser");

    // Build search tokens
    m_SearchTokens = new PanelSearchToken[0];

    // Set filters and get Ids
    refresh(null);
  }

  public String getPageName() {
    if ((m_ParentGroupId == null) || (m_ParentGroupId.length() <= 0)) {
      return m_PageName;
    } else {
      return m_rs.getString("selectionPeas.usersOfGroup") + m_ParentGroupName
          + " ";
    }
  }

  public void setMiniFilter(int filterIndex, String filterValue) {
    super.setMiniFilter(filterIndex, filterValue);
    // Only one filter : 0 : lastName
    if (filterIndex == 0) {
      ((PanelMiniFilterEdit) m_MiniFilters[0]).m_Text = filterValue;
      refresh(null);
    }
  }

  public void refresh(String[] filters) {
    UserDetail modelUser = new UserDetail();

    if ((((PanelMiniFilterEdit) m_MiniFilters[0]).m_Text != null)
        && (((PanelMiniFilterEdit) m_MiniFilters[0]).m_Text.length() > 0)) {
      modelUser.setLastName(((PanelMiniFilterEdit) m_MiniFilters[0]).m_Text
          + "%");
    }
    modelUser.setDomainId(m_SelectionExtraParams.getDomainId());
    m_Ids = m_oc.searchUsersIds(m_ParentGroupId, m_SelectionExtraParams
        .getComponentId(), m_SelectionExtraParams.getProfileIds(), modelUser);

    // Set search tokens values
    verifIndexes();
  }
}
