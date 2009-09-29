/*
 * NewsInstanciator.java
 *
 * Created on 13 juillet 2000, 09:54
 */

package com.stratelia.webactiv.contact;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.stratelia.webactiv.beans.admin.SQLRequest;
import com.stratelia.webactiv.beans.admin.instance.control.InstanciationException;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * 
 * @author squere
 * @version update by the Sébastien Antonio - Externalisation of the SQL request
 */
public class ContactInstanciator extends SQLRequest {
  private static ResourceLocator settings = new ResourceLocator(
      "com.stratelia.webactiv.contact.contactSettings", "fr");

  /** Creates new ContactInstanciator */
  public ContactInstanciator() {
  }

  public ContactInstanciator(String fullPathName) {
    super("com.stratelia.webactiv.contact");
  }

  public void create(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    try {
      // Create the attachments directory on the server disk
      createAttachmentsAndImagesDirectory(spaceId, componentId);
    } catch (Exception e) {
      throw new InstanciationException("ContactInstanciator.create()",
          InstanciationException.ERROR, "root.CREATING_DATA_DIRECTORY_FAILED",
          "componentId = " + componentId, e);
    }
  }

  public void delete(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    // read the property file which contains all SQL queries to delete rows
    setDeleteQueries();
    deleteDataOfInstance(con, componentId, "InfoAttachment");
    deleteDataOfInstance(con, componentId, "InfoImage");
    deleteDataOfInstance(con, componentId, "InfoText");
    deleteDataOfInstance(con, componentId, "InfoLink");
    deleteDataOfInstance(con, componentId, "contactfather");
    deleteDataOfInstance(con, componentId, "Contact");
    deleteDataOfInstance(con, componentId, "Info");
    try {
      // Delete the attachments directory on the server disk
      deleteAttachmentsAndImagesDirectory(spaceId, componentId);
    } catch (Exception e) {
      throw new InstanciationException("ContactInstanciator.delete()",
          InstanciationException.ERROR, "root.DELETING_DATA_DIRECTORY_FAILED",
          "componentId = " + componentId, e);
    }
  }

  /**
   * Delete all data of one instance from the table.
   * 
   * @param con
   *          (Connection) the connection to the data base
   * @param componentId
   *          (String) the instance id of the Silverpeas component .
   * @param suffixName
   *          (String) the suffixe of a table
   */
  private void deleteDataOfInstance(Connection con, String componentId,
      String suffixName) throws InstanciationException {
    Statement stmt = null;
    // get the delete query from the external file
    String deleteQuery = getDeleteQuery(componentId, suffixName);
    // execute the delete query
    try {
      stmt = con.createStatement();
      stmt.executeUpdate(deleteQuery);
      stmt.close();
    } catch (SQLException se) {
      throw new InstanciationException(
          "ContactInstanciator.deleteDataOfInstance",
          InstanciationException.ERROR, "root.EX_TABLE_DELETE_FAILED",
          "componentId = " + componentId + " delete query =" + deleteQuery, se);
    } finally {
      try {
        stmt.close();
      } catch (SQLException err_closeStatement) {
        throw new InstanciationException(
            "ContactInstanciator.deleteDataOfInstance",
            InstanciationException.ERROR,
            "root.EX_GEN_CONNECTION_CLOSE_FAILED", "componentId = "
                + componentId + " delete query =" + deleteQuery,
            err_closeStatement);
      }
    }

  }

  private void createAttachmentsAndImagesDirectory(String spaceId,
      String componentId) throws java.lang.Exception {
    // Create the subdirectory for the attachments
    FileRepositoryManager.createAbsolutePath(componentId, settings
        .getString("attachmentsSubDirectory"));
    // Create the subdirectory for the images
    FileRepositoryManager.createAbsolutePath(componentId, settings
        .getString("imagesSubDirectory"));
  }

  private void deleteAttachmentsAndImagesDirectory(String spaceId,
      String componentId) throws java.lang.Exception {
    // Delete the subdirectory for the attachments
    FileRepositoryManager.deleteAbsolutePath(spaceId, componentId, settings
        .getString("attachmentsSubDirectory"));
    // Delete the subdirectory for the images
    FileRepositoryManager.deleteAbsolutePath(spaceId, componentId, settings
        .getString("imagesSubDirectory"));
  }
}