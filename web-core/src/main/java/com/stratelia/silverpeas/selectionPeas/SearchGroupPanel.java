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
