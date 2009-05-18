/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.silverpeas.silverstatistics.control;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.MissingResourceException;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.SpaceInst;

/**
 * This is the alimentation for the statistics on volume.
 * It gets the number of elements from each components from each space.
 * All components must implements the ComponentStatisticsInterface.
 * @author sleroux
 */
public class SilverStatisticsVolumeAlimentation
{
    static java.util.ResourceBundle  resources = null;

    static
    {
        if (resources == null)
        {
            try
            {
                resources = java.util.ResourceBundle.getBundle("com.stratelia.silverpeas.silverstatistics.SilverStatistics");
            }
            catch (Exception ex)
            {
                SilverTrace.error("silverstatistics", "SilverStatisticsVolumeAlimentation", "root.EX_CLASS_NOT_INITIALIZED", ex);
            }
        }
    }

    /**
     * Method declaration
     *
     *
     * @see
     */
    public static void makeVolumeAlimentationForAllComponents()
    {
        java.util.Date now = new java.util.Date();

        ArrayList      listAllSpacesId = null;

        // get all spaces
        listAllSpacesId = getAllSpacesAndAllSubSpacesId();
        String currentSpaceId = null;
        String currentComponentsId = null;

        if ((listAllSpacesId != null) && (!listAllSpacesId.isEmpty()))
        {
            Iterator iteratorAllSpacesId = listAllSpacesId.iterator();

            while (iteratorAllSpacesId.hasNext())
            {
                ArrayList listAllComponentsInst = null;

                // get all components from a space
                currentSpaceId = (String) iteratorAllSpacesId.next();
                listAllComponentsInst = getAllComponentsInst(currentSpaceId);
                Iterator iteratorAllComponentsInst = listAllComponentsInst.iterator();

                while (iteratorAllComponentsInst.hasNext())
                {
                    Collection    collectionUserIdCountVolume = null;

                    ComponentInst ci = (ComponentInst) iteratorAllComponentsInst.next();

                    currentComponentsId = ci.getId();

                    // get all elements from a component
                    collectionUserIdCountVolume = getCollectionUserIdCountVolume(currentSpaceId, ci);

                    if (collectionUserIdCountVolume != null)
                    {
                        Iterator iteratorUserIdCountVolume = collectionUserIdCountVolume.iterator();

                        while (iteratorUserIdCountVolume.hasNext())
                        {
                            UserIdCountVolumeCouple currentUserIdCountVolume = (UserIdCountVolumeCouple) iteratorUserIdCountVolume.next();

							/*
                            System.out.println("\n addStatVolume = "+" userId= "+
                             currentUserIdCountVolume.getUserId()+" countVolume=  "+
                             currentUserIdCountVolume.getCountVolume()+" name= "+
                             ci.getName()+" spaceId= "+
                             currentSpaceId+" compoId= "+currentComponentsId);
                             */
							SilverTrace.debug("silverstatistics", "SilverStatisticsVolumeAlimentation.makeVolumeAlimentationForAllComponents", 
							"userId= "+ currentUserIdCountVolume.getUserId()+
							" countVolume=  "+ currentUserIdCountVolume.getCountVolume()+
							" name= "+ ci.getName()+
							" spaceId= "+ currentSpaceId+
							" compoId= "+ currentComponentsId);
							
                            // notify statistics
                            SilverStatisticsManager.getInstance().addStatVolume(currentUserIdCountVolume.getUserId(), currentUserIdCountVolume.getCountVolume(), now, ci.getName(), currentSpaceId, currentComponentsId);

                        }
                    }
                }
            }
        }
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    private static ArrayList getAllSpacesAndAllSubSpacesId()
    {
        ArrayList       resultList = new ArrayList();
        AdminController myAdminController = new AdminController("");
        String[]        spaceIds = null;

        try
        {
            spaceIds = myAdminController.getAllSpaceIds();
        }
        catch (Exception e)
        {
            SilverTrace.error("silverstatistics", "SilverStatisticsVolumeAlimentation.getAllSpacesAndAllSubSpacesId()", "admin.MSG_ERR_GET_ALL_SPACE_IDS", e);
        }

        if (spaceIds != null)
        {
            for (int i = 0; i < spaceIds.length; i++)
            {
                resultList.add(spaceIds[i]);
            }
        }
        return resultList;
    }

    /**
     * Method declaration
     *
     *
     * @param spaceId
     *
     * @return
     *
     * @see
     */
    private static ArrayList getAllComponentsInst(String spaceId)
    {
        AdminController myAdminController = new AdminController("");
        SpaceInst       mySpaceInst = myAdminController.getSpaceInstById(spaceId);

        return mySpaceInst.getAllComponentsInst();
    }

    /**
     * Method declaration
     *
     *
     * @param spaceId
     * @param componentId
     *
     * @return
     *
     * @see
     */
    private static Collection getCollectionUserIdCountVolume(String spaceId, ComponentInst ci)
    {
        ComponentStatisticsInterface myCompo = null;
        Collection                   c = null;

        try
        {
            SilverTrace.info("silverstatistics", "SilverStatisticsVolumeAlimentation.getCollectionUserIdCountVolume()", "root.MSG_GEN_PARAM_VALUE", "spaceId=" + spaceId);
            SilverTrace.info("silverstatistics", "SilverStatisticsVolumeAlimentation.getCollectionUserIdCountVolume()", "root.MSG_GEN_PARAM_VALUE", "componentId=" + ci.getId());

            String className = getComponentStatisticsClassName(ci.getName());
            if (className != null)
            {
            	myCompo = (ComponentStatisticsInterface) Class.forName(className).newInstance();
            	Collection v = myCompo.getVolume(spaceId, ci.getId());
            
            	c = agregateUser(v);
            }
        }
        catch (ClassNotFoundException ce)
        {
            SilverTrace.info("silverstatistics", "SilverStatisticsVolumeAlimentation.getCollectionUserIdCountVolume()", "silverstatistics.EX_SUPPLY_VOLUME_COMPONENT_NOT_FOUND", "component = " + ci.getName(), ce);
        }
        catch (Exception e)
        {
            SilverTrace.error("silverstatistics", "SilverStatisticsVolumeAlimentation.getCollectionUserIdCountVolume()", "silverstatistics.EX_SUPPLY_VOLUME_COMPONENT_FAILED", "component = " + ci.getName(), e);
        }
        return c;
    }

    private static String getComponentStatisticsClassName(String componentName)
    {
        String componentStatisticsClassName = null;

        try
        {
            componentStatisticsClassName = resources.getString(componentName);
        }
        catch (MissingResourceException e)
        {
            // in order to trigger the ClassNotFoundException of the
            // getCollectionUserIdCountVolume method
            componentStatisticsClassName = null;
            SilverTrace.error("silverstatistics", "SilverStatisticsVolumeAlimentation.getCollectionUserIdCountVolume()", "silverstatistics.EX_SUPPLY_VOLUME_COMPONENT_FAILED", "No statistic implementation class for component '" + componentName+"'");
        }

        return componentStatisticsClassName;
    }

    private static Collection agregateUser(Collection in)
    {
                
        if (in == null)
        	return null;
        	
        ArrayList  myArrayList = new ArrayList();
        
        Iterator   iter1 = in.iterator();
        // parcours collection initiale
        while (iter1.hasNext())
        {
			// lecture d'un userId			
			// s'il n'existe pas dans la collection finale alors on l'ajoute
			// sinon on modifie le countVolume et on passe au suivant
			
			UserIdCountVolumeCouple       eltIn = (UserIdCountVolumeCouple) iter1.next();
			UserIdCountVolumeCouple       eltOut = getCouple(myArrayList, eltIn);
			
			SilverTrace.debug("silverstatistics", "SilverStatisticsVolumeAlimentation.agregateUser)", "eltIn.getUserId() = " + eltIn.getUserId() + "eltIn.getCountVolume() = " + eltIn.getCountVolume() );
			
			if (eltOut==null)
			{
        	    myArrayList.add(eltIn);
        	    SilverTrace.debug("silverstatistics", "SilverStatisticsVolumeAlimentation.agregateUser)", "add eltIn");
        	}
        	else
        	{
				eltOut.setCountVolume(eltIn.getCountVolume() + eltOut.getCountVolume());
				SilverTrace.debug("silverstatistics", "SilverStatisticsVolumeAlimentation.agregateUser)", "eltOut.getUserId() = " + eltOut.getUserId() + "eltOut.getCountVolume() = " + eltOut.getCountVolume() );
			}
				
		}
        
        return myArrayList;
    }

    private static UserIdCountVolumeCouple getCouple(Collection in, UserIdCountVolumeCouple eltIn)
    {
		Iterator   iter2 = in.iterator();
        while (iter2.hasNext())
	    {
	    	UserIdCountVolumeCouple       elt = (UserIdCountVolumeCouple) iter2.next();
	    	if (elt.getUserId().equals(eltIn.getUserId()))
	    	{
	    		return elt;
	    	}
	    }
	    return null;
	}			

}
