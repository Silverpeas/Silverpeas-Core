package com.stratelia.webactiv.beans.admin;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.silverpeas.util.i18n.I18NHelper;
import com.silverpeas.util.i18n.Translation;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.instance.control.SPParameter;
import com.stratelia.webactiv.beans.admin.instance.control.SPParameters;
import com.stratelia.webactiv.organization.ComponentInstanceI18NRow;
import com.stratelia.webactiv.organization.ComponentInstanceRow;
import com.stratelia.webactiv.organization.SpaceRow;
import com.stratelia.webactiv.util.exception.SilverpeasException;

public class ComponentInstManager
{
    static ProfileInstManager m_ProfileInstManager = new ProfileInstManager();

	/**
	 * Constructor
	 */
	public ComponentInstManager()
	{
	}

    /** Return a copy of the given componentInst */
    public ComponentInst copy(ComponentInst componentInstToCopy)
    {
		ComponentInst componentInst = new ComponentInst();
        componentInst.setId(componentInstToCopy.getId());
        componentInst.setName(componentInstToCopy.getName());
        componentInst.setLabel(componentInstToCopy.getLabel());
        componentInst.setDescription(componentInstToCopy.getDescription());
        componentInst.setDomainFatherId(componentInstToCopy.getDomainFatherId());
        componentInst.setOrderNum(componentInstToCopy.getOrderNum());
        
        componentInst.setCreateDate(componentInstToCopy.getCreateDate());
        componentInst.setUpdateDate(componentInstToCopy.getUpdateDate());
        componentInst.setRemoveDate(componentInstToCopy.getRemoveDate());
        componentInst.setStatus(componentInstToCopy.getStatus());
        componentInst.setCreatorUserId(componentInstToCopy.getCreatorUserId());
        componentInst.setUpdaterUserId(componentInstToCopy.getUpdaterUserId());
        componentInst.setRemoverUserId(componentInstToCopy.getRemoverUserId());

        for(int nI=0; nI < componentInstToCopy.getNumProfileInst(); nI++)
            componentInst.addProfileInst(componentInstToCopy.getProfileInst(nI));

		SPParameters parameters = componentInstToCopy.getSPParameters();
		componentInst.setSPParameters(parameters);
		
		componentInst.setLanguage(componentInstToCopy.getLanguage());
		
		//Create a copy of component translations
        Iterator translations = componentInstToCopy.getTranslations().values().iterator();
        while (translations.hasNext())
        {
        	ComponentI18N translation = (ComponentI18N) translations.next();
        	componentInst.addTranslation((Translation) translation);
        }
        
        componentInst.setPublic(componentInstToCopy.isPublic());
        componentInst.setHidden(componentInstToCopy.isHidden());
        componentInst.setInheritanceBlocked(componentInstToCopy.isInheritanceBlocked());

		return componentInst;
    }

	/**
	 * Creates a component instance in database
	 */
    public String createComponentInst(ComponentInst componentInst, DomainDriverManager ddManager, String sFatherId) throws AdminException
    {
		try
        {
            // Create the component node
            ComponentInstanceRow newInstance = makeComponentInstanceRow(componentInst);
            newInstance.spaceId = idAsInt(sFatherId);
            ddManager.organization.instance.createComponentInstance(newInstance);
			String sComponentNodeId = idAsString(newInstance.id);

			// Add the parameter if necessary
			List parameters = componentInst.getParameters();

			SilverTrace.info("admin", "ComponentInstManager.createComponentInst", "root.MSG_GEN_PARAM_VALUE", "nb parameters = "+parameters.size());

			SPParameter parameter = null;
			for(int nI = 0; nI < parameters.size(); nI++)
			{
				parameter = (SPParameter) parameters.get(nI);
				ddManager.organization.instanceData.createInstanceData(idAsInt(sComponentNodeId), parameter);
			}

			// Create the profile nodes
            for(int nI = 0; nI < componentInst.getNumProfileInst(); nI++)
                m_ProfileInstManager.createProfileInst(componentInst.getProfileInst(nI), ddManager, sComponentNodeId);

            return sComponentNodeId;
        }
        catch(Exception  e)
        {
			throw new AdminException("ComponentInstManager.createComponentInst", SilverpeasException.ERROR, "admin.EX_ERR_ADD_COMPONENT", "component name: '" + componentInst.getName() + "'", e);
        }
    }
    
