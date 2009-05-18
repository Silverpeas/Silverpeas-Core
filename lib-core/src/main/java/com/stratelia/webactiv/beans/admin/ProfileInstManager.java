/** 
 *
 * @author  nchaix
 * @version 
 */


package com.stratelia.webactiv.beans.admin;
import java.util.ArrayList;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.organization.ComponentInstanceRow;
import com.stratelia.webactiv.organization.UserRoleRow;
import com.stratelia.webactiv.util.exception.SilverpeasException;

public class ProfileInstManager extends Object
{
	/**
	 * Constructor
	 */
    public ProfileInstManager() 
    {
    }
    
	/**	
	 * Create a new Profile instance in database
	 */
    public String createProfileInst(ProfileInst profileInst, DomainDriverManager ddManager, String sFatherCompoId) throws AdminException
    {
        try
        {
			// Create the spaceProfile node
            UserRoleRow newRole = makeUserRoleRow(profileInst);
            newRole.instanceId = idAsInt(sFatherCompoId);
			ddManager.organization.userRole.createUserRole(newRole);
            String sProfileNodeId = idAsString(newRole.id);

			// Update the CSpace with the links TProfile-TGroup
            for(int nI = 0; nI < profileInst.getNumGroup(); nI++)
				ddManager.organization.userRole.addGroupInUserRole(idAsInt(profileInst.getGroup(nI)), idAsInt(sProfileNodeId) );

            // Update the CSpace with the links TProfile-TUser
            for(int nI = 0; nI < profileInst.getNumUser(); nI++)
				ddManager.organization.userRole.addUserInUserRole(idAsInt(profileInst.getUser(nI)), idAsInt(sProfileNodeId) );

            return sProfileNodeId;
        }
        catch(Exception  e)
        {
			throw new AdminException("ProfileInstManager.createProfileInst", SilverpeasException.ERROR, "admin.EX_ERR_ADD_PROFILE", "profile name: '" + profileInst.getName() + "', father component Id: '" + sFatherCompoId + "'", e);
        }
    }

    /**
	 * Get Profileinformation from database with the given id 
	 * and creates a new Profile instance
	 */
    public ProfileInst getProfileInst(DomainDriverManager ddManager,String sProfileId, String sFatherId) throws AdminException
    {
        if(sFatherId == null)
        {
            try
            {
				ddManager.getOrganizationSchema();
				ComponentInstanceRow instance = ddManager.organization.instance.getComponentInstanceOfUserRole(idAsInt(sProfileId));
				if (instance == null) instance = new ComponentInstanceRow();
				sFatherId = idAsString(instance.id);
            }
            catch(Exception e)
            {
				throw new AdminException("ProfileInstManager.getProfileInst", SilverpeasException.ERROR, "admin.EX_ERR_GET_PROFILE", "profile Id: '" + sProfileId + "', father component Id: '" + sFatherId + "'", e);
            }
			finally
			{
				ddManager.releaseOrganizationSchema();
			}
        }

		ProfileInst profileInst = new ProfileInst();
        setProfileInst(profileInst, ddManager, sProfileId, sFatherId);		
        
        return profileInst;
    }

    /**
	 * Set Profile information with the given id 
	 */
    public void setProfileInst(ProfileInst profileInst, DomainDriverManager ddManager, String sProfileId, String sFatherId) throws AdminException
    {
        try
        {
			ddManager.getOrganizationSchema();

			// Load the profile detail
			UserRoleRow userRole = ddManager.organization.userRole.getUserRole(idAsInt(sProfileId));
			
			if (userRole != null)
			{
	            // Set the attributes of the profile Inst
	            profileInst.setId(sProfileId);
	            profileInst.setName(userRole.roleName);
	            profileInst.setLabel(userRole.name);
	            profileInst.setDescription(userRole.description);
				profileInst.setComponentFatherId(sFatherId);
				if (userRole.isInherited == 1)
					profileInst.setInherited(true);
				profileInst.setObjectId(userRole.objectId);
	            
	            // Get the groups
	            String[] asGroupIds = ddManager.organization.group.getDirectGroupIdsInUserRole(idAsInt(sProfileId));
	
				// Set the groups to the profile
	            for(int nI=0; asGroupIds != null && nI < asGroupIds.length; nI++)
	            	profileInst.addGroup(asGroupIds[nI]);
	
	            // Get the Users
	            String[] asUsersIds = ddManager.organization.user.getDirectUserIdsOfUserRole(idAsInt(sProfileId));
	            
	            // Set the Users to the profile
	            for(int nI=0; asUsersIds != null && nI < asUsersIds.length; nI++)
	            	profileInst.addUser(asUsersIds[nI]);
			}
			else
			{
				SilverTrace.error("admin", "ProfileInstManager.setProfileInst", "root.EX_RECORD_NOT_FOUND", "sProfileId = "+sProfileId);
			}
        }
        catch(Exception e)
        {
			throw new AdminException("ProfileInstManager.setProfileInst", SilverpeasException.ERROR, "admin.EX_ERR_SET_PROFILE", "profile Id: '" + sProfileId + "', father component Id: '" + sFatherId + "'", e);
        }
		finally
		{
			ddManager.releaseOrganizationSchema();
		}
    }

