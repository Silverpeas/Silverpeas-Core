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

package com.stratelia.silverpeas.notificationManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;

/**
 * Cette classe est utilisee par les composants pour envoyer une notification a un (ou des)
 * utilisateur(s) (ou groupes) Elle package les appels et appelle la fonction du NotificationManager
 * pour reellement envoyer les notifications
 * @author Thierry Leroi
 * @version %I%, %G%
 */
public class NotificationSender implements java.io.Serializable {
  protected NotificationManager m_Manager = null;

  protected int m_instanceId = -1;

  /**
   * Constructor for a standard component
   * @param instanceId the instance Id of the calling's component
   */
  public NotificationSender(String instanceId) {
    m_instanceId = extractLastNumber(instanceId);
    m_Manager = new NotificationManager(null);
  }

  /**
   * Method declaration
   * @param metaData
   * @throws NotificationManagerException
   * @see
   */
  public void notifyUser(NotificationMetaData metaData)
      throws NotificationManagerException {
    notifyUser(NotificationParameters.ADDRESS_COMPONENT_DEFINED, metaData);
  }

  /**
   * Method declaration
   * @param aMediaType
   * @param Metadata
   * @throws NotificationManagerException
   * @see
   */

  public void notifyUser(int aMediaType, NotificationMetaData metaData)
      throws NotificationManagerException {

    // String[] allUsers;
    HashSet usersSet = new HashSet();
    Collection userRecipients = metaData.getUserRecipients();
    Collection groupRecipients = metaData.getGroupRecipients();

    // Delete doublons between direct users and users included in groups
    SilverTrace.info("notificationManager", "NotificationSender.notifyUser()",
        "root.MSG_GEN_ENTER_METHOD");

    // First get direct users

    usersSet.addAll(userRecipients);

    // Then get users included in groups
    Iterator iter = groupRecipients.iterator();
    while (iter.hasNext()) {
      usersSet.addAll(Arrays.asList(m_Manager.getUsersFromGroup((String) iter
          .next())));
    }

    OrganizationController orgaController = new OrganizationController();
    Hashtable usersLanguage = orgaController.getUsersLanguage(new ArrayList(
        usersSet));

    Set languages = metaData.getLanguages();
    Iterator iLanguages = languages.iterator();
    String language = null;
    NotificationParameters params = null;
    List userIds = null;

    // All usersId to notify
    Set allUserIds = usersLanguage.keySet();
    SilverTrace.info("notificationManager", "NotificationSender.notifyUser()",
        "root.MSG_GEN_PARAM_VALUE", "allUserIds = " + allUserIds);
    while (iLanguages.hasNext()) {
      language = (String) iLanguages.next();

      SilverTrace.info("notificationManager",
          "NotificationSender.notifyUser()", "root.MSG_GEN_PARAM_VALUE",
          "language = " + language);
      params = getNotificationParameters(aMediaType, metaData);

      params.sTitle = metaData.getTitle(language);
      params.sMessage = metaData.getContent(language);
      params.sLanguage = language;

      // Notify users with their native language
      userIds = getUserIds(language, usersLanguage);
      // remove users already notified in their language
      allUserIds.removeAll(userIds);
      SilverTrace.info("notificationManager",
          "NotificationSender.notifyUser()", "root.MSG_GEN_PARAM_VALUE",
          "allUserIds apres remove= " + allUserIds);
      m_Manager.notifyUsers(params, (String[]) userIds.toArray(new String[0]));
    }

    // Notify other user in language of the sender.
    m_Manager.notifyUsers(params, (String[]) allUserIds.toArray(new String[0]));

    SilverTrace.info("notificationManager", "NotificationSender.notifyUser()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  private List getUserIds(String lang, Hashtable usersLanguage) {
    List userIds = new ArrayList(usersLanguage.keySet());
    Iterator languages = usersLanguage.values().iterator();
    List result = new ArrayList();
    String language = null;
    int u = 0;
    while (languages.hasNext()) {
      language = (String) languages.next();
      SilverTrace.debug("notificationManager",
          "NotificationSender.getUserIds()", "root.MSG_GEN_PARAM_VALUE",
          "language = " + language);
      if (lang.equalsIgnoreCase(language))
        result.add((String) userIds.get(u));
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
    if (m_instanceId != -1)
      params.iComponentInstance = m_instanceId;
    else
      params.iComponentInstance = extractLastNumber(metaData.getComponentId());
    params.iMediaType = aMediaType;
    params.connection = metaData.getConnection();
    params.bAnswerAllowed = metaData.isAnswerAllowed();
    String sender = metaData.getSender();
    if (aMediaType == NotificationParameters.ADDRESS_BASIC_POPUP
        || aMediaType == NotificationParameters.ADDRESS_BASIC_COMMUNICATION_USER) {
      if (metaData.isAnswerAllowed() && StringUtil.isDefined(sender))
        params.iFromUserId = Integer.parseInt(metaData.getSender());
    } else if (StringUtil.isInteger(sender)) {
      SilverTrace.info("notificationManager",
          "NotificationSender.getNotificationParameters()",
          "root.MSG_GEN_PARAM_VALUE", metaData.getSender());
      params.iFromUserId = Integer.parseInt(metaData.getSender());
    } else {
      params.iFromUserId = -1;
      params.senderName = sender;
    }
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
    if (s.length() > 0)
      return Integer.parseInt(s);
    else
      return -1;
  }

  // The next 4 static functions are for the use of NotificationUser component
  // as a popup window
  // -------------------------------------------------------------------------------------------

  static public String getIdsLineFromIdsArray(String[] asrc) {
    StringBuffer toIds = new StringBuffer("");

    if (asrc != null) {
      for (int i = 0; i < asrc.length; i++) {
        if (i > 0) {
          toIds.append("_");
        }
        toIds.append(asrc[i]);
      }
    }
    return toIds.toString();
  }

  static public String getIdsLineFromUserArray(UserDetail[] users) {
    StringBuffer toIds = new StringBuffer("");

    if (users != null) {
      for (int i = 0; i < users.length; i++) {
        if (i > 0) {
          toIds.append("_");
        }
        toIds.append(users[i].getId());
      }
    }
    return toIds.toString();
  }

  static public String getIdsLineFromGroupArray(Group[] groups) {
    StringBuffer toIds = new StringBuffer("");

    if (groups != null) {
      for (int i = 0; i < groups.length; i++) {
        if (i > 0) {
          toIds.append("_");
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
