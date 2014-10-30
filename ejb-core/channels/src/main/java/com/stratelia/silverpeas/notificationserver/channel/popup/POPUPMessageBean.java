/**
 * Copyright (C) 2000 - 2013 Silverpeas
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

package com.stratelia.silverpeas.notificationserver.channel.popup;

import org.silverpeas.persistence.model.identifier.UniqueIntegerIdentifier;
import org.silverpeas.persistence.model.jpa.AbstractJpaCustomEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "ST_PopupMessage")
@NamedQueries({
    @NamedQuery(name = "findByUserId",
        query = "select m from POPUPMessageBean m where m.id in (select min(p.id) FROM " +
            "POPUPMessageBean p WHERE p.userId = :userId)"),
    @NamedQuery(name = "deleteByUserIdAndSenderId",
        query = "delete from POPUPMessageBean m where m.userId = :userId and m.senderId = " +
            ":senderId")})
public class POPUPMessageBean
    extends AbstractJpaCustomEntity<POPUPMessageBean, UniqueIntegerIdentifier> {

  private static final long serialVersionUID = 7025111830012761169L;

  public POPUPMessageBean() {
  }

  @Column(nullable = false)
  @NotNull
  private int userId = -1;
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
    userId = (int) value;
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

  public boolean isAnswerAllowed() {
    return "1".equals(getAnswerAllowed());
  }

}