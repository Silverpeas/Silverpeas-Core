/** 
 * @author  lbertin
 * @version 1.0
 */

package com.stratelia.webactiv.beans.admin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SpaceProfileInst extends Object implements Serializable
{
    private String m_sId;
    private String m_sName;
    private String m_sLabel;
    private String m_sDescription;
    private String m_sSpaceFatherId;
    private ArrayList m_alGroups;
    private ArrayList m_alUsers;
    
    private boolean isInherited = false;

    /** Creates new SpaceProfileInst */
    public SpaceProfileInst() 
    {
        m_sId = "";
        m_sName = "";
        m_sLabel = "";
        m_sDescription = "";
        m_sSpaceFatherId = "";
        m_alGroups = new ArrayList();
        m_alUsers = new ArrayList();
    }
  
    public void setId(String sId)
    {
        m_sId = sId;
    }

    public String getId()
    {
        return m_sId;
    }

    public void setName(String sName)
    {
        m_sName = sName;
    }
    
    public String getName()
    {
        return m_sName;
    }

    public void setLabel(String sLabel)
    {
        m_sLabel = sLabel;
    }
    
    public String getLabel()
    {
        return m_sLabel;
    }

    public void setDescription(String sDescription)
    {
        m_sDescription = sDescription;
    }
    
    public String getDescription()
    {
        return m_sDescription;
    }

	public void setSpaceFatherId(String sSpaceFatherId)
    {
        m_sSpaceFatherId = sSpaceFatherId;
    }
    
    public String getSpaceFatherId()
    {
        return m_sSpaceFatherId;
    }

    public int getNumGroup()
    {
        return m_alGroups.size();
    }

    public String getGroup(int nIndex)
    {
        return (String)m_alGroups.get(nIndex);
    }

    public void addGroup(String sGroupId)
    {
        m_alGroups.add(sGroupId);
    }
    
    public void removeGroup(String sGroupId)
    {
        m_alGroups.remove(sGroupId);
    }
    
    public ArrayList getAllGroups()
    {
        return m_alGroups;
    }
    
    public void removeAllGroups()
    {
        m_alGroups = new ArrayList();
    }

    public int getNumUser()
    {
        return m_alUsers.size();
    }

    public String getUser(int nIndex)
    {
        return (String)m_alUsers.get(nIndex);
    }

    public void addUser(String sUserId)
    {
        m_alUsers.add(sUserId);
    }
    
    public void removeUser(String sUserId)
    {
        m_alUsers.remove(sUserId);
    }
    
    public void addUsers(List users)
    {
    	m_alUsers.addAll(users);
    }
    
    public void addGroups(List groups)
    {
    	m_alGroups.addAll(groups);
    }
    
   public ArrayList getAllUsers()
    {
        return m_alUsers;
    }
    
    public void removeAllUsers()
    {
        m_alUsers = new ArrayList();
    }
    
    public boolean isInherited() {
		return isInherited;
	}

	public void setInherited(boolean isInherited) {
		this.isInherited = isInherited;
	}
}