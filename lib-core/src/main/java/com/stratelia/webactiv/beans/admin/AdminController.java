/*
 * @author Norbert CHAIX
 * @version 1.0
  date 14/09/2000
*/
package com.stratelia.webactiv.beans.admin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.spaceTemplates.SpaceTemplateProfile;

/*
This objet is used by all the admin jsp such as SpaceManagement, UserManagement, etc...
It provides access functions to query and modify the domains as well as the company organization
It should be used only by a client that has the administrator rights
*/

public class AdminController extends AdminReference implements java.io.Serializable
{
    String m_UserId = null;

    public AdminController(String sUserId)
    {
        m_UserId = sUserId;
    }

    // Start the processes

    public void startServer() throws Exception
    {
        m_Admin.startServer();
    }

    //----------------------------------------------
    //        Space Instances related functions
    //----------------------------------------------

    public String getGeneralSpaceId()
    {
		SilverTrace.info("admin", "AdminController.getGeneralSpaceId", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.getGeneralSpaceId();
        }
        catch(Exception e)
        {
			SilverTrace.fatal("admin", "AdminController.getGeneralSpaceId", "admin.MSG_FATAL_GET_GENERAL_SPACE_ID", e);
            return "";
        }
    }

    /* Return true if the given space name exists */
    public boolean isSpaceInstExist(String sClientSpaceId)
    {
		SilverTrace.info("admin", "AdminController.isSpaceInstExist", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.isSpaceInstExist(sClientSpaceId);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.isSpaceInstExist", "admin.MSG_ERR_IS_SPACE_EXIST", e);
            return false;
        }
    }

    /** Return the space Instance corresponding to the given space id */
    public SpaceInst getSpaceInstById(String sSpaceId)
    {
		SilverTrace.info("admin", "AdminController.getSpaceInstById", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.getSpaceInstById(sSpaceId);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getSpaceInstById", "admin.MSG_ERR_GET_SPACE", e);
            return null;
        }
    }

	public SpaceInstLight getSpaceInstLight(String sSpaceId)
    {
		SilverTrace.info("admin", "AdminController.getSpaceInstLight", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.getSpaceInstLightById(sSpaceId);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getSpaceInstLight", "admin.MSG_ERR_GET_SPACE", e);
            return null;
        }
    }

	public Hashtable getTreeView(String userId, String spaceId)
	{
		SilverTrace.info("admin", "AdminController.getTreeView", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.getTreeView(userId, spaceId);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getTreeView", "admin.MSG_ERR_GET_SPACE", e);
            return null;
        }
    }
	
	public List getPathToComponent(String componentId)
	{
		SilverTrace.info("admin", "AdminController.getPathToComponent", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.getPathToComponent(componentId);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getPathToComponent", "admin.MSG_ERR_GET_SPACE", e);
            return null;
        }
	}
	
	public List getPathToSpace(String spaceId, boolean includeTarget)
	{
		SilverTrace.info("admin", "AdminController.getPathToSpace", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.getPathToSpace(spaceId, includeTarget);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getPathToSpace", "admin.MSG_ERR_GET_SPACE", e);
            return null;
        }
	}

      /** Return the space Instance corresponding to the given space id */
    public String[] getUserManageableSpaceRootIds(String sUserId)
    {
		SilverTrace.info("admin", "AdminController.getUserManageableSpaceRootIds", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.getUserManageableSpaceRootIds(sUserId);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getUserManageableSpaceRootIds", "admin.MSG_ERR_GET_SPACE", e);
            return null;
        }
    }

      /** Return the space Instance corresponding to the given space id */
    public String[] getUserManageableSubSpaceIds(String sUserId, String sParentSpace)
    {
		SilverTrace.info("admin", "AdminController.getUserManageableSubSpaceIds", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.getUserManageableSubSpaceIds(sUserId, sParentSpace);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getUserManageableSubSpaceIds", "admin.MSG_ERR_GET_SPACE", e);
            return null;
        }
    }

      /** Return the space Instance corresponding to the given space id : FORMAT EX : 123 */
    public String[] getUserManageableSpaceIds(String sUserId)
    {
		SilverTrace.info("admin", "AdminController.getUserManageableSpaceIds", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.getUserManageableSpaceIds(sUserId);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getUserManageableSpaceIds", "admin.MSG_ERR_GET_SPACE", e);
            return null;
        }
    }

    /** Return the space Instance corresponding to the given space id : FORMAT EX : WA123 */
    /** If user is Admin, return all space Ids */
    public String[] getUserManageableSpaceClientIds(String sUserId)
    {
		SilverTrace.info("admin", "AdminController.getUserManageableSpaceIds", "root.MSG_GEN_ENTER_METHOD");
        try
        {
			UserDetail user = m_Admin.getUserDetail(sUserId);
			if (user.getAccessLevel().equals("A") || sUserId.equals("0"))
			{
				return m_Admin.getClientSpaceIds(m_Admin.getAllSpaceIds());
			}
			else
			{
	            return m_Admin.getClientSpaceIds(m_Admin.getUserManageableSpaceIds(sUserId));
			}
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getUserManageableSpaceClientIds", "admin.MSG_ERR_GET_SPACE", e);
            return null;
        }
    }

    /** Add the given space Instance */
    public String addSpaceInst(SpaceInst spaceInst)
    {
		SilverTrace.info("admin", "AdminController.addSpaceInst", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.addSpaceInst(m_UserId, spaceInst);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.addSpaceInst", "admin.MSG_ERR_ADD_SPACE", e);
		    return "";
        }
    }
    
    /** Delete the space Instance corresponding to the given space id */
    public String deleteSpaceInstById(String sSpaceInstId, boolean definitive)
    {
		SilverTrace.info("admin", "AdminController.deleteSpaceInstById", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.deleteSpaceInstById(m_UserId, sSpaceInstId, definitive);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.deleteSpaceInstById", "admin.MSG_ERR_DELETE_SPACE", e);
            return "";
        }
    }

	/** Update the space Instance corresponding to the given space name  wuth the given SpaceInst*/
    public String updateSpaceInst(SpaceInst spaceInstNew)
    {
		SilverTrace.info("admin", "AdminController.updateSpaceInst", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.updateSpaceInst(spaceInstNew);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.updateSpaceInst", "admin.MSG_ERR_UPDATE_SPACE", e);
            return "";
        }
    }
    
    public Hashtable getAllSpaceTemplates()
    {
        return m_Admin.getAllSpaceTemplates();
    }
    
    public SpaceTemplateProfile[] getTemplateProfiles(String templateName)
    {
        return m_Admin.getTemplateProfiles(templateName);
    }
    
    public SpaceInst getSpaceInstFromTemplate(String templateName)
    {
        return m_Admin.getSpaceInstFromTemplate(templateName);
    }

    /** Return all the spaces Id available in webactiv */
    public String[] getAllRootSpaceIds()
    {
		SilverTrace.info("admin", "AdminController.getAllSpaceIds", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.getAllRootSpaceIds();
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getAllSpaceIds", "admin.MSG_ERR_GET_ALL_SPACE_IDS", e);
            return new String[0];
        }
    }

    /** Return all the spaces Id available in webactiv */
    public String[] getAllSpaceIds()
    {
		SilverTrace.info("admin", "AdminController.getAllSpaceIds", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.getAllSpaceIds();
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getAllSpaceIds", "admin.MSG_ERR_GET_ALL_SPACE_IDS", e);
            return new String[0];
        }
    }

	/** Return all the spaces Id available for the given userId */
    public String[] getAllSpaceIds(String userId)
    {
		SilverTrace.info("admin", "AdminController.getAllSpaceIds", "root.MSG_GEN_ENTER_METHOD", "userId = "+userId);
        try
        {
            return m_Admin.getAllSpaceIds(userId);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getAllSpaceIds", "admin.MSG_ERR_GET_ALL_SPACE_IDS", e);
            return new String[0];
        }
    }

    /** Return all the sub spaces Id available in webactiv given the fatherDomainId */
    public String[] getAllSubSpaceIds(String sDomainFatherId)
    {
		SilverTrace.info("admin", "AdminController.getAllSubSpaceIds", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.getAllSubSpaceIds(sDomainFatherId);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getAllSubSpaceIds", "admin.MSG_ERR_GET_SUBSPACE_IDS", e);
            return new String[0];
        }
    }

	/** Return all the sub spaces Id available for the given user and the given fatherDomainId */
    public String[] getAllSubSpaceIds(String sDomainFatherId, String userId)
    {
		SilverTrace.info("admin", "AdminController.getAllSubSpaceIds", "root.MSG_GEN_ENTER_METHOD", "sDomainFatherId = "+sDomainFatherId+", userId = "+userId);
        try
        {
            return m_Admin.getAllSubSpaceIds(sDomainFatherId, userId);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getAllSubSpaceIds", "admin.MSG_ERR_GET_SUBSPACE_IDS", e);
            return new String[0];
        }
    }

    /** Return the the spaces name corresponding to the given space ids */
    public String[] getSpaceNames(String[] asSpaceIds)
    {
		SilverTrace.info("admin", "AdminController.getSpaceNames", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.getSpaceNames(asSpaceIds);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getSpaceNames", "admin.MSG_ERR_GET_SPACE_NAMES", e);
            return new String[0];
        }
    }

    public void updateSpaceOrderNum(String sSpaceId, int orderNum)
    {
		SilverTrace.info("admin", "AdminController.updateSpaceOrderNum", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            m_Admin.updateSpaceOrderNum(sSpaceId, orderNum);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.updateSpaceOrderNum", "admin.MSG_ERR_UPDATE_SPACE", e);
        }
    }
       
    //----------------------------------------------
    //        Component Instances related functions
    //----------------------------------------------

    /** Return all the components names available in webactiv */
    public Hashtable getAllComponentsNames()
    {
		SilverTrace.info("admin", "AdminController.getAllComponentsNames", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.getAllComponentsNames();
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getAllComponentsNames", "admin.MSG_ERR_GET_ALL_COMPONENT_NAMES", e);
            return new Hashtable();
        }
    }

	/** Return all the components of silverpeas read in the xmlComponent directory */
    public Hashtable getAllComponents()
    {
		SilverTrace.info("admin", "AdminController.getAllComponents", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.getAllComponents();
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getAllComponents", "admin.MSG_ERR_GET_ALL_COMPONENTS", e);
            return new Hashtable();
        }
    }

	/** Return the component Instance corresponding to the given component id */
    public ComponentInst getComponentInst(String sComponentId)
    {
		SilverTrace.info("admin", "AdminController.getComponentInst", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.getComponentInst(sComponentId);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getComponentInst", "admin.MSG_ERR_GET_COMPONENT", e);
            return null;
        }
    }
    
    public ComponentInstLight getComponentInstLight(String sComponentId)
    {
    	SilverTrace.info("admin", "AdminController.getComponentInstLight", "root.MSG_GEN_ENTER_METHOD");
    	try
        {
            return m_Admin.getComponentInstLight(sComponentId);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getComponentInstLight", "admin.MSG_ERR_GET_COMPONENT", e);
            return null;
        }
    }

	/** Add the given component Instance */
    public String addComponentInst(ComponentInst componentInst)
    {
		SilverTrace.info("admin", "AdminController.addComponentInst", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.addComponentInst(m_UserId, componentInst);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.addComponentInst", "admin.MSG_ERR_ADD_COMPONENT", e);
	  	    return "";
        }
    }


