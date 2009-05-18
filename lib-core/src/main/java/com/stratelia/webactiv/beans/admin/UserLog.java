/*
 * @author Ludovic BERTIN
 * @version 1.0
 * date 10/08/2001
 */
package com.stratelia.webactiv.beans.admin;

import java.util.Date;

public class UserLog
{
    private String m_sSessionId		= "";             // Client session id (http)
    private String m_sUserId		= "";             // User Id (silverpeas)
    private String m_sUserLogin		= "";             // User login (silverpeas)
    private Date m_LogDate			= null;           // Log date

    public UserLog()
    {
    }
    
    public void setSessionId(String sSessionId)
    { 
        m_sSessionId = sSessionId;
    }

    public String getSessionId()
    { 
        return m_sSessionId;
    }

    public void setUserId(String sUserId)
    { 
        m_sUserId = sUserId;
    }

    public String getUserId()
    { 
        return m_sUserId;
    }
    
    public void setUserLogin(String sUserLogin)
    { 
        m_sUserLogin = sUserLogin;
    }

    public String getUserLogin()
    { 
        return m_sUserLogin;
    }

	public void setLogDate(Date date)
    {
        m_LogDate = date;
    }

    public Date getLogDate()
    {
        return m_LogDate;
    }
}