/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.notification.user.server.channel.silvermail;

import org.silverpeas.core.persistence.datasource.model.identifier.UniqueLongIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.BasicJpaEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "ST_SilverMailMessage")
@NamedQueries({
    @NamedQuery(name = "markAllMessagesAsReadByUserIdAndIds",
        query = "update SILVERMAILMessageBean m set m.readen = 1 where m.userId = :userId " +
            "and m.readen != 1 and m.id in :ids"),
    @NamedQuery(name = "getLongTextIdsOfAllMessagesByUserIdAndIds",
        query = "select distinct m.body from SILVERMAILMessageBean m where m.userId = :userId " +
            "and m.id in :ids"),
    @NamedQuery(name = "deleteAllMessagesByUserIdAndIds",
        query = "delete SILVERMAILMessageBean m where m.userId = :userId " +
            "and m.id in :ids"),
    @NamedQuery(name = "markAllMessagesAsReadByUserIdAndFolderId",
        query = "update SILVERMAILMessageBean m set m.readen = 1 where m.userId = :userId " +
            "and m.folderId = :folderId and m.readen != 1"),
    @NamedQuery(name = "getLongTextIdsOfAllMessagesByUserIdAndFolderId",
        query = "select distinct m.body from SILVERMAILMessageBean m where m.userId = :userId " +
            "and m.folderId = :folderId"),
    @NamedQuery(name = "deleteAllMessagesByUserIdAndFolderId",
        query = "delete SILVERMAILMessageBean m where m.userId = :userId " +
            "and m.folderId = :folderId")})
public class SILVERMAILMessageBean
    extends BasicJpaEntity<SILVERMAILMessageBean, UniqueLongIdentifier> {
  private static final long serialVersionUID = -3073514330044912996L;

  @Column(nullable = false)
  @NotNull
  private long userId = -1;
  // 0 = INBOX
  @Column
  private long folderId = 0;
  @Column
  private String senderName = "";
  @Column(length = 1024)
  private String subject = "";
  @Column
  private String source = "";
  @Column
  private String url = "";
  @Column
  private String dateMsg;
  @Column(length = 4000)
  private String body = "";
  @Column(nullable = false)
  private int readen = 0;
  @Column
  private String header;

  public SILVERMAILMessageBean() {
    // For JPA
  }

  public long getUserId() {
    return userId;
  }

  public void setUserId(long value) {
    userId = value;
  }

  public long getFolderId() {
    return folderId;
  }

  public void setFolderId(long value) {
    folderId = value;
  }

  public String getSenderName() {
    return senderName;
  }

  public void setSenderName(String value) {
    senderName = value;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String value) {
    subject = value;
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

  public String getDateMsg() {
    return dateMsg;
  }

  public void setDateMsg(String value) {
    dateMsg = value;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String value) {
    body = value;
  }

  public int getReaden() {
    return readen;
  }

  public void setReaden(int value) {
    readen = value;
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

    final SILVERMAILMessageBean that = (SILVERMAILMessageBean) o;

    if ((getId() == null && that.getId() != null) ||
        (getId() != null && !getId().equals(that.getId()))) {
      return false;
    }

    if (folderId != that.folderId) {
      return false;
    }
    if (readen != that.readen) {
      return false;
    }
    if (userId != that.userId) {
      return false;
    }
    if (body != null ? !body.equals(that.body) : that.body != null) {
      return false;
    }
    if (dateMsg != null ? !dateMsg.equals(that.dateMsg) : that.dateMsg != null) {
      return false;
    }
    if (senderName != null ? !senderName.equals(that.senderName) : that.senderName != null) {
      return false;
    }
    if (source != null ? !source.equals(that.source) : that.source != null) {
      return false;
    }
    if (subject != null ? !subject.equals(that.subject) : that.subject != null) {
      return false;
    }
    return url != null ? url.equals(that.url) : that.url == null;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (getId() != null ? getId().hashCode():0);
    result = 31 * result + (int) (userId ^ (userId >>> 32));
    result = 31 * result + (int) (folderId ^ (folderId >>> 32));
    result = 31 * result + (senderName != null ? senderName.hashCode() : 0);
    result = 31 * result + (subject != null ? subject.hashCode() : 0);
    result = 31 * result + (source != null ? source.hashCode() : 0);
    result = 31 * result + (url != null ? url.hashCode() : 0);
    result = 31 * result + (dateMsg != null ? dateMsg.hashCode() : 0);
    result = 31 * result + (body != null ? body.hashCode() : 0);
    result = 31 * result + readen;
    return result;
  }
}