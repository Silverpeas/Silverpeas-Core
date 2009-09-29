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

import com.silverpeas.util.EncodeHelper;
import com.stratelia.silverpeas.genericPanel.PanelLine;
import com.stratelia.silverpeas.genericPanel.PanelMiniFilterEdit;
import com.stratelia.silverpeas.genericPanel.PanelMiniFilterSelect;
import com.stratelia.silverpeas.genericPanel.PanelMiniFilterToken;
import com.stratelia.silverpeas.genericPanel.PanelOperation;
import com.stratelia.silverpeas.genericPanel.PanelProvider;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.selection.SelectionExtraParams;
import com.stratelia.silverpeas.selection.SelectionUsersGroups;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.ResourceLocator;

public class CacheManagerUsersGroups extends CacheManager {
  protected static final int COL_USER_LASTNAME = 0;
  protected static final int COL_USER_FIRSTNAME = 1;
  protected static final int COL_USER_EMAIL = 2;
  protected static final int COL_USER_DOMAIN = 3;

  protected static final int COL_GROUP_NAME = 0;
  protected static final int COL_GROUP_DESCRIPTION = 1;
  protected static final int COL_GROUP_NBUSERS = 2;

  protected OrganizationController m_oc = new OrganizationController();
  protected AdminController m_ac = new AdminController(null);
  protected UserDetail m_ud = null;

  public CacheManagerUsersGroups(String language, ResourceLocator local,
      ResourceLocator icon, Selection selection, UserDetail ud) {
    super(language, local, icon, selection);
    m_ud = ud;
  }

  protected String getSetParentName(String id) {
    Group theGroup = m_oc.getGroup(id);
    if (theGroup != null) {
      String superId = theGroup.getSuperGroupId();
      Group theSuperGroup = m_oc.getGroup(superId);

      if (theSuperGroup != null) {
        return theSuperGroup.getName();
      } else {
        return "";
      }
    } else {
      return "";
    }
  }

  public String[][] getContentLines(int what, String id) {
    String[][] valret = new String[0][0];
    if (what == CM_ELEMENT) {
      if (SelectionPeasSettings.m_DisplayUsersGroups) {
        AdminController ac = new AdminController(id);
        String[] groupIds = ac.getDirectGroupsIdsOfUser(id);
        String[] columns = getColumnsNames(CM_SET);

        if (groupIds != null && groupIds.length > 0) {
          valret = new String[groupIds.length][columns.length];
          PanelLine pl;
          for (int iGrp = 0; iGrp < groupIds.length; iGrp++) {
            pl = getLineFromId(CM_SET, groupIds[iGrp]);
            for (int i = 0; i < columns.length; i++) {
              valret[iGrp][i] = pl.m_Values[i];
            }
          }
        }
      }
    } else if (what == CM_SET) {
      if (SelectionPeasSettings.m_DisplayGroupsUsers) {
        Group theGroup = m_oc.getGroup(id);
        String[] userIds = null;
        String[] columns = getColumnsNames(CM_ELEMENT);

        if (theGroup != null) {
          userIds = theGroup.getUserIds();
        }
        if (userIds != null && userIds.length > 0) {
          valret = new String[userIds.length][columns.length];
          PanelLine pl;
          for (int iUsr = 0; iUsr < userIds.length; iUsr++) {
            pl = getLineFromId(CM_ELEMENT, userIds[iUsr]);
            for (int i = 0; i < columns.length; i++) {
              valret[iUsr][i] = pl.m_Values[i];
            }
          }
        }
      }
    }
    return valret;
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
    PanelLine pl;
    String[] names;
    String[][] valret;

    if (what == CM_ELEMENT) {
      pl = getInfos(CM_ELEMENT, id);
      names = getColumnsNames(CM_ELEMENT);
      valret = new String[names.length][2];
      for (int i = 0; (i < names.length) && (i < pl.m_Values.length); i++) {
        valret[i][0] = names[i];
        valret[i][1] = pl.m_Values[i];
      }
    } else {
      pl = getInfos(CM_SET, id);
      names = getColumnsNames(CM_SET);
      valret = new String[names.length + 1][2];
      for (int i = 0; (i < names.length) && (i < pl.m_Values.length); i++) {
        valret[i][0] = names[i];
        valret[i][1] = pl.m_Values[i];
      }
      valret[names.length][0] = m_Local.getString("selectionPeas.groupParent");
      valret[names.length][1] = getSetParentName(id);
    }
    return valret;
  }

