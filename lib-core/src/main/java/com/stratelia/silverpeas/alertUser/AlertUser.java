package com.stratelia.silverpeas.alertUser;

import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.util.PairObject;

public class AlertUser
{  
	protected String				m_hostSpaceName;
	protected String				m_hostComponentId;
    protected PairObject			m_hostComponentName;
	protected NotificationMetaData	m_notificationMetaData;

    public AlertUser()
    {
        resetAll();
    }

    public void resetAll()
    {
        m_hostSpaceName = "";
		m_hostComponentId = "";
        m_hostComponentName = new PairObject("","");
    }
    
    static public String getAlertUserURL()
    {
        return "/RalertUserPeas/jsp/Main";	
    }

    public void setHostSpaceName(String hostSpaceName) { if (hostSpaceName != null) { m_hostSpaceName = hostSpaceName; } else { m_hostSpaceName = ""; } }
    public String getHostSpaceName() { return m_hostSpaceName; }
    public void setHostComponentId(String hostComponentId) { if (hostComponentId != null) { m_hostComponentId = hostComponentId; } else { m_hostComponentId = ""; } }
    public String getHostComponentId() { return m_hostComponentId; }
    public void setHostComponentName(PairObject hostComponentName) { if (hostComponentName != null) { m_hostComponentName = hostComponentName; } else { m_hostComponentName = new PairObject("",""); } }
    public PairObject getHostComponentName() { return m_hostComponentName; }
	public void setNotificationMetaData(NotificationMetaData notificationMetaData) { if (notificationMetaData != null) { m_notificationMetaData = notificationMetaData; } else { m_notificationMetaData = new NotificationMetaData(); } }
	public NotificationMetaData getNotificationMetaData() { return m_notificationMetaData; }
}
