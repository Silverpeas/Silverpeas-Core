/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

package com.stratelia.silverpeas.genericPanel;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;
import org.silverpeas.core.admin.OrganisationController;
import org.silverpeas.core.admin.OrganisationControllerFactory;

import java.util.Hashtable;

public class AllUserPanel extends PanelProvider {
  protected static final int FILTER_LASTNAME = 0;
  protected static final int FILTER_FIRSTNAME = 1;

  protected static final int COL_LASTNAME = 0;
  protected static final int COL_FIRSTNAME = 1;
  protected static final int COL_EMAIL = 2;

  protected OrganisationController m_oc =  OrganisationControllerFactory.getOrganisationController();

  protected Hashtable<String, UserDetail> m_AllUserDetail = new Hashtable<String, UserDetail>();

  public AllUserPanel(String language, String title) {
    initAll(language, title);
  }

  public void initAll(String language, String title) {
    String[] filters = new String[2];

    // Set the language
    this.language = language;

    ResourceLocator message = GeneralPropertiesManager
        .getGeneralMultilang(this.language);

    // Set the resource locator for columns header
    resourceLocator = new ResourceLocator(
        "com.stratelia.silverpeas.genericPanel.multilang.genericPanelBundle",
        this.language);

    // Set the Page name
    pageName = title;
    pageSubTitle = resourceLocator.getString("genericPanel.usersList");

    // Set column headers
    columnHeaders = new String[3];
    columnHeaders[COL_LASTNAME] = message.getString("GML.lastName");
    columnHeaders[COL_FIRSTNAME] = message.getString("GML.firstName");
    columnHeaders[COL_EMAIL] = message.getString("GML.email");

    // Build search tokens
    searchTokens = new PanelSearchToken[2];

    searchTokens[FILTER_LASTNAME] = new PanelSearchEdit(0, message
        .getString("GML.lastName"), "");
    searchTokens[FILTER_FIRSTNAME] = new PanelSearchEdit(1, message
        .getString("GML.firstName"), "");

    // Set filters and get Ids
    filters[FILTER_FIRSTNAME] = "";
    filters[FILTER_LASTNAME] = "";
    refresh(filters);
  }

  public void refresh(String[] filters) {
    UserDetail modelUser = new UserDetail();
    UserDetail[] result = null;

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

    result = m_oc.searchUsers(modelUser, true);
    m_AllUserDetail.clear();
    ids = new String[result.length];
    for (int i = 0; i < result.length; i++) {
      ids[i] = result[i].getId();
      m_AllUserDetail.put(ids[i], result[i]);
    }

    // Set search tokens values
    ((PanelSearchEdit) searchTokens[FILTER_FIRSTNAME]).m_Text =
        getSureString(filters[FILTER_FIRSTNAME]);
    ((PanelSearchEdit) searchTokens[FILTER_LASTNAME]).m_Text =
        getSureString(filters[FILTER_LASTNAME]);
    verifIndexes();
  }

  public PanelLine getElementInfos(String id) {
    UserDetail theUser = m_AllUserDetail.get(id);
    String[] theValues;
    PanelLine valret = null;

    SilverTrace.info("genericPanel", "AllUserPanel.getElementInfos()",
        "root.GEN_MSG_PARAM_VALUE", "id=" + id);
    theValues = new String[3];
    theValues[COL_LASTNAME] = theUser.getLastName();
    theValues[COL_FIRSTNAME] = theUser.getFirstName();
    theValues[COL_EMAIL] = theUser.geteMail();
    valret = new PanelLine(theUser.getId(), theValues, false);
    return valret;
  }
}
