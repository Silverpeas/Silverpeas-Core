package com.stratelia.silverpeas.notificationserver.channel.popup;

/*
 * SilverMessage.java
 *
 * Created on 30 august 2001
 *
 * Author D.Blot
 */

/**
 *
 * @author  dblot
 * @version
 */
public class SilverMessage {

	private String m_What = null;
	private String m_Content = null;
	private String m_ID = null;
	private String m_SenderId = null;
	private String m_SenderName = null;
	private boolean m_AnswerAllowed = false;
	//private String m_Date = null;
	//private String m_Time = null;
	

	/**--------------------------------------------------------------------------constructor
	 * constructor
	 */
	public SilverMessage ( String what , String content ) {
	   this.m_What = what;
	   this.m_Content = content;
	}

	/**--------------------------------------------------------------------------constructor
	 * constructor
	 */
	public SilverMessage ( String what ) {
	   this.m_What = what;
	   this.m_Content = new String ("");
	}

	/**--------------------------------------------------------------------------getWhat
	 * return what
	 */
	public String getWhat () {
		return m_What;
	};

	/**--------------------------------------------------------------------------getContent
	 * return content
	 */
	public String getContent () {
		return m_Content;
	};

	/**--------------------------------------------------------------------------setContent
	 * set the content
	 */
	public void setContent ( String content ) {
		m_Content = content;
	};

	/**--------------------------------------------------------------------------setContent
	 * set the ID
	 */
	public void setID ( String ID ) {
		m_ID = ID;
	};

	/**--------------------------------------------------------------------------setContent
	 * get the ID
	 */
	public String getID () {
		return m_ID;
	}

	public String getSenderId() {
		return m_SenderId;
	}

	public void setSenderId(String senderId) {
		m_SenderId = senderId;
	}

	public String getSenderName() {
		return m_SenderName;
	}

	public void setSenderName(String senderName) {
		m_SenderName = senderName;
	}
	
	public boolean isAnswerAllowed() {
		return m_AnswerAllowed;
	}

	public void setAnswerAllowed(boolean answerAllowed) {
		m_AnswerAllowed = answerAllowed;
	}
	
	public void setDate(String date)
	{
		//m_Date = date;
	}
	
	public void setTime(String time)
	{
		//m_Time = time;
	}
}
