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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.stratelia.silverpeas.notificationserver.channel.silvermail;

import java.util.Date;

import com.stratelia.webactiv.persistence.SilverpeasBean;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;

public class SILVERMAILMessageBean extends SilverpeasBean {
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

  private long folderid = 0; // 0 = INBOX

  public void setFolderId(long value) {
    folderid = value;
  }

  public long getFolderId() {
    return folderid;
  }

  private String sendername = "";

  public void setSenderName(String value) {
    sendername = value;
  }

  public String getSenderName() {
    return sendername;
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

  private Date dateMsg = new Date();

  public void setDateMsg(Date value) {
    dateMsg = value;
  }

  public Date getDateMsg() {
    return dateMsg;
  }

  private String body = "";

  public void setBody(String value) {
    body = value;
  }

  public String getBody() {
    return body;
  }

  private int m_readen = 0;

  public void setReaden(int readen) {
    m_readen = readen;
  }

  public int getReaden() {
    return m_readen;
  }

  public int _getConnectionType() {
    return SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
  }

  public String _getTableName() {
    return "ST_SilverMailMessage";
  }

}