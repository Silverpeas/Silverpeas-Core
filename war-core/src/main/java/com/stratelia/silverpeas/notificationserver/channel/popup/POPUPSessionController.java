/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.notificationserver.channel.popup;

import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * Class declaration
 * 
 * 
 * @author
 * @version %I%, %G%
 */
public class POPUPSessionController extends AbstractComponentSessionController {
  protected String currentFunction;
  protected long currentMessageId = -1;

  /**
   * Constructor declaration
   * 
   * 
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
   * 
   * 
   * @param currentFunction
   * 
   * @see
   */
  public void setCurrentFunction(String currentFunction) {
    this.currentFunction = currentFunction;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getCurrentFunction() {
    return currentFunction;
  }

  /**
   * Method declaration
   * 
   * 
   * @param messageId
   * 
   * @return
   * 
   * @see
   */
  public POPUPMessage getMessage(long messageId) throws POPUPException {
    return POPUPPersistence.getMessage(messageId);
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public long getCurrentMessageId() {
    return currentMessageId;
  }

  /**
   * Method declaration
   * 
   * 
   * @param value
   * 
   * @see
   */
  public void setCurrentMessageId(long value) {
    currentMessageId = value;
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
      notifMetaData.setSender(getUserId());
      notifMetaData.setAnswerAllowed(true);
      notifMetaData.addUserRecipient(userId);
      notificationSender.notifyUser(NotificationParameters.ADDRESS_BASIC_POPUP,
          notifMetaData);
    } catch (Exception ex) {
      SilverTrace.error("communicationUser",
          "CommunicationUserSessionController.NotifySession",
          "root.EX_CANT_SEND_MESSAGE", ex);
    }
  }

}
