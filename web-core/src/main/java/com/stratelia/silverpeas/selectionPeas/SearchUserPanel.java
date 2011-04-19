/**
 * Copyright (C) 2000 - 2011 Silverpeas
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


package com.stratelia.silverpeas.selectionPeas;

import com.stratelia.silverpeas.genericPanel.PanelSearchEdit;
import com.stratelia.silverpeas.genericPanel.PanelSearchToken;
import com.stratelia.silverpeas.selection.SelectionUsersGroups;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.ResourceLocator;

public class SearchUserPanel extends BrowsePanelProvider {
  protected static final int FILTER_LASTNAME = 0;
  protected static final int FILTER_FIRSTNAME = 1;

  SelectionUsersGroups m_SelectionExtraParams = null;

  public SearchUserPanel(String language, ResourceLocator rs, CacheManager cm,
      SelectionUsersGroups sug) {
    super(language, rs, cm, CacheType.CM_ELEMENT);
    initAll(sug);
  }

  public void initAll(SelectionUsersGroups sug) {
    String[] filters = new String[2];

    setSelectMiniFilter(cacheManager.getSelectMiniFilter(m_what));

    // Set the number displayed to a new value
    nbDisplayed = SelectionPeasSettings.elementBySearchPage;

    // Set the Selection's extra parameters
    if (sug == null) {
      m_SelectionExtraParams = new SelectionUsersGroups();
    } else {
      m_SelectionExtraParams = sug;
    }

    // Set the Page name
    pageName = resourceLocator.getString("selectionPeas.usersList");
    pageSubTitle = resourceLocator.getString("selectionPeas.searchUser");

    // Build search tokens
    searchTokens = new PanelSearchToken[2];

    searchTokens[FILTER_LASTNAME] = new PanelSearchEdit(0, resource
        .getString("GML.lastName"), "");
    searchTokens[FILTER_FIRSTNAME] = new PanelSearchEdit(1, resource
        .getString("GML.firstName"), "");

    // Set filters and get Ids
    filters[FILTER_FIRSTNAME] = "";
    filters[FILTER_LASTNAME] = "";
    if (SelectionPeasSettings.displayAllSearchByDefault) {
      refresh(filters);
    } else {
      ids = new String[0];
    }
  }

  public void setNewParentSet(String newSetId) {
    parentGroupId = newSetId;
    // refresh(null);
  }

  public void refresh(String[] filters) {
    UserDetail modelUser = new UserDetail();

    if ((filters[FILTER_FIRSTNAME] != null)
        && (filters[FILTER_FIRSTNAME].length() > 0)) {
      modelUser.setFirstName(filters[FILTER_FIRSTNAME] + "%");
    } else {
      modelUser.setFirstName("");
    }

    if ((filters[FILTER_LASTNAME] != null)
        && (filters[FILTER_LASTNAME].length() > 0)) {
      modelUser.setLastName(filters[FILTER_LASTNAME] + "%");
    } else {
      modelUser.setLastName("");
    }
    modelUser.setDomainId(m_SelectionExtraParams.getDomainId());

    ids = organizationCOntroller.searchUsersIds(parentGroupId, m_SelectionExtraParams
        .getComponentId(), m_SelectionExtraParams.getProfileIds(), modelUser);

    // Set search tokens values
    ((PanelSearchEdit) searchTokens[FILTER_FIRSTNAME]).m_Text =
        getSureString(filters[FILTER_FIRSTNAME]);
    ((PanelSearchEdit) searchTokens[FILTER_LASTNAME]).m_Text =
        getSureString(filters[FILTER_LASTNAME]);
    verifIndexes();
  }
}
