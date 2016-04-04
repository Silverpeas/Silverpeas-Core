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

/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

package org.silverpeas.core.notification.user.client;

import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.Date;

/**
 * This class contents all needed parameters to send a notification with the NotificationManager
 * @author Thierry Leroi
 * @version %I%, %G%
 */
public class NotificationParameters {
  static public final int NORMAL = 0;
  static public final int URGENT = 1;
  static public final int ERROR = 2;

  static public final int ADDRESS_DEFAULT = -1;
  static public final int ADDRESS_COMPONENT_DEFINED = -2; // Send by media :
  // 1)Component specific
  // 2)Default
  // 3)ADDRESS_BASIC_SILVERMAIL
  /**
   * The channel used for notifications is the popup.
   */
  static public final int ADDRESS_BASIC_POPUP = -10;
  /**
   * The channel used for notifications is the trash (recieved notifications are removed).
   */
  static public final int ADDRESS_BASIC_REMOVE = -11;
  /**
   * The channel used for notifications is the internal Silverpeas messaging system.
   */
  static public final int ADDRESS_BASIC_SILVERMAIL = -12;
  /**
   * The channel used for notifications is the SMTP mail system.
   */
  static public final int ADDRESS_BASIC_SMTP_MAIL = -13;
  /**
   * The channel used for notifications is the server one (used by the server to send
   * notifications).
   */
  static public final int ADDRESS_BASIC_SERVER = -14;
  /**
   * The channel used for notifications is the peer to peer user communication (chatting).
   */
  static public final int ADDRESS_BASIC_COMMUNICATION_USER = -15;

  static public final String USAGE_PRO = "addressUsePro";
  static public final String USAGE_PERSO = "addressUsePerso";
  static public final String USAGE_REP = "addressUseRep";
  static public final String USAGE_URGENT = "addressUseUrgent";

  static public final int MAX_SIZE_TITLE = 1023; // Maximum size of the title in
  // tables SILVERMAIL and POPUP

  public int iMessagePriority = NORMAL;
  public int iMediaType = ADDRESS_COMPONENT_DEFINED;
  public int iComponentInstance = -1;
  public int iFromUserId = -1;

  public String sTitle = "";
  public String senderName = "";
  public String sMessage = "";
  public String sURL = "";
  public String sLinkLabel = "";
  public String sSource = "";
  public String sSessionId = "";
  public String sOriginalExtraMessage = null;
  public boolean bAnswerAllowed = false;
  public boolean bSendImmediately = false;

  public Date dDate = new Date();
  public String sLanguage = null;

  public NotifAction eAction = null;
  public NotificationResourceData nNotificationResourceData = null;

  public void traceObject() {
    StringBuilder trace = new StringBuilder("Notification Parameters Dump: {");
    switch (iMessagePriority) {
      case NORMAL:
        trace.append("MessagePriority: NORMAL, ");
        break;
      case URGENT:
        trace.append("MessagePriority: URGENT, ");
        break;
      case ERROR:
        trace.append("MessagePriority: ERROR, ");
        break;
    }
    switch (iMediaType) {
      case ADDRESS_DEFAULT:
        trace.append("MediaType: ADDRESS_DEFAULT, ");
        break;
      case ADDRESS_COMPONENT_DEFINED:
        trace.append("MediaType: ADDRESS_COMPONENT_DEFINED, ");
        break;
      case ADDRESS_BASIC_POPUP:
        trace.append("MediaType: ADDRESS_BASIC_POPUP, ");
        break;
      case ADDRESS_BASIC_REMOVE:
        trace.append("MediaType: ADDRESS_BASIC_REMOVE, ");
        break;
      case ADDRESS_BASIC_SILVERMAIL:
        trace.append("MediaType: ADDRESS_BASIC_SILVERMAIL, ");
        break;
      case ADDRESS_BASIC_SMTP_MAIL:
        trace.append("MediaType: ADDRESS_BASIC_SMTP_MAIL, ");
        break;
      case ADDRESS_BASIC_SERVER:
        trace.append("MediaType: ADDRESS_BASIC_SERVER, ");
        break;
      case ADDRESS_BASIC_COMMUNICATION_USER:
        trace.append("MediaType: ADDRESS_BASIC_COMMUNICATION_USER, ");
        break;
      default:
        trace.append("MediaType: ").append(Integer.toString(iMediaType)).append(", ");
        break;
    }
    trace.append("ComponentInstance: " + Integer.toString(iComponentInstance));
    trace.append(", Title: " + sTitle);
    trace.append(", Message: " + sMessage);
    trace.append(", FromUserId: " + Integer.toString(iFromUserId));
    trace.append(", FromSenderName: " + senderName);
    trace.append(", AnswerAllowed: " + bAnswerAllowed);
    trace.append(", SendImmediately: " + bSendImmediately);
    trace.append(", Source: " + sSource);
    trace.append(", SessionId: " + sSessionId);
    trace.append(", Date: " + dDate.toString());
    trace.append(", Action: " + (eAction != null ? eAction.name() : "N/A"));
    trace.append("}");
    SilverLogger.getLogger(this).info(trace.toString());
  }
}