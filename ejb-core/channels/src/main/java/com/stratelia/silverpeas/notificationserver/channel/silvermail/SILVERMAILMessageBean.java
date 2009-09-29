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

package com.stratelia.silverpeas.notificationserver.channel.silvermail;

/**
 * Titre :
 * Description :
 * Copyright :    Copyright (c) 2001
 * Société :
 * @author eDurand
 * @version 1.0
 */

import java.util.Date;

import com.stratelia.webactiv.persistence.SilverpeasBean;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;

/**
 * Class declaration
 * 
 * 
 * @author
 * @version %I%, %G%
 */
public class SILVERMAILMessageBean extends SilverpeasBean {

  /**
   * Constructor declaration
   * 
   * 
   * @see
   */
  public SILVERMAILMessageBean() {
  }

  private long userId = -1;

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public long getUserId() {
    return userId;
  }

  /**
   * Method declaration
   * 
   * 
   * @param value
   * 
   * @see
   */
  public void setUserId(long value) {
    userId = value;
  }

  private long folderid = 0; // 0 = INBOX

  /**
   * Method declaration
   * 
   * 
   * @param value
   * 
   * @see
   */
  public void setFolderId(long value) {
    folderid = value;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public long getFolderId() {
    return folderid;
  }

  private String sendername = "";

  /**
   * Method declaration
   * 
   * 
   * @param value
   * 
   * @see
   */
  public void setSenderName(String value) {
    sendername = value;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getSenderName() {
    return sendername;
  }

  private String subject = "";

  /**
   * Method declaration
   * 
   * 
   * @param value
   * 
   * @see
   */
  public void setSubject(String value) {
    subject = value;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getSubject() {
    return subject;
  }

  private String source = "";

  /**
   * Method declaration
   * 
   * 
   * @param value
   * 
   * @see
   */
  public void setSource(String value) {
    source = value;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getSource() {
    return source;
  }

  private String url = "";

  /**
   * Method declaration
   * 
   * 
   * @param value
   * 
   * @see
   */
  public void setUrl(String value) {
    url = value;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getUrl() {
    return url;
  }

  private Date dateMsg = new Date();

  /**
   * Method declaration
   * 
   * 
   * @param value
   * 
   * @see
   */
  public void setDateMsg(Date value) {
    dateMsg = value;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public Date getDateMsg() {
    return dateMsg;
  }

  private String body = "";

  /**
   * Method declaration
   * 
   * 
   * @param value
   * 
   * @see
   */
  public void setBody(String value) {
    body = value;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
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

  /**
   * *************************************************************************
   */

  /**
	 * 
	 */
  public int _getConnectionType() {
    return SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
  }

  /**
	 * 
	 */
  public String _getTableName() {
    return "ST_SilverMailMessage";
  }

}
