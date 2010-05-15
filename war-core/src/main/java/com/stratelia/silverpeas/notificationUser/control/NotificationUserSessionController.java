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

/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

/*
 * NotificationUserSessionControl.java
 * 
 */

package com.stratelia.silverpeas.notificationUser.control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Properties;

import com.silverpeas.util.EncodeHelper;
import com.stratelia.silverpeas.notificationManager.NotificationManager;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.notificationUser.NotificationUserException;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.PairObject;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * @author tleroi
 * @version
 */
public class NotificationUserSessionController extends AbstractComponentSessionController {
  Selection sel = null;

  /* paramaters of a notification */
  String txtTitle = null;
  String txtMessage = null;
  String notificationId = null;
  String priorityId = null;

  /**
   * Constructor declaration
   * @param mainSessionCtrl
   * @param componentContext
   * @see
   */
  public NotificationUserSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "com.stratelia.silverpeas.notificationUser.multilang.notificationUserBundle");
    setComponentRootName(URLManager.CMP_NOTIFICATIONUSER);
    sel = getSelection();
  }

  public void resetNotification() {
    setTxtTitle(null);
    setTxtMessage(null);
    setNotificationId(null);
    setPriorityId(null);
  }

  /**
   * Method declaration
   * @return
   * @throws NotificationManagerException
   * @see
   */
  public ArrayList<Properties> getDefaultAddresses() throws NotificationManagerException {
    // Retreive all default addresses except Trashbean address
    ArrayList<Properties> al = new ArrayList<Properties>();
    NotificationManager nm = new NotificationManager(getLanguage());
    int uId = Integer.parseInt(getUserId());

    al.add(nm.getNotificationAddress(
        NotificationParameters.ADDRESS_BASIC_POPUP, uId));
    al.add(nm.getNotificationAddress(
        NotificationParameters.ADDRESS_BASIC_SILVERMAIL, uId));
    al.add(nm.getNotificationAddress(
        NotificationParameters.ADDRESS_BASIC_SMTP_MAIL, uId));
    return al;
  }

  /**
   * Method declaration
   * @return
   * @throws NotificationManagerException
   * @see
   */
  public ArrayList<Properties> getNotifPriorities() throws NotificationManagerException {
    NotificationManager nm = new NotificationManager(getLanguage());
    return nm.getNotifPriorities();
  }

  /**
   * Method declaration
   * @return
   * @throws NotificationUserException
   * @see
   */
  public ArrayList<Properties> getAvailableGroups() throws NotificationUserException {
    Group[] allGroups = null;
    Properties p;
    int i;
    ArrayList<Properties> ar = new ArrayList<Properties>();

    allGroups = getOrganizationController().getAllGroups();
    for (i = 0; i < allGroups.length; i++) {
      p = new Properties();
      p.setProperty("id", allGroups[i].getId());
      p.setProperty("name", allGroups[i].getName());
      ar.add(p);
    }
    return ar;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public ArrayList<Properties> getAvailableUsers() {
    UserDetail[] allUsers = null;
    Properties p;
    int i;
    ArrayList<Properties> ar = new ArrayList<Properties>();

    allUsers = getOrganizationController().getAllUsers();
    for (i = 0; i < allUsers.length; i++) {
      p = new Properties();
      p.setProperty("id", allUsers[i].getId());
      p.setProperty("name", allUsers[i].getLastName() + " "
          + allUsers[i].getFirstName());
      ar.add(p);
    }
    return ar;
  }

  /**
   * Method declaration
   * @param compoId
   * @param notificationId
   * @param priorityId
   * @param txtTitle
   * @param txtMessage
   * @param selectedUsers
   * @param selectedGroups
   * @throws NotificationManagerException
   * @see
   */

  public void sendMessage(String compoId, String notificationId,
      String priorityId, String txtTitle, String txtMessage,
      String[] selectedUsers, String[] selectedGroups)
      throws NotificationManagerException {
    NotificationSender notifSender = new NotificationSender(compoId);
    int notifTypeId = NotificationParameters.ADDRESS_COMPONENT_DEFINED;

    SilverTrace
        .debug("notificationUser",
        "NotificationUsersessionController.sendMessage()",
        "root.MSG_GEN_PARAM_VALUE", "  AVANT CONTROLE priorityId="
        + priorityId);

    if ((notificationId != null) && (notificationId.length() > 0)) {
      notifTypeId = Integer.parseInt(notificationId);
    }

    // pb sous bea, quand prirityId est vide, le Integer.parseInt(priorityId)
    // cause une exception
    if (priorityId == null || priorityId.length() == 0)
      priorityId = "0";

    NotificationMetaData notifMetaData = new NotificationMetaData(Integer
        .parseInt(priorityId), txtTitle, txtMessage);
    notifMetaData.setSender(getUserId());
    notifMetaData.setSource(getString("manualNotification"));
    notifMetaData.addUserRecipients(selectedUsers);
    notifMetaData.addGroupRecipients(selectedGroups);
    notifSender.notifyUser(notifTypeId, notifMetaData);
  }

  /**
   * Method declaration
   * @param src
   * @return
   * @see
   */
  public String[] getIdsArrayFromIdsLine(String src) {
    return NotificationSender.getIdsArrayFromIdsLine(src);
  }

  public String buildOptions(ArrayList<Properties> ar, String selectValue, String selectText) {
    return buildOptions(ar, selectValue, selectText, false);
  }

  public String buildOptions(ArrayList<Properties> ar, String selectValue,
      String selectText, boolean bSorted) {
    StringBuffer valret = new StringBuffer();
    Properties elmt = null;
    String selected;
    ArrayList arToDisplay = ar;
    int i;

    if (selectText != null) {
      if ((selectValue == null) || (selectValue.length() <= 0)) {
        selected = "SELECTED";
      } else {
        selected = "";
      }
      valret.append("<option value=\"\" " + selected + ">"
          + EncodeHelper.javaStringToHtmlString(selectText) + "</option>\n");
    }
    if (bSorted) {
      Properties[] theList = (Properties[]) ar.toArray(new Properties[0]);
      Arrays.sort(theList, new Comparator() {
          public int compare(Object o1, Object o2) {
          return (((Properties) o1).getProperty("name")).toUpperCase()
              .compareTo(((Properties) o2).getProperty("name").toUpperCase());
          }

        public boolean equals(Object o) {
          return false;
          }
                });
      arToDisplay = new ArrayList<Properties>(theList.length);
      for (i = 0; i < theList.length; i++) {
        arToDisplay.add(theList[i]);
      }
    }
    if (arToDisplay != null) {
      for (i = 0; i < arToDisplay.size(); i++) {
        elmt = (Properties) arToDisplay.get(i);
        if (elmt.getProperty("id").equalsIgnoreCase(selectValue)) {
          selected = "SELECTED";
        } else {
          selected = "";
        }
        valret.append("<option value=\"" + elmt.getProperty("id") + "\" "
            + selected + ">"
            + EncodeHelper.javaStringToHtmlString(elmt.getProperty("name"))
            + "</option>\n");
      }
    }
    return valret.toString();
  }

  // JCG
  public String initSelectionPeas(String[] idUsers, String[] idGroups,
      String paramValues) {
    SilverTrace.debug("notificationUser",
        "NotificationUsersessionController.initSelectionPeas()",
        "root.MSG_GEN_PARAM_VALUE", "ENTER METHOD");

    String m_context = GeneralPropertiesManager.getGeneralResourceLocator()
        .getString("ApplicationURL");
    String hostUrl = m_context
        + URLManager.getURL(URLManager.CMP_NOTIFICATIONUSER) + "GetTarget"
        + paramValues;
    String cancelUrl = m_context
        + URLManager.getURL(URLManager.CMP_NOTIFICATIONUSER) + "GetTarget"
        + paramValues;
    PairObject hostComponentName = new PairObject("", "");

    sel.resetAll();
    sel.setHostSpaceName(this.getString("domainName"));
    sel.setHostComponentName(hostComponentName);
    sel.setHostPath(null);

    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(cancelUrl);

    sel.setSelectedElements(idUsers);
    sel.setSelectedSets(idGroups);
    // Contraintes
    sel.setMultiSelect(true);
    sel.setPopupMode(true);
    if (((idUsers == null) || (idUsers.length == 0))
        && ((idGroups == null) || (idGroups.length == 0))) {
      sel.setFirstPage(Selection.FIRST_PAGE_BROWSE);
    } else {
      sel.setFirstPage(Selection.FIRST_PAGE_CART);
    }
    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  public String[] getTargetIdUsers() {
    return sel.getSelectedElements();
  }

  public String[] getTargetIdGroups() {
    return sel.getSelectedSets();
  }

  public String[] initTargetsUsers(String theTargetsUsers) {
    String[] idUsers = new String[0];
    if (theTargetsUsers != null && theTargetsUsers.length() > 0) {
      if (theTargetsUsers.equals("Administrators"))
        idUsers = this.getOrganizationController().getAdministratorUserIds(
            getUserId());
      else
        idUsers = this.getIdsArrayFromIdsLine(theTargetsUsers);
    }
    return idUsers;
  }

  public String[] initTargetsGroups(String theTargetsGroups) {
    String[] idGroups = new String[0];
    if (theTargetsGroups != null && theTargetsGroups.length() > 0) {
      idGroups = this.getIdsArrayFromIdsLine(theTargetsGroups);
    }
    return idGroups;
  }

  public ArrayList<Properties> getSelectedUsers(String[] selectedUersId)
      throws NotificationUserException {
    Properties p;
    int i;
    ArrayList<Properties> ar = new ArrayList<Properties>();
    UserDetail[] selectedUsers = null;

    if (selectedUersId != null && selectedUersId.length > 0) {
      selectedUsers = this.getUserDetailList(selectedUersId);
      if ((selectedUsers != null) && (selectedUsers.length > 0)) {
        for (i = 0; i < selectedUsers.length; i++) {
          p = new Properties();
          p.setProperty("id", selectedUsers[i].getId());
          p.setProperty("name", selectedUsers[i].getLastName() + " "
              + selectedUsers[i].getFirstName());
          ar.add(p);
        }
        if (ar.size() != selectedUsers.length) {
          throw new NotificationUserException(
              "NotificationUserSessionControl.getSelectedUsers()",
              SilverpeasException.ERROR,
              "notificationUser.EX_CANT_GET_SELECTED_USERS_INFOS");
        }
      }
    }
    return ar;
  }

  public ArrayList<Properties> getSelectedGroups(String[] selectedGroupsId)
      throws NotificationUserException {
    SilverTrace.debug("notificationUser",
        "NotificationUsersessionController.getSelectedGroups()",
        "root.MSG_GEN_PARAM_VALUE", "ENTER METHOD");
    Group[] selectedGroups = null;
    Properties p;
    int i;
    ArrayList<Properties> ar = new ArrayList<Properties>();

    if (selectedGroupsId != null && selectedGroupsId.length > 0) {
      selectedGroups = this.getGroupList(selectedGroupsId);

      if ((selectedGroups != null) && (selectedGroups.length > 0)) {
        for (i = 0; i < selectedGroups.length; i++) {
          p = new Properties();
          p.setProperty("id", selectedGroups[i].getId());
          p.setProperty("name", selectedGroups[i].getName());
          ar.add(p);
        }
        if (ar.size() != selectedGroups.length) {
          throw new NotificationUserException(
              "NotificationUserSessionControl.getSelectedGroups()",
              SilverpeasException.ERROR,
              "notificationUser.EX_CANT_GET_SELECTED_GROUPS_INFOS");
        }
      }
    }
    return ar;
  }

  private UserDetail[] getUserDetailList(String[] idUsers) {
    SilverTrace.debug("notificationUser",
        "NotificationUsersessionController.getUserDetailList()",
        "root.MSG_GEN_PARAM_VALUE", "Enter Method");
    return this.getOrganizationController().getUserDetails(idUsers);
  }

  private Group[] getGroupList(String[] idGroups) {
    Group[] setOfGroup = null;
    if (idGroups != null && idGroups.length > 0) {
      setOfGroup = new Group[idGroups.length];
      for (int i = 0; i < idGroups.length; i++) {
        setOfGroup[i] = this.getOrganizationController().getGroup(idGroups[i]);
      }
    }
    return setOfGroup;
  }

  public String getNotificationId() {
    return notificationId;
  }

  public void setNotificationId(String notificationId) {
    this.notificationId = notificationId;
  }

  public String getPriorityId() {
    return priorityId;
  }

  public void setPriorityId(String priorityId) {
    this.priorityId = priorityId;
  }

  public String getTxtMessage() {
    return txtMessage;
  }

  public void setTxtMessage(String txtMessage) {
    this.txtMessage = txtMessage;
  }

  public String getTxtTitle() {
    return txtTitle;
  }

  public void setTxtTitle(String txtTitle) {
    this.txtTitle = txtTitle;
  }
}
