package com.stratelia.webactiv.agenda.model;

public class CalendarImportSettings {
	public final static int TYPE_NO_IMPORT = 0;
	public final static int TYPE_OUTLOOK_IMPORT = 1;
	public final static int TYPE_NOTES_IMPORT = 2;
	public final static int DEFAULT_DELAY = 5;
	
	/**
	 * Id of user whose settings belong to
	 */
	private int userId = -1;
	
	/**
	 * importation will only occur on this host
	 */
	private String hostName = null;

	/**
	 * Synchronisation type : None, Outlook, Notes
	 */
	private int synchroType = TYPE_NO_IMPORT;
	
	/**
	 * Delay in minutes between each synchronisation
	 */
	private int synchroDelay = DEFAULT_DELAY;

	private String urlIcalendar = null; 
	private String loginIcalendar = null; 
	private String pwdIcalendar = null; 
	private String charset = "ISO-8859-1"; 
		
	/**
	 * @return Returns the hostName. Importation will only occur on this host
	 */
	public String getHostName() {
		return hostName;
	}

	/**
	 * @param hostName The hostName to set. Importation will only occur on this host
	 */
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	/**
	 * @return Returns the Delay in minutes between each synchronisation.
	 */
	public int getSynchroDelay() {
		return synchroDelay;
	}

	/**
	 * @param synchroDelay Delay in minutes between each synchronisation.
	 */
	public void setSynchroDelay(int synchroDelay) {
		this.synchroDelay = synchroDelay;
	}

	/**
	 * @return Returns the Synchronisation type : None, Outlook, Notes.
	 */
	public int getSynchroType() {
		return synchroType;
	}

	/**
	 * @param synchroType The Synchronisation type : None, Outlook, Notes.
	 */
	public void setSynchroType(int synchroType) {
		this.synchroType = synchroType;
	}

	/**
	 * @return Returns the Id of user whose settings belong to.
	 */
	public int getUserId() {
		return userId;
	}

	/**
	 * @param userId The Id of user whose settings belong to.
	 */
	public void setUserId(int userId) {
		this.userId = userId;
	}

	/**
	 * @return Returns the url to iCalendar
	 */
	public String getUrlIcalendar() {
		return urlIcalendar;
	}

	/**
	 * @param userId The Id of user whose settings belong to.
	 */
	public void setUrlIcalendar(String url) {
		this.urlIcalendar = url;
	}

	/**
	 * @return Returns the login to remote iCalendar
	 */
	public String getLoginIcalendar() {
		return loginIcalendar;
	}
	/**
	 * @param login The Pwd to remote iCalendar
	 */
	public void setLoginIcalendar(String login) {
		this.loginIcalendar = login;
	}

	/**
	 * @return Returns the login to remote iCalendar
	 */
	public String getPwdIcalendar() {
		return pwdIcalendar;
	}
	/**
	 * @param pwd The Pwd to remote iCalendar
	 */
	public void setPwdIcalendar(String pwd) {
		this.pwdIcalendar = pwd;
	}

	/**
	 * @return Returns charset of remote iCalendar
	 */
	public String getCharset() {
		return charset;
	}
	/**
	 * @param charset to remote iCalendar
	 */
	public void setCharset(String charset) {
		this.charset = charset;
	}


}
