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

import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.panel.PanelLine;
import org.silverpeas.core.web.panel.PanelMiniFilterEdit;
import org.silverpeas.core.web.panel.PanelSearchToken;
import org.silverpeas.core.admin.user.model.Group;

public class BrowseGroupPanel extends BrowsePanelProvider {
  SelectionUsersGroups extraParams = null;

  public BrowseGroupPanel(String language, LocalizationBundle rs, CacheManager cm,
      SelectionUsersGroups sug) {
    super(language, rs, cm, CacheType.CM_SET);
    initAll(sug);
  }

  public void setNewParentSet(String newSetId) {
    parentGroupId = newSetId;
    if ((parentGroupId == null) || (parentGroupId.length() <= 0)) {
      parentGroupName = "";
    } else {
      PanelLine pl = cacheManager.getLineFromId(CacheType.CM_SET, parentGroupId);
      parentGroupName = pl.m_Values[0];
    }
    refresh(null);
  }

  public void initAll(SelectionUsersGroups sug) {
    miniFilters = cacheManager.getPanelMiniFilters(m_what);
    setSelectMiniFilter(cacheManager.getSelectMiniFilter(m_what));

    // Set the number displayed to a new value
    nbDisplayed = SelectionPeasSettings.setByBrowsePage;

    // Set the Selection's extra parameters
    if (sug == null) {
      extraParams = new SelectionUsersGroups();
    } else {
      extraParams = sug;
    }

    // Set the Page name
    pageName = messages.getString("selectionPeas.groupsAll");
    pageSubTitle = messages.getString("selectionPeas.searchGroup");

    // Build search tokens
    searchTokens = new PanelSearchToken[1];

    // Set filters and get Ids
    refresh(null);
  }

  public String getPageName() {
    if ((parentGroupId == null) || (parentGroupId.length() <= 0)) {
      return pageName;
    }
    return messages.getString("selectionPeas.groupsOfGroup") + parentGroupName + " ";
  }

  public void setMiniFilter(int filterIndex, String filterValue) {
    super.setMiniFilter(filterIndex, filterValue);
    // Only one filter : 0 : lastName
    if (filterIndex == 0) {
      ((PanelMiniFilterEdit) miniFilters[0]).m_Text = filterValue;
      refresh(null);
    }
  }

  public void refresh(String[] filters) {
    Group modelGroup = new Group();

    if ((((PanelMiniFilterEdit) miniFilters[0]).m_Text != null)
        && (((PanelMiniFilterEdit) miniFilters[0]).m_Text.length() > 0)) {
      modelGroup.setName(((PanelMiniFilterEdit) miniFilters[0]).m_Text + "%");
    }
    modelGroup.setDomainId(extraParams.getDomainId());
    modelGroup.setSuperGroupId(parentGroupId);

    if (StringUtil.isDefined(parentGroupId)) {
      ids = organizationCOntroller.searchGroupsIds(false, null, null, modelGroup);
    } else {
      boolean getOnlyRootGroups = true; // By default, display only root groups
      if (StringUtil.isDefined(extraParams.getComponentId())) {
        // in case of use in a component (to send notifications by example)
        // display all groups (whatever the level).
        // This permit to select a subgroup.
        getOnlyRootGroups = false;
      }
      ids = organizationCOntroller.searchGroupsIds(getOnlyRootGroups, extraParams.getComponentId(),
          extraParams.getProfileIds(), modelGroup);
    }
    verifIndexes();
  }
}
