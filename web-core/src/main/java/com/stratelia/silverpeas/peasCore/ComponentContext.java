/**
 * @author nicolas et didier
 * @version 1.0
 */

package com.stratelia.silverpeas.peasCore;



/**
 * Built by the main sesion controller
 * the ComponentContext objects
 * store the context of a component instance : space, user, ...
 * Used by the abstract component session controllers.
 */
public class ComponentContext
{
    private String		m_sCurSpaceName;
    private String		m_sCurSpaceId;
    private String		m_sCurCompoId;
    private String		m_sCurCompoName;
    private String		m_sCurCompoLabel;
    private String[]	m_asCurProfile;

	ComponentContext() {
		m_sCurSpaceName   = "";
		m_sCurSpaceId     = "";
		m_sCurCompoId     = "";
		m_sCurCompoName   = "";
		m_sCurCompoLabel  = "";
		m_asCurProfile    = new String[0];
	}

	public void setCurrentSpaceName(String CurrentSpaceName)
    {
        if(CurrentSpaceName != null)
			m_sCurSpaceName = CurrentSpaceName;
        else 
            m_sCurSpaceName = "";
    }

    public String getCurrentSpaceName()
    {
       return m_sCurSpaceName;
    }

	public void setCurrentSpaceId(String CurrentSpaceId)
    {
        if(CurrentSpaceId != null)
			m_sCurSpaceId = CurrentSpaceId;
        else 
            m_sCurSpaceId = "";
    }

    public String getCurrentSpaceId()
    {
       return m_sCurSpaceId;
    }

    public void setCurrentComponentId(String sClientComponentId)
    {
        if(sClientComponentId != null)
            m_sCurCompoId = sClientComponentId;
        else
            m_sCurCompoId = "";
    }

    public String getCurrentComponentId()
    {
       return m_sCurCompoId;
    }
    
    public void setCurrentComponentName(String sCurrentComponentName)
    {
        if(sCurrentComponentName != null)
            m_sCurCompoName= sCurrentComponentName;
        else
            m_sCurCompoName = "";
    }

    public String getCurrentComponentName()
    {
       return m_sCurCompoName;
    }


    public void setCurrentComponentLabel(String sCurrentComponentLabel)
    {
        if(sCurrentComponentLabel != null)
            m_sCurCompoLabel= sCurrentComponentLabel;
        else
            m_sCurCompoLabel = "";
    }
	
    public String getCurrentComponentLabel()
    {
       return m_sCurCompoLabel;
    }

    public void setCurrentProfile(String[] asCurrentProfile)
    {
        if(asCurrentProfile != null)
            m_asCurProfile = asCurrentProfile;
        else
            m_asCurProfile = new String[0];
    }

    public String[] getCurrentProfile()
    {
       return m_asCurProfile;
    }
}
