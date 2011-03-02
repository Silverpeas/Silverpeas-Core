/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.contact;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.silverpeas.admin.components.InstanciationException;
import com.stratelia.webactiv.beans.admin.SQLRequest;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;

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
   * @param con (Connection) the connection to the data base
   * @param componentId (String) the instance id of the Silverpeas component .
   * @param suffixName (String) the suffixe of a table
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