    public void sendComponentToBasket(DomainDriverManager ddManager, String componentId, String tempLabel, String userId) throws AdminException
    {
        try
        {
			ddManager.organization.instance.sendComponentToBasket(idAsInt(componentId), tempLabel, userId);
        }
        catch(Exception e)
        {
			throw new AdminException("ComponentInstManager.sendComponentToBasket", SilverpeasException.ERROR, "admin.EX_ERR_SEND_COMPONENT_TO_BASKET", "componentId = " + componentId, e);
        }
    }
    
    public void restoreComponentFromBasket(DomainDriverManager ddManager, String componentId) throws AdminException
    {
        try
        {
			ddManager.organization.instance.restoreComponentFromBasket(idAsInt(componentId));
        }
        catch(Exception e)
        {
			throw new AdminException("ComponentInstManager.restoreComponentFromBasket", SilverpeasException.ERROR, "admin.EX_ERR_RESTORE_COMPONENT_FROM_BASKET", "componentId = " + componentId, e);
        }
    }

	/**
	 * Get component instance with the given id
	 */
    public ComponentInst getComponentInst(DomainDriverManager ddManager, String sComponentId, String sFatherId) throws AdminException
    {
        if(sFatherId == null)
        {
            try
              {
				  ddManager.getOrganizationSchema();
				  SpaceRow space = ddManager.organization.space.getSpaceOfInstance(idAsInt(sComponentId));
				  if (space == null) space = new SpaceRow();
                  sFatherId = idAsString(space.id);
              }
              catch(Exception e)
              {
				  throw new AdminException("ComponentInstManager.getComponentInst", SilverpeasException.ERROR, "admin.EX_ERR_GET_COMPONENT", "component id: '" + sComponentId + "'", e);
              }
			finally
			{
				ddManager.releaseOrganizationSchema();
			}
        }

        ComponentInst componentInst = new ComponentInst();
        componentInst.removeAllProfilesInst();
        this.setComponentInst(componentInst, ddManager, sComponentId, sFatherId);

        return componentInst;
    }
    
    /**
	 * Return the all the root spaces ids available in Silverpeas 
	 */
    public List getRemovedComponents(DomainDriverManager ddManager) throws AdminException
    {
        try
        {
			ddManager.getOrganizationSchema();
			ComponentInstanceRow[] componentRows = ddManager.organization.instance.getRemovedComponents();
            
			return componentInstanceRows2ComponentInstLights(componentRows);
        }
        catch(Exception e)
        {
			throw new AdminException("SpaceInstManager.getRemovedSpaces", SilverpeasException.ERROR, "admin.EX_ERR_GET_REMOVED_SPACES", e);
        }
    }
    
	/**
	 * Get component instance name with the given id
	 */
	public String getComponentInstName(DomainDriverManager ddManager, String sComponentId) throws AdminException
	{
		String compoName = null;
		try
		{
			ddManager.getOrganizationSchema();
			ComponentInstanceRow compo = ddManager.organization.instance.getComponentInstance(idAsInt(sComponentId));
			if (compo!=null)
				compoName = compo.componentName;
		}
		catch(Exception e)
		{
			throw new AdminException("ComponentInstManager.getComponentInstName", SilverpeasException.ERROR, "admin.EX_ERR_GET_COMPONENT_NAME", "component id: '" + sComponentId + "'", e);
		}
		finally
		{
			ddManager.releaseOrganizationSchema();
		}
		return compoName;
	}