	/**
	 * Deletes profile instance from Silverpeas
	 */
    public void deleteProfileInst(ProfileInst profileInst, DomainDriverManager ddManager) throws AdminException
    {
        try
        {
            // delete the node link Profile_Group
            for(int nI = 0; nI < profileInst.getNumGroup(); nI++)
				ddManager.organization.userRole.removeGroupFromUserRole(idAsInt(profileInst.getGroup(nI)), idAsInt(profileInst.getId()));				

            // delete the node link Profile_User
            for(int nI = 0; nI < profileInst.getNumUser(); nI++)
				ddManager.organization.userRole.removeUserFromUserRole(idAsInt(profileInst.getUser(nI)), idAsInt(profileInst.getId()));

            // delete the profile node
			ddManager.organization.userRole.removeUserRole(idAsInt(profileInst.getId()));
        }
        catch(Exception  e)
        {
			throw new AdminException("ProfileInstManager.deleteProfileInst", SilverpeasException.ERROR, "admin.EX_ERR_DELETE_PROFILE", "profile Id: '" + profileInst.getId() + "'", e);
        }
    }

    public String updateProfileInst(ProfileInst profileInst, DomainDriverManager ddManager, ProfileInst profileInstNew) throws AdminException
    {
        ArrayList alOldProfileGroup  = new ArrayList();
        ArrayList alNewProfileGroup  = new ArrayList();
        ArrayList alAddGroup  = new ArrayList();
        ArrayList alRemGroup  = new ArrayList();
        ArrayList alStayGroup = new ArrayList();
        ArrayList alOldProfileUser  = new ArrayList();
        ArrayList alNewProfileUser  = new ArrayList();
        ArrayList alAddUser  = new ArrayList();
        ArrayList alRemUser  = new ArrayList();
        ArrayList alStayUser = new ArrayList();

        try
        {
            // Compute the Old profile group list
            ArrayList alGroup = profileInst.getAllGroups();
            for(int nI =0; nI < alGroup.size(); nI++)
                alOldProfileGroup.add((String)alGroup.get(nI));

            // Compute the New profile group list
            alGroup = profileInstNew.getAllGroups();
            for(int nI =0; nI < alGroup.size(); nI++)
                alNewProfileGroup.add((String)alGroup.get(nI));

            // Compute the remove group list
            for(int nI = 0; nI < alOldProfileGroup.size(); nI++)
                if(alNewProfileGroup.indexOf(alOldProfileGroup.get(nI)) == -1)
                    alRemGroup.add(alOldProfileGroup.get(nI));

            // Compute the add and stay group list
            for(int nI = 0; nI < alNewProfileGroup.size(); nI++)
                if(alOldProfileGroup.indexOf(alNewProfileGroup.get(nI)) == -1)
                    alAddGroup.add(alNewProfileGroup.get(nI));
                else
                    alStayGroup.add(alNewProfileGroup.get(nI));

            // Add the new Groups
            for(int nI = 0; nI < alAddGroup.size(); nI++)
            {
                // Create the links between the profile and the group
				ddManager.organization.userRole.addGroupInUserRole(idAsInt((String)alAddGroup.get(nI)), idAsInt(profileInst.getId()));
            }

            // Remove the removed groups
            for(int nI = 0; nI < alRemGroup.size(); nI++)
            {
                // delete the node link Profile_Group
				ddManager.organization.userRole.removeGroupFromUserRole(idAsInt((String)alRemGroup.get(nI)), idAsInt(profileInst.getId()));
            }
            
            // Compute the Old profile User list
            ArrayList alUser = profileInst.getAllUsers();
            for(int nI =0; nI < alUser.size(); nI++)
                alOldProfileUser.add((String)alUser.get(nI));

            // Compute the New profile User list
            alUser = profileInstNew.getAllUsers();
            for(int nI =0; nI < alUser.size(); nI++)
                alNewProfileUser.add((String)alUser.get(nI));

            // Compute the remove User list
            for(int nI = 0; nI < alOldProfileUser.size(); nI++)
                if(alNewProfileUser.indexOf(alOldProfileUser.get(nI)) == -1)
                    alRemUser.add(alOldProfileUser.get(nI));

            // Compute the add and stay User list
            for(int nI = 0; nI < alNewProfileUser.size(); nI++)
                if(alOldProfileUser.indexOf(alNewProfileUser.get(nI)) == -1)
                    alAddUser.add(alNewProfileUser.get(nI));
                else
                    alStayUser.add(alNewProfileUser.get(nI));

            // Add the new Users
            for(int nI = 0; nI < alAddUser.size(); nI++)
            {
                // Create the links between the profile and the User
				ddManager.organization.userRole.addUserInUserRole(idAsInt((String)alAddUser.get(nI)), idAsInt(profileInst.getId()));
            }

            // Remove the removed Users
            for(int nI = 0; nI < alRemUser.size(); nI++)
            {
                // delete the node link Profile_User
				ddManager.organization.userRole.removeUserFromUserRole(idAsInt((String)alRemUser.get(nI)), idAsInt(profileInst.getId()));
            }

            // update the profile node
            UserRoleRow changedUserRole = makeUserRoleRow(profileInstNew);
            changedUserRole.id = idAsInt(profileInstNew.getId());
            ddManager.organization.userRole.updateUserRole(changedUserRole);

            return idAsString(changedUserRole.id);
        }
        catch(Exception  e)
        {
			throw new AdminException("ProfileInstManager.updateProfileInst", SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_PROFILE", "profile Id: '" + profileInst.getId() + "'", e);
        }
    }

	/**
	 * Get all the profiles Id for the given user
	 */
	public String[] getProfileIdsOfUser(DomainDriverManager ddManager, String sUserId) throws AdminException
	{
		try
		{
			ddManager.getOrganizationSchema();
			return ddManager.organization.userRole.getAllUserRoleIdsOfUser(idAsInt(sUserId));
        }
        catch(Exception  e)
        {
			throw new AdminException("ProfileInstManager.getProfileIdsOfUser", SilverpeasException.ERROR, "admin.EX_ERR_GET_USER_PROFILES", "user Id: '" + sUserId + "'", e);
        }
		finally
		{
			ddManager.releaseOrganizationSchema();
		}
	}

	/**
	 * Get all the profiles Id for the given group
	 */
	public String[] getProfileIdsOfGroup(DomainDriverManager ddManager, String sGroupId) throws AdminException
	{
		try
		{
			ddManager.getOrganizationSchema();
			return ddManager.organization.userRole.getAllUserRoleIdsOfGroup(idAsInt(sGroupId));
        }
        catch(Exception  e)
        {
			throw new AdminException("ProfileInstManager.getProfileIdsOfGroup", SilverpeasException.ERROR, "admin.EX_ERR_GET_GROUP_PROFILES", "group Id: '" + sGroupId + "'", e);
        }
		finally
		{
			ddManager.releaseOrganizationSchema();
		}
	}

	/**
	 * Converts ProfileInst to UserRoleRow
	 */
    private UserRoleRow makeUserRoleRow(ProfileInst profileInst)
    {
		UserRoleRow userRole = new UserRoleRow();

		userRole.id				= idAsInt(profileInst.getId());
		userRole.roleName		= profileInst.getName();
		userRole.name			= profileInst.getLabel();
		userRole.description	= profileInst.getDescription();
		if (profileInst.isInherited())
			userRole.isInherited = 1;
		userRole.objectId		= profileInst.getObjectId();
		userRole.objectType		= profileInst.getObjectType();

		return userRole;
    }

	/**
   	 * Convert String Id to int Id
	 */
    private int idAsInt(String id)
    {
       if (id == null || id.length() == 0) return -1; //the null id.

       try
       {
           return Integer.parseInt(id);
       }
       catch (NumberFormatException e)
       {
           return -1; // the null id.
       }
    }

	/**
   	 * Convert int Id to String Id
	 */
    static private String idAsString(int id)
    {
       return Integer.toString(id);
    }
}