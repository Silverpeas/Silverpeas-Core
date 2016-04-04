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

package org.silverpeas.core.notification.user.server.channel.server;

import org.silverpeas.core.persistence.datasource.model.identifier.UniqueLongIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.AbstractJpaCustomEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "ST_ServerMessage")
@NamedQueries({@NamedQuery(name = "findByUserIdAndSessionId",
    query = "select m from ServerMessageBean m where m.id in (select min(s.id) from " +
        "ServerMessageBean s where s.userId=:userId and s.sessionId=:sessionId)"),
    @NamedQuery(name = "deleteByUserIdAndSessionId",
        query = "delete from ServerMessageBean m where m.userId = :userId and m.sessionId = " +
            ":sessionId")})
public class ServerMessageBean
    extends AbstractJpaCustomEntity<ServerMessageBean, UniqueLongIdentifier> {

  private static final long serialVersionUID = 769537113068849221L;

  public ServerMessageBean() {
  }

  @Column(nullable = false)
  @NotNull
  private long userId = -1;

  public long getUserId() {
    return userId;
  }

  public void setUserId(long value) {
    userId = value;
  }

  private String body = "";

  public String getBody() {
    return body;
  }

  public void setBody(String value) {
    body = value;
  }

  private String sessionId = "";

  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(String value) {
    sessionId = value;
  }

  private String header;
  private String subject;
  private Character type;


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

    final ServerMessageBean bean = (ServerMessageBean) o;

    if ((getId() == null && bean.getId() != null) ||
        (getId() != null && !getId().equals(bean.getId()))) {
      return false;
    }

    if (userId != bean.userId) {
      return false;
    }
    if (body != null ? !body.equals(bean.body) : bean.body != null) {
      return false;
    }
    if (sessionId != null ? !sessionId.equals(bean.sessionId) : bean.sessionId != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (getId() != null ? getId().hashCode() : 0);
    result = 31 * result + (int) (userId ^ (userId >>> 32));
    result = 31 * result + (body != null ? body.hashCode() : 0);
    result = 31 * result + (sessionId != null ? sessionId.hashCode() : 0);
    return result;
  }
}