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
package org.silverpeas.core.notification.user.client;

import org.silverpeas.core.personalization.service.PersonalizationServiceProvider;
import org.silverpeas.core.notification.user.client.constant.NotifMediaType;
import org.silverpeas.core.notification.user.client.model.SentNotificationInterface;
import org.silverpeas.core.notification.user.client.model.SentNotificationInterfaceImpl;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.exception.SilverpeasRuntimeException;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.i18n.I18NHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Cette classe est utilisee par les composants pour envoyer une notification a un (ou des)
 * utilisateur(s) (ou groupes) Elle package les appels et appelle la fonction du NotificationManager
 * pour reellement envoyer les notifications
 */
public class NotificationSender implements java.io.Serializable {

  private static final long serialVersionUID = 4165938893905145809L;

  protected NotificationManager notificationManager = null;
  protected int instanceId = -1;

  /**
   * Default constructor
   */
  protected NotificationSender() {
    this(null);
  }

  /**
   * Constructor for a standard component
   * @param instanceId the instance Id of the calling's component
   */
  public NotificationSender(String instanceId) {
    this.instanceId = extractLastNumber(instanceId);
    notificationManager = new NotificationManager(null);
  }

  /**
   * Method declaration
   * @param metaData
   * @throws NotificationManagerException
   * @see
   */
  public void notifyUser(NotificationMetaData metaData)
      throws NotificationManagerException {
    notifyUser(NotifMediaType.COMPONENT_DEFINED.getId(), metaData);
  }

  /**
   * Method declaration
   * @param aMediaType
   * @param metaData
   * @throws NotificationManagerException
   * @see
   */
  public void notifyUser(int aMediaType, NotificationMetaData metaData)
      throws NotificationManagerException {
    CurrentUserNotificationContext.getCurrentUserNotificationContext().checkManualUserNotification(metaData);

    // Getting all the user recipients that represents the user and group recipients
    Set<UserRecipient> usersSet = metaData.getAllUserRecipients(true);

    Set<String> languages = metaData.getLanguages();
    Map<String, String> usersLanguage = new HashMap<>(usersSet.size());
    for (UserRecipient user : usersSet) {
      usersLanguage.put(user.getUserId(),
          PersonalizationServiceProvider.getPersonalizationService().
          getUserSettings(user.getUserId()).getLanguage());
    }

    NotificationParameters params = null;

    // All usersId to notify
    Set<String> allUserIds = usersLanguage.keySet();

    for (String language : languages) {
      params = getNotificationParameters(aMediaType, metaData);
      params.sTitle = metaData.getTitle(language);
      params.sLinkLabel = metaData.getLinkLabel(language);
      params.sMessage = metaData.getContent(language);
      params.sLanguage = language;
      params.nNotificationResourceData = metaData.getNotificationResourceData(language);

      // Notify users with their native language
      List<String> userIds = getUserIds(language, usersLanguage);
      // remove users already notified in their language
      allUserIds.removeAll(userIds);
      notificationManager.notifyUsers(params, userIds.toArray(new String[userIds.size()]));
    }
    // Notify other users in language of the sender.
    notificationManager.notifyUsers(params, allUserIds.toArray(new String[allUserIds.size()]));

    if (CollectionUtil.isNotEmpty(metaData.getExternalRecipients())) {
      // We only use default language for external notification
      params.sLanguage = I18NHelper.defaultLanguage;
      params.sTitle = metaData.getTitle(params.sLanguage);
      params.sLinkLabel = metaData.getLinkLabel(params.sLanguage);
      params.sMessage = metaData.getContent(params.sLanguage);
      notificationManager.notifyExternals(params, metaData.getExternalRecipients());
    }

    if (metaData.isSendByAUser() &&
        aMediaType != NotificationParameters.ADDRESS_BASIC_COMMUNICATION_USER) {
      // save notification for history
      saveNotification(metaData, usersSet);
    }


  }

  /**
   * Saving the notification into history if users have been notified.
   * @param metaData
   * @param usersSet
   * @throws NotificationManagerException
   */
  private void saveNotification(NotificationMetaData metaData, Set<UserRecipient> usersSet)
      throws NotificationManagerException {
    if (!usersSet.isEmpty()) {
      getNotificationInterface().saveNotifUser(metaData, usersSet);
    }
  }

