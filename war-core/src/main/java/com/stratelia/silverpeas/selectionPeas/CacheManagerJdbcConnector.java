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

import com.stratelia.silverpeas.genericPanel.PanelLine;
import com.stratelia.silverpeas.genericPanel.PanelMiniFilterEdit;
import com.stratelia.silverpeas.genericPanel.PanelMiniFilterSelect;
import com.stratelia.silverpeas.genericPanel.PanelMiniFilterToken;
import com.stratelia.silverpeas.genericPanel.PanelOperation;
import com.stratelia.silverpeas.genericPanel.PanelProvider;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.selection.SelectionExtraParams;
import com.stratelia.silverpeas.selection.SelectionJdbcParams;
import com.stratelia.silverpeas.selection.SelectionUsersGroups;
import com.stratelia.silverpeas.selectionPeas.jdbc.JdbcConnectorDAO;
import com.stratelia.webactiv.util.ResourceLocator;

public class CacheManagerJdbcConnector extends CacheManager {

  Selection selection;
  JdbcConnectorDAO jdbcConnectorDAO;

  public CacheManagerJdbcConnector(String language, ResourceLocator local,
      ResourceLocator icon, Selection selection) {
    super(language, local, icon, selection);
    this.selection = selection;
  }

  public JdbcConnectorDAO getJdbcConnectorDAO() {
    if (jdbcConnectorDAO == null) {
      SelectionJdbcParams jdbcParams = (SelectionJdbcParams) selection
          .getExtraParams();
      jdbcConnectorDAO = new JdbcConnectorDAO(jdbcParams);
    }
    return jdbcConnectorDAO;
  }

  public BrowsePanelProvider getBrowsePanelProvider(int what, CacheManager cm,
      SelectionExtraParams sep) {
    return new BrowseJdbcPanel(m_Language, m_Local, cm, sep);
  }

  public PanelProvider getCartPanelProvider(int what, CacheManager cm,
      SelectionExtraParams sep) {
    return null;
  }

  public String[] getColumnsNames(int what) {
    return getJdbcConnectorDAO().getColumnsNames();
  }

  public String[] getContentColumnsNames(int what) {
    if (what == CM_ELEMENT) {
      return getColumnsNames(CM_SET);
    } else if (what == CM_SET) {
      return getColumnsNames(CM_ELEMENT);
    } else {
      return new String[0];
    }
  }

  public String[][] getContentInfos(int what, String id) {
    String[][] result = { { "ci11", "ci12", "ci13", "ci14" },
        { "ci21", "ci22", "ci23", "ci24" } };
    return result;
  }

  public String[][] getContentLines(int what, String id) {
    String[][] result = { { "cl11", "cl12", "cl13", "cl14" },
        { "cl21", "cl22", "cl23", "cl24" } };
    return result;
  }

  public String getContentText(int what) {
    return "content text";
  }

  protected PanelLine getLineFromId(int what, String id) {
    String[] line = getJdbcConnectorDAO().getLine(id);
    return new PanelLine(id, line, false);
  }

  public int getLineCount(int what) {
    return getJdbcConnectorDAO().getLineCount();
  }

  public PanelMiniFilterToken[] getPanelMiniFilters(int what) {
    if (what == CM_SET) {
      PanelMiniFilterToken[] theArray = new PanelMiniFilterToken[1];
      theArray[0] = new PanelMiniFilterEdit(0, Integer.toString(what), "",
          m_Context + m_Icon.getString("selectionPeas.filter"), m_Local
          .getString("selectionPeas.filter"), m_Local
          .getString("selectionPeas.filter"));
      return theArray;
    } else if (what == CM_ELEMENT) {
      PanelMiniFilterToken[] theArray = new PanelMiniFilterToken[1];
      theArray[0] = new PanelMiniFilterEdit(0, Integer.toString(what), "",
          m_Context + m_Icon.getString("selectionPeas.filter"), m_Local
          .getString("selectionPeas.filter"), m_Local
          .getString("selectionPeas.filter"));
      return theArray;
    } else {
      return new PanelMiniFilterToken[0];
    }
  }

  public PanelOperation getPanelOperation(String operation) {
    if ("DisplayBrowse".equals(operation)) {
      return new PanelOperation(m_Local.getString("selectionPeas.helpBrowse"),
          m_Context + m_Icon.getString("selectionPeas.browseArb"), operation);
    } else if ("DisplaySearchElement".equals(operation)) {
      return new PanelOperation(m_Local
          .getString("selectionPeas.helpSearchElement"), m_Context
          + m_Icon.getString("selectionPeas.userSearc"), operation);
    } else if ("DisplaySearchSet".equals(operation)) {
      return new PanelOperation(m_Local
          .getString("selectionPeas.helpSearchSet"), m_Context
          + m_Icon.getString("selectionPeas.groupSearc"), operation);
    } else {
      return null;
    }
  }

  public BrowsePanelProvider getSearchPanelProvider(int what, CacheManager cm,
      SelectionExtraParams sep) {
    return null;
  }

  public PanelMiniFilterSelect getSelectMiniFilter(int what) {
    if (what == CM_SET) {
      return new PanelMiniFilterSelect(999, Integer.toString(what), "set",
          m_Context + m_Icon.getString("selectionPeas.selectAll"), m_Context
          + m_Icon.getString("selectionPeas.unSelectAll"), m_Local
          .getString("selectionPeas.selectAll"), m_Local
          .getString("selectionPeas.unSelectAll"), m_Local
          .getString("selectionPeas.selectAll"), m_Local
          .getString("selectionPeas.unSelectAll"));
    } else if (what == CM_ELEMENT) {
      return new PanelMiniFilterSelect(999, Integer.toString(what), "element",
          m_Context + m_Icon.getString("selectionPeas.selectAll"), m_Context
          + m_Icon.getString("selectionPeas.unSelectAll"), m_Local
          .getString("selectionPeas.selectAll"), m_Local
          .getString("selectionPeas.unSelectAll"), m_Local
          .getString("selectionPeas.selectAll"), m_Local
          .getString("selectionPeas.unSelectAll"));
    } else {
      return null;
    }
  }

  protected SelectionUsersGroups getSureExtraParams(SelectionExtraParams sep) {
    SelectionUsersGroups valret = (SelectionUsersGroups) sep;
    if (valret == null) {
      valret = new SelectionUsersGroups();
    }
    return valret;
  }

  public void setJdbcParams(SelectionJdbcParams jdbcParams) {
    // this.jdbcParams = jdbcParams;
  }

}