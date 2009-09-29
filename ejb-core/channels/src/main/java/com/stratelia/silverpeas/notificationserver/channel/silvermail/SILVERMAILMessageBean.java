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
