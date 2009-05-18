/** 
 * @author Norbert CHAIX
 * @version 1.0
 * 13/10/2000
*/

package com.stratelia.webactiv.beans.admin;

import java.io.Serializable;
import java.util.ArrayList;

public class AdminGroupInst implements Serializable
{
    private Group m_Group;	// Admin group detail of this node
    private ArrayList m_alChildrenAdminGroupInst;	// Children Admin group inst of this node
    
    /** Creates a new Space */
    public AdminGroupInst()
    {
        m_Group = null;
        m_alChildrenAdminGroupInst = new ArrayList();
    }
    
    public Group getGroup()
    {
        return m_Group;
    }
    
    public void setGroup(Group group)
    {
        m_Group = group;
    }
	
    public void setChildrenAdminGroupInst(ArrayList alChildrenAdminGroupInst)
    {
        m_alChildrenAdminGroupInst = alChildrenAdminGroupInst;
    }

    public ArrayList getAllChildrenAdminGroupInst()
    {
        return m_alChildrenAdminGroupInst;
    }
}
