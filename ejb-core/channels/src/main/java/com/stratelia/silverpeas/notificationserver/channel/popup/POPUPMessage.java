/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.silverpeas.notificationserver.channel.popup;


/**
 * Titre :
 * Description :
 * Copyright :    Copyright (c) 2001
 * Société :
 * @author eDurand
 * @version 2.0
 */

public class POPUPMessage
{
	private String m_Date;
	private String m_Time;

	/**
	 * Constructor declaration
	 *
	 *
	 * @see
	 */
	public POPUPMessage() {}

	private long m_Id;

	/**
	 * Method declaration
	 *
	 *
	 * @param value
	 *
	 * @see
	 */
	public void setId(long value)
	{
		m_Id = value;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @return
	 *
	 * @see
	 */
	public long getId()
	{
		return m_Id;
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
	public long getUserId()
	{
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
	public void setUserId(long value)
	{
		userId = value;
	}

	private String m_UserLogin;

	/**
	 * Method declaration
	 *
	 *
	 * @param value
	 *
	 * @see
	 */
	public void setUserLogin(String value)
	{
		m_UserLogin = value;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @return
	 *
	 * @see
	 */
	public String getUserLogin()
	{
		return m_UserLogin;
	}

	private String m_SenderName;

	/**
	 * Method declaration
	 *
	 *
	 * @param value
	 *
	 * @see
	 */
	public void setSenderName(String value)
	{
		m_SenderName = value;
		
	}

	/**
	 * Method declaration
	 *
	 *
	 * @return
	 *
	 * @see
	 */
	public String getSenderName()
	{
		return m_SenderName;
	}

	private String m_Subject;

	/**
	 * Method declaration
	 *
	 *
	 * @param value
	 *
	 * @see
	 */
	public void setSubject(String value)
	{
		m_Subject = value;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @return
	 *
	 * @see
	 */
	public String getSubject()
	{
		return m_Subject;
	}

	private String m_Body;

	/**
	 * Method declaration
	 *
	 *
	 * @param value
	 *
	 * @see
	 */
	public void setBody(String value)
	{
		m_Body = value;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @return
	 *
	 * @see
	 */
	public String getBody()
	{
		return m_Body;
	}

	private String m_Source;

	/**
	 * Method declaration
	 *
	 *
	 * @param value
	 *
	 * @see
	 */
	public void setSource(String source)
	{
		m_Source = source;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @return
	 *
	 * @see
	 */
	public String getSource()
	{
		return m_Source;
	}

	/**
	 * @author dlesimple
	 * Sender
	 */
	private String m_senderId = null;

	public String getSenderId() {
		return m_senderId;
	}

	public void setSenderId(String senderId) {
		this.m_senderId = senderId;
	}

	/**
	 * @author dlesimple
	 * isAnswerAllowed
	 */
	private boolean m_answerAllowed = false;

	public boolean isAnswerAllowed() {
		return m_answerAllowed;
	}

	public void setAnswerAllowed(boolean answerAllowed) {
		this.m_answerAllowed = answerAllowed;
	}
	
	public void setAnswerAllowed(String answerAllowed) {			
		this.m_answerAllowed = "1".equals(answerAllowed);
	}

	public void setDate(String date)
	{
		m_Date = date;
	}

	public void setTime(String time)
	{
		m_Time = time;
	}

	public String getDate()
	{
		return m_Date;
	}

	public String getTime()
	{
		return m_Time;
	}

}