    /** Delete the component Instance corresponding to the given component id */
    public String deleteComponentInst(String sComponentId, boolean definitive)
    {
		SilverTrace.info("admin", "AdminController.deleteComponentInst", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.deleteComponentInst(m_UserId, sComponentId, definitive);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.deleteComponentInst", "admin.MSG_ERR_DELETE_COMPONENT", e);
            return "";
        }
    }

    /** Update the component Instance corresponding to the given space component  with the given ComponentInst*/
    public String updateComponentInst(ComponentInst componentInst)
    {
		SilverTrace.info("admin", "AdminController.updateComponentInst", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.updateComponentInst(componentInst);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.updateComponentInst", "admin.MSG_ERR_UPDATE_COMPONENT", e);
            return "";
        }
    }

//NEWD DLE
	/** Move the component Instance in the given space with the given componentId*/
	public void moveComponentInst(String spaceId, String componentId, String idComponentBefore, ComponentInst[] componentInsts) throws AdminException
	{
		SilverTrace.info("admin", "AdminController.moveComponentInst", "root.MSG_GEN_ENTER_METHOD");
		m_Admin.moveComponentInst(spaceId, componentId, idComponentBefore, componentInsts);
	}
//	NEWF DLE

    /** Return the component ids available for the cuurent user Id in the given space id */
    public String[] getAvailCompoIds(String sClientSpaceId, String sUserId)
    {
		SilverTrace.info("admin", "AdminController.getAvailCompoIds", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.getAvailCompoIds(sClientSpaceId, sUserId);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getAvailCompoIds", "admin.MSG_ERR_GET_USER_AVAILABLE_COMPONENT_IDS", e);
            return new String[0];
        }
    }

	public boolean isComponentAvailable(String componentId, String userId)
	{
		SilverTrace.info("admin", "AdminController.isComponentAvailable", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.isComponentAvailable(componentId, userId);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.isComponentAvailable", "admin.MSG_ERR_GET_USER_AVAILABLE_COMPONENT", e);
            return false;
        }
	}

    public void updateComponentOrderNum(String sComponentId, int orderNum)
    {
		SilverTrace.info("admin", "AdminController.updateComponentOrderNum", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            m_Admin.updateComponentOrderNum(sComponentId, orderNum);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.updateComponentOrderNum", "admin.MSG_ERR_UPDATE_COMPONENT", e);
        }
    }
        
    //----------------------------------------------
    //        Space and Component Bin
    //----------------------------------------------
    public List getRemovedSpaces()
    {
    	try
    	{
    		return m_Admin.getRemovedSpaces();
    	}
    	catch (Exception e)
    	{
    		SilverTrace.error("admin", "AdminController.getRemovedSpaces", "admin.MSG_ERR_GET_REMOVED_SPACES", e);
    		return null;
    	}
    }
    public List getRemovedComponents()
    {
    	try
    	{
    		return m_Admin.getRemovedComponents();
    	}
    	catch (Exception e)
    	{
    		SilverTrace.error("admin", "AdminController.getRemovedComponents", "admin.MSG_ERR_GET_REMOVED_COMPONENTS", e);
    		return null;
    	}
    }
    
    public void restoreSpaceFromBasket(String spaceId)
    {
    	try {
			m_Admin.restoreSpaceFromBasket(spaceId);
		} catch (Exception e) {
			SilverTrace.error("admin", "AdminController.restoreSpaceFromBasket", "admin.MSG_ERR_GET_RESTORE_SPACE_FROM_BASKET", e);
		}
    }
    
    public void restoreComponentFromBasket(String componentId)
    {
    	try {
			m_Admin.restoreComponentFromBasket(componentId);
		} catch (Exception e) {
			SilverTrace.error("admin", "AdminController.restoreComponentFromBasket", "admin.MSG_ERR_GET_RESTORE_COMPONENT_FROM_BASKET", e);
		}
    }
    
	//----------------------------------------------
    //        Profile Instances related functions
    //----------------------------------------------

    /** Return all the profiles names available for the given profile */
    public String[] getAllProfilesNames(String sComponentName)
    {
		SilverTrace.info("admin", "AdminController.getAllProfilesNames", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.getAllProfilesNames(sComponentName);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getAllProfilesNames", "admin.MSG_ERR_GET_ALL_PROFILE_NAMES", e);
            return new String[0];
        }
    }

    /** Return the profile Instance corresponding to the given profile id */
    public ProfileInst getProfileInst(String sProfileId)
    {
		SilverTrace.info("admin", "AdminController.getProfileInst", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.getProfileInst(sProfileId);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getProfileInst", "admin.MSG_ERR_GET_PROFILE", e);
            return null;
        }
    }
    
    public List getProfilesByObject(String objectId, String objectType, String componentId)
    {
    	SilverTrace.info("admin", "AdminController.getProfilesByObject", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.getProfilesByObject(objectId, objectType, componentId);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getProfilesByObject", "admin.MSG_ERR_GET_PROFILE", e);
            return null;
        }
    }
    
    public String[] getProfilesByObjectAndUserId(int objectId, String objectType, String componentId, String userId)
    {
    	SilverTrace.info("admin", "AdminController.getProfilesByObjectAndUserId", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.getProfilesByObjectAndUserId(objectId, objectType, componentId, userId);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.isObjectAvailable", "admin.MSG_ERR_GET_PROFILE", e);
            return new String[0];
        }
    }
    
    public boolean isObjectAvailable(int objectId, String objectType, String componentId, String userId)
    {
    	SilverTrace.info("admin", "AdminController.isObjectAvailable", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.isObjectAvailable(componentId, objectId, objectType, userId);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.isObjectAvailable", "admin.MSG_ERR_GET_PROFILE", e);
            return false;
        }
    }

