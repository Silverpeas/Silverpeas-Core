package com.stratelia.webactiv.beans.admin;

import java.util.ArrayList;
import java.util.List;

import com.stratelia.webactiv.organization.UserRoleRow;
import com.stratelia.webactiv.util.exception.SilverpeasException;

public class ProfiledObjectManager
{
    static ProfileInstManager m_ProfileInstManager = new ProfileInstManager();

	/**
	 * Constructor
	 */
	public ProfiledObjectManager()
	{
	}
	
	public List getProfiles(DomainDriverManager ddManager, int objectId, String objectType, int componentId) throws AdminException
	{
		List profiles = new ArrayList();
		
		String[] asProfileIds = null;
		try
		{
			ddManager.getOrganizationSchema();
			//Get the profiles
			asProfileIds = ddManager.organization.userRole.getAllUserRoleIdsOfObject(objectId, objectType, componentId);
		}
		catch (Exception e)
		{
			throw new AdminException("ProfiledObjectManager.getProfiles", SilverpeasException.ERROR, "admin.EX_ERR_GET_PROFILE", "objectId = " + objectId + ", componentId = " + componentId, e);
		}
		finally
		{
			ddManager.releaseOrganizationSchema();
		}		

        //Insert the profileInst in the componentInst
        for (int nI=0; asProfileIds != null && nI<asProfileIds.length; nI++)
        {
            ProfileInst profileInst = m_ProfileInstManager.getProfileInst(ddManager, asProfileIds[nI], Integer.toString(componentId));
            profileInst.setObjectType(objectType);
            profiles.add(profileInst);
        }
        
        return profiles;
	}
	
	public String[] getUserProfileNames(DomainDriverManager ddManager, int objectId, String objectType, int componentId, int userId) throws AdminException
	{
		String[] profileNames = null;
		try
		{
			ddManager.getOrganizationSchema();
			//Get the profiles
			UserRoleRow[] roles = ddManager.organization.userRole.getRolesOfUserAndObject(objectId, objectType, componentId, userId);
			
			profileNames = new String[roles.length];
			for (int r=0; r<roles.length; r++)
			{
				profileNames[r] = roles[r].roleName;
			}
		}
		catch (Exception e)
		{
			throw new AdminException("ProfiledObjectManager.getUserProfileNames", SilverpeasException.ERROR, "admin.EX_ERR_GET_PROFILE", "objectId = " + objectId + ", componentId = " + componentId+ ", userId = " + userId, e);
		}
		finally
		{
			ddManager.releaseOrganizationSchema();
		}
        return profileNames;
	}
	
	public boolean isObjectAvailable(DomainDriverManager ddManager, int userId, int objectId, String objectType, int componentId) throws AdminException
    {
    	try
		{
			ddManager.getOrganizationSchema();
			return ddManager.organization.userRole.isObjectAvailable(userId, componentId, objectId, objectType);
		}
		catch(Exception e)
		{
  		    throw new AdminException("ComponentInstManager.isComponentAvailable", SilverpeasException.ERROR, "admin.EX_ERR_GET_USER_AVAILABLE_COMPONENT_IDS", "userId = " + userId + ", componentId = "+componentId+", objectId = "+objectId, e);
		}
		finally
		{
			ddManager.releaseOrganizationSchema();
		}
    }
	
	public List getProfiles(DomainDriverManager ddManager, int componentId) throws AdminException
	{
		List profiles = new ArrayList();
		
		String[] asProfileIds = null;
		try
		{
			ddManager.getOrganizationSchema();
			//Get the profiles
			asProfileIds = ddManager.organization.userRole.getAllObjectUserRoleIdsOfInstance(componentId);
		}
		catch (Exception e)
		{
			throw new AdminException("ProfiledObjectManager.getProfiles", SilverpeasException.ERROR, "admin.EX_ERR_GET_PROFILE", "componentId = " + componentId, e);
		}
		finally
		{
			ddManager.releaseOrganizationSchema();
		}		

        for (int nI=0; asProfileIds != null && nI<asProfileIds.length; nI++)
        {
            ProfileInst profileInst = m_ProfileInstManager.getProfileInst(ddManager, asProfileIds[nI], Integer.toString(componentId));
            //profileInst.setObjectType(objectType);
            profiles.add(profileInst);
        }
        
        return profiles;
	}
}