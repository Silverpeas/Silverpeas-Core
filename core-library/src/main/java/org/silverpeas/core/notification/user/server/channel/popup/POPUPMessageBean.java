/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.notification.user.server.channel.popup;

import org.silverpeas.core.persistence.datasource.model.identifier.UniqueLongIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.BasicJpaEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Entity
@Table(name = "ST_PopupMessage")
@NamedQuery(name = "findByUserId",
    query = "select m from POPUPMessageBean m where m.id in (select min(p.id) FROM " +
        "POPUPMessageBean p WHERE p.userId = :userId)")
@NamedQuery(name = "deleteByUserIdAndSenderId",
    query = "delete from POPUPMessageBean m where m.userId = :userId and m.senderId = :senderId")
public class POPUPMessageBean
    extends BasicJpaEntity<POPUPMessageBean, UniqueLongIdentifier> {
  private static final long serialVersionUID = 7025111830012761169L;

  @Column(nullable = false)
  @NotNull
  private long userId = -1;
  @Column(length = 4000)
  private String body = "";
  @Column(length = 10)
  private String senderId = null;
  @Column(length = 200)
  private String senderName = null;
  @Column(length = 1)
  private String answerAllowed = "0";
  @Column
  private String source = "";
  @Column
  private String url = "";
  @Column(length = 10)
  private String msgDate = null;
  @Column(length = 5)
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

  public String getSource() {
    return source;
  }

  public void setSource(String value) {
    source = value;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String value) {
    url = value;
  }

  public String getMsgDate() {
    return msgDate;
  }

  public void setMsgDate(String date) {
    msgDate = date;
  }

  public String getMsgTime() {
    return msgTime;
  }

  public void setMsgTime(String time) {
    msgTime = time;
  }

  public boolean isAnswerAllowed() {
    return "1".equals(getAnswerAllowed());
  }

  public void setAnswerAllowed(boolean answerAllowed) {
    if (answerAllowed) {
      this.answerAllowed = "1";
    } else {
      this.answerAllowed = "0";
    }
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    final POPUPMessageBean that = (POPUPMessageBean) o;

    if ((getId() == null && that.getId() != null) ||
        (getId() != null && !getId().equals(that.getId()))) {
      return false;
    }

    if (userId != that.userId) {
      return false;
    }
    if (!Objects.equals(answerAllowed, that.answerAllowed)) {
      return false;
    }
    if (!Objects.equals(body, that.body)) {
      return false;
    }
    if (!Objects.equals(source, that.source)) {
      return false;
    }
    if (!Objects.equals(url, that.url)) {
      return false;
    }
    if (!Objects.equals(msgDate, that.msgDate)) {
      return false;
    }
    if (!Objects.equals(msgTime, that.msgTime)) {
      return false;
    }
    if (!Objects.equals(senderId, that.senderId)) {
      return false;
    }
    return Objects.equals(senderName, that.senderName);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (getId() != null ? getId().hashCode() : 0);
    result = 31 * result + Long.hashCode(userId);
    result = 31 * result + (body != null ? body.hashCode() : 0);
    result = 31 * result + (senderId != null ? senderId.hashCode() : 0);
    result = 31 * result + (senderName != null ? senderName.hashCode() : 0);
    result = 31 * result + (answerAllowed != null ? answerAllowed.hashCode() : 0);
    result = 31 * result + (source != null ? source.hashCode() : 0);
    result = 31 * result + (url != null ? url.hashCode() : 0);
    result = 31 * result + (msgDate != null ? msgDate.hashCode() : 0);
    result = 31 * result + (msgTime != null ? msgTime.hashCode() : 0);
    return result;
  }
}