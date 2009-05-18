/*
 * PublicationInstanciator.java
 *
 * Created on 13 juillet 2000, 09:54
 */

package com.stratelia.webactiv.publication;


import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.wysiwyg.WysiwygInstanciator;
import com.stratelia.webactiv.beans.admin.SQLRequest;
import com.stratelia.webactiv.beans.admin.instance.control.InstanciationException;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 *
 * @author  squere
 * @version
 * update by the Sébastien Antonio - Externalisation of the SQL request
 */
public class PublicationInstanciator extends SQLRequest {
	private static ResourceLocator settings = new ResourceLocator("com.stratelia.webactiv.util.publication.publicationSettings", "fr");

    /** Creates new PublicationInstanciator */
    public PublicationInstanciator(){}

	public PublicationInstanciator(String fullPathName){
		super("com.stratelia.webactiv.publication");
	}

    public void create(Connection con, String spaceId, String componentId, String userId)
      throws InstanciationException
    {
		SilverTrace.info("publication", "PublicationInstanciator.create()", "root.MSG_GEN_ENTER_METHOD");
	
        try {
            //Create the attachments directory on the server disk
            createAttachmentsAndImagesDirectory(spaceId, componentId);
        } catch (Exception e) {
			throw new InstanciationException("PublicationInstanciator.create()", SilverpeasException.ERROR, "root.EX_CANT_CREATE_FILE", e);
        }

        // PCH le 8/6/2001
        WysiwygInstanciator wysiwygI = new WysiwygInstanciator("com.stratelia.webactiv.publication");
        wysiwygI.create(con, spaceId, componentId, userId);

        SilverTrace.info("publication", "PublicationInstanciator.create()", "root.root.MSG_GEN_EXIT_METHOD");
    }

    public void delete(Connection con, String spaceId, String componentId, String userId)
      throws InstanciationException
    {
        SilverTrace.info("publication", "PublicationInstanciator.delete()", "root.MSG_GEN_ENTER_METHOD");

		// read the property file which contains all SQL queries to delete rows
		setDeleteQueries();

		deleteDataOfInstance(con, componentId, "InfoAttachment");
		deleteDataOfInstance(con, componentId, "InfoImage");
		deleteDataOfInstance(con, componentId, "InfoText");
		deleteDataOfInstance(con, componentId, "InfoLink");
		deleteDataOfInstance(con, componentId, "publicationfather");
		deleteDataOfInstance(con, componentId, "Publication");
		deleteDataOfInstance(con, componentId, "Info");
	
        try {
            //Delete the attachments directory on the server disk
            deleteAttachmentsAndImagesDirectory(spaceId, componentId);
        } catch (Exception e) {
			//No exceptions are throwed still attachments directory is no more used
            //throw new InstanciationException("PublicationInstanciator.delete()", SilverpeasException.ERROR, "root.EX_CANT_DELETE_FILE", e);
        }

        WysiwygInstanciator wysiwygI = new WysiwygInstanciator("com.stratelia.webactiv.publication");
        wysiwygI.delete(con, spaceId, componentId, userId);

        SilverTrace.info("publication", "PublicationInstanciator.delete()", "root.root.MSG_GEN_EXIT_METHOD");
    }


    private void createAttachmentsAndImagesDirectory(String spaceId, String componentId) throws java.lang.Exception {
        SilverTrace.info("publication", "PublicationInstanciator.createAttachmentsAndImagesDirectory()", "root.MSG_GEN_ENTER_METHOD");

        //Create the subdirectory for the attachments
        //FileRepositoryManager.createAbsolutePath(spaceId, componentId, settings.getString("attachmentsSubDirectory"));
        //Create the subdirectory for the images
        FileRepositoryManager.createAbsolutePath(componentId, settings.getString("imagesSubDirectory"));
    }

    private void deleteAttachmentsAndImagesDirectory(String spaceId, String componentId) throws java.lang.Exception {
        SilverTrace.info("publication", "PublicationInstanciator.deleteAttachmentsAndImagesDirectory()", "root.MSG_GEN_ENTER_METHOD");

        //Delete the subdirectory for the attachments
        FileRepositoryManager.deleteAbsolutePath(spaceId, componentId, settings.getString("attachmentsSubDirectory"));
        //Delete the subdirectory for the images
        FileRepositoryManager.deleteAbsolutePath(spaceId, componentId, settings.getString("imagesSubDirectory"));
    }

	/**
	* Delete all data of one forum instance from the forum table.
	* @param con (Connection) the connection to the data base
	* @param componentId (String) the instance id of the Silverpeas component forum.
	* @param suffixName (String) the suffixe of a Forum table
	*/
	private void deleteDataOfInstance(Connection con, String componentId, String suffixName) throws InstanciationException {
		Statement stmt = null;
		
		// get the delete query from the external file
		String deleteQuery = getDeleteQuery(componentId,suffixName);
		
		// execute the delete query
		try {
			stmt = con.createStatement();
		    stmt.executeUpdate(deleteQuery);
		} catch (SQLException se) {
		    throw new InstanciationException("PublicationInstanciator.deleteDataOfInstance()", SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", se);
		}
		finally {
			try{
				stmt.close();
			}
			catch (SQLException err_closeStatement){
				SilverTrace.error("publication", "PublicationInstanciator.deleteDataOfInstance()", "root.EX_RESOURCE_CLOSE_FAILED", "", err_closeStatement);
			}
		}		
	}
}