/** 
 * @author  neysseri
 * @version 1.0
 */

package com.stratelia.webactiv.beans.admin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GroupProfileInst extends Object implements Serializable
{
    private String m_sId;
    private String m_sName;
    private String m_sGroupId;
    private ArrayList m_alGroups;
    private ArrayList m_alUsers;

    /** Creates new GroupProfileInst */
    public GroupProfileInst() 
    {
        m_sId 		= "";
        m_sName 	= "";
        m_sGroupId 	= "";
        m_alGroups 	= new ArrayList();
        m_alUsers 	= new ArrayList();
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

	public void setGroupId(String sGroupId)
    {
		m_sGroupId = sGroupId;
    }
    
    public String getGroupId()
    {
        return m_sGroupId;
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
}