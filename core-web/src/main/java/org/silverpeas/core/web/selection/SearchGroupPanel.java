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

import org.silverpeas.core.web.panel.PanelSearchEdit;
import org.silverpeas.core.web.panel.PanelSearchToken;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.LocalizationBundle;

public class SearchGroupPanel extends BrowsePanelProvider {
  protected static final int FILTER_NAME = 0;

  SelectionUsersGroups m_SelectionExtraParams = null;

  public SearchGroupPanel(String language, LocalizationBundle rs, CacheManager cm,
      SelectionUsersGroups sug) {
    super(language, rs, cm, CacheType.CM_SET);
    initAll(sug);
  }

  public void setNewParentSet(String newSetId) {
    parentGroupId = newSetId;
    // refresh(null);
  }

  public void initAll(SelectionUsersGroups sug) {
    String[] filters = new String[1];

    setSelectMiniFilter(cacheManager.getSelectMiniFilter(CacheType.CM_ELEMENT));

    // Set the number displayed to a new value
    nbDisplayed = SelectionPeasSettings.setBySearchPage;

    // Set the Selection's extra parameters
    if (sug == null) {
      m_SelectionExtraParams = new SelectionUsersGroups();
    } else {
      m_SelectionExtraParams = sug;
    }

    // Set the Page name
    pageName = messages.getString("selectionPeas.groupsList");
    pageSubTitle = messages.getString("selectionPeas.searchGroup");

    // Build search tokens
    searchTokens = new PanelSearchToken[1];

    searchTokens[FILTER_NAME] = new PanelSearchEdit(0, messages
        .getString("GML.name"), "");

    // Set filters and get Ids
    filters[FILTER_NAME] = "";
    if (SelectionPeasSettings.displayAllSearchByDefault) {
      refresh(filters);
    } else {
      ids = ArrayUtil.EMPTY_STRING_ARRAY;
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
    modelGroup.setSuperGroupId(parentGroupId);
    ids = organizationCOntroller.searchGroupsIds(false,
        m_SelectionExtraParams.getComponentId(), m_SelectionExtraParams
        .getProfileIds(), modelGroup);

    // Set search tokens values
    ((PanelSearchEdit) searchTokens[FILTER_NAME]).m_Text = getSureString(filters[FILTER_NAME]);
    verifIndexes();
  }
}
