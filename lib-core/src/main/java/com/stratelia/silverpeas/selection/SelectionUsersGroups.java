/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.silverpeas.selection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.ProfileInst;
import com.stratelia.webactiv.beans.admin.UserDetail;

public class SelectionUsersGroups implements SelectionExtraParams
{
    static		OrganizationController m_oc = new OrganizationController();
    String		m_domainId		= null;
    String		m_componentId	= null;
	List		profileIds		= null;
	ArrayList	profileNames	= null;

	public String[] getProfileIds() {
        if (profileIds != null)
        {
            return (String[]) profileIds.toArray(new String[0]);
        }
        else
        {
            return null;
        }
	}

	public ArrayList getProfileNames() {
		return profileNames;
	}

	public void setProfileNames(ArrayList profileNames)
	{
		this.profileNames = profileNames;
		ComponentInst componentInst = m_oc.getComponentInst(m_componentId);
		int nbProfiles = componentInst.getNumProfileInst();
		ProfileInst profileInst = null;
		profileIds = new ArrayList();
		for (int p=0; p<nbProfiles; p++) {
            profileInst = componentInst.getProfileInst(p);
            if (profileNames.contains(profileInst.getName())) {
                profileIds.add(profileInst.getId());
            }
		}
	}
	
	public void setProfileIds(List profileIds)
	{
		this.profileIds = profileIds;
	}
	
	public void addProfileId(String profileId)
	{
		if (profileIds == null)
			profileIds = new ArrayList();
		
		profileIds.add(profileId);
	}
	
	public void addProfileIds(List profileIds)
	{
		if (this.profileIds == null)
			this.profileIds = new ArrayList();
		
		profileIds.addAll(profileIds);
	}

    public String getComponentId() { return m_componentId; }
    public void setComponentId(String componentId) { m_componentId = componentId; }

    public String getDomainId() { return m_domainId; }
    public void setDomainId(String domainId) { m_domainId = domainId; }
    
    public String getParameter(String name)
    {
    	return null;
    }

    static public String[] getDistinctUserIds(String[] selectedUsers, String[] selectedGroups)
    {
		int     g, u;
		HashSet usersSet = new HashSet();
        UserDetail[] groupUsers;

		if ((selectedUsers != null) && (selectedUsers.length > 0))
		{
            for (u = 0; u < selectedUsers.length; u++)
            {
                usersSet.add(selectedUsers[u]);
            }
        }
		if ((selectedGroups != null) && (selectedGroups.length > 0))
		{
			for (g = 0; g < selectedGroups.length; g++)
			{
				groupUsers = m_oc.getAllUsersOfGroup(selectedGroups[g]);
				for (u = 0; u < groupUsers.length; u++)
				{
					usersSet.add(groupUsers[u].getId());
				}
			}
		}
		return (String[]) usersSet.toArray(new String[0]);
 	}

    static public UserDetail[] getUserDetails(String[] userIds)
    {
		return m_oc.getUserDetails(userIds);
    }

    static public Group[] getGroups(String[] groupIds)
    {
		if ((groupIds != null) && (groupIds.length > 0))
		{
            Group[] valret = new Group[groupIds.length];
			for (int g = 0; g < groupIds.length; g++)
			{
				valret[g] = m_oc.getGroup(groupIds[g]);
			}
            return valret;
		}
		else
        {
            return new Group[0];
        }
    }

    static public String[] getUserIds(UserDetail[] users)
    {
        String[] valret;

        if (users == null)
        {
            return new String[0];
        }
        valret = new String[users.length];
        for (int i = 0; i < users.length; i++ )
        {
            valret[i] = users[i].getId();
        }
		return valret;
 	}

    static public String[] getGroupIds(Group[] groups)
    {
        String[] valret;

        if (groups == null)
        {
            return new String[0];
        }
        valret = new String[groups.length];
        for (int i = 0; i < groups.length; i++ )
        {
            valret[i] = groups[i].getId();
        }
		return valret;
 	}
}
