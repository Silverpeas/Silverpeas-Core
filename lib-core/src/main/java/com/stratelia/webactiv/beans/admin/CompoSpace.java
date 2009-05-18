/*
 * CompoSpace.java
 *
 * Created on 28 décembre 2000, 15:14
 * author: norbert CHAIX
 */
 
package com.stratelia.webactiv.beans.admin;

public class CompoSpace
{
    private String	m_sComponentId			= "";
    private String	m_sComponentLabel		= "";
    private String	m_sTablePrefixSpaceId	= "";
	private String	m_sSpaceLabel			= "";
	private int		spaceLevel				= 1;
  
    public CompoSpace() 
    {
    }
    
    public void setComponentId(String sComponentId)
    {
        m_sComponentId = sComponentId;
    }

    public String getComponentId()
    {
        return m_sComponentId;
    }
    
    public void setComponentLabel(String sComponentLabel)
    {
        m_sComponentLabel = sComponentLabel;
    }

    public String getComponentLabel()
    {
        return m_sComponentLabel;
    }

	public void setSpaceId(String sTablePrefixSpaceId)
    {
        m_sTablePrefixSpaceId = sTablePrefixSpaceId;
    }

    public String getSpaceId()
    {
        return m_sTablePrefixSpaceId;
    }

	public void setSpaceLabel(String sSpaceLabel)
    {
        m_sSpaceLabel = sSpaceLabel;
    }

    public String getSpaceLabel()
    {
        return m_sSpaceLabel;
    }

	public void setSpaceLevel(int level)
	{
		spaceLevel = level;
	}

	public int getSpaceLevel()
	{
		return spaceLevel;
	}
}