  public String getContentText(int what) {
    if (what == CM_SET) {
      return m_Local.getString("selectionPeas.directUsersOfGroup");
    } else if (what == CM_ELEMENT) {
      return m_Local.getString("selectionPeas.groupsOfUser");
    } else {
      return "";
    }
  }

  public String[] getColumnsNames(int what) {
    if (what == CM_SET) {
      String[] columnsHeader;
      if (SelectionPeasSettings.m_DisplayNbUsersByGroup) {
        if (SelectionPeasSettings.m_DisplayDomains) {
          columnsHeader = new String[4];
          columnsHeader[COL_GROUP_NAME] = m_Global.getString("GML.nom");
          columnsHeader[COL_GROUP_DESCRIPTION] = m_Global
              .getString("GML.description");
          columnsHeader[COL_GROUP_NBUSERS] = m_Global.getString("GML.users");
          columnsHeader[COL_GROUP_NBUSERS + 1] = m_Local
              .getString("selectionPeas.domain");
        } else {
          columnsHeader = new String[3];
          columnsHeader[COL_GROUP_NAME] = m_Global.getString("GML.nom");
          columnsHeader[COL_GROUP_DESCRIPTION] = m_Global
              .getString("GML.description");
          columnsHeader[COL_GROUP_NBUSERS] = m_Global.getString("GML.users");
        }
      } else {
        if (SelectionPeasSettings.m_DisplayDomains) {
          columnsHeader = new String[3];
          columnsHeader[COL_GROUP_NAME] = m_Global.getString("GML.nom");
          columnsHeader[COL_GROUP_DESCRIPTION] = m_Global
              .getString("GML.description");
          columnsHeader[COL_GROUP_DESCRIPTION + 1] = m_Local
              .getString("selectionPeas.domain");
        } else {
          columnsHeader = new String[2];
          columnsHeader[COL_GROUP_NAME] = m_Global.getString("GML.nom");
          columnsHeader[COL_GROUP_DESCRIPTION] = m_Global
              .getString("GML.description");
        }
      }

      return columnsHeader;
    } else if (what == CM_ELEMENT) {
      String[] columnsHeader = null;
      if (SelectionPeasSettings.m_DisplayDomains) {
        columnsHeader = new String[4];
        columnsHeader[COL_USER_LASTNAME] = m_Global.getString("GML.lastName");
        columnsHeader[COL_USER_FIRSTNAME] = m_Global.getString("GML.firstName");
        columnsHeader[COL_USER_EMAIL] = m_Global.getString("GML.eMail");
        columnsHeader[COL_USER_DOMAIN] = m_Local
            .getString("selectionPeas.domain");
      } else {
        columnsHeader = new String[3];
        columnsHeader[COL_USER_LASTNAME] = m_Global.getString("GML.lastName");
        columnsHeader[COL_USER_FIRSTNAME] = m_Global.getString("GML.firstName");
        columnsHeader[COL_USER_EMAIL] = m_Global.getString("GML.eMail");
      }
      return columnsHeader;
    } else {
      return new String[0];
    }
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

  protected PanelLine getLineFromId(int what, String id) {
    String[] theValues;
    if (what == CM_SET) {
      Group theGroup = m_oc.getGroup(id);

      SilverTrace.info("selectionPeas",
          "CacheManagerUsersGroups.getSetLineFromId()",
          "root.GEN_MSG_PARAM_VALUE", "id=" + id);
      if (SelectionPeasSettings.m_DisplayNbUsersByGroup) {
        if (SelectionPeasSettings.m_DisplayDomains) {
          theValues = new String[4];
          theValues[COL_GROUP_NAME] = EncodeHelper
              .javaStringToHtmlString(theGroup.getName());
          theValues[COL_GROUP_DESCRIPTION] = EncodeHelper
              .javaStringToHtmlString(theGroup.getDescription());
          theValues[COL_GROUP_NBUSERS] = Integer.toString(m_oc
              .getAllSubUsersNumber(theGroup.getId()));
          theValues[COL_GROUP_NBUSERS + 1] = EncodeHelper
              .javaStringToHtmlString(m_ac.getDomain(theGroup.getDomainId())
                  .getName());
        } else {
          theValues = new String[3];
          theValues[COL_GROUP_NAME] = EncodeHelper
              .javaStringToHtmlString(theGroup.getName());
          theValues[COL_GROUP_DESCRIPTION] = EncodeHelper
              .javaStringToHtmlString(theGroup.getDescription());
          theValues[COL_GROUP_NBUSERS] = Integer.toString(m_oc
              .getAllSubUsersNumber(theGroup.getId()));
        }
      } else {
        if (SelectionPeasSettings.m_DisplayDomains) {
          theValues = new String[3];
          theValues[COL_GROUP_NAME] = EncodeHelper
              .javaStringToHtmlString(theGroup.getName());
          theValues[COL_GROUP_DESCRIPTION] = EncodeHelper
              .javaStringToHtmlString(theGroup.getDescription());
          theValues[COL_GROUP_DESCRIPTION + 1] = EncodeHelper
              .javaStringToHtmlString(m_ac.getDomain(theGroup.getDomainId())
                  .getName());
        } else {
          theValues = new String[2];
          theValues[COL_GROUP_NAME] = EncodeHelper
              .javaStringToHtmlString(theGroup.getName());
          theValues[COL_GROUP_DESCRIPTION] = EncodeHelper
              .javaStringToHtmlString(theGroup.getDescription());
        }
      }
      return new PanelLine(theGroup.getId(), theValues, false);
    } else if (what == CM_ELEMENT) {
      UserDetail theUser = m_oc.getUserDetail(id);

      SilverTrace.info("selectionPeas",
          "CacheManagerUsersGroups.getElementLineFromId()",
          "root.GEN_MSG_PARAM_VALUE", "id=" + id);
      if (SelectionPeasSettings.m_DisplayDomains) {
        theValues = new String[4];
        theValues[COL_USER_LASTNAME] = EncodeHelper
            .javaStringToHtmlString(theUser.getLastName());
        theValues[COL_USER_FIRSTNAME] = EncodeHelper
            .javaStringToHtmlString(theUser.getFirstName());
        theValues[COL_USER_EMAIL] = EncodeHelper.javaStringToHtmlString(theUser
            .geteMail());
        theValues[COL_USER_DOMAIN] = EncodeHelper.javaStringToHtmlString(m_ac
            .getDomain(theUser.getDomainId()).getName());
      } else {
        theValues = new String[3];
        theValues[COL_USER_LASTNAME] = EncodeHelper
            .javaStringToHtmlString(theUser.getLastName());
        theValues[COL_USER_FIRSTNAME] = EncodeHelper
            .javaStringToHtmlString(theUser.getFirstName());
        theValues[COL_USER_EMAIL] = EncodeHelper.javaStringToHtmlString(theUser
            .geteMail());
      }
      return new PanelLine(theUser.getId(), theValues, false);
    } else {
      return null;
    }
  }

  public int getLineCount(int what) {
    return 0;
  }

  public BrowsePanelProvider getSearchPanelProvider(int what, CacheManager cm,
      SelectionExtraParams sep) {
    if (what == CM_SET) {
      return new SearchGroupPanel(m_Language, m_Local, cm,
          getSureExtraParams(sep));
    } else if (what == CM_ELEMENT) {
      return new SearchUserPanel(m_Language, m_Local, cm,
          getSureExtraParams(sep));
    } else {
      return null;
    }
  }

  public BrowsePanelProvider getBrowsePanelProvider(int what, CacheManager cm,
      SelectionExtraParams sep) {
    if (what == CM_SET) {
      return new BrowseGroupPanel(m_Language, m_Local, cm,
          getSureExtraParams(sep));
    } else if (what == CM_ELEMENT) {
      return new BrowseUserPanel(m_Language, m_Local, cm,
          getSureExtraParams(sep));
    } else {
      return null;
    }
  }

  public PanelProvider getCartPanelProvider(int what, CacheManager cm,
      SelectionExtraParams sep) {
    if (what == CM_SET) {
      return new CartGroupPanel(m_Language, m_Local, cm,
          getSureExtraParams(sep));
    } else if (what == CM_ELEMENT) {
      return new CartUserPanel(m_Language, m_Local, cm, getSureExtraParams(sep));
    } else {
      return null;
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

  protected SelectionUsersGroups getSureExtraParams(SelectionExtraParams sep) {
    SelectionUsersGroups valret = (SelectionUsersGroups) sep;
    if (valret == null)
      valret = new SelectionUsersGroups();
    // If domain restricted -> add it (if not yet added)
    if (m_ud.isDomainAdminRestricted()
        && ((valret.getDomainId() == null) || (valret.getDomainId().length() <= 0))) {
      valret.setDomainId(m_ud.getDomainId());
    }
    return valret;
  }
}