    public void setSpaceProfilesToComponent(ComponentInst component, SpaceInst space)
    {
    	SilverTrace.info("admin", "AdminController.setSpaceProfilesToComponent", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            m_Admin.setSpaceProfilesToComponent(component, space);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.setSpaceProfilesToComponent", "admin.MSG_ERR_GET_PROFILE", e);
        }
    }
    
    
    
	/** Add the given Profile Instance */
    public String addProfileInst(ProfileInst profileInst)
    {
    	return addProfileInst(profileInst, null);
    }
    
    public String addProfileInst(ProfileInst profileInst, String userId)
    {
		SilverTrace.info("admin", "AdminController.addProfileInst", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.addProfileInst(profileInst, userId);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.addProfileInst", "admin.MSG_ERR_ADD_PROFILE", e);
		    return "";
        }
    }

    /** Delete the Profile Instance corresponding to the given Profile id */
    public String deleteProfileInst(String sProfileId)
    {
    	return deleteProfileInst(sProfileId, null);
    }
    
    public String deleteProfileInst(String sProfileId, String userId)
    {
		SilverTrace.info("admin", "AdminController.deleteProfileInst", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.deleteProfileInst(sProfileId, userId);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.deleteProfileInst", "admin.MSG_ERR_DELETE_PROFILE", e);
            return "";
        }
    }

    /** Update the Profile Instance corresponding to the given space Profile  with the given ProfileInst*/
    public String updateProfileInst(ProfileInst profileInst)
    {
    	return updateProfileInst(profileInst, null);
    }
    
    public String updateProfileInst(ProfileInst profileInst, String userId)
    {
		SilverTrace.info("admin", "AdminController.updateProfileInst", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.updateProfileInst(profileInst, userId);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.updateProfileInst", "admin.MSG_ERR_UPDATE_PROFILE", e);
            return "";
        }
    }

	/**
	 * Get the profile label from its name
	 */
    public String getProfileLabelfromName(String sComponentName, String sProfileName)
    {
		SilverTrace.info("admin", "AdminController.getProfileLabelfromName", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.getProfileLabelfromName(sComponentName, sProfileName);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getProfileLabelfromName", "admin.MSG_ERR_GET_PROFILE_LABEL_FROM_NAME", "component name: " + sComponentName + ", profile name: " + sProfileName, e);
            return new String("");
        }
	}

	//----------------------------------------------
    //        User Profile related functions
    //----------------------------------------------

	//JCC 10/04/2002 ajout de getProfileIds
	/**
	 * All the profiles to which the user belongs
	 * @return an array of profile IDs
	 */
    public String[] getProfileIds(String sUserId)
    {
		SilverTrace.info("admin", "AdminController.getProfileIds", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.getProfileIds(sUserId);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getProfileIds", "admin.MSG_ERR_GET_USERPROFILE", e);
            return null;
        }
    }

	//----------------------------------------------
    //        Group Profile related functions
    //----------------------------------------------

	//JCC 10/04/2002 ajout de getProfileIdsOfGroup
	/**
	 * All the profiles to which the group belongs
	 * @return an array of profile IDs
	 */
    public String[] getProfileIdsOfGroup(String sGroupId)
    {
		SilverTrace.info("admin", "AdminController.getProfileIdsOfGroup", "root.MSG_GEN_ENTER_METHOD");
        try
        {
			return m_Admin.getProfileIdsOfGroup(sGroupId);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getProfileIdsOfGroup", "admin.MSG_ERR_GET_USERPROFILE", e);
            return null;
        }
    }

	//----------------------------------------------
    //        User related functions
    //----------------------------------------------