  private SentNotificationInterface getNotificationInterface()
      throws NotificationManagerException {
    SentNotificationInterface notificationInterface = null;
    try {
      notificationInterface = new SentNotificationInterfaceImpl();
    } catch (Exception e) {
      throw new NotificationManagerException(
          "NotificationSender.getNotificationInterface()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return notificationInterface;
  }

  private List<String> getUserIds(String lang, Map<String, String> usersLanguage) {
    List<String> userIds = new ArrayList<>(usersLanguage.keySet());
    Iterator<String> languages = usersLanguage.values().iterator();
    List<String> result = new ArrayList<>();
    String language;
    int u = 0;
    while (languages.hasNext()) {
      language = languages.next();
      if (lang.equalsIgnoreCase(language)) {
        result.add(userIds.get(u));
      }
      u++;
    }
    return result;
  }

  private NotificationParameters getNotificationParameters(int aMediaType,
      NotificationMetaData metaData) {
    NotificationParameters params = new NotificationParameters();

    params.iMessagePriority = metaData.getMessageType();
    params.dDate = metaData.getDate();
    params.sTitle = metaData.getTitle();
    params.sMessage = metaData.getContent();
    params.sSource = metaData.getSource();
    params.sURL = metaData.getLink();
    params.sSessionId = metaData.getSessionId();
    params.sOriginalExtraMessage = metaData.getOriginalExtraMessage();
    params.bSendImmediately = metaData.isSendImmediately();
    if (instanceId != -1) {
      params.iComponentInstance = instanceId;
    } else {
      params.iComponentInstance = extractLastNumber(metaData.getComponentId());
    }
    params.iMediaType = aMediaType;
    params.bAnswerAllowed = metaData.isAnswerAllowed();
    String sender = metaData.getSender();
    if (aMediaType == NotificationParameters.ADDRESS_BASIC_POPUP
        || aMediaType == NotificationParameters.ADDRESS_BASIC_COMMUNICATION_USER) {
      if (metaData.isAnswerAllowed() && StringUtil.isDefined(sender)) {
        params.iFromUserId = Integer.parseInt(metaData.getSender());
      }
    } else if (StringUtil.isInteger(sender)) {
      params.iFromUserId = Integer.parseInt(metaData.getSender());
    } else {
      params.iFromUserId = -1;
      params.senderName = sender;
    }
    params.eAction = metaData.getAction();
    return params;
  }

  /**
   * Extract the last number from the string
   * @param chaine The String to clean
   * @return the clean String Example 1 : kmelia47 -> 47 Example 2 : b2b34 -> 34
   */
  static int extractLastNumber(String chaine) {
    String s = "";

    if (chaine != null) {
      for (int i = 0; i < chaine.length(); i++) {
        char car = chaine.charAt(i);

        switch (car) {
          case '0':
          case '1':
          case '2':
          case '3':
          case '4':
          case '5':
          case '6':
          case '7':
          case '8':
          case '9':
            s = s + car;
            break;
          default:
            s = "";
        }
      }
    }
    if (s.length() > 0) {
      return Integer.parseInt(s);
    } else {
      return -1;
    }
  }

  // The next 4 static functions are for the use of NotificationUser component
  // as a popup window
  // -------------------------------------------------------------------------------------------
  static public String getIdsLineFromIdsArray(String[] asrc) {
    StringBuilder toIds = new StringBuilder("");

    if (asrc != null) {
      for (int i = 0; i < asrc.length; i++) {
        if (i > 0) {
          toIds.append('_');
        }
        toIds.append(asrc[i]);
      }
    }
    return toIds.toString();
  }

  static public String getIdsLineFromUserArray(UserDetail[] users) {
    StringBuilder toIds = new StringBuilder("");

    if (users != null) {
      for (int i = 0; i < users.length; i++) {
        if (i > 0) {
          toIds.append('_');
        }
        toIds.append(users[i].getId());
      }
    }
    return toIds.toString();
  }

  static public String getIdsLineFromGroupArray(Group[] groups) {
    StringBuilder toIds = new StringBuilder("");

    if (groups != null) {
      for (int i = 0; i < groups.length; i++) {
        if (i > 0) {
          toIds.append('_');
        }
        toIds.append(groups[i].getId());
      }
    }
    return toIds.toString();
  }

  static public String[] getIdsArrayFromIdsLine(String src) {
    if (src == null) {
      return new String[0];
    }
    StringTokenizer strTok = new StringTokenizer(src, "_");
    int nbElmt = strTok.countTokens();
    String[] valret = new String[nbElmt];

    for (int i = 0; i < nbElmt; i++) {
      valret[i] = strTok.nextToken();
    }
    return valret;
  }

}
