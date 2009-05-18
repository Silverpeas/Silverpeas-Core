package com.stratelia.webactiv.beans.admin;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.exception.SilverpeasException;

public class UserSetManager 
{
	/**
	 * Constructor
	 */
	public UserSetManager() 
	{
	}

    public int getAllSubUsersNumber(DomainDriverManager ddManager, String setType, String setId) throws AdminException
    {
        try
        {
            SilverTrace.info("admin", "UserSetManager.getAllSubUsersNumber()", "root.MSG_GEN_ENTER_METHOD", "setType="+setType+", setId="+setId);
            ddManager.getOrganizationSchema();
            return ddManager.organization.userSet.getSubUserNumber(setType, idAsInt(setId));
        }
        catch(Exception e)
        {
			throw new AdminException("UserSetManager.getAllSubUsersNumber", SilverpeasException.ERROR, "admin.EX_ERR_GET_USERSET_NUMBER", "setType="+setType+", setId="+setId, e);
        }
		finally
		{
			ddManager.releaseOrganizationSchema();
		}
    }

    /**
	 * Delete all userset relations
	 */
    public void resetAll(DomainDriverManager ddManager) throws AdminException
    {
        try
        {
	        SilverTrace.info("admin", "UserSetManager.resetAll", "admin.MSG_INFO_RESET_USERSET_TABLES");
            ddManager.organization.userSet.resetAll();
        }
        catch(Exception e)
        {
			throw new AdminException("UserSetManager.resetAll", SilverpeasException.ERROR, "admin.EX_ERR_RESET_USERSET_TABLES", e);
        }
    }

    /**
	* Add a new userSet record in database
	 */
    public void addUserSet(DomainDriverManager ddManager, String userSetType, String userSetId) throws AdminException
    {
        try
        {
            ddManager.organization.userSet.createUserSet(userSetType, idAsInt(userSetId));
        }
        catch(Exception e)
        {
			throw new AdminException("UserSetManager.addUserSet", SilverpeasException.ERROR, "admin.EX_ERR_ADD_USERSET", "userset type: '" + userSetType + "', userSetId: '" + userSetId + "'", e);
        }
    }

    /**
	* Add a new userSet-userSet relation record in database
	 */
    public void addUserSetRelation(DomainDriverManager ddManager, String superSetType, String superSetId, String subSetType, String subSetId) throws AdminException
    {
        try
        {
            ddManager.organization.userSet.addUserSetInUserSet(subSetType, idAsInt(subSetId), superSetType, idAsInt(superSetId));
        }
        catch(Exception e)
        {
			throw new AdminException("UserSetManager.addUserSetRelation", SilverpeasException.ERROR, "admin.EX_ERR_ADD_USERSET_USERSET_RELATION", "relation : " + superSetType + superSetId + " --> " + subSetType + subSetId, e);
        }
    }

    /**
	* Add a new userSet-user relation record in database
	 */
    public void addUserRelation(DomainDriverManager ddManager, String superSetType, String superSetId, String userId) throws AdminException
    {
        try
        {
            ddManager.organization.userSet.addUserInUserSet(idAsInt(userId), superSetType, idAsInt(superSetId));
        }
        catch(Exception e)
        {
			throw new AdminException("UserSetManager.addUserRelation", SilverpeasException.ERROR, "admin.EX_ERR_ADD_USERSET_USER_RELATION", "relation : " + superSetType + superSetId + " --> user id '" + userId + "'", e);
        }
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
}