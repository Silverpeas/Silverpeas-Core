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

import com.stratelia.webactiv.persistence.SilverpeasBean;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;

public class POPUPMessageBean extends SilverpeasBean {

  private static final long serialVersionUID = 7025111830012761169L;

  public POPUPMessageBean() {
  }

  private long userId = -1;
  private String body = "";
  private String senderId = null;
  private String senderName = null;
  private String answerAllowed = "0";
  private String msgDate = null;
  private String msgTime = null;

  public long getUserId() {
    return userId;
  }

  public void setUserId(long value) {
    userId = value;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String value) {
    body = value;
  }

  public String getSenderId() {
    return senderId;
  }

  public void setSenderId(String senderId) {
    this.senderId = senderId;
  }

  public String getSenderName() {
    return senderName;
  }

  public void setSenderName(String senderName) {
    this.senderName = senderName;
  }

  public String getAnswerAllowed() {
    return answerAllowed;
  }

  public void setAnswerAllowed(String answerAllowed) {
    this.answerAllowed = answerAllowed;
  }

  public void setAnswerAllowed(boolean answerAllowed) {
    if (answerAllowed) {
      this.answerAllowed = "1";
    } else {
      this.answerAllowed = "0";
    }
  }

  public String getMsgDate() {
    return msgDate;
  }

  public String getMsgTime() {
    return msgTime;
  }

  public void setMsgDate(String date) {
    msgDate = date;
  }

  public void setMsgTime(String time) {
    msgTime = time;
  }

  public int _getConnectionType() {
    return SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
  }

  public String _getTableName() {
    return "ST_PopupMessage";
  }

  public boolean _getAnswerAllowed() {
    return "1".equals(getAnswerAllowed());
  }

}