/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.alertuser.control;

import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.mvc.util.AlertUser;
import org.silverpeas.core.notification.user.client.GroupRecipient;
import org.silverpeas.core.notification.NotificationException;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.NotificationSender;
import org.silverpeas.core.notification.user.client.UserRecipient;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.core.web.selection.SelectionUsersGroups;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.owasp.encoder.Encode;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.StringUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Class declaration
 *
 * @author
 */
public class AlertUserPeasSessionController extends AbstractComponentSessionController {

  protected AlertUser alertUser;
  protected Selection selection;
  protected NotificationSender notificationSender;
  protected String webContext = URLUtil.getApplicationURL();
  protected UserDetail[] userRecipients;
  protected Group[] groupRecipients;

  /**
   * Standard Session Controller Constructeur
   *
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   *
   */
  public AlertUserPeasSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "org.silverpeas.alertUserPeas.multilang.alertUserPeasBundle",
        "org.silverpeas.alertUserPeas.settings.alertUserPeasIcons");
    setComponentRootName(URLUtil.CMP_ALERTUSERPEAS);
    alertUser = getAlertUser();
    selection = getSelection();
    notificationSender = new NotificationSender(null);
  }

  public void init() {
    userRecipients = new UserDetail[0];
    groupRecipients = new Group[0];
  }

  // -------------------------------------------------------------------------------------------------------------------
  // ------------------------------------------- Navigation Functions
  // ----------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  public Pair getHostComponentName() {
    return alertUser.getHostComponentName();
  }

  public String getHostSpaceName() {
    return alertUser.getHostSpaceName();
  }

  public String getHostComponentId() {
    return alertUser.getHostComponentId();
  }

  public List<String> getHostPath() {
    return alertUser.getHostPath();
  }

  public UserDetail[] getUserRecipients() {
    return userRecipients;
  }

  public Group[] getGroupRecipients() {
    return groupRecipients;
  }

  public NotificationMetaData getNotificationMetaData() {
    return alertUser.getNotificationMetaData();
  }

  // initialisation de Selection pour nav vers SelectionPeas
  public String initSelection() {
    String url = webContext + URLUtil.getURL(getComponentRootName(), null, null);
    String goUrl = url + "FromSelection";
    String cancelUrl = url + "Close";

    selection.resetAll();

    selection.setGoBackURL(goUrl);
    selection.setCancelURL(cancelUrl);

    // bien que le up s'affiche en popup, le mécanisme de fermeture est assuré
    // par le composant=> il est donc nécessaire d'indiquer
    // à l'UserPanelPeas de ne pas s'occuper de cette fermeture!
    selection.setHostPath(null);
    selection.setHostComponentName(getHostComponentName());
    selection.setHostSpaceName(getHostSpaceName());

    // Add extra params
    SelectionUsersGroups sug = alertUser.getSelectionUsersGroups();
    if (sug == null) {
      sug = new SelectionUsersGroups();
    }
    sug.setComponentId(getHostComponentId());
    selection.setExtraParams(sug);

    // Limitations
    if (getUserDetail().isUserManualNotificationUserReceiverLimit()) {
      selection
          .setSelectedUserLimit(getUserDetail().getUserManualNotificationUserReceiverLimitValue());
    }

    return Selection.getSelectionURL();
  }

  // recupération des users et groupes selectionnés au travers de
  // selectionPeas
  public void computeSelection() {
    userRecipients = SelectionUsersGroups.getUserDetails(selection
        .getSelectedElements());
    groupRecipients = SelectionUsersGroups.getGroups(selection
        .getSelectedSets());
    Arrays.sort(userRecipients);
    Arrays.sort(groupRecipients);
  }

  public void prepareNotification(String message) {
    NotificationMetaData notifMetaData = getNotificationMetaData();
    for (String userId : SelectionUsersGroups.getUserIds(getUserRecipients())) {
      notifMetaData.addUserRecipient(new UserRecipient(userId));
    }
    for (String groupId : SelectionUsersGroups.getGroupIds(getGroupRecipients())) {
      notifMetaData.addGroupRecipient(new GroupRecipient(groupId));
    }
    if (StringUtil.isDefined(message) && (ArrayUtil.isNotEmpty(getUserRecipients())
        || ArrayUtil.isNotEmpty(getGroupRecipients()))) {
      String safeMessage = Encode.forHtml(message);
      for (String language : DisplayI18NHelper.getLanguages()) {
        setNotificationContent(safeMessage, language);
      }
    }
    setSender();
  }

  private void setSender() {
    getNotificationMetaData().setSender(getUserId());
    for (SilverpeasTemplate template : getNotificationMetaData().getTemplates().values()) {
      template.setAttribute("sender", getUserDetail());
      template.setAttribute("senderName", getUserDetail().getDisplayedName());
    }
  }

  private void setNotificationContent(String message, String language) {
    getNotificationMetaData().addExtraMessage(message, language);
  }

  public void sendNotification() throws NotificationException {
    notificationSender.notifyUser(getNotificationMetaData().manualUserNotification());
  }
}