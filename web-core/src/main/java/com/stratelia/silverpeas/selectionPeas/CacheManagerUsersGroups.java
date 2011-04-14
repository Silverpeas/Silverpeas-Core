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

import com.silverpeas.util.EncodeHelper;
import com.stratelia.silverpeas.genericPanel.PanelLine;
import com.stratelia.silverpeas.genericPanel.PanelMiniFilterEdit;
import com.stratelia.silverpeas.genericPanel.PanelMiniFilterSelect;
import com.stratelia.silverpeas.genericPanel.PanelMiniFilterToken;
import com.stratelia.silverpeas.genericPanel.PanelOperation;
import com.stratelia.silverpeas.genericPanel.PanelProvider;
import com.stratelia.silverpeas.peasCore.URLManager;
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
  protected AdminController adminController = new AdminController(null);
  protected UserDetail userDetail = null;

  public CacheManagerUsersGroups(String language, ResourceLocator local,
      ResourceLocator icon, Selection selection, UserDetail ud) {
    super(language, local, icon, selection);
    userDetail = ud;
  }

  protected String getSetParentName(String id) {
    Group theGroup = m_oc.getGroup(id);
    if (theGroup != null) {
      String superId = theGroup.getSuperGroupId();
      Group theSuperGroup = m_oc.getGroup(superId);

      if (theSuperGroup != null) {
        return theSuperGroup.getName();
      }
      return "";
    }
    return "";
  }

  public String[][] getContentLines(CacheType what, String id) {
    String[][] result = new String[0][0];
    switch (what) {
      case CM_ELEMENT: {
        if (SelectionPeasSettings.displayUsersGroups) {
          AdminController ac = new AdminController(id);
          String[] groupIds = ac.getDirectGroupsIdsOfUser(id);
          String[] columns = getColumnsNames(CacheType.CM_SET);

          if (groupIds != null && groupIds.length > 0) {
            result = new String[groupIds.length][columns.length];
            PanelLine pl;
            for (int iGrp = 0; iGrp < groupIds.length; iGrp++) {
              pl = getLineFromId(CacheType.CM_SET, groupIds[iGrp]);
              for (int i = 0; i < columns.length; i++) {
                result[iGrp][i] = pl.m_Values[i];
              }
            }
          }
        }
      }
      return result;
      case CM_SET: {
        if (SelectionPeasSettings.displayGroupsUsers) {
          Group theGroup = m_oc.getGroup(id);
          String[] userIds = null;
          String[] columns = getColumnsNames(CacheType.CM_ELEMENT);
          if (theGroup != null) {
            userIds = theGroup.getUserIds();
          }
          if (userIds != null && userIds.length > 0) {
            result = new String[userIds.length][columns.length];
            PanelLine pl;
            for (int iUsr = 0; iUsr < userIds.length; iUsr++) {
              pl = getLineFromId(CacheType.CM_ELEMENT, userIds[iUsr]);
              for (int i = 0; i < columns.length; i++) {
                result[iUsr][i] = pl.m_Values[i];
              }
            }
          }
        }
      }
      return result;
    }
    return result;
  }

  public String[] getContentColumnsNames(CacheType what) {
    switch (what) {
      case CM_ELEMENT:
        return getColumnsNames(CacheType.CM_SET);
      case CM_SET:
        return getColumnsNames(CacheType.CM_ELEMENT);
      default:
        return new String[0];
    }
  }

  public String[][] getContentInfos(CacheType what, String id) {
    String[][] valret;

    if (what == CacheType.CM_ELEMENT) {
      PanelLine pl = getInfos(CacheType.CM_ELEMENT, id);
      String[] names = getColumnsNames(CacheType.CM_ELEMENT);
      valret = new String[names.length][2];
      for (int i = 0; (i < names.length) && (i < pl.m_Values.length); i++) {
        valret[i][0] = names[i];
        valret[i][1] = pl.m_Values[i];
      }
    } else {
      PanelLine pl = getInfos(CacheType.CM_SET, id);
      String[] names = getColumnsNames(CacheType.CM_SET);
      valret = new String[names.length + 1][2];
      for (int i = 0; (i < names.length) && (i < pl.m_Values.length); i++) {
        valret[i][0] = names[i];
        valret[i][1] = pl.m_Values[i];
      }
      valret[names.length][0] = localResourceLocator.getString("selectionPeas.groupParent");
      valret[names.length][1] = getSetParentName(id);
    }
    return valret;
  }

  public String getContentText(CacheType what) {
    if (what == CacheType.CM_SET) {
      return localResourceLocator.getString("selectionPeas.directUsersOfGroup");
    }
    if (what == CacheType.CM_ELEMENT) {
      return localResourceLocator.getString("selectionPeas.groupsOfUser");
    }
    return "";
  }

  public String[] getColumnsNames(CacheType what) {
    if (what == CacheType.CM_SET) {
      String[] columnsHeader;
      if (SelectionPeasSettings.displayNbUsersByGroup) {
        if (SelectionPeasSettings.displayDomains) {
          columnsHeader = new String[4];
          columnsHeader[COL_GROUP_NAME] = globalResourceLocator.getString("GML.nom");
          columnsHeader[COL_GROUP_DESCRIPTION] = globalResourceLocator
              .getString("GML.description");
          columnsHeader[COL_GROUP_NBUSERS] = globalResourceLocator.getString("GML.users");
          columnsHeader[COL_GROUP_NBUSERS + 1] = localResourceLocator
              .getString("selectionPeas.domain");
        } else {
          columnsHeader = new String[3];
          columnsHeader[COL_GROUP_NAME] = globalResourceLocator.getString("GML.nom");
          columnsHeader[COL_GROUP_DESCRIPTION] = globalResourceLocator
              .getString("GML.description");
          columnsHeader[COL_GROUP_NBUSERS] = globalResourceLocator.getString("GML.users");
        }
      } else {
        if (SelectionPeasSettings.displayDomains) {
          columnsHeader = new String[3];
          columnsHeader[COL_GROUP_NAME] = globalResourceLocator.getString("GML.nom");
          columnsHeader[COL_GROUP_DESCRIPTION] = globalResourceLocator
              .getString("GML.description");
          columnsHeader[COL_GROUP_DESCRIPTION + 1] = localResourceLocator
              .getString("selectionPeas.domain");
        } else {
          columnsHeader = new String[2];
          columnsHeader[COL_GROUP_NAME] = globalResourceLocator.getString("GML.nom");
          columnsHeader[COL_GROUP_DESCRIPTION] = globalResourceLocator
              .getString("GML.description");
        }
      }

      return columnsHeader;
    } else if (what == CacheType.CM_ELEMENT) {
      String[] columnsHeader;
      if (SelectionPeasSettings.displayDomains) {
        columnsHeader = new String[4];
        columnsHeader[COL_USER_LASTNAME] = globalResourceLocator.getString("GML.lastName");
        columnsHeader[COL_USER_FIRSTNAME] = globalResourceLocator.getString("GML.firstName");
        columnsHeader[COL_USER_EMAIL] = globalResourceLocator.getString("GML.eMail");
        columnsHeader[COL_USER_DOMAIN] = localResourceLocator.getString("selectionPeas.domain");
      } else {
        columnsHeader = new String[3];
        columnsHeader[COL_USER_LASTNAME] = globalResourceLocator.getString("GML.lastName");
        columnsHeader[COL_USER_FIRSTNAME] = globalResourceLocator.getString("GML.firstName");
        columnsHeader[COL_USER_EMAIL] = globalResourceLocator.getString("GML.eMail");
      }
      return columnsHeader;
    }
    return new String[0];
  }

  public PanelMiniFilterSelect getSelectMiniFilter(CacheType what) {
    if (what == CacheType.CM_SET) {
      return new PanelMiniFilterSelect(999, Integer.toString(what.getValue()), "set",
          URLManager.getApplicationURL() + iconResourceLocator.getString("selectionPeas.selectAll"),
          URLManager.getApplicationURL() + iconResourceLocator.getString(
              "selectionPeas.unSelectAll"), localResourceLocator.getString(
          "selectionPeas.selectAll"), localResourceLocator.getString("selectionPeas.unSelectAll"),
          localResourceLocator.getString("selectionPeas.selectAll"), localResourceLocator
          .getString("selectionPeas.unSelectAll"));
    } else if (what == CacheType.CM_ELEMENT) {
      return new PanelMiniFilterSelect(999, Integer.toString(what.getValue()), "element",
          URLManager.getApplicationURL() + iconResourceLocator.getString("selectionPeas.selectAll"),
          URLManager.getApplicationURL()
              + iconResourceLocator.getString("selectionPeas.unSelectAll"), localResourceLocator
          .getString("selectionPeas.selectAll"), localResourceLocator
          .getString("selectionPeas.unSelectAll"), localResourceLocator
          .getString("selectionPeas.selectAll"), localResourceLocator
          .getString("selectionPeas.unSelectAll"));
    } else {
      return null;
    }
  }

  public PanelMiniFilterToken[] getPanelMiniFilters(CacheType what) {
    if (what == CacheType.CM_SET) {
      PanelMiniFilterToken[] theArray = new PanelMiniFilterToken[1];
      theArray[0] = new PanelMiniFilterEdit(0, Integer.toString(what.getValue()), "",
          URLManager.getApplicationURL() + iconResourceLocator.getString("selectionPeas.filter"),
          localResourceLocator
              .getString("selectionPeas.filter"), localResourceLocator
          .getString("selectionPeas.filter"));
      return theArray;
    } else if (what == CacheType.CM_ELEMENT) {
      PanelMiniFilterToken[] theArray = new PanelMiniFilterToken[1];
      theArray[0] = new PanelMiniFilterEdit(0, Integer.toString(what.getValue()), "",
          URLManager.getApplicationURL() + iconResourceLocator.getString("selectionPeas.filter"),
          localResourceLocator
              .getString("selectionPeas.filter"), localResourceLocator
          .getString("selectionPeas.filter"));
      return theArray;
    } else {
      return new PanelMiniFilterToken[0];
    }
  }

  protected PanelLine getLineFromId(CacheType what, String id) {
    String[] theValues;
    if (what == CacheType.CM_SET) {
      Group theGroup = m_oc.getGroup(id);

      SilverTrace.info("selectionPeas",
          "CacheManagerUsersGroups.getSetLineFromId()",
          "root.GEN_MSG_PARAM_VALUE", "id=" + id);
      if (SelectionPeasSettings.displayNbUsersByGroup) {
        if (SelectionPeasSettings.displayDomains) {
          theValues = new String[4];
          theValues[COL_GROUP_NAME] = EncodeHelper
              .javaStringToHtmlString(theGroup.getName());
          theValues[COL_GROUP_DESCRIPTION] = EncodeHelper
              .javaStringToHtmlString(theGroup.getDescription());
          theValues[COL_GROUP_NBUSERS] = Integer.toString(m_oc
              .getAllSubUsersNumber(theGroup.getId()));
          theValues[COL_GROUP_NBUSERS + 1] = EncodeHelper
              .javaStringToHtmlString(adminController.getDomain(theGroup.getDomainId())
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
        if (SelectionPeasSettings.displayDomains) {
          theValues = new String[3];
          theValues[COL_GROUP_NAME] = EncodeHelper
              .javaStringToHtmlString(theGroup.getName());
          theValues[COL_GROUP_DESCRIPTION] = EncodeHelper
              .javaStringToHtmlString(theGroup.getDescription());
          theValues[COL_GROUP_DESCRIPTION + 1] = EncodeHelper
              .javaStringToHtmlString(adminController.getDomain(theGroup.getDomainId())
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
    } else if (what == CacheType.CM_ELEMENT) {
      UserDetail theUser = m_oc.getUserDetail(id);

      SilverTrace.info("selectionPeas",
          "CacheManagerUsersGroups.getElementLineFromId()",
          "root.GEN_MSG_PARAM_VALUE", "id=" + id);
      if (SelectionPeasSettings.displayDomains) {
        theValues = new String[4];
        theValues[COL_USER_LASTNAME] = EncodeHelper
            .javaStringToHtmlString(theUser.getLastName());
        theValues[COL_USER_FIRSTNAME] = EncodeHelper
            .javaStringToHtmlString(theUser.getFirstName());
        theValues[COL_USER_EMAIL] = EncodeHelper.javaStringToHtmlString(theUser
            .geteMail());
        theValues[COL_USER_DOMAIN] = EncodeHelper.javaStringToHtmlString(adminController
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

  public int getLineCount(CacheType what) {
    return 0;
  }

  public BrowsePanelProvider getSearchPanelProvider(CacheType what, SelectionExtraParams sep) {
    switch (what) {
      case CM_SET:
        return new SearchGroupPanel(language, localResourceLocator, this, getSureExtraParams(sep));
      case CM_ELEMENT:
        return new SearchUserPanel(language, localResourceLocator, this, getSureExtraParams(sep));
      default:
        return null;
    }
  }

  public BrowsePanelProvider getBrowsePanelProvider(CacheType what, SelectionExtraParams sep) {
    switch (what) {
      case CM_SET:
        return new BrowseGroupPanel(language, localResourceLocator, this, getSureExtraParams(sep));
      case CM_ELEMENT:
        return new BrowseUserPanel(language, localResourceLocator, this, getSureExtraParams(sep));
      default:
        return null;
    }
  }

  public PanelProvider getCartPanelProvider(CacheType what, SelectionExtraParams sep) {
    switch (what) {
      case CM_SET:
        return new CartGroupPanel(language, localResourceLocator, this, getSureExtraParams(sep));
      case CM_ELEMENT:
        return new CartUserPanel(language, localResourceLocator, this, getSureExtraParams(sep));
      default:
        return null;
    }
  }

  public PanelOperation getPanelOperation(String operation) {
    if ("DisplayBrowse".equals(operation)) {
      return new PanelOperation(localResourceLocator.getString("selectionPeas.helpBrowse"),
          URLManager.getApplicationURL() + iconResourceLocator.getString("selectionPeas.browseArb"),
          operation);
    } else if ("DisplaySearchElement".equals(operation)) {
      return new PanelOperation(localResourceLocator.getString("selectionPeas.helpSearchElement"),
          URLManager.getApplicationURL() + iconResourceLocator.getString("selectionPeas.userSearc"),
          operation);
    } else if ("DisplaySearchSet".equals(operation)) {
      return new PanelOperation(localResourceLocator.getString("selectionPeas.helpSearchSet"),
          URLManager.getApplicationURL() + iconResourceLocator.getString(
              "selectionPeas.groupSearc"), operation);
    } else {
      return null;
    }
  }

  protected SelectionUsersGroups getSureExtraParams(SelectionExtraParams sep) {
    SelectionUsersGroups valret = (SelectionUsersGroups) sep;
    if (valret == null) {
      valret = new SelectionUsersGroups();
    }
    // If domain restricted -> add it (if not yet added)
    if (userDetail.isDomainAdminRestricted()
        && ((valret.getDomainId() == null) || (valret.getDomainId().length() <= 0))) {
      valret.setDomainId(userDetail.getDomainId());
    }
    return valret;
  }
}
