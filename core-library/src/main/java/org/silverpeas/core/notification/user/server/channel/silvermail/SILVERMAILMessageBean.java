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

package org.silverpeas.core.notification.user.server.channel.silvermail;

import org.silverpeas.core.persistence.datasource.model.identifier.UniqueLongIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.AbstractJpaCustomEntity;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "ST_SilverMailMessage")
@NamedQueries({
    @NamedQuery(name = "findByUserIdAndFolderIdAndReadState",
        query = "select m from SILVERMAILMessageBean m where m.userId = :userId and m.folderId = " +
            ":folderId and m.readen = :readState order by m.id desc"),
    @NamedQuery(name = "findByUserIdAndFolderId",
        query = "select m from SILVERMAILMessageBean m where m.userId = :userId and m.folderId = " +
            ":folderId order by m.id desc")})
public class SILVERMAILMessageBean
    extends AbstractJpaCustomEntity<SILVERMAILMessageBean, UniqueLongIdentifier> {
  private static final long serialVersionUID = -3073514330044912996L;

  public SILVERMAILMessageBean() {
  }

  private long userId = -1;

  public long getUserId() {
    return userId;
  }

  public void setUserId(long value) {
    userId = value;
  }

  private long folderId = 0; // 0 = INBOX

  public void setFolderId(long value) {
    folderId = value;
  }

  public long getFolderId() {
    return folderId;
  }

  private String senderName = "";

  public void setSenderName(String value) {
    senderName = value;
  }

  public String getSenderName() {
    return senderName;
  }

  private String subject = "";

  public void setSubject(String value) {
    subject = value;
  }

  public String getSubject() {
    return subject;
  }

  private String source = "";

  public void setSource(String value) {
    source = value;
  }

  public String getSource() {
    return source;
  }

  private String url = "";

  public void setUrl(String value) {
    url = value;
  }

  public String getUrl() {
    return url;
  }

  private String dateMsg;

  public void setDateMsg(String value) {
    dateMsg = value;
  }

  public String getDateMsg() {
    return dateMsg;
  }

  private String body = "";

  public void setBody(String value) {
    body = value;
  }

  public String getBody() {
    return body;
  }

  private int readen = 0;

  public void setReaden(int readen) {
    readen = readen;
  }

  public int getReaden() {
    return readen;
  }

  private String header;


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
    if (url != null ? !url.equals(that.url) : that.url != null) {
      return false;
    }

    return true;
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