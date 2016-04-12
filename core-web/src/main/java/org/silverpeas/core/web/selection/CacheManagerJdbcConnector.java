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

import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.web.panel.PanelLine;
import org.silverpeas.core.web.panel.PanelMiniFilterEdit;
import org.silverpeas.core.web.panel.PanelMiniFilterSelect;
import org.silverpeas.core.web.panel.PanelMiniFilterToken;
import org.silverpeas.core.web.panel.PanelOperation;
import org.silverpeas.core.web.panel.PanelProvider;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.selection.jdbc.JdbcConnectorDAO;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.SettingBundle;

public class CacheManagerJdbcConnector extends CacheManager {

  JdbcConnectorDAO jdbcConnectorDAO;
  LocalizationBundle messages;
  SettingBundle iconSettings;

  public CacheManagerJdbcConnector(String language, LocalizationBundle messages,
      SettingBundle icons, Selection selection) {
    super(language, selection);
    this.messages = messages;
    this.iconSettings = icons;
    this.selection = selection;
  }

  public JdbcConnectorDAO getJdbcConnectorDAO() {
    if (jdbcConnectorDAO == null) {
      SelectionJdbcParams jdbcParams = (SelectionJdbcParams) selection.getExtraParams();
      jdbcConnectorDAO = new JdbcConnectorDAO(jdbcParams);
    }
    return jdbcConnectorDAO;
  }

  public BrowsePanelProvider getBrowsePanelProvider(CacheType what, SelectionExtraParams sep) {
    return new BrowseJdbcPanel(language, messages, this, sep);
  }

  public PanelProvider getCartPanelProvider(CacheType what, SelectionExtraParams sep) {
    return null;
  }

  public String[] getColumnsNames(CacheType what) {
    return getJdbcConnectorDAO().getColumnsNames();
  }

  public String[] getContentColumnsNames(CacheType what) {
    switch (what) {
      case CM_ELEMENT:
        return getColumnsNames(CacheType.CM_SET);
      case CM_SET:
        return getColumnsNames(CacheType.CM_ELEMENT);
      default:
        return ArrayUtil.EMPTY_STRING_ARRAY;
    }
  }

  public String[][] getContentInfos(CacheType what, String id) {
    return new String[][] { { "ci11", "ci12", "ci13", "ci14" }, { "ci21", "ci22", "ci23", "ci24" } };
  }

  public String[][] getContentLines(CacheType what, String id) {
    return new String[][] { { "cl11", "cl12", "cl13", "cl14" }, { "cl21", "cl22", "cl23", "cl24" } };
  }

  public String getContentText(CacheType what) {
    return "content text";
  }

  protected PanelLine getLineFromId(CacheType what, String id) {
    String[] line = getJdbcConnectorDAO().getLine(id);
    return new PanelLine(id, line, false);
  }

  public int getLineCount(CacheType what) {
    return getJdbcConnectorDAO().getLineCount();
  }

  public PanelMiniFilterToken[] getPanelMiniFilters(CacheType what) {
    switch (what) {
      case CM_SET: {
        PanelMiniFilterToken[] theArray = new PanelMiniFilterToken[1];
        theArray[0] = new PanelMiniFilterEdit(0, Integer.toString(what.getValue()), "",
            URLUtil.getApplicationURL() + iconSettings.getString("selectionPeas.filter"),
            messages.getString("selectionPeas.filter"), messages
            .getString("selectionPeas.filter"));
        return theArray;
      }
      case CM_ELEMENT: {
        PanelMiniFilterToken[] theArray = new PanelMiniFilterToken[1];
        theArray[0] = new PanelMiniFilterEdit(0, Integer.toString(what.getValue()), "",
            URLUtil.getApplicationURL() + iconSettings.getString("selectionPeas.filter"),
            messages.getString("selectionPeas.filter"), messages
            .getString("selectionPeas.filter"));
        return theArray;
      }
      default:
        return new PanelMiniFilterToken[0];
    }

  }

  public PanelOperation getPanelOperation(String operation) {
    switch (operation) {
      case "DisplayBrowse":
        return new PanelOperation(messages.getString("selectionPeas.helpBrowse"),
            URLUtil.getApplicationURL() +
                iconSettings.getString("selectionPeas.browseArb"), operation);
      case "DisplaySearchElement":
        return new PanelOperation(messages.getString("selectionPeas.helpSearchElement"),
            URLUtil.getApplicationURL() +
                iconSettings.getString("selectionPeas.userSearc"), operation);
      case "DisplaySearchSet":
        return new PanelOperation(messages.getString("selectionPeas.helpSearchSet"),
            URLUtil.getApplicationURL() +
                iconSettings.getString("selectionPeas.groupSearc"), operation);
      default:
        return null;
    }
  }

  public BrowsePanelProvider getSearchPanelProvider(CacheType what, SelectionExtraParams sep) {
    return null;
  }

  public PanelMiniFilterSelect getSelectMiniFilter(CacheType what) {
    switch (what) {
      case CM_SET: {
        return new PanelMiniFilterSelect(999, Integer.toString(what.getValue()), "set",
            URLUtil.getApplicationURL() + iconSettings.getString(
            "selectionPeas.selectAll"),
            URLUtil.getApplicationURL()
            + iconSettings.getString("selectionPeas.unSelectAll"), messages
            .getString("selectionPeas.selectAll"), messages
            .getString("selectionPeas.unSelectAll"), messages
            .getString("selectionPeas.selectAll"), messages
            .getString("selectionPeas.unSelectAll"));
      }
      case CM_ELEMENT: {
        return new PanelMiniFilterSelect(999, Integer.toString(what.getValue()), "element",
            URLUtil.getApplicationURL() + iconSettings.getString(
            "selectionPeas.selectAll"),
            URLUtil.getApplicationURL()
            + iconSettings.getString("selectionPeas.unSelectAll"), messages
            .getString("selectionPeas.selectAll"), messages
            .getString("selectionPeas.unSelectAll"), messages
            .getString("selectionPeas.selectAll"), messages
            .getString("selectionPeas.unSelectAll"));
      }
      default:
        return null;
    }
  }

  protected SelectionUsersGroups getSureExtraParams(SelectionExtraParams sep) {
    SelectionUsersGroups valRet = (SelectionUsersGroups) sep;
    if (valRet == null) {
      valRet = new SelectionUsersGroups();
    }
    return valRet;
  }
}