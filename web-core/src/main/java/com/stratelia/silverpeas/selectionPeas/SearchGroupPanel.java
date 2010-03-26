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

import com.stratelia.silverpeas.genericPanel.PanelSearchEdit;
import com.stratelia.silverpeas.genericPanel.PanelSearchToken;
import com.stratelia.silverpeas.selection.SelectionUsersGroups;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.util.ResourceLocator;

public class SearchGroupPanel extends BrowsePanelProvider {
  protected static final int FILTER_NAME = 0;

  SelectionUsersGroups m_SelectionExtraParams = null;

  public SearchGroupPanel(String language, ResourceLocator rs, CacheManager cm,
      SelectionUsersGroups sug) {
    super(language, rs, cm, CacheManager.CM_SET);
    initAll(sug);
  }

  public void setNewParentSet(String newSetId) {
    m_ParentGroupId = newSetId;
    // refresh(null);
  }

  public void initAll(SelectionUsersGroups sug) {
    String[] filters = new String[1];

    setSelectMiniFilter(m_Cm.getSelectMiniFilter(CacheManager.CM_ELEMENT));

    // Set the number displayed to a new value
    m_NbDisplayed = SelectionPeasSettings.m_SetBySearchPage;

    // Set the Selection's extra parameters
    if (sug == null) {
      m_SelectionExtraParams = new SelectionUsersGroups();
    } else {
      m_SelectionExtraParams = sug;
    }

    // Set the Page name
    m_PageName = m_rs.getString("selectionPeas.groupsList");
    m_PageSubTitle = m_rs.getString("selectionPeas.searchGroup");

    // Build search tokens
    m_SearchTokens = new PanelSearchToken[1];

    m_SearchTokens[FILTER_NAME] = new PanelSearchEdit(0, m_Message
        .getString("GML.name"), "");

    // Set filters and get Ids
    filters[FILTER_NAME] = "";
    if (SelectionPeasSettings.m_DisplayAllSearchByDefault) {
      refresh(filters);
    } else {
      m_Ids = new String[0];
    }
  }

  public void refresh(String[] filters) {
    Group modelGroup = new Group();

    if ((filters[FILTER_NAME] != null) && (filters[FILTER_NAME].length() > 0)) {
      modelGroup.setName(filters[FILTER_NAME] + "%");
    } else {
      modelGroup.setName("");
    }
    modelGroup.setDomainId(m_SelectionExtraParams.getDomainId());
    modelGroup.setSuperGroupId(m_ParentGroupId);
    m_Ids = m_oc.searchGroupsIds(false,
        m_SelectionExtraParams.getComponentId(), m_SelectionExtraParams
        .getProfileIds(), modelGroup);

    // Set search tokens values
    ((PanelSearchEdit) m_SearchTokens[FILTER_NAME]).m_Text = getSureString(filters[FILTER_NAME]);
    verifIndexes();
  }
}
