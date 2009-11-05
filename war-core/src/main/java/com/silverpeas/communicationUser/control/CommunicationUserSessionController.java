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

/*
 * CommunicationUserSessionControl.java
 *
 */

package com.silverpeas.communicationUser.control;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.silverpeas.communicationUser.CommunicationUserException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.peasCore.*;

import com.stratelia.silverpeas.selection.*;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;

/**
 * 
 * @author dlesimple
 * @version
 */
public class CommunicationUserSessionController extends
    AbstractComponentSessionController {
  Selection sel = null;

  // CBO : ADD
  private String m_PathDiscussions = null;
  private Collection m_listCurrentDiscussion = new ArrayList();

  // CBO : FIN ADD

  /**
   * Constructor declaration
   * 
   * 
   * @param mainSessionCtrl
   * @param componentContext
   * 
   * @see
   */
  public CommunicationUserSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    // CBO : UPDATE
    // super(mainSessionCtrl,
    // componentContext,"com.silverpeas.communicationUser.multilang.communicationUserBundle");
    super(mainSessionCtrl, componentContext,
        "com.silverpeas.communicationUser.multilang.communicationUserBundle",
        "com.silverpeas.communicationUser.settings.communicationUserIcons",
        "com.silverpeas.communicationUser.settings.communicationUserSettings");

    setComponentRootName(URLManager.CMP_COMMUNICATIONUSER);
    sel = getSelection();
  }

  /**
   * Get nb of connected users
   * 
   * @author dlesimple
   * @return
   */
  public int getNbConnectedUsersList() {
    return SessionManager.getInstance().getNbConnectedUsersList();
  }

  /**
   * Get connected users
   * 
   * @author dlesimple
   * @return Collection of connected Users
   */
  public Collection getDistinctConnectedUsersList() {
    return SessionManager.getInstance().getDistinctConnectedUsersList();
  }

  /**
   * Get UserDetail
   * 
   * @param userId
   * @return User
   */
  public UserDetail getTargetUserDetail(String userId) {
    return getUserDetail(userId);
  }

  /**
   * Send message to user
   * 
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

      // CBO : ADD
      notifMetaData.setSender(getUserId());
      notifMetaData.setAnswerAllowed(true);
      // CBO : FIN ADD

      notifMetaData.addUserRecipient(userId);

      // CBO : UPDATE
      // notificationSender.notifyUser(NotificationParameters.ADDRESS_BASIC_POPUP,
      // notifMetaData);
      notificationSender.notifyUser(
          NotificationParameters.ADDRESS_BASIC_COMMUNICATION_USER,
          notifMetaData);
    } catch (Exception ex) {
      SilverTrace.error("communicationUser",
          "CommunicationUserSessionController.NotifySession",
          "root.EX_CANT_SEND_MESSAGE", ex);
    }
  }

  // CBO : ADD

  /**
   * @param discussion
   */
  public void addCurrentDiscussion(File discussion) {
    Iterator it = this.m_listCurrentDiscussion.iterator();
    File disc;
    boolean trouve = false;
    while (it.hasNext()) {
      disc = (File) it.next();
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
   * @param discussion
   */
  public Collection getListCurrentDiscussion() {
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

  public File getExistingFileDiscussion(String userId)
      throws CommunicationUserException {
    String currentUserId = this.getUserId();

    // serveur de fichiers : récupérer la discussion en cours entre ces 2
    // utilisateurs
    return getExistingFileDiscussion(userId, currentUserId);
  }

  private File getExistingFileDiscussion(String userId, String currentUserId)
      throws CommunicationUserException {
    // serveur de fichiers : récupérer la discussion entre userId et
    // currentUserId
    if (getPathDiscussions() != null) {
      try {
        Collection listFile = FileFolderManager
            .getAllFile(getPathDiscussions());
        if (listFile != null && listFile.size() > 0) {
          Iterator it = listFile.iterator();
          File file;
          String fileName;
          String userId1;
          String userId2;
          while (it.hasNext()) {
            file = (File) it.next();
            fileName = file.getName(); // userId1.userId2.txt
            userId1 = fileName.substring(0, fileName.indexOf("."));
            userId2 = fileName.substring(fileName.indexOf(".") + 1, fileName
                .lastIndexOf("."));
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
          File discussion = new File(directory, currentUserId + "." + userId
              + ".txt");

          /* Ecriture dans le fichier */
          FileWriter file_write = new FileWriter(discussion);
          BufferedWriter flux_out = new BufferedWriter(file_write);
          flux_out.write("\n");
          flux_out.close();
          file_write.close();
          return discussion;
        } else {
          SilverTrace.error("communicationUser",
              "CommunicationUserSessionController.createDiscussion",
              "util.EX_CREATE_FILE_ERROR", getPathDiscussions());
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

  public String getDiscussion(File fileDiscussion)
      throws CommunicationUserException {
    // serveur de fichiers : récupérer la discussion en cours entre ces 2
    // utilisateurs
    try {
      /* lecture du contenu du fichier */
      FileReader file_read = new FileReader(fileDiscussion);
      BufferedReader flux_in = new BufferedReader(file_read);

      String ligne;
      String messages = "";
      while ((ligne = flux_in.readLine()) != null) {
        messages += ligne + "\n";
      }
      flux_in.close();
      file_read.close();

      return messages;
    } catch (IOException e) {
      throw new CommunicationUserException(
          "CommunicationUserSessionController.getDiscussion()",
          SilverpeasException.ERROR, "root.EX_NO_MESSAGE", e);
    }
  }

  public void addMessageDiscussion(File fileDiscussion, String message)
      throws CommunicationUserException {
    // serveur de fichiers : récupérer la discussion en cours entre ces 2
    // utilisateurs
    if (getPathDiscussions() != null) {
      try {
        // lecture du contenu du fichier
        String messages = getDiscussion(fileDiscussion);

        messages += message + "\n";

        // écrase le contenu du fichier avec ce nouveau contenu
        FileFolderManager.createFile(getPathDiscussions(), fileDiscussion
            .getName(), messages);

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
        FileFolderManager.createFile(getPathDiscussions(), fileDiscussion
            .getName(), " ");
      } catch (UtilException e) {
        throw new CommunicationUserException(
            "CommunicationUserSessionController.clearDiscussion()",
            SilverpeasException.ERROR, "root.EX_NO_MESSAGE", e);
      }
    }
  }
  // CBO : FIN ADD
}