    public String[] getDirectGroupsIdsOfUser(String userId)
    {
		SilverTrace.info("admin", "AdminController.getDirectGroupsIdsOfUser", "root.MSG_GEN_ENTER_METHOD");
		try
		{
			return m_Admin.getDirectGroupsIdsOfUser(userId);
		}
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getDirectGroupsIdsOfUser", "admin.MSG_ERR_GET_DOMAIN", "user id: " + userId, e);
            return null;
        }
    }

    /**
	 * Add a new domain
	 */
    public String addDomain(Domain theDomain)
    {
		SilverTrace.info("admin", "AdminController.addDomain", "root.MSG_GEN_ENTER_METHOD");
		try
		{
			return m_Admin.addDomain(theDomain);
		}
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.addDomain", "admin.MSG_ERR_ADD_DOMAIN", e);
            return "";
        }
	 }

    /**
	 * update a domain
	 */
    public String updateDomain(Domain theDomain)
    {
		SilverTrace.info("admin", "AdminController.updateDomain", "root.MSG_GEN_ENTER_METHOD");
		try
		{
			return m_Admin.updateDomain(theDomain);
		}
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.updateDomain", "admin.EX_ERR_UPDATE_DOMAIN", e);
            return "";
        }
	 }

    /**
	 * Remove a domain
	 */
    public String removeDomain(String domainId)
    {
		SilverTrace.info("admin", "AdminController.removeDomain", "root.MSG_GEN_ENTER_METHOD");
		try
		{
			return m_Admin.removeDomain(domainId);
		}
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.removeDomain", "admin.MSG_ERR_DELETE_DOMAIN", e);
            return "";
        }
	 }

    /**
	 * Get a domain with given id
	 */
    public Domain getDomain(String domainId)
    {
		SilverTrace.info("admin", "AdminController.getDomain", "root.MSG_GEN_ENTER_METHOD");
		try
		{
			return m_Admin.getDomain(domainId);
		}
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getDomain", "admin.MSG_ERR_GET_DOMAIN", "domain id: " + domainId, e);
            return null;
        }
	 }

    /**
	 * Get a domain's possible actions
	 */
    public long getDomainActions(String domainId)
    {
		SilverTrace.info("admin", "AdminController.getDomainActions", "root.MSG_GEN_ENTER_METHOD", "domainID = " + domainId);
		try
		{
			return m_Admin.getDomainActions(domainId);
		}
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getDomainActions", "admin.MSG_ERR_GET_ALL_DOMAINS", e);
            return 0;
        }
    }

    /**
	 * Get ALL the domain's groups
	 */
    public Group[] getRootGroupsOfDomain(String domainId) 
    {
		SilverTrace.info("admin", "AdminController.getRootGroupsOfDomain", "root.MSG_GEN_ENTER_METHOD", "domainID = " + domainId);
		try
		{
			return m_Admin.getRootGroupsOfDomain(domainId);
		}
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getRootGroupsOfDomain", "admin.MSG_ERR_GET_ALL_DOMAINS", e);
            return new Group[0];
        }
    }
    
    /**
	 * Get ALL Group Ids for the domain's groups
	 */
    public String[] getRootGroupIdsOfDomain(String domainId)
    {
		SilverTrace.info("admin", "AdminController.getRootGroupIdsOfDomain", "root.MSG_GEN_ENTER_METHOD", "domainID = " + domainId);
		try
		{
			return m_Admin.getRootGroupIdsOfDomain(domainId);
		}
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getRootGroupIdsOfDomain", "admin.MSG_ERR_GET_ALL_DOMAINS", e);
            return new String[0];
        }
    }    

    /**
	 * Get ALL the users that are in a group or his sub groups
	 */
    public UserDetail[] getAllUsersOfGroup(String groupId)
    {
		SilverTrace.info("admin", "AdminController.getAllUsersOfGroup", "root.MSG_GEN_ENTER_METHOD", "groupId = " + groupId);
		try
		{
			return m_Admin.getAllUsersOfGroup(groupId);
		}
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getAllUsersOfGroup", "admin.MSG_ERR_GET_ALL_DOMAINS", e);
            return new UserDetail[0];
        }
    }

    /**
	 * Get ALL the domain's users
	 */
    public UserDetail[] getUsersOfDomain(String domainId)
    {
		SilverTrace.info("admin", "AdminController.getUsersOfDomain", "root.MSG_GEN_ENTER_METHOD", "domainID = " + domainId);
		try
		{
			return m_Admin.getUsersOfDomain(domainId);
		}
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getUsersOfDomain", "admin.MSG_ERR_GET_ALL_DOMAINS", e);
            return new UserDetail[0];
        }
    }
    
    /**
	 * Get ALL the userId of the domain
	 */
    public String[] getUserIdsOfDomain(String domainId)
    {
		SilverTrace.info("admin", "AdminController.getUserIdsOfDomain", "root.MSG_GEN_ENTER_METHOD", "domainID = " + domainId);
		try
		{
			return m_Admin.getUserIdsOfDomain(domainId);
		}
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getUserIdsOfDomain", "admin.MSG_ERR_GET_ALL_DOMAINS", e);
            return new String[0];
        }
    }
        

    /**
	 * Get number of the domain's users
	 */
    public int getUsersNumberOfDomain(String domainId)
    {
		SilverTrace.info("admin", "AdminController.getUsersNumberOfDomain", "root.MSG_GEN_ENTER_METHOD", "domainID = " + domainId);
		try
		{
			return m_Admin.getUsersNumberOfDomain(domainId);
		}
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getUsersNumberOfDomain", "admin.MSG_ERR_GET_ALL_DOMAINS", e);
            return 0;
        }
    }

    /**
	 * Get all domains declared in Silverpeas
	 */
	 public Domain[] getAllDomains()
	 {
		SilverTrace.info("admin", "AdminController.getAllDomains", "root.MSG_GEN_ENTER_METHOD");
		try
		{
			return m_Admin.getAllDomains();
		}
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getAllDomains", "admin.MSG_ERR_GET_ALL_DOMAINS", e);
            return null;
        }
	 }


	//----------------------------------------------
    //        Space Profile related functions
    //----------------------------------------------
    /** Return the space profile Instance corresponding to the given space profile id */
    public SpaceProfileInst getSpaceProfileInst(String sSpaceProfileId)
    {
		SilverTrace.info("admin", "AdminController.getSpaceProfileInst", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.getSpaceProfileInst(sSpaceProfileId);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getSpaceProfileInst", "admin.MSG_ERR_GET_SPACE_PROFILE", e);
            return null;
        }
    }

	/** Add the given Space Profile Instance */
    public String addSpaceProfileInst(SpaceProfileInst spaceProfileInst, String userId)
    {
		SilverTrace.info("admin", "AdminController.addSpaceProfileInst", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.addSpaceProfileInst(spaceProfileInst, userId);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.addSpaceProfileInst", "admin.MSG_ERR_ADD_SPACE_PROFILE", e);
		    return "";
        }
    }


    /** Delete the Space Profile Instance corresponding to the given Space Profile id */
    public String deleteSpaceProfileInst(String sSpaceProfileId, String userId)
    {
		SilverTrace.info("admin", "AdminController.deleteSpaceProfileInst", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.deleteSpaceProfileInst(sSpaceProfileId, userId);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.deleteSpaceProfileInst", "admin.MSG_ERR_DELETE_SPACE_PROFILE", e);
            return "";
        }
    }

    /** Update the Space Profile Instance corresponding to the given space Profile with the given SpaceProfileInst*/
    public String updateSpaceProfileInst(SpaceProfileInst spaceProfileInst, String userId)
    {
		SilverTrace.info("admin", "AdminController.updateSpaceProfileInst", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.updateSpaceProfileInst(spaceProfileInst, userId);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.updateSpaceProfileInst", "admin.MSG_ERR_UPDATE_SPACE_PROFILE", e);
            return "";
        }
    }

	//----------------------------------------------
    //        Groups related functions
    //----------------------------------------------

    /** Return all the groups ids available in webactiv */
    public String[] getAllGroupsIds()
    {
		SilverTrace.info("admin", "AdminController.getAllGroupsIds", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.getAllGroupIds();
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getAllGroupIds", "admin.MSG_ERR_GET_ALL_GROUP_IDS", e);
            return new String[0];
        }
    }

    /**
	 * @return all the root groups ids available
	 */
    public String[] getAllRootGroupIds()
    {
		SilverTrace.info("admin", "AdminController.getAllRootGroupsIds", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.getAllRootGroupIds();
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getAllRootGroupsIds", "admin.MSG_ERR_GET_ALL_GROUP_IDS", e);
            return new String[0];
        }
    }

    /**
	 * @return all the direct subgroups ids of a given group
	 */
    public String[] getAllSubGroupIds(String groupId)
    {
		SilverTrace.info("admin", "AdminController.getDirectSubgroupIds", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.getAllSubGroupIds(groupId);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getDirectSubgroupIds", "admin.MSG_ERR_GET_ALL_GROUP_IDS", e);
            return new String[0];
        }
    }
    
    /**
	 * @return all subgroups ids of a given group
	 */
   public String[] getAllSubGroupIdsRecursively(String groupId)
   {
	   SilverTrace.info("admin", "AdminController.getAllSubGroupIdsRecursively", "root.MSG_GEN_ENTER_METHOD");
       try
       {
           return m_Admin.getAllSubGroupIdsRecursively(groupId);
       }
       catch(Exception e)
       {
    	   SilverTrace.error("admin", "AdminController.getAllSubGroupIdsRecursively", "admin.MSG_ERR_GET_ALL_GROUP_IDS", e);
           return new String[0];
       }
   }

    /** Return all the group names corresponding to the given group Ids*/
    public String[] getGroupNames(String[] asGroupIds)
    {
		SilverTrace.info("admin", "AdminController.getGroupNames", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.getGroupNames(asGroupIds);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getGroupNames", "admin.MSG_ERR_GET_GROUP_NAMES", e);
            return new String[0];
        }
    }

    /** Return the group name corresponding to the given group Id*/
    public String getGroupName(String sGroupId)
    {
		SilverTrace.info("admin", "AdminController.getGroupName", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.getGroupName(sGroupId);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getGroupName", "admin.MSG_ERR_GET_GROUP_NAME", "group id: " + sGroupId, e);
            return "";
        }
    }

    /** Return all the user ids available in webactiv */
    public String[] getAllUsersIds()
    {
		SilverTrace.info("admin", "AdminController.getAllUsersIds", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.getAllUsersIds();
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getAllUsersIds", "admin.MSG_ERR_GET_ALL_USER_IDS", e);
            return null;
        }
    }

	/**
	 * The spaces that can be managed by the given group
	 * @return the array of space IDs
	 */
    public String[] getGroupManageableSpaceIds(String sGroupId)
    {
		SilverTrace.info("admin", "AdminController.getGroupManageableSpaceIds", "root.MSG_GEN_ENTER_METHOD");
        try
        {
			return m_Admin.getGroupManageableSpaceIds(sGroupId);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getGroupManageableSpaceIds", "admin.MSG_ERR_GET_SPACE", e);
            return null;
        }
    }
    
    /** Return the group profile */
    public GroupProfileInst getGroupProfile(String groupId)
    {
		SilverTrace.info("admin", "AdminController.getGroupProfile", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.getGroupProfileInst(groupId);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getGroupProfile", "admin.MSG_ERR_GET_GROUP_PROFILE", e);
            return null;
        }
    }
    
    /** Delete the Group Profile */
    public String deleteGroupProfile(String groupId)
    {
		SilverTrace.info("admin", "AdminController.deleteGroupProfile", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.deleteGroupProfileInst(groupId);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.deleteGroupProfile", "admin.MSG_ERR_DELETE_GROUP_PROFILE", e);
            return "";
        }
    }

    /** Update the Group Profile*/
    public String updateGroupProfile(GroupProfileInst profile)
    {
		SilverTrace.info("admin", "AdminController.updateGroupProfile", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.updateGroupProfileInst(profile);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.updateGroupProfile", "admin.MSG_ERR_UPDATE_GROUP_PROFILE", e);
            return "";
        }
    }

	//----------------------------------------------
    //        General Admin ID related functions
    //----------------------------------------------

    /** Return the general admin id */
	public String getDAPIGeneralAdminId()
	{
		SilverTrace.info("admin", "AdminController.getDAPIGeneralAdminId", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.getDAPIGeneralAdminId();
        }
        catch(Exception e)
        {
			SilverTrace.fatal("admin", "AdminController.getDAPIGeneralAdminId", "admin.MSG_FATAL_GET_GENERAL_ADMIN_ID", e);
            return null;
        }
	}

	//----------------------------------------------
    //        Admin User Detail related functions
    //----------------------------------------------

    /** Return the admin user detail corresponding to the given id */
    public UserDetail getUserDetail(String sId)
    {
		SilverTrace.info("admin", "AdminController.getUserDetail", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.getUserDetail(sId);
        }
        catch(Exception e)
        {
			SilverTrace.warn("admin", "AdminController.getUserDetail", "admin.EX_ERR_GET_USER_DETAIL", "user id: " + sId, e);
            return null;
        }
    }

    /**
     * Return the UserFull of the user with the given Id
     */
    public UserFull getUserFull(String sUserId)
    {
		SilverTrace.info("admin", "AdminController.getUserFull", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.getUserFull(sUserId);
        }
        catch (Exception e)
        {
            SilverTrace.error("admin", "AdminController.getUserFull", "admin.EX_ERR_GET_USER_DETAIL", "user Id : '" + sUserId + "'", e);
            return null;
        }
    }
    
    public UserFull getUserFull(String domainId, String specificId)
    {
		SilverTrace.info("admin", "AdminController.getUserFull", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.getUserFull(domainId, specificId);
        }
        catch (Exception e)
        {
            SilverTrace.error("admin", "AdminController.getUserFull", "admin.EX_ERR_GET_USER_DETAIL", "specificId = " + specificId, e);
            return null;
        }
    }

    public String getUserIdByLoginAndDomain(String sLogin, String sDomainId)
    {
		SilverTrace.info("admin", "AdminController.getUserIdByLoginAndDomain", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.getUserIdByLoginAndDomain(sLogin, sDomainId);
        }
        catch (Exception e)
        {
            SilverTrace.warn("admin", "AdminController.getUserIdByLoginAndDomain", "admin.EX_ERR_GET_USER_DETAIL", "sLogin : '" + sLogin + "' Domain = " + sDomainId, e);
            return null;
        }
    }

    
    /** Return an array of UserDetail corresponding to the given user Id array */
    public UserDetail[] getUserDetails(String[] asUserIds)
    {
		SilverTrace.info("admin", "AdminController.getUserDetails", "root.MSG_GEN_ENTER_METHOD");
        try
        {
	    if(asUserIds != null)
            {
                return m_Admin.getUserDetails(asUserIds);
            }
            else
                return new UserDetail[0];
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getUserDetails", "admin.EX_ERR_GET_USER_DETAILS", e);
            return null;
        }
    }

	/** Add the given user */
    public String addUser(UserDetail userDetail)
    {
		SilverTrace.info("admin", "AdminController.addUser", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.addUser(userDetail);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.addUser", "admin.EX_ERR_ADD_USER", e);
		    return "";
        }
    }

    /** Delete the given user */
    public String deleteUser(String sUserId)
    {
		SilverTrace.info("admin", "AdminController.deleteUser", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.deleteUser(sUserId);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.deleteUser", "admin.EX_ERR_DELETE_USER", e);
            return "";
        }
    }

	/** Update the given user */
    public String updateUser(UserDetail userDetail)
    {
		SilverTrace.info("admin", "AdminController.updateUser", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.updateUser(userDetail);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.updateUser", "admin.EX_ERR_UPDATE_USER", e);
            return "";
        }
    }

	/** Update the silverpeas specific infos of a synchronized user. For the moment : same as updateUser */
    public String updateSynchronizedUser(UserDetail userDetail)
    {
		SilverTrace.info("admin", "AdminController.updateSynchronizedUser", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.updateUser(userDetail);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.updateSynchronizedUser", "admin.EX_ERR_UPDATE_USER", e);
            return "";
        }
    }

	/** Update the given user */
    public String updateUserFull(UserFull userFull)
    {
		SilverTrace.info("admin", "AdminController.updateUserFull", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.updateUserFull(userFull);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.updateUserFull", "admin.EX_ERR_UPDATE_USER", e);
            return "";
        }
    }
    
    public String authenticate(String sKey, String sSessionId, boolean isAppInMaintenance)
    {
    	try
    	{
    		return m_Admin.authenticate(sKey, sSessionId, isAppInMaintenance);
    	}
    	catch (Exception e)
    	{
    		return "-1";
    	}
    }

    //----------------------------------------------
    //        Admin Group Detail related functions
    //----------------------------------------------

    /** Return all the groups Id available in webactiv */
    public String[] getAllGroupIds()
    {
		SilverTrace.info("admin", "AdminController.getAllGroupIds", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.getAllGroupIds();
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getAllGroupIds", "admin.EX_ERR_GET_ALL_GROUP_IDS", e);
            return new String[0];
        }
    }

	/** Return true if the group with the given name */
    public boolean isGroupExist(String sName)
    {
		SilverTrace.info("admin", "AdminController.isGroupExist", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.isGroupExist(sName);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.isGroupExist", "admin.EX_ERR_IS_GROUP_EXIST", e);
            return false;
        }
    }

    /** Return the admin group detail corresponding to the given id */
    public Group getGroupById(String sGroupId)
    {
		SilverTrace.info("admin", "AdminController.getGroupById", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.getGroup(sGroupId);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getGroupById", "admin.EX_ERR_GET_GROUP", e);
            return null;
        }
    }
    
    /** Return the groupIds from root to group */
    public List getPathToGroup(String groupId)
    {
		SilverTrace.info("admin", "AdminController.getPathToGroup", "root.MSG_GEN_ENTER_METHOD", "groupId ="+groupId);
        try
        {
            return m_Admin.getPathToGroup(groupId);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getPathToGroup", "admin.EX_ERR_GET_GROUP", e);
            return null;
        }
    }
    
    /** Return the admin group detail corresponding to the given group Name */
    public Group getGroupByNameInDomain(String sGroupName, String sDomainFatherId)
    {
		SilverTrace.info("admin", "AdminController.getGroupByNameInDomain", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.getGroupByNameInDomain(sGroupName, sDomainFatherId);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getGroupByNameInDomain", "admin.EX_ERR_GET_GROUP", e);
            return null;
        }
    }

    /** Add the given group */
    public String addGroup(Group group)
    {
		SilverTrace.info("admin", "AdminController.addGroup", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.addGroup(group);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.addGroup", "admin.EX_ERR_ADD_GROUP", e);
		    return "";
        }
    }

    /** Delete the given group */
    public String deleteGroupById(String sGroupId)
    {
		SilverTrace.info("admin", "AdminController.deleteGroupById", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.deleteGroupById(sGroupId);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.deleteGroupById", "admin.EX_ERR_DELETE_GROUP", e);
		    return "";
        }
    }

    /** Update the given group */
    public String updateGroup(Group group)
    {
		SilverTrace.info("admin", "AdminController.updateGroup", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.updateGroup(group);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.updateGroup", "admin.EX_ERR_UPDATE_GROUP", e);
		    return "";
        }
    }

    public AdminGroupInst[] getAdminOrganization()
    {
		SilverTrace.info("admin", "AdminController.getAdminOrganization", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            return m_Admin.getAdminOrganization();
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getAdminOrganization", "admin.EX_ERR_GET_ADMIN_ORGANIZATION", e);
		    return null;
        }
    }

     //----------------------------------------------
    //        Exploitation related functions
    //----------------------------------------------
    public UserLog[] getUserConnected()
    {
		SilverTrace.info("admin", "AdminController.getUserConnected", "root.MSG_GEN_ENTER_METHOD");
        return m_Admin.getUserConnected();
    }

	////////////////////////////////////////////////////////////
	// Synchronization tools
	////////////////////////////////////////////////////////////

	/**
	 *	Synchronize users and groups between cache and domain's datastore
	 *
	 *  @param		domainId		Id of domain to synchronize
	 *  @return String to show as the report of synchronization
	 */
	public String synchronizeSilverpeasWithDomain(String domainId)
	{
		SilverTrace.info("admin", "AdminController.synchronizeSilverpeasWithDomain", "root.MSG_GEN_ENTER_METHOD");
        try
        {
			return m_Admin.synchronizeSilverpeasWithDomain(domainId);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.synchronizeSilverpeasWithDomain", "admin.MSG_ERR_SYNCHRONIZE_DOMAIN", e);
			return "Error has occurred";
        }
	}

	public String synchronizeUser(String userId)
	{
		SilverTrace.info("admin", "AdminController.synchronizeUser", "root.MSG_GEN_ENTER_METHOD");
        try
        {
			return m_Admin.synchronizeUser(userId,true);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.synchronizeUser", "admin.MSG_ERR_SYNCHRONIZE_USER", e);
			return "";
        }
	}

	public String synchronizeImportUser(String domainId, String userLogin)
	{
		SilverTrace.info("admin", "AdminController.synchronizeImportUser", "root.MSG_GEN_ENTER_METHOD");
        try
        {
			return m_Admin.synchronizeImportUser(domainId,userLogin,true);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.synchronizeImportUser", "admin.MSG_ERR_SYNCHRONIZE_USER", e);
			return "";
        }
	}
	
	public List getSpecificPropertiesToImportUsers(String domainId, String language)
	{
		SilverTrace.info("admin", "AdminController.getSpecificPropertiesToImportUsers", "root.MSG_GEN_ENTER_METHOD");
        try
        {
			return m_Admin.getSpecificPropertiesToImportUsers(domainId,language);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.getSpecificPropertiesToImportUsers", "admin.MSG_ERR_SYNCHRONIZE_USER", e);
			return null;
        }
	}
	
	public List searchUsers(String domainId, Hashtable query)
	{
		SilverTrace.info("admin", "AdminController.searchUsers", "root.MSG_GEN_ENTER_METHOD");
        try
        {
			return Arrays.asList(m_Admin.searchUsers(domainId, query));
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.searchUsers", "admin.MSG_ERR_SYNCHRONIZE_USER", e);
			return new ArrayList();
        }
	}

	public String synchronizeRemoveUser(String userId)
	{
		SilverTrace.info("admin", "AdminController.synchronizeRemoveUser", "root.MSG_GEN_ENTER_METHOD");
        try
        {
			return m_Admin.synchronizeRemoveUser(userId);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.synchronizeRemoveUser", "admin.MSG_ERR_SYNCHRONIZE_USER", e);
			return "";
        }
	}

	public String synchronizeGroup(String groupId)
	{
		SilverTrace.info("admin", "AdminController.synchronizeGroup", "root.MSG_GEN_ENTER_METHOD");
        try
        {
			return m_Admin.synchronizeGroup(groupId,true);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.synchronizeGroup", "admin.MSG_ERR_SYNCHRONIZE_GROUP", e);
			return "";
        }
	}

	public String synchronizeImportGroup(String domainId, String groupName)
	{
		SilverTrace.info("admin", "AdminController.synchronizeImportGroup", "root.MSG_GEN_ENTER_METHOD");
        try
        {
			return m_Admin.synchronizeImportGroup(domainId,groupName,null,true,false);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.synchronizeImportGroup", "admin.MSG_ERR_SYNCHRONIZE_GROUP", e);
			return "";
        }
	}

	public String synchronizeRemoveGroup(String groupId)
	{
		SilverTrace.info("admin", "AdminController.synchronizeRemoveGroup", "root.MSG_GEN_ENTER_METHOD");
        try
        {
			return m_Admin.synchronizeRemoveGroup(groupId);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.synchronizeRemoveGroup", "admin.MSG_ERR_SYNCHRONIZE_GROUP", e);
			return "";
        }
	}
    
    public void resetAllDBConnections(boolean isScheduled)
	{
//		SilverTrace.info("admin", "AdminController.resetAllDBConnections", "root.MSG_GEN_ENTER_METHOD");
        try
        {
			m_Admin.resetAllDBConnections(isScheduled);
        }
        catch(Exception e)
        {
			SilverTrace.error("admin", "AdminController.resetAllDBConnections", "admin.MSG_ERR_SYNCHRONIZE_GROUP", e);
        }
	}
}