	/**
	 * Get component instance light with the given id
	 */
	public ComponentInstLight getComponentInstLight(DomainDriverManager ddManager, String sComponentId) throws AdminException
	{
		ComponentInstLight compoLight = null;
		try
		{
			ddManager.getOrganizationSchema();
			ComponentInstanceRow compo = ddManager.organization.instance.getComponentInstance(idAsInt(sComponentId));
			if (compo!=null)
			{
				compoLight = new ComponentInstLight(compo);
				compoLight.setId(compoLight.getName()+sComponentId);
				compoLight.setDomainFatherId("WA"+compoLight.getDomainFatherId());
				
				//Add default translation
	            ComponentI18N translation = new ComponentI18N(compo.lang, compo.name, compo.description);
	            compoLight.addTranslation((Translation) translation);
	            
	            List translations = ddManager.organization.instanceI18N.getTranslations(compo.id);
	            for (int t=0; translations != null && t<translations.size(); t++)
	            {
	            	ComponentInstanceI18NRow row = (ComponentInstanceI18NRow) translations.get(t);
	            	compoLight.addTranslation((Translation) new ComponentI18N(row));
	            }
			}
		}
		catch(Exception e)
		{
			throw new AdminException("ComponentInstManager.getComponentInstName", SilverpeasException.ERROR, "admin.EX_ERR_GET_COMPONENT_NAME", "component id: '" + sComponentId + "'", e);
		}
		finally
		{
			ddManager.releaseOrganizationSchema();
		}
		return compoLight;
	}
	
	/*
	 * Get component instance information with given component id
	 */
    public void setComponentInst(ComponentInst componentInst, DomainDriverManager ddManager, String sComponentId, String sFatherId) throws AdminException
    {
        try
        {
			ddManager.getOrganizationSchema();

			//Load the component detail
			ComponentInstanceRow instance = ddManager.organization.instance.getComponentInstance(idAsInt(sComponentId));
			
			if (instance != null)
			{
	            //Set the attributes of the component Inst
	            componentInst.setId(idAsString(instance.id));
	            componentInst.setName(instance.componentName);
	            componentInst.setLabel(instance.name);
	            componentInst.setDescription(instance.description);
				componentInst.setDomainFatherId(sFatherId);
	            componentInst.setOrderNum(instance.orderNum);
	            
	            if (instance.createTime != null)
	            	componentInst.setCreateDate(new Date(Long.parseLong(instance.createTime)));
	            if (instance.updateTime != null)
	            	componentInst.setUpdateDate(new Date(Long.parseLong(instance.updateTime)));
	            if (instance.removeTime != null)
	            	componentInst.setRemoveDate(new Date(Long.parseLong(instance.removeTime)));
	            
	            componentInst.setCreatorUserId(idAsString(instance.createdBy));
	            componentInst.setUpdaterUserId(idAsString(instance.updatedBy));
	            componentInst.setRemoverUserId(idAsString(instance.removedBy));
	            
	            componentInst.setStatus(instance.status);
	
				//Get the parameters if any
				SPParameters parameters = ddManager.organization.instanceData.getAllParametersInComponent(idAsInt(sComponentId));
				componentInst.setSPParameters(parameters);
	
				//Get the profiles
				String[] asProfileIds = ddManager.organization.userRole.getAllUserRoleIdsOfInstance(idAsInt(componentInst.getId()));
	
	            //Insert the profileInst in the componentInst
                for (int nI=0; asProfileIds != null && nI<asProfileIds.length; nI++)
                {
                    ProfileInst profileInst = m_ProfileInstManager.getProfileInst(ddManager, asProfileIds[nI], sComponentId);
                    componentInst.addProfileInst(profileInst);
                }
	            
	            componentInst.setLanguage(instance.lang);
	            
	            //Add default translation
	            ComponentI18N translation = new ComponentI18N(instance.lang, instance.name, instance.description);
	            componentInst.addTranslation((Translation) translation);
	            
	            List translations = ddManager.organization.instanceI18N.getTranslations(instance.id);
	            for (int t=0; translations != null && t<translations.size(); t++)
	            {
	            	ComponentInstanceI18NRow row = (ComponentInstanceI18NRow) translations.get(t);
	            	componentInst.addTranslation((Translation) new ComponentI18N(row));
	            }
	            
	            componentInst.setPublic((instance.publicAccess == 1));
	            componentInst.setHidden((instance.hidden == 1));
	            componentInst.setInheritanceBlocked((instance.inheritanceBlocked == 1));
			}
			else
			{
				SilverTrace.error("admin", "ComponentInstManager.setComponentInst", "root.EX_RECORD_NOT_FOUND", "instanceId = "+sComponentId);
			}
        }
        catch(Exception e)
        {
  		    throw new AdminException("ComponentInstManager.setComponentInst", SilverpeasException.ERROR, "admin.EX_ERR_SET_COMPONENT", "component id: '" + sComponentId + "'", e);
        }
		finally
		{
			ddManager.releaseOrganizationSchema();
		}
    }

	/**
	 * Deletes component instance from Silverpeas
	 */
    public void deleteComponentInst(ComponentInst componentInst, DomainDriverManager ddManager) throws AdminException
    {
        try
        {
        	//delete translations
        	ddManager.organization.instanceI18N.removeTranslations(idAsInt(componentInst.getId()));
        	
            //delete the component node
			ddManager.organization.instance.removeComponentInstance(idAsInt(componentInst.getId()));
        }
        catch(Exception  e)
        {
  		    throw new AdminException("ComponentInstManager.deleteComponentInst", SilverpeasException.ERROR, "admin.EX_ERR_DELETE_COMPONENT", "component id: '" + componentInst.getId() + "'", e);
        }
    }

    /*
	 * Updates component  in Silverpeas
	 */
    public void updateComponentOrder(DomainDriverManager ddManager, String sComponentId, int orderNum) throws AdminException
    {
        try
        {
			ddManager.getOrganizationSchema();
            ddManager.organization.instance.updateComponentOrder(idAsInt(sComponentId),orderNum);
        }
        catch(Exception e)
        {
			throw new AdminException("ComponentInstManager.updateComponentOrder", SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_COMPONENT", "Component Id : '" + sComponentId + "'", e);
        }
		finally
		{
			ddManager.releaseOrganizationSchema();
		}
    }
    
    /*
	 * Updates component  in Silverpeas
	 */
    public void updateComponentInheritance(DomainDriverManager ddManager, String sComponentId, boolean inheritanceBlocked) throws AdminException
    {
        try
        {
			ddManager.getOrganizationSchema();
            ddManager.organization.instance.updateComponentInheritance(idAsInt(sComponentId), inheritanceBlocked);
        }
        catch(Exception e)
        {
			throw new AdminException("ComponentInstManager.updateComponentOrder", SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_COMPONENT_INHERITANCE", "Component Id : '" + sComponentId + "'", e);
        }
		finally
		{
			ddManager.releaseOrganizationSchema();
		}
    }

    /*
	 * Updates component instance and recursively sub-elements in Silverpeas
	 */
    public void updateComponentInstRecur(ComponentInst componentInst, DomainDriverManager ddManager, ComponentInst compoInstNew) throws AdminException
    {
        ArrayList alOldCompoProfile  = new ArrayList();
        ArrayList alNewCompoProfile  = new ArrayList();
        ArrayList alAddProfile  = new ArrayList();
        ArrayList alRemProfile  = new ArrayList();
        ArrayList alStayProfile = new ArrayList();

        try
        {
            // Compute the Old component profile list
            ArrayList alProfileInst = componentInst.getAllProfilesInst();
            for(int nI =0; nI < alProfileInst.size(); nI++)
                alOldCompoProfile.add(((ProfileInst)alProfileInst.get(nI)).getName());

            // Compute the New component profile list
            alProfileInst = compoInstNew.getAllProfilesInst();
            for(int nI =0; nI < alProfileInst.size(); nI++)
                alNewCompoProfile.add(((ProfileInst)alProfileInst.get(nI)).getName());

            // Compute the remove Profile list
            for(int nI = 0; nI < alOldCompoProfile.size(); nI++)
                if(alNewCompoProfile.indexOf(alOldCompoProfile.get(nI)) == -1)
                    alRemProfile.add(alOldCompoProfile.get(nI));

            // Compute the add and stay Profile list
            for(int nI = 0; nI < alNewCompoProfile.size(); nI++)
                if(alOldCompoProfile.indexOf(alNewCompoProfile.get(nI)) == -1)
                    alAddProfile.add(alNewCompoProfile.get(nI));
                else
                    alStayProfile.add(alNewCompoProfile.get(nI));

            // Add the new Profiles
            for(int nI = 0; nI < alAddProfile.size(); nI++)
                m_ProfileInstManager.createProfileInst(compoInstNew.getProfileInst((String)alAddProfile.get(nI)), ddManager, componentInst.getId());

            // Remove the removed profiles
            for(int nI = 0; nI < alRemProfile.size(); nI++)
                m_ProfileInstManager.deleteProfileInst(componentInst.getProfileInst((String)alRemProfile.get(nI)), ddManager);

            // Update the stayed profile
            for(int nI = 0; nI < alStayProfile.size(); nI++)
                m_ProfileInstManager.updateProfileInst(componentInst.getProfileInst((String)alStayProfile.get(nI)), ddManager, compoInstNew.getProfileInst((String)alStayProfile.get(nI)));
        }
        catch(Exception  e)
        {
  		    throw new AdminException("ComponentInstManager.updateComponentInstRecur", SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_COMPONENT", "component id: '" + componentInst.getId() + "'", e);
        }
    }

    /*
	 * Updates component instance in Silverpeas
	 */
    public String updateComponentInst(DomainDriverManager ddManager, ComponentInst compoInstNew) throws AdminException
    {
        try
        {
			List parameters = compoInstNew.getParameters();
			for(int nI=0; nI < parameters.size(); nI++)
			{
				ddManager.organization.instanceData.updateInstanceData(idAsInt(compoInstNew.getId()), (SPParameter) parameters.get(nI));
			}
			
			//Create the component node
            ComponentInstanceRow changedInstance = makeComponentInstanceRow(compoInstNew);
            changedInstance.id = idAsInt(compoInstNew.getId());
			
			ComponentInstanceRow old = ddManager.organization.instance.getComponentInstance(changedInstance.id);
            
            SilverTrace.debug("admin", this.getClass().getName()+".updateComponentInst", "root.MSG_GEN_PARAM_VALUE", "remove = "+compoInstNew.isRemoveTranslation()+", translationId = "+compoInstNew.getTranslationId());
            
            if (compoInstNew.isRemoveTranslation())
            {
            	//Remove of a translation is required 
            	if (old.lang.equalsIgnoreCase(compoInstNew.getLanguage()))
            	{
            		//Default language = translation 
            		List translations = ddManager.organization.instanceI18N.getTranslations(changedInstance.id);
            		
            		if (translations != null && translations.size() > 0)
            		{
            			ComponentInstanceI18NRow translation = (ComponentInstanceI18NRow) translations.get(0);
            			
            			changedInstance.lang 			= translation.lang;
            			changedInstance.name 			= translation.name;
            			changedInstance.description 	= translation.description;
            			
            			ddManager.organization.instance.updateComponentInstance(changedInstance);
            			
            			ddManager.organization.instanceI18N.removeTranslation(translation.id);
            		}
            	}
            	else
            	{
            		ddManager.organization.instanceI18N.removeTranslation(Integer.parseInt(compoInstNew.getTranslationId()));
            	}
            }
            else
            {
            	//Add or update a translation
	            if (changedInstance.lang != null)
	            {
	            	if (old.lang == null)
	            	{
	            		//translation for the first time
	            		old.lang = I18NHelper.defaultLanguage;
	            	}
	            	
	            	if (!old.lang.equalsIgnoreCase(changedInstance.lang))
	                {
	            		ComponentInstanceI18NRow row = new ComponentInstanceI18NRow(changedInstance);
	            		String translationId = compoInstNew.getTranslationId();
	            		if (translationId != null && !translationId.equals("-1"))
	            		{
	            			//update translation
	                		row.id = Integer.parseInt(compoInstNew.getTranslationId());
	                		
	                        ddManager.organization.instanceI18N.updateTranslation(row);
	            		}
	            		else
	            		{
	            			ddManager.organization.instanceI18N.createTranslation(row);
	            		}
	            		
	            		changedInstance.lang = old.lang;
	            		changedInstance.name = old.name;
	            		changedInstance.description = old.description;
	                }
	            }
            
	            ddManager.organization.instance.updateComponentInstance(changedInstance);
            }

            return idAsString(changedInstance.id);
        }
        catch(Exception e)
        {
  		    throw new AdminException("ComponentInstManager.updateComponentInst", SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_COMPONENT", "component id: '" + compoInstNew.getId() + "'", e);
        }
    }

	/*
	 * Move component instance in Silverpeas
	 */
	public void moveComponentInst(DomainDriverManager ddManager, String spaceId, String componentId) throws AdminException
	{
		try
		{
			// Create the component node
			ddManager.organization.instance.moveComponentInstance(idAsInt(spaceId),idAsInt(componentId));
		}
		catch(Exception e)
		{
			throw new AdminException("ComponentInstManager.moveComponentInst", SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_COMPONENT", "spaceId= " + spaceId + " componentId="+componentId, e);
		}
	}

	/**
	 * Get the component ids allowed for the given user Id
	 */
    public String[] getAvailCompoIds(DomainDriverManager ddManager, String sUserId) throws AdminException
	{
		try
		{
			ddManager.getOrganizationSchema();
			return ddManager.organization.instance.getAvailCompoIds(idAsInt(sUserId));
		}
		catch(Exception e)
		{
  		    throw new AdminException("ComponentInstManager.getAvailCompoIds", SilverpeasException.ERROR, "admin.EX_ERR_GET_USER_AVAILABLE_COMPONENT_IDS", "user id: '" + sUserId + "'", e);
		}
		finally
		{
			ddManager.releaseOrganizationSchema();
		}
	}
    
    /**
	 * Get the component ids allowed for the given user Id
	 */
    public String[] getAvailCompoIds(DomainDriverManager ddManager, String sUserId, String componentName) throws AdminException
	{
		try
		{
			ddManager.getOrganizationSchema();
			//return ddManager.organization.instance.getAvailableComponentIds(idAsInt(sUserId), componentName);
			
			List rows = ddManager.organization.instance.getAvailableComponents(idAsInt(sUserId), componentName);
			
			String[] ids = new String[rows.size()];
			ComponentInstanceRow row = null;
			for (int i=0; rows != null && i<rows.size(); i++)
			{
				row = (ComponentInstanceRow) rows.get(i);
				ids[i] = Integer.toString(row.id);
			}
			
			return ids;
		}
		catch(Exception e)
		{
  		    throw new AdminException("ComponentInstManager.getAvailCompoIds", SilverpeasException.ERROR, "admin.EX_ERR_GET_USER_AVAILABLE_COMPONENT_IDS", "user id: '" + sUserId + "', componentName = "+componentName, e);
		}
		finally
		{
			ddManager.releaseOrganizationSchema();
		}
	}
    
    /**
	 * Get the component ids allowed for the given user Id
	 */
    public List getAvailComponentInstLights(DomainDriverManager ddManager, String sUserId, String componentName) throws AdminException
	{
		try
		{
			ddManager.getOrganizationSchema();
			List componentRows = ddManager.organization.instance.getAvailableComponents(idAsInt(sUserId), componentName);
			List result = new ArrayList();
			Iterator i = componentRows.iterator();
			while (i.hasNext())
			{
				ComponentInstanceRow row = (ComponentInstanceRow) i.next();
				ComponentInstLight component = new ComponentInstLight(row);
				component.setId(component.getName()+component.getId());
				component.setDomainFatherId("WA"+component.getDomainFatherId());
				result.add(component);
			}
			return result;
		}
		catch(Exception e)
		{
  		    throw new AdminException("ComponentInstManager.getAvailCompoIds", SilverpeasException.ERROR, "admin.EX_ERR_GET_USER_AVAILABLE_COMPONENT_IDS", "user id: '" + sUserId + "', componentName = "+componentName, e);
		}
		finally
		{
			ddManager.releaseOrganizationSchema();
		}
	}

	public boolean isComponentAvailable(DomainDriverManager ddManager, String sUserId, String componentId) throws AdminException
    {
    	try
		{
			ddManager.getOrganizationSchema();
			return ddManager.organization.instance.isComponentAvailable(idAsInt(sUserId), idAsInt(componentId));
		}
		catch(Exception e)
		{
  		    throw new AdminException("ComponentInstManager.isComponentAvailable", SilverpeasException.ERROR, "admin.EX_ERR_GET_USER_AVAILABLE_COMPONENT_IDS", "user id: '" + sUserId + "', componentId = "+componentId, e);
		}
		finally
		{
			ddManager.releaseOrganizationSchema();
		}
    }

	/**
	 * Get the component ids allowed for the given user Id in the given space
	 */
    public String[] getAvailCompoIdsInSpace(DomainDriverManager ddManager, String spaceId, String sUserId) throws AdminException
	{
		try
		{
			ddManager.getOrganizationSchema();
			ComponentInstanceRow[] rows = ddManager.organization.instance.getAvailCompoInSpace(idAsInt(spaceId), idAsInt(sUserId));
			
			String[] ids = new String[rows.length];
			ComponentInstanceRow row = null;
			for (int i=0; rows != null && i<rows.length; i++)
			{
				row = rows[i];
				ids[i] = Integer.toString(row.id);
			}
			
			return ids;
		}
		catch(Exception e)
		{
  		    throw new AdminException("ComponentInstManager.getAvailCompoIdsInSpace", SilverpeasException.ERROR, "admin.EX_ERR_GET_USER_AVAILABLE_COMPONENT_IDS", "user id: '" + sUserId + "', space Id: '" + spaceId + "'", e);
		}
		finally
		{
			ddManager.releaseOrganizationSchema();
		}
	}

	/**
	 * Get the components allowed for the given user Id in the given space
	 */
    public List getAvailCompoInSpace(DomainDriverManager ddManager, String spaceId, String sUserId) throws AdminException
	{
		try
		{
			ddManager.getOrganizationSchema();
			ComponentInstanceRow[] componentRows = ddManager.organization.instance.getAvailCompoInSpace(idAsInt(spaceId), idAsInt(sUserId));

			return componentInstanceRows2ComponentInstLights(componentRows);
		}
		catch(Exception e)
		{
  		    throw new AdminException("ComponentInstManager.getAvailCompoInSpace", SilverpeasException.ERROR, "admin.EX_ERR_GET_USER_AVAILABLE_COMPONENTS", "user id: '" + sUserId + "', space Id: '" + spaceId + "'", e);
		}
		finally
		{
			ddManager.releaseOrganizationSchema();
		}
	}

	/**
	 * Get the component ids allowed for the given user Id in the given space
	 */
    public String[] getAvailCompoIdsInSpaceAtRoot(DomainDriverManager ddManager, String sClientSpaceId, String sUserId) throws AdminException
	{
		try
		{
			ddManager.getOrganizationSchema();
			ComponentInstanceRow[] rows = ddManager.organization.instance.getAvailComposInSpaceAtRoot(idAsInt(sClientSpaceId), idAsInt(sUserId));
			
			String[] ids = new String[rows.length];
			ComponentInstanceRow row = null;
			for (int i=0; rows != null && i<rows.length; i++)
			{
				row = rows[i];
				ids[i] = Integer.toString(row.id);
			}
			
			return ids;
		}
		catch(Exception e)
		{
  		    throw new AdminException("ComponentInstManager.getAvailCompoIdsInSpaceAtRoot", SilverpeasException.ERROR, "admin.EX_ERR_GET_USER_AVAILABLE_COMPONENT_IDS", "user id: '" + sUserId + "', space Id: '" + sClientSpaceId + "'", e);
		}
		finally
		{
			ddManager.releaseOrganizationSchema();
		}
	}

	/**
	 * Get the component ids with the given component name
	 */
    public String[] getAllCompoIdsByComponentName(DomainDriverManager ddManager, String sComponentName) throws AdminException
	{
		try
		{
			ddManager.getOrganizationSchema();

			// Initialize a ComponentInstanceRow for search
			ComponentInstanceRow cir = new ComponentInstanceRow();
			cir.name = null;
			cir.description = null;
			cir.orderNum = -1;
			cir.componentName = sComponentName;

			// Search for components instance with given component name
			ComponentInstanceRow[] cirs = ddManager.organization.instance.getAllMatchingComponentInstances(cir);
			if (cirs==null)
				return new String[0];

			String[] compoIds = new String[cirs.length];
			for (int nI=0; nI<cirs.length; nI++)
			{
				compoIds[nI] = idAsString(cirs[nI].id);
			}
			return compoIds;
		}
		catch(Exception e)
		{
  		    throw new AdminException("ComponentInstManager.getAllCompoIdsByComponentName", SilverpeasException.ERROR, "admin.EX_ERR_GET_USER_AVAILABLE_INSTANCES_OF_COMPONENT", "component name: '" + sComponentName + "'", e);
		}
		finally
		{
			ddManager.releaseOrganizationSchema();
		}
	}
    
	/*
	 * Get the component ids recursively in the given space 
	 */ 
	public String[] getAllCompoIdsInSpace(DomainDriverManager ddManager, String sClientSpaceId) throws AdminException 
	{ 
		try 
		{ 
			ddManager.getOrganizationSchema(); 
		    return ddManager.organization.instance.getAllCompoIdsInSpace(idAsInt(sClientSpaceId)); 
		} 
		catch(Exception e)
		{ 
			throw new AdminException("ComponentInstManager.getAllCompoIdsInSpace", SilverpeasException.ERROR, "admin.EX_ERR_GET_USER_ALL_COMPONENT_IDS", "space Id: '" + sClientSpaceId + "'", e);
		} 
		finally 
		{ 
			ddManager.releaseOrganizationSchema(); 
		}
	}

	/**
	 * Converts ComponentInst to ComponentInstanceRow
	 */
	private ComponentInstanceRow makeComponentInstanceRow(ComponentInst componentInst)
    {
		ComponentInstanceRow instance = new ComponentInstanceRow();

		instance.id				= idAsInt(componentInst.getId());
		instance.componentName	= componentInst.getName();
		instance.name			= componentInst.getLabel();
		instance.description	= componentInst.getDescription();
        instance.orderNum       = componentInst.getOrderNum();
        instance.lang			= componentInst.getLanguage();
        instance.createdBy		= idAsInt(componentInst.getCreatorUserId());
        instance.updatedBy		= idAsInt(componentInst.getUpdaterUserId());
        
        if (componentInst.isPublic())
        	instance.publicAccess = 1;
        else
        	instance.publicAccess = 0;
        
        if (componentInst.isHidden())
        	instance.hidden = 1;
        else
        	instance.hidden = 0;
        
        if (componentInst.isInheritanceBlocked())
        	instance.inheritanceBlocked = 1;
        else
        	instance.inheritanceBlocked = 0;
        
		return instance;
    }
	
	private List componentInstanceRows2ComponentInstLights(ComponentInstanceRow[] rows)
    {
    	List components = new ArrayList();
		ComponentInstLight componentLight = null;
		for (int s=0; rows != null && s<rows.length; s++)
		{
			componentLight = new ComponentInstLight((ComponentInstanceRow) rows[s]);
			componentLight.setId(componentLight.getName()+componentLight.getId());
			componentLight.setDomainFatherId("WA"+componentLight.getDomainFatherId());
			components.add(componentLight);
		}
		return components;
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