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

package org.silverpeas.web.communicationuser.control;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.silverpeas.core.security.session.SessionManagement;
import org.silverpeas.core.security.session.SessionManagementProvider;
import org.apache.commons.io.FileUtils;

import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.web.communicationuser.CommunicationUserException;
import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.NotificationParameters;
import org.silverpeas.core.notification.user.client.NotificationSender;
import org.silverpeas.core.notification.user.client.UserRecipient;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.exception.UtilException;
import org.silverpeas.core.util.file.FileFolderManager;

/**
 * @author dlesimple
 * @version
 */
public class CommunicationUserSessionController extends AbstractComponentSessionController {

  private String m_PathDiscussions = null;
  private Collection<File> m_listCurrentDiscussion = new ArrayList<File>();

  /**
   * Constructor declaration
   * @param mainSessionCtrl
   * @param componentContext
   * @see
   */
  public CommunicationUserSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "org.silverpeas.communicationUser.multilang.communicationUserBundle",
        "org.silverpeas.communicationUser.settings.communicationUserIcons",
        "org.silverpeas.communicationUser.settings.communicationUserSettings");
    setComponentRootName(URLUtil.CMP_COMMUNICATIONUSER);
  }

  /**
   * Get nb of connected users
   * @author dlesimple
   * @return
   */
  public int getNbConnectedUsersList() {
    SessionManagement sessionManagement = SessionManagementProvider.getSessionManagement();
    return sessionManagement.getNbConnectedUsersList(getUserDetail());
  }

  /**
   * Get connected users
   * @author dlesimple
   * @return Collection of connected Users
   */
  public Collection<SessionInfo> getDistinctConnectedUsersList() {
    SessionManagement sessionManagement = SessionManagementProvider.getSessionManagement();
    return sessionManagement.getDistinctConnectedUsersList(getUserDetail());
  }

  /**
   * Get UserDetail
   * @param userId
   * @return User
   */
  public UserDetail getTargetUserDetail(String userId) {
    return getUserDetail(userId);
  }

  /**
   * Send message to user
   * @param userId
   * @param message
   */
  public void notifySession(String userId, String message) {
    try {
      NotificationSender notificationSender = new NotificationSender(null);
      NotificationMetaData notifMetaData = new NotificationMetaData();
      notifMetaData.setTitle("");
      notifMetaData.setContent(message);
      notifMetaData.setSource(getUserDetail().getDisplayedName());
      notifMetaData.setSender(getUserId());
      notifMetaData.setAnswerAllowed(true);
      notifMetaData.addUserRecipient(new UserRecipient(userId));
      notificationSender.notifyUser(NotificationParameters.ADDRESS_BASIC_COMMUNICATION_USER,
          notifMetaData);
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error("Error on communication notify", ex);
    }
  }

  /**
   * @param discussion
   */
  public void addCurrentDiscussion(File discussion) {
    Iterator<File> it = this.m_listCurrentDiscussion.iterator();
    File disc;
    boolean trouve = false;
    while (it.hasNext()) {
      disc = it.next();
      if (disc.getName().equals(discussion.getName())) {
        trouve = true;
        break;
      }
    }
    if (!trouve) {
      this.m_listCurrentDiscussion.add(discussion);
    }
  }

  /**
   * @return
   */
  public Collection<File> getListCurrentDiscussion() {
    return this.m_listCurrentDiscussion;
  }

  /**
   * @return
   * @throws CommunicationUserException
   */
  private String getPathDiscussions() throws CommunicationUserException {
    if (m_PathDiscussions == null) {
      m_PathDiscussions = this.getSettings().getString("pathDiscussions");
      if (m_PathDiscussions != null) {
        try {
          FileFolderManager.createFolder(m_PathDiscussions);
        } catch (UtilException e) {
          throw new CommunicationUserException(
              "CommunicationUserSessionController.getPathDiscussions()",
              SilverpeasException.ERROR, "root.EX_NO_MESSAGE", e);
        }
      }
    }
    return m_PathDiscussions;
  }

  public File getExistingFileDiscussion(String userId) throws CommunicationUserException {
    String currentUserId = this.getUserId();
    return getExistingFileDiscussion(userId, currentUserId);
  }

  private File getExistingFileDiscussion(String userId, String currentUserId)
      throws CommunicationUserException {
    // serveur de fichiers : récupérer la discussion entre userId et
    // currentUserId
    if (getPathDiscussions() != null) {
      try {
        Collection<File> listFile = FileFolderManager.getAllFile(getPathDiscussions());
        if (listFile != null && listFile.size() > 0) {
          Iterator<File> it = listFile.iterator();
          while (it.hasNext()) {
            File file = it.next();
            String fileName = file.getName(); // userId1.userId2.txt
            String userId1 = fileName.substring(0, fileName.indexOf('.'));
            String userId2 =
                fileName.substring(fileName.indexOf('.') + 1, fileName.lastIndexOf('.'));
            if ((userId.equals(userId1) && currentUserId.equals(userId2))
                || (userId.equals(userId2) && currentUserId.equals(userId1))) {
              return file;
            }
          }
        }
      } catch (UtilException e) {
        throw new CommunicationUserException(
            "CommunicationUserSessionController.getExistingFileDiscussion()",
            SilverpeasException.ERROR, "root.EX_NO_MESSAGE", e);
      }
    }
    return null;
  }

  public File createDiscussion(String userId) throws CommunicationUserException {
    String currentUserId = this.getUserId();
    // serveur de fichiers : crée le fichier userId1.userId2.txt
    if (getPathDiscussions() != null) {
      try {
        File directory = new File(getPathDiscussions());
        if (directory.isDirectory()) {
          /* Création d'un nouveau fichier sous la bonne arborescence */
          File discussion = new File(directory, currentUserId + '.' + userId + ".txt");
          /* Ecriture dans le fichier */
          FileUtils.writeStringToFile(discussion, "\n", "UTF-8");
          return discussion;
        } else {
          SilverLogger.getLogger(this).error("Creation discussion error", getPathDiscussions());
          throw new CommunicationUserException(
              "CommunicationUserSessionController.createDiscussion",
              SilverpeasException.ERROR, "util.EX_CREATE_FILE_ERROR");
        }
      } catch (IOException e) {
        throw new CommunicationUserException(
            "CommunicationUserSessionController.createDiscussion()",
            SilverpeasException.ERROR, "root.EX_NO_MESSAGE", e);
      }
    }
    return null;
  }

  public String getDiscussion(File fileDiscussion) throws CommunicationUserException {
    try {
      List<String> lines = FileUtils.readLines(fileDiscussion, "UTF-8");
      StringBuilder messages = new StringBuilder("");
      for (String line : lines) {
        messages.append(line).append("\n");
      }
      return messages.toString();
    } catch (IOException e) {
      throw new CommunicationUserException(
          "CommunicationUserSessionController.getDiscussion()",
          SilverpeasException.ERROR, "root.EX_NO_MESSAGE", e);
    }
  }

  public void addMessageDiscussion(File fileDiscussion, String message)
      throws CommunicationUserException {
    if (getPathDiscussions() != null) {
      try {
        String messages = getDiscussion(fileDiscussion);
        messages += message + "\n";
        FileFolderManager.createFile(getPathDiscussions(), fileDiscussion.getName(), messages);
      } catch (UtilException e1) {
        throw new CommunicationUserException(
            "CommunicationUserSessionController.addMessageDiscussion()",
            SilverpeasException.ERROR, "root.EX_NO_MESSAGE", e1);
      }
    }
  }

  public void clearDiscussion(File fileDiscussion)
      throws CommunicationUserException {
    if (getPathDiscussions() != null) {
      // écrase le contenu du fichier avec ce nouveau contenu vide
      try {
        FileFolderManager.createFile(getPathDiscussions(), fileDiscussion.getName(), " ");
      } catch (UtilException e) {
        throw new CommunicationUserException(
            "CommunicationUserSessionController.clearDiscussion()",
            SilverpeasException.ERROR, "root.EX_NO_MESSAGE", e);
      }
    }
  }
}
