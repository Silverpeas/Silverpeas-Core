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

import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.EncodeHelper;
import org.silverpeas.core.web.panel.PanelLine;
import org.silverpeas.core.web.panel.PanelMiniFilterEdit;
import org.silverpeas.core.web.panel.PanelMiniFilterSelect;
import org.silverpeas.core.web.panel.PanelMiniFilterToken;
import org.silverpeas.core.web.panel.PanelOperation;
import org.silverpeas.core.web.panel.PanelProvider;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.SettingBundle;

public class CacheManagerUsersGroups extends CacheManager {
  protected static final int COL_USER_LASTNAME = 0;
  protected static final int COL_USER_FIRSTNAME = 1;
  protected static final int COL_USER_EMAIL = 2;
  protected static final int COL_USER_DOMAIN = 3;

  protected static final int COL_GROUP_NAME = 0;
  protected static final int COL_GROUP_DESCRIPTION = 1;
  protected static final int COL_GROUP_NBUSERS = 2;

  protected OrganizationController m_oc = OrganizationControllerProvider.getOrganisationController();
  protected AdminController adminController = ServiceProvider.getService(AdminController.class);
  protected UserDetail userDetail = null;
  protected LocalizationBundle messages;
  protected SettingBundle iconSettings;

  public CacheManagerUsersGroups(String language, LocalizationBundle messages,
      SettingBundle icons, Selection selection, UserDetail ud) {
    super(language, selection);
    this.messages = messages;
    this.iconSettings = icons;
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
          AdminController ac = ServiceProvider.getService(AdminController.class);
          String[] groupIds = ac.getDirectGroupsIdsOfUser(id);
          String[] columns = getColumnsNames(CacheType.CM_SET);

          if (groupIds != null && groupIds.length > 0) {
            result = new String[groupIds.length][columns.length];
            PanelLine pl;
            for (int iGrp = 0; iGrp < groupIds.length; iGrp++) {
              pl = getLineFromId(CacheType.CM_SET, groupIds[iGrp]);
              System.arraycopy(pl.m_Values, 0, result[iGrp], 0, columns.length);
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
              System.arraycopy(pl.m_Values, 0, result[iUsr], 0, columns.length);
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
        return ArrayUtil.EMPTY_STRING_ARRAY;
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
      valret[names.length][0] = messages.getString("selectionPeas.groupParent");
      valret[names.length][1] = getSetParentName(id);
    }
    return valret;
  }

  public String getContentText(CacheType what) {
    if (what == CacheType.CM_SET) {
      return messages.getString("selectionPeas.directUsersOfGroup");
    }
    if (what == CacheType.CM_ELEMENT) {
      return messages.getString("selectionPeas.groupsOfUser");
    }
    return "";
  }

  public String[] getColumnsNames(CacheType what) {
    if (what == CacheType.CM_SET) {
      String[] columnsHeader;
      if (SelectionPeasSettings.displayNbUsersByGroup) {
        if (SelectionPeasSettings.displayDomains) {
          columnsHeader = new String[4];
          columnsHeader[COL_GROUP_NAME] = messages.getString("GML.nom");
          columnsHeader[COL_GROUP_DESCRIPTION] = messages
              .getString("GML.description");
          columnsHeader[COL_GROUP_NBUSERS] = messages.getString("GML.users");
          columnsHeader[COL_GROUP_NBUSERS + 1] = messages
              .getString("selectionPeas.domain");
        } else {
          columnsHeader = new String[3];
          columnsHeader[COL_GROUP_NAME] = messages.getString("GML.nom");
          columnsHeader[COL_GROUP_DESCRIPTION] = messages
              .getString("GML.description");
          columnsHeader[COL_GROUP_NBUSERS] = messages.getString("GML.users");
        }
      } else {
        if (SelectionPeasSettings.displayDomains) {
          columnsHeader = new String[3];
          columnsHeader[COL_GROUP_NAME] = messages.getString("GML.nom");
          columnsHeader[COL_GROUP_DESCRIPTION] = messages
              .getString("GML.description");
          columnsHeader[COL_GROUP_DESCRIPTION + 1] = messages
              .getString("selectionPeas.domain");
        } else {
          columnsHeader = new String[2];
          columnsHeader[COL_GROUP_NAME] = messages.getString("GML.nom");
          columnsHeader[COL_GROUP_DESCRIPTION] = messages
              .getString("GML.description");
        }
      }

      return columnsHeader;
    } else if (what == CacheType.CM_ELEMENT) {
      String[] columnsHeader;
      if (SelectionPeasSettings.displayDomains) {
        columnsHeader = new String[4];
        columnsHeader[COL_USER_LASTNAME] = messages.getString("GML.lastName");
        columnsHeader[COL_USER_FIRSTNAME] = messages.getString("GML.firstName");
        columnsHeader[COL_USER_EMAIL] = messages.getString("GML.eMail");
        columnsHeader[COL_USER_DOMAIN] = messages.getString("selectionPeas.domain");
      } else {
        columnsHeader = new String[3];
        columnsHeader[COL_USER_LASTNAME] = messages.getString("GML.lastName");
        columnsHeader[COL_USER_FIRSTNAME] = messages.getString("GML.firstName");
        columnsHeader[COL_USER_EMAIL] = messages.getString("GML.eMail");
      }
      return columnsHeader;
    }
    return ArrayUtil.EMPTY_STRING_ARRAY;
  }

  public PanelMiniFilterSelect getSelectMiniFilter(CacheType what) {
    if (what == CacheType.CM_SET) {
      return new PanelMiniFilterSelect(
          999,
          Integer.toString(what.getValue()),
          "set",
          URLUtil.getApplicationURL() + iconSettings.getString("selectionPeas.selectAll"),
          URLUtil.getApplicationURL() + iconSettings.getString(
              "selectionPeas.unSelectAll"), messages.getString(
              "selectionPeas.selectAll"), messages
              .getString("selectionPeas.unSelectAll"),
          messages.getString("selectionPeas.selectAll"), messages
              .getString("selectionPeas.unSelectAll"));
    } else if (what == CacheType.CM_ELEMENT) {
      return new PanelMiniFilterSelect(
          999,
          Integer.toString(what.getValue()),
          "element",
          URLUtil.getApplicationURL() + iconSettings.getString("selectionPeas.selectAll"),
          URLUtil.getApplicationURL()
              + iconSettings.getString("selectionPeas.unSelectAll"), messages
              .getString("selectionPeas.selectAll"), messages
              .getString("selectionPeas.unSelectAll"), messages
              .getString("selectionPeas.selectAll"), messages
              .getString("selectionPeas.unSelectAll"));
    } else {
      return null;
    }
  }

  public PanelMiniFilterToken[] getPanelMiniFilters(CacheType what) {
    if (what == CacheType.CM_SET) {
      PanelMiniFilterToken[] theArray = new PanelMiniFilterToken[1];
      theArray[0] = new PanelMiniFilterEdit(0, Integer.toString(what.getValue()), "",
          URLUtil.getApplicationURL() + iconSettings.getString("selectionPeas.filter"),
          messages
          .getString("selectionPeas.filter"), messages
          .getString("selectionPeas.filter"));
      return theArray;
    } else if (what == CacheType.CM_ELEMENT) {
      PanelMiniFilterToken[] theArray = new PanelMiniFilterToken[1];
      theArray[0] = new PanelMiniFilterEdit(0, Integer.toString(what.getValue()), "",
          URLUtil.getApplicationURL() + iconSettings.getString("selectionPeas.filter"),
          messages
          .getString("selectionPeas.filter"), messages
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
        return new SearchGroupPanel(language, messages, this, getSureExtraParams(sep));
      case CM_ELEMENT:
        return new SearchUserPanel(language, messages, this, getSureExtraParams(sep));
      default:
        return null;
    }
  }

  public BrowsePanelProvider getBrowsePanelProvider(CacheType what, SelectionExtraParams sep) {
    switch (what) {
      case CM_SET:
        return new BrowseGroupPanel(language, messages, this, getSureExtraParams(sep));
      case CM_ELEMENT:
        return new BrowseUserPanel(language, messages, this, getSureExtraParams(sep));
      default:
        return null;
    }
  }

  public PanelProvider getCartPanelProvider(CacheType what, SelectionExtraParams sep) {
    switch (what) {
      case CM_SET:
        return new CartGroupPanel(language, messages, this, getSureExtraParams(sep));
      case CM_ELEMENT:
        return new CartUserPanel(language, messages, this, getSureExtraParams(sep));
      default:
        return null;
    }
  }

  public PanelOperation getPanelOperation(String operation) {
    if ("DisplayBrowse".equals(operation)) {
      return new PanelOperation(
          messages.getString("selectionPeas.helpBrowse"),
          URLUtil.getApplicationURL() + iconSettings.getString("selectionPeas.browseArb"),
          operation);
    } else if ("DisplaySearchElement".equals(operation)) {
      return new PanelOperation(
          messages.getString("selectionPeas.helpSearchElement"),
          URLUtil.getApplicationURL() + iconSettings.getString("selectionPeas.userSearc"),
          operation);
    } else if ("DisplaySearchSet".equals(operation)) {
      return new PanelOperation(messages.getString("selectionPeas.helpSearchSet"),
          URLUtil.getApplicationURL() + iconSettings.getString(
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
