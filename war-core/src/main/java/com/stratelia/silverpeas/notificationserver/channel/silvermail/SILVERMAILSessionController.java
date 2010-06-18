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

package com.stratelia.silverpeas.notificationserver.channel.silvermail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.model.SendedNotificationDetail;
import com.stratelia.silverpeas.notificationManager.model.SendedNotificationInterface;
import com.stratelia.silverpeas.notificationManager.model.SendedNotificationInterfaceImpl;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

/**
 * Class declaration
 * @author
 * @version %I%, %G%
 */
public class SILVERMAILSessionController extends AbstractComponentSessionController {
  protected String currentFunction;
  protected long currentMessageId = -1;

  /**
   * Constructor declaration
   * @see
   */
  public SILVERMAILSessionController(MainSessionController mainSessionCtrl,
      ComponentContext context) {
    super(
        mainSessionCtrl,
        context,
        "com.stratelia.silverpeas.notificationserver.channel.silvermail.multilang.silvermail",
        "com.stratelia.silverpeas.notificationserver.channel.silvermail.settings.silvermailIcons");
    setComponentRootName(URLManager.CMP_SILVERMAIL);
  }

  protected String getComponentInstName() {
    return URLManager.CMP_SILVERMAIL;
  }

  /**
   * Method declaration
   * @param currentFunction
   * @see
   */
  public void setCurrentFunction(String currentFunction) {
    this.currentFunction = currentFunction;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getCurrentFunction() {
    return currentFunction;
  }

  /**
   * Method declaration
   * @param folderName
   * @return
   * @see
   */
  public Collection getFolderMessageList(String folderName)
      throws SILVERMAILException {
    return SILVERMAILPersistence.getMessageOfFolder(Integer
        .parseInt(getUserId()), folderName);
  }

  /**
   * Method declaration
   * @param userId
   * @return
   * @throws NotificationManagerException
   * @see
   */
  public List<SendedNotificationDetail> getUserMessageList()
      throws NotificationManagerException {
    String userId = getUserId();
    List<SendedNotificationDetail> notifByUser = new ArrayList<SendedNotificationDetail>();
    List<SendedNotificationDetail> sendedNotifByUser = new ArrayList<SendedNotificationDetail>();
    try {
      notifByUser = getNotificationInterface().getAllNotifByUser(userId);
      for (SendedNotificationDetail sendedNotif : notifByUser) {
        sendedNotif.setSource(getSource(sendedNotif.getComponentId()));
        sendedNotifByUser.add(sendedNotif);

      }
    } catch (NotificationManagerException e) {
      throw new NotificationManagerException(
          "NotificationSender.getUserMessageList()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return sendedNotifByUser;
  }

  /**
   * Method declaration
   * @param userId
   * @return
   * @throws NotificationManagerException
   * @see
   */
  public SendedNotificationDetail getSendedNotification(String notifId)
      throws NotificationManagerException {
    SendedNotificationDetail sendedNotification = null;
    try {
      sendedNotification = getNotificationInterface().getNotification(Integer.parseInt(notifId));
      sendedNotification.setSource(getSource(sendedNotification.getComponentId()));
    } catch (NotificationManagerException e) {
      throw new NotificationManagerException(
          "NotificationSender.getUserMessageList()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return sendedNotification;
  }

  private String getSource(String componentId) {
    ResourceLocator m_Multilang = new ResourceLocator(
        "com.stratelia.silverpeas.notificationserver.channel.silvermail.multilang.silvermail",
        getLanguage());
    String source = m_Multilang.getString("UserNotification");
    if (StringUtil.isDefined(componentId)) {
      OrganizationController orga = new OrganizationController();
      ComponentInst instance = orga.getComponentInst(componentId);
      
      // Sometimes, source could not be found
      SpaceInst space = orga.getSpaceInstById(instance.getDomainFatherId());
      if (space != null) {
        source = space.getName() + " - " + instance.getLabel();
      }
      else {
        source = m_Multilang.getString("UnknownSource");
      }
    }

    return source;
  }

  public void deleteSendedNotif(String notifId) throws NotificationManagerException {
    try {
      getNotificationInterface().deleteNotif(Integer.parseInt(notifId));
    } catch (NotificationManagerException e) {
      throw new NotificationManagerException(
          "NotificationSender.deleteSendedNotif()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public void deleteAllSendedNotif() throws NotificationManagerException {
    try {
      getNotificationInterface().deleteNotifByUser(getUserId());
    } catch (NotificationManagerException e) {
      throw new NotificationManagerException(
          "NotificationSender.deleteAllSendedNotif()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  private SendedNotificationInterface getNotificationInterface()
      throws NotificationManagerException {
    SendedNotificationInterface notificationInterface = null;
    try {
      notificationInterface = new SendedNotificationInterfaceImpl();
    } catch (Exception e) {
      throw new NotificationManagerException(
          "NotificationSender.getNotificationInterface()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return notificationInterface;
  }

  /**
   * Method declaration
   * @param messageId
   * @return
   * @see
   */
  public SILVERMAILMessage getMessage(long messageId)
      throws SILVERMAILException {
    return SILVERMAILPersistence.getMessage(messageId);
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public long getCurrentMessageId() {
    return currentMessageId;
  }

  /**
   * Method declaration
   * @param value
   * @see
   */
  public void setCurrentMessageId(long value) {
    currentMessageId = value;
  }

}
