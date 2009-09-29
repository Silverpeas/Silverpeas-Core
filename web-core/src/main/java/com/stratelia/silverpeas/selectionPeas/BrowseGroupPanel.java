/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.selectionPeas;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.genericPanel.PanelLine;
import com.stratelia.silverpeas.genericPanel.PanelMiniFilterEdit;
import com.stratelia.silverpeas.genericPanel.PanelSearchToken;
import com.stratelia.silverpeas.selection.SelectionUsersGroups;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.util.ResourceLocator;

public class BrowseGroupPanel extends BrowsePanelProvider {
  SelectionUsersGroups m_SelectionExtraParams = null;

  public BrowseGroupPanel(String language, ResourceLocator rs, CacheManager cm,
      SelectionUsersGroups sug) {
    super(language, rs, cm, CacheManager.CM_SET);
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
    m_NbDisplayed = SelectionPeasSettings.m_SetByBrowsePage;

    // Set the Selection's extra parameters
    if (sug == null) {
      m_SelectionExtraParams = new SelectionUsersGroups();
    } else {
      m_SelectionExtraParams = sug;
    }

    // Set the Page name
    m_PageName = m_rs.getString("selectionPeas.groupsAll");
    m_PageSubTitle = m_rs.getString("selectionPeas.searchGroup");

    // Build search tokens
    m_SearchTokens = new PanelSearchToken[1];

    // Set filters and get Ids
    refresh(null);
  }

  public String getPageName() {
    if ((m_ParentGroupId == null) || (m_ParentGroupId.length() <= 0)) {
      return m_PageName;
    } else {
      return m_rs.getString("selectionPeas.groupsOfGroup") + m_ParentGroupName
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
    Group modelGroup = new Group();

    if ((((PanelMiniFilterEdit) m_MiniFilters[0]).m_Text != null)
        && (((PanelMiniFilterEdit) m_MiniFilters[0]).m_Text.length() > 0)) {
      modelGroup.setName(((PanelMiniFilterEdit) m_MiniFilters[0]).m_Text + "%");
    }
    modelGroup.setDomainId(m_SelectionExtraParams.getDomainId());
    modelGroup.setSuperGroupId(m_ParentGroupId);

    if (StringUtil.isDefined(m_ParentGroupId)) {
      m_Ids = m_oc.searchGroupsIds(false, null, null, modelGroup);
    } else {
      m_Ids = m_oc
          .searchGroupsIds(false, m_SelectionExtraParams.getComponentId(),
              m_SelectionExtraParams.getProfileIds(), modelGroup);
    }

    verifIndexes();
  }
}
