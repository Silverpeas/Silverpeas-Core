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

import java.sql.Connection;
import java.util.Date;

import com.stratelia.silverpeas.silvertrace.SilverTrace;

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
  // 1)Component
  // specific 2)Default
  // 3)ADDRESS_BASIC_SILVERMAIL
  static public final int ADDRESS_BASIC_POPUP = -10;
  static public final int ADDRESS_BASIC_REMOVE = -11;
  static public final int ADDRESS_BASIC_SILVERMAIL = -12;
  static public final int ADDRESS_BASIC_SMTP_MAIL = -13;
  static public final int ADDRESS_BASIC_SERVER = -14;
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
  public String sSource = "";
  public String sSessionId = "";
  public boolean bAnswerAllowed = false;

  public Date dDate = new Date();
  public String sLanguage = null;

  public Connection connection = null;

  public void traceObject() {
    if (SilverTrace.getTraceLevel("notificationManager", true) <= SilverTrace.TRACE_LEVEL_INFO) {
      switch (iMessagePriority) {
        case NORMAL:
          SilverTrace.info("notificationManager",
              "NotificationParameters.traceObject",
              "notificationManager.MSG_INFO_DUMPNOTIFICATION",
              "MessagePriority : NORMAL");
          break;
        case URGENT:
          SilverTrace.info("notificationManager",
              "NotificationParameters.traceObject",
              "notificationManager.MSG_INFO_DUMPNOTIFICATION",
              "MessagePriority : URGENT");
          break;
        case ERROR:
          SilverTrace.info("notificationManager",
              "NotificationParameters.traceObject",
              "notificationManager.MSG_INFO_DUMPNOTIFICATION",
              "MessagePriority : ERROR");
          break;
      }
      switch (iMediaType) {
        case ADDRESS_DEFAULT:
          SilverTrace.info("notificationManager",
              "NotificationParameters.traceObject",
              "notificationManager.MSG_INFO_DUMPNOTIFICATION",
              "MediaType : ADDRESS_DEFAULT");
          break;
        case ADDRESS_COMPONENT_DEFINED:
          SilverTrace.info("notificationManager",
              "NotificationParameters.traceObject",
              "notificationManager.MSG_INFO_DUMPNOTIFICATION",
              "MediaType : ADDRESS_COMPONENT_DEFINED");
          break;
        case ADDRESS_BASIC_POPUP:
          SilverTrace.info("notificationManager",
              "NotificationParameters.traceObject",
              "notificationManager.MSG_INFO_DUMPNOTIFICATION",
              "MediaType : ADDRESS_BASIC_POPUP");
          break;
        case ADDRESS_BASIC_REMOVE:
          SilverTrace.info("notificationManager",
              "NotificationParameters.traceObject",
              "notificationManager.MSG_INFO_DUMPNOTIFICATION",
              "MediaType : ADDRESS_BASIC_REMOVE");
          break;
        case ADDRESS_BASIC_SILVERMAIL:
          SilverTrace.info("notificationManager",
              "NotificationParameters.traceObject",
              "notificationManager.MSG_INFO_DUMPNOTIFICATION",
              "MediaType : ADDRESS_BASIC_SILVERMAIL");
          break;
        case ADDRESS_BASIC_SMTP_MAIL:
          SilverTrace.info("notificationManager",
              "NotificationParameters.traceObject",
              "notificationManager.MSG_INFO_DUMPNOTIFICATION",
              "MediaType : ADDRESS_BASIC_SMTP_MAIL");
          break;
        case ADDRESS_BASIC_SERVER:
          SilverTrace.info("notificationManager",
              "NotificationParameters.traceObject",
              "notificationManager.MSG_INFO_DUMPNOTIFICATION",
              "MediaType : ADDRESS_BASIC_SERVER");
          break;
        case ADDRESS_BASIC_COMMUNICATION_USER:
          SilverTrace.info("notificationManager",
              "NotificationParameters.traceObject",
              "notificationManager.MSG_INFO_DUMPNOTIFICATION",
              "MediaType : ADDRESS_BASIC_COMMUNICATION_USER");
          break;
        default:
          SilverTrace.info("notificationManager",
              "NotificationParameters.traceObject",
              "notificationManager.MSG_INFO_DUMPNOTIFICATION", "MediaType : "
              + Integer.toString(iMediaType));
          break;
      }
      SilverTrace.info("notificationManager",
          "NotificationParameters.traceObject",
          "notificationManager.MSG_INFO_DUMPNOTIFICATION",
          "ComponentInstance : " + Integer.toString(iComponentInstance));
      SilverTrace.info("notificationManager",
          "NotificationParameters.traceObject",
          "notificationManager.MSG_INFO_DUMPNOTIFICATION", "Title : " + sTitle);
      SilverTrace.info("notificationManager",
          "NotificationParameters.traceObject",
          "notificationManager.MSG_INFO_DUMPNOTIFICATION", "Message : "
          + sMessage);
      SilverTrace.info("notificationManager",
          "NotificationParameters.traceObject",
          "notificationManager.MSG_INFO_DUMPNOTIFICATION", "FromUserId : "
          + Integer.toString(iFromUserId));
      SilverTrace.info("notificationManager",
          "NotificationParameters.traceObject",
          "notificationManager.MSG_INFO_DUMPNOTIFICATION", "FromSenderName : "
          + senderName);
      SilverTrace.info("notificationManager",
          "NotificationParameters.traceObject",
          "notificationManager.MSG_INFO_DUMPNOTIFICATION", "AnswerAllowed : "
          + bAnswerAllowed);
      SilverTrace.info("notificationManager",
          "NotificationParameters.traceObject",
          "notificationManager.MSG_INFO_DUMPNOTIFICATION", "Source : "
          + sSource);
      SilverTrace.info("notificationManager",
          "NotificationParameters.traceObject",
          "notificationManager.MSG_INFO_DUMPNOTIFICATION", "SessionId : "
          + sSessionId);
      SilverTrace.info("notificationManager",
          "NotificationParameters.traceObject",
          "notificationManager.MSG_INFO_DUMPNOTIFICATION", "Date : "
          + dDate.toString());

      if (connection != null)
        SilverTrace.info("notificationManager",
            "NotificationParameters.traceObject",
            "notificationManager.MSG_INFO_DUMPNOTIFICATION",
            "connection is not null");
      else
        SilverTrace.info("notificationManager",
            "NotificationParameters.traceObject",
            "notificationManager.MSG_INFO_DUMPNOTIFICATION",
            "connection is null");

    }
  }
}