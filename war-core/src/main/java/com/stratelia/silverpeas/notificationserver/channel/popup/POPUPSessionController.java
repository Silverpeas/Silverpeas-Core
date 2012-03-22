/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.silverpeas.notificationserver.channel.popup;

import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.notificationManager.UserRecipient;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * Class declaration
 * @author
 * @version %I%, %G%
 */
public class POPUPSessionController extends AbstractComponentSessionController {
  protected String currentFunction;
  protected long currentMessageId = -1;

  /**
   * Constructor declaration
   * @see
   */
  public POPUPSessionController(MainSessionController mainSessionCtrl,
      ComponentContext context) {
    super(mainSessionCtrl, context,
        "com.silverpeas.notificationserver.channel.popup.multilang.popup",
        "com.silverpeas.notificationserver.channel.popup.settings.popupIcons");
    setComponentRootName(URLManager.CMP_POPUP);
  }

  protected String getComponentInstName() {
    return URLManager.CMP_POPUP;
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
   * @param messageId
   * @return
   * @see
   */
  public POPUPMessage getMessage(long messageId) throws POPUPException {
    return POPUPPersistence.getMessage(messageId);
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
      notificationSender.notifyUser(NotificationParameters.ADDRESS_BASIC_POPUP,
          notifMetaData);
    } catch (Exception ex) {
      SilverTrace.error("communicationUser",
          "CommunicationUserSessionController.NotifySession",
          "root.EX_CANT_SEND_MESSAGE", ex);
    }
  }

}
