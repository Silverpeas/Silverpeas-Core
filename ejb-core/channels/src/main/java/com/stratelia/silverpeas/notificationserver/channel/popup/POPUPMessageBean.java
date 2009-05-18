package com.stratelia.silverpeas.notificationserver.channel.popup;

import com.stratelia.webactiv.persistence.*;

/**
 * Titre :
 * Description :
 * Copyright :    Copyright (c) 2001
 * Société :
 * @author eDurand
 * @version 1.0
 */
public class POPUPMessageBean extends SilverpeasBean
{

  public POPUPMessageBean()
  {
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

  public void setUserId( long value )
  {
    userId = value;
  }
  public String getBody()
  {
    return body;
  }
  public void setBody( String value )
  {
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
	
	public void setAnswerAllowed(boolean answerAllowed)
	{
		if (answerAllowed)
			this.answerAllowed = "1";
		else
			this.answerAllowed = "0";
	}

	public String getMsgDate()
	{
		return msgDate;
	}
	public String getMsgTime()
	{
		return msgTime;
	}

	public void setMsgDate(String date)
	{
		msgDate = date;
	}

	public void setMsgTime(String time)
	{
		msgTime = time;
	}

/*****************************************************************************/
  /**
   *
   */
  public int _getConnectionType()
  {
    return SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
  }

  /**
   *
   */
  public String _getTableName()
  {
    return "ST_PopupMessage";
  }
  
  public boolean _getAnswerAllowed()
  {
	  return "1".equals(getAnswerAllowed());
  }

}