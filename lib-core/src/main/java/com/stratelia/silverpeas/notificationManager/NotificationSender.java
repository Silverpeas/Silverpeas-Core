/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.stratelia.silverpeas.notificationManager;

import com.silverpeas.SilverpeasServiceProvider;
import com.silverpeas.util.CollectionUtil;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.stratelia.silverpeas.notificationManager.constant.NotifMediaType;
import com.stratelia.silverpeas.notificationManager.model.SentNotificationInterface;
import com.stratelia.silverpeas.notificationManager.model.SentNotificationInterfaceImpl;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import org.silverpeas.core.admin.OrganisationController;
import org.silverpeas.core.admin.OrganisationControllerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import static com.stratelia.silverpeas.notificationManager.NotificationTemplateKey
    .notification_receiver_groups;
import static com.stratelia.silverpeas.notificationManager.NotificationTemplateKey
    .notification_receiver_users;

/**
 * Cette classe est utilisee par les composants pour envoyer une notification a un (ou des)
 * utilisateur(s) (ou groupes) Elle package les appels et appelle la fonction du NotificationManager
 * pour reellement envoyer les notifications
 */
public class NotificationSender implements java.io.Serializable {

  private static final long serialVersionUID = 4165938893905145809L;
  private static ResourceLocator settings = new ResourceLocator(
      "com.stratelia.silverpeas.notificationManager.settings.notificationManagerSettings", "");

  protected NotificationManager notificationManager = null;
  protected int instanceId = -1;

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

    OrganisationController orgaController = OrganisationControllerFactory.
        getOrganisationController();

    // String[] allUsers;
    Set<UserRecipient> usersSet = new HashSet<UserRecipient>();
    Collection<UserRecipient> userRecipients = metaData.getUserRecipients();
    Collection<GroupRecipient> groupRecipients = metaData.getGroupRecipients();

    // Delete doublons between direct users and users included in groups
    SilverTrace.info("notificationManager", "NotificationSender.notifyUser()",
        "root.MSG_GEN_ENTER_METHOD");

    // First get direct users
    usersSet.addAll(userRecipients);

    // Then get users included in groups
    for (GroupRecipient group : groupRecipients) {
      usersSet.addAll(notificationManager.getUsersFromGroup(group.getGroupId()));
    }

    // Then exclude users that don't have to be notified
    usersSet.removeAll(metaData.getUserRecipientsToExclude());

    Set<String> languages = metaData.getLanguages();
    Map<String, String> usersLanguage = new HashMap<String, String>(usersSet.size());
    for (UserRecipient user : usersSet) {
      usersLanguage.put(user.getUserId(), SilverpeasServiceProvider.getPersonalizationService().
          getUserSettings(user.getUserId()).getLanguage());
    }

    NotificationParameters params = null;

    // All usersId to notify
    Set<String> allUserIds = usersLanguage.keySet();
    SilverTrace.info("notificationManager", "NotificationSender.notifyUser()",
        "root.MSG_GEN_PARAM_VALUE", "allUserIds = " + allUserIds);
    for (String language : languages) {
      SilverTrace.info("notificationManager",
          "NotificationSender.notifyUser()", "root.MSG_GEN_PARAM_VALUE",
          "language = " + language);
      params = getNotificationParameters(aMediaType, metaData);
      params.sTitle = metaData.getTitle(language);
      // ajout des destinataires dans le corps du message
      if (!metaData.isTemplateUsed()) {
        manageManualNotification(getUserSet(metaData, settings), getGroupSet(metaData, settings),
            languages, metaData, orgaController);
      } else {
        Map<String, SilverpeasTemplate> templates = metaData.getTemplates();
        if (templates != null && !templates.isEmpty()) {
          // pour chaque langue
          SilverpeasTemplate template = templates.get(language);
          if (template != null) {
            try {
              String receiver_users = addReceiverUsers(getUserSet(metaData, settings));
              if (StringUtil.isDefined(receiver_users)) {
                template.setAttribute(notification_receiver_users.toString(), receiver_users);
              }
              String receiver_groups = addReceiverGroups(getGroupSet(metaData, settings));
              if (StringUtil.isDefined(receiver_groups)) {
                template.setAttribute(notification_receiver_groups.toString(), receiver_groups);
              }
            } catch (NotificationManagerException e) {
              SilverTrace.warn("alertUserPeas",
                  "AlertUserPeasSessionController.prepareNotification()",
                  "root.EX_ADD_USERS_FAILED", e);
            }
          }
        }
      }
      params.sMessage = metaData.getContent(language);
      params.sLanguage = language;
      params.nNotificationResourceData = metaData.getNotificationResourceData(language);

      // Notify users with their native language
      List<String> userIds = getUserIds(language, usersLanguage);
      // remove users already notified in their language
      allUserIds.removeAll(userIds);
      SilverTrace.info("notificationManager",
          "NotificationSender.notifyUser()", "root.MSG_GEN_PARAM_VALUE",
          "allUserIds apres remove= " + allUserIds);
      notificationManager.notifyUsers(params, userIds.toArray(new String[userIds.size()]));
    }
    // Notify other users in language of the sender.
    notificationManager.notifyUsers(params, allUserIds.toArray(new String[allUserIds.size()]));

    if (CollectionUtil.isNotEmpty(metaData.getExternalRecipients())) {
      // We only use default language for external notification
      params.sLanguage = I18NHelper.defaultLanguage;
      params.sMessage = metaData.getContent(params.sLanguage);
      params.sTitle = metaData.getTitle(params.sLanguage);
      notificationManager.notifyExternals(params, metaData.getExternalRecipients());
    }

    if (metaData.isManual() &&
        aMediaType != NotificationParameters.ADDRESS_BASIC_COMMUNICATION_USER) {
      // save notification for history
      saveNotification(metaData, usersSet);
    }

    SilverTrace.info("notificationManager", "NotificationSender.notifyUser()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  private Set<UserRecipient> getUserSet(NotificationMetaData metaData, ResourceLocator settings)
      throws NotificationManagerException {
    HashSet<UserRecipient> usersSet = new HashSet<UserRecipient>();
    Collection<UserRecipient> userRecipients = metaData.getUserRecipients();
    Collection<GroupRecipient> groupRecipients = metaData.getGroupRecipients();
    usersSet.addAll(userRecipients);
    for (GroupRecipient group : groupRecipients) {
      if (!displayGroup(settings, group.getGroupId())) {
        usersSet.addAll(new NotificationManager(null).getUsersFromGroup(group.getGroupId()));
      }
    }

    // Then exclude users that don't have to be notified
    usersSet.removeAll(metaData.getUserRecipientsToExclude());

    return usersSet;
  }

  private boolean displayGroup(ResourceLocator settings, String groupId) {
    String threshold = settings.getString("notif.receiver.displayUser.threshold");
    OrganisationController orgaController = OrganisationControllerFactory.
        getOrganisationController();
    Group group = orgaController.getGroup(groupId);
    int nbUsers = group.getNbUsers();
    boolean res1 = settings.getBoolean("notif.receiver.displayGroup", false);
    boolean res2 = StringUtil.isDefined(threshold);
    boolean res3 = StringUtil.isInteger(threshold);
    boolean res4 = nbUsers > Integer.parseInt(threshold);
    boolean result = res1 || (res2 && res3 && res4);
    return result;
  }

  private Set<GroupRecipient> getGroupSet(NotificationMetaData metaData, ResourceLocator settings) {
    HashSet<GroupRecipient> groupsSet = new HashSet<GroupRecipient>();
    Collection<GroupRecipient> groupRecipients = metaData.getGroupRecipients();
    for (GroupRecipient group : groupRecipients) {
      if (displayGroup(settings, group.getGroupId())) {
        // add groups names
        groupsSet.add(group);
      }
    }
    return groupsSet;
  }

  private String addReceiverUsers(Set<UserRecipient> usersSet) {
    OrganisationController orgaController = OrganisationControllerFactory.
        getOrganisationController();
    StringBuilder users = new StringBuilder();
    if (settings.getBoolean("addReceiversInBody", false)) {
      boolean first = true;
      for (UserRecipient anUsersSet : usersSet) {
        if (!first) {
          users.append(", ");
        }
        users.append(orgaController.getUserDetail(anUsersSet.getUserId()).getDisplayedName());
        first = false;
      }
    }
    return users.toString();
  }

  private String addReceiverGroups(Set<GroupRecipient> groupsSet) {
    OrganisationController orgaController = OrganisationControllerFactory
        .getOrganisationController();
    StringBuilder groups = new StringBuilder();
    if (settings.getBoolean("addReceiversInBody", false)) {
      boolean first = true;
      for (GroupRecipient aGroupsSet : groupsSet) {
        if (!first) {
          groups.append(", ");
        }
        groups.append(orgaController.getGroup(aGroupsSet.getGroupId()).getName());
        first = false;
      }
    }
    return groups.toString();
  }

  /**
   * Add all recipients to the notification message.
   * @param usersSet set of the recipients.
   * @param languages set of recipients languages.
   * @param metaData the message metadata.
   * @param orgaController the controller.
   * @throws NotificationManagerException
   */
  protected void manageManualNotification(Set<UserRecipient> usersSet,
      Set<GroupRecipient> groupsSet,
      Set<String> languages, NotificationMetaData metaData,
      OrganisationController orgaController) throws NotificationManagerException {
    if (metaData.isManual()) {
      if (settings.getBoolean("addReceiversInBody", false)) {
        for (String language : languages) {
          String newContent = addReceiversInContent(usersSet, groupsSet, metaData.getContent(
              language), language, orgaController);
          metaData.setContent(newContent, language);
        }
      }
    }
  }

  private String addReceiversInContent(Set<UserRecipient> usersSet, Set<GroupRecipient> groupsSet,
      String content, String language, OrganisationController orgaController) {
    ResourceLocator m_Multilang = new ResourceLocator(
        "com.stratelia.silverpeas.notificationserver.channel.silvermail.multilang.silvermail",
        language);
    StringBuilder listReceivers = new StringBuilder(500);
    listReceivers.append(content);
    boolean first = true;
    for (UserRecipient anUsersSet : usersSet) {
      if (first) {
        listReceivers.append('\n');
        listReceivers.append(m_Multilang.getString("NameOfReceivers"));
      } else {
        listReceivers.append(", ");
      }
      listReceivers.append(orgaController.getUserDetail(anUsersSet.getUserId()).getDisplayedName());
      first = false;
    }
    first = true;
    for (GroupRecipient aGroupsSet : groupsSet) {
      if (first) {
        listReceivers.append('\n');
        listReceivers.append(m_Multilang.getString("NameOfGroupReceivers"));
      } else {
        listReceivers.append(", ");
      }
      listReceivers.append(orgaController.getGroup(aGroupsSet.getGroupId()).getName());
      first = false;
    }
    return listReceivers.toString();
  }

  private void saveNotification(NotificationMetaData metaData, Set<UserRecipient> usersSet)
      throws NotificationManagerException {
    getNotificationInterface().saveNotifUser(metaData, usersSet);
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
    List<String> userIds = new ArrayList<String>(usersLanguage.keySet());
    Iterator<String> languages = usersLanguage.values().iterator();
    List<String> result = new ArrayList<String>();
    String language;
    int u = 0;
    while (languages.hasNext()) {
      language = languages.next();
      SilverTrace.debug("notificationManager", "NotificationSender.getUserIds()",
          "root.MSG_GEN_PARAM_VALUE", "language = " + language);
      if (lang.equalsIgnoreCase(language)) {
        result.add(userIds.get(u));
      }
      u++;
    }
    SilverTrace.info("notificationManager", "NotificationSender.getUserIds()",
        "root.MSG_GEN_EXIT_METHOD", result.size() + " users for language '"
            + lang + "' ");
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
      SilverTrace.info("notificationManager",
          "NotificationSender.getNotificationParameters()",
          "root.MSG_GEN_PARAM_VALUE", metaData.getSender());
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
