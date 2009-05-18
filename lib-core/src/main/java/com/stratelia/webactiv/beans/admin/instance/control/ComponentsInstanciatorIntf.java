//Source file: D:\\Silverpeas\\Cabernet\\Bus\\admin\\JavaLib\\com\\stratelia\\webactiv\\beans\\admin\\instance\\control\\ComponentsInstanciatorIntf.java

package com.stratelia.webactiv.beans.admin.instance.control;

import java.sql.Connection;

/**
 * Interface for the instanciator component class.
 * An instanciator creates and deletes components on a space for a user.
 * @author Joaquim Vieira
 */
public interface ComponentsInstanciatorIntf 
{
    /** The name of the component descriptor parameter holding the process file name */
    public static final String PROCESS_XML_FILE_NAME = "XMLFileName";

			
			/**
			 * Create a new instance of the component for a requested user and space.
			 * @param connection - Connection to the database used to save the create 
			 * information.
			 * @param spaceId - Identity of the space where the component will be instancied.
			 * @param componentId - Identity of the component to instanciate.
			 * @param userId - Identity of the user who want the component
			 * @throws 
			 * com.stratelia.webactiv.beans.admin.instance.control.InstanciationException
			 * @roseuid 3B82286B0236
			 */
			public void create(Connection connection, String spaceId, String componentId, String userId) throws InstanciationException;
			
			/**
			 * Delete the component instance created for the user on the requested space.
			 * @param connection - Connection to the database where the create information 
			 * will be destroyed.
			 * @param spaceId - Identity of the space where the instanced component will be 
			 * deleted.
			 * @param componentId - Identity of the instanced component
			 * @param userId - Identity of the user who have instantiate the component.
			 * @throws 
			 * com.stratelia.webactiv.beans.admin.instance.control.InstanciationException
			 * @roseuid 3B8228740117
			 */
			public void delete(Connection connection, String spaceId, String componentId, String userId) throws InstanciationException;
}
