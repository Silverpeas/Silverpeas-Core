/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.notificationuser.control;

import org.silverpeas.core.notification.user.client.NotificationManager;
import org.silverpeas.core.notification.user.client.NotificationManagerException;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.NotificationParameters;
import org.silverpeas.core.notification.user.client.NotificationSender;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.web.notificationuser.Notification;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.StringUtil;

import java.util.ArrayList;
import java.util.Properties;

/**
 * @author tleroi
 * @version
 */
public class NotificationUserSessionController extends AbstractComponentSessionController {

  Selection sel = null;

  /* paramaters of a notification */
  Notification notification = null;

  /**
   * Constructor declaration
   *
   * @param mainSessionCtrl
   * @param componentContext
   * @see
   */
  public NotificationUserSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "org.silverpeas.notificationUser.multilang.notificationUserBundle");
    setComponentRootName(URLUtil.CMP_NOTIFICATIONUSER);
    sel = getSelection();
  }

  public Notification resetNotification() {
    notification = new Notification();
    sel.resetAll();
    return notification;
  }

  /**
   * Method declaration
   *
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
   * @param notification
   * @throws NotificationManagerException
   */
  public void sendMessage(Notification notification) throws NotificationManagerException {
    NotificationSender notifSender = new NotificationSender(null);
    NotificationMetaData notifMetaData = notification.toNotificationMetaData();
    notifMetaData.setSender(getUserId());
    notifMetaData.setSource(getString("manualNotification"));
    if (StringUtil.isDefined(sel.getFirstSelectedElement()) ||
        StringUtil.isDefined(sel.getFirstSelectedSet())) {
      // The selection container has been set from the user panel, so the notification must be
      // tagged as a manuel one.
      notifMetaData.manualUserNotification();
    } else {
      // The user panel has not been displayed and the receiver container has been set
      // automatically, so the notification is not tagged as a manual one (and centralized
      // verifications will be skipped).
    }

    notifSender.notifyUser(notification.getChannel(), notifMetaData);
  }

  /**
   * Method declaration
   *
   * @param src
   * @return
   * @see
   */
  public String[] getIdsArrayFromIdsLine(String src) {
    return NotificationSender.getIdsArrayFromIdsLine(src);
  }

  public String initSelectionPeas(String paramValues) {
    String m_context = URLUtil.getApplicationURL();
    String hostUrl = m_context
        + URLUtil.getURL(URLUtil.CMP_NOTIFICATIONUSER) + "GetTarget"
        + paramValues;
    String cancelUrl = hostUrl;
    Pair<String, String> hostComponentName = new Pair<>("", "");

    sel.resetAll();
    sel.setHostSpaceName("");
    sel.setHostComponentName(hostComponentName);
    sel.setHostPath(null);

    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(cancelUrl);

    Notification notification = getNotification();
    sel.setSelectedElements(notification.getUserIds());
    sel.setSelectedSets(notification.getGroupIds());
    // Contraintes
    sel.setMultiSelect(true);
    sel.setPopupMode(true);

    // Limitations
    if (getUserDetail().isUserManualNotificationUserReceiverLimit()) {
      sel.setSelectedUserLimit(getUserDetail().getUserManualNotificationUserReceiverLimitValue());
    }

    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  public Notification initTargets(String theTargetsUsers, String theTargetsGroups) {
    Notification notification = resetNotification();
    notification.setUsers(initTargetsUsers(theTargetsUsers));
    notification.setGroups(initTargetsGroups(theTargetsGroups));
    return notification;
  }

  private String[] initTargetsUsers(String theTargetsUsers) {
    String[] idUsers = new String[0];
    if (StringUtil.isDefined(theTargetsUsers)) {
      if (theTargetsUsers.equals("Administrators")) {
        idUsers = getOrganisationController().getAdministratorUserIds(getUserId());
      } else {
        idUsers = this.getIdsArrayFromIdsLine(theTargetsUsers);
      }
    }
    return idUsers;
  }

  private String[] initTargetsGroups(String theTargetsGroups) {
    String[] idGroups = new String[0];
    if (theTargetsGroups != null && theTargetsGroups.length() > 0) {
      idGroups = this.getIdsArrayFromIdsLine(theTargetsGroups);
    }
    return idGroups;
  }

  public Notification getNotificationWithNewRecipients() {
    if (notification != null) {
      notification.setUsers(sel.getSelectedElements());
      notification.setGroups(sel.getSelectedSets());
    }
    return notification;
  }

  public Notification getNotification() {
    return notification;
  }

  public void setNotification(Notification notification) {
    this.notification = notification;
  }

}
