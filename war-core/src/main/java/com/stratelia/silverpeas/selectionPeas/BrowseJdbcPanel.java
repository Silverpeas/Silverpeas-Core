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
package com.stratelia.silverpeas.selectionPeas;

import com.stratelia.silverpeas.selection.SelectionExtraParams;
import com.stratelia.webactiv.util.ResourceLocator;

public class BrowseJdbcPanel extends BrowsePanelProvider {

  public BrowseJdbcPanel(String language, ResourceLocator rs, CacheManager cm,
      SelectionExtraParams sep) {
    super(language, rs, cm, CacheManager.CM_ELEMENT);
    init(sep.getParameter("tableName"));
  }

  private void init(String pageName) {
    m_PageName = pageName;
    setSelectMiniFilter(m_Cm.getSelectMiniFilter(m_what));
    refresh(null);
  }

  public void setNewParentSet(String newSetId) {
    // TODO Auto-generated method stub
  }

  public void refresh(String[] filters) {
    int lineCount = m_Cm.getLineCount(CacheManager.CM_ELEMENT);
    m_Ids = new String[lineCount];
    for (int i = 0; i < lineCount; i++) {
      m_Ids[i] = String.valueOf(i);
    }
  }

}
