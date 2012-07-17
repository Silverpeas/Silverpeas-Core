/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.util.attachment;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.silverpeas.admin.components.InstanciationException;
import com.stratelia.webactiv.beans.admin.SQLRequest;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;

/*
 * CVS Informations
 *
 * $Id: AttachmentInstanciator.java,v 1.5 2006/01/17 16:04:30 neysseri Exp $
 *
 * $Log: AttachmentInstanciator.java,v $
 * Revision 1.5  2006/01/17 16:04:30  neysseri
 * Suppression des warnings
 *
 * Revision 1.4  2004/11/17 19:44:46  neysseri
 * Amélioration + nettoyage sources
 *
 * Revision 1.3  2004/06/22 15:03:13  neysseri
 * renommage des includes + nettoyage source (eclipse)
 *
 * Revision 1.2  2002/10/09 07:41:03  neysseri
 * no message
 *
 * Revision 1.1.1.1.6.1  2002/09/27 08:05:09  abudnikau
 * Remove debug
 *
 * Revision 1.1.1.1  2002/08/06 14:47:40  nchaix
 * no message
 *
 * Revision 1.6  2001/12/31 15:44:21  groccia
 * stabilisation
 *
 */

/**
 * Class declaration
 * @author
 */
public class AttachmentInstanciator extends SQLRequest {

  /**
   * to create the attachment directory
   */
  private final String[] sDirectory = { "Attachment" };

  /**
   * constructor
   */
  public AttachmentInstanciator() {
    super("com.stratelia.webactiv.util.attachment");
  }

  /**
   * Constructor declaration
   * @param fullPathName
   * @see
   */
  public AttachmentInstanciator(String fullPathName) {
    super("com.stratelia.webactiv.util.attachment");
  }

  /**
   * to create Attachment table in data base
   */

  /*
   * public void create(Connection con, String spaceId, String componentId, String userId) throws
   * InstanciationException { Debug.debug(600, "AttachmentInstanciator.create()",
   * "enter with: space=" + spaceId + " componentId=" + componentId, null, null);
   * createAttachmentDirectory(spaceId, componentId); Debug.debug(600,
   * "AttachmentInstanciator.create()", "finished", null, null); }
   */
  public void create(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    createAttachmentDirectory(spaceId, componentId);
  }

  /**
   * Creates an attachment directory for the upload files
   * @param spaceId (String) the id of the silverpeas space
   * @param componentId (String) the id of the instance of the job'Peas
   * @throws InstanciationException
   */

  private void createAttachmentDirectory(String spaceId, String componentId)
      throws InstanciationException {
    try {
      // to create attachment directory
      String pathName = FileRepositoryManager.getAbsolutePath(componentId,
          sDirectory);
      File d = new File(pathName);

      if (!d.exists()) {
        FileFolderManager.createFolder(pathName);
      }
    } catch (Exception e) {
      InstanciationException ie = new InstanciationException(
          "AttachmentInstanciator.createAttachmentDirectory(String spaceId, String componentId)",
          SilverpeasException.ERROR, "root.CREATING_DATA_DIRECTORY_FAILED ",
          "componentId : " + componentId + ", spaceId=" + spaceId);

      throw ie;
    }
  }

  /**
   * Method declaration
   * @param con
   * @param spaceId
   * @param componentId
   * @param userId
   * @throws com.silverpeas.admin.components.InstanciationException
   * @see
   */
  public void delete(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    // read the property file which contains all SQL queries to delete rows
    setDeleteQueries();
    deleteDataOfInstance(con, componentId, "Attachment");
    deleteAttachmentDirectory(spaceId, componentId);
  }

  /**
   * deletes the attachment directory
   * @param spaceId (String) the id of the silverpeas space
   * @param componentId (String) the id of the instance of the job'Peas
   * @throws InstanciationException
   */
  private void deleteAttachmentDirectory(String spaceId, String componentId)
      throws InstanciationException {
    try {
      // delete the attachment directory
      String pathName = FileRepositoryManager.getAbsolutePath(componentId,
          sDirectory);
      File d = new File(pathName);

      if (d.exists()) {
        FileFolderManager.deleteFolder(pathName);
      }
    } catch (Exception e) {
      InstanciationException ie = new InstanciationException(
          "AttachmentInstanciator.deleteAttachmentDirectory(String spaceId, String componentId)",
          SilverpeasException.ERROR, "root.DELETING_DATA_DIRECTORY_FAILED",
          "componentId : " + componentId + ", spaceId=" + spaceId);

      throw ie;
    }
  }

  /**
   * Delete all data from the attachment table.
   * @param con (Connection) the connection to the data base
   * @param componentId (String) the instance id of the Silverpeas component forum.
   * @param suffixName (String) the suffixe of a Forum table
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
      InstanciationException ie = new InstanciationException(
          "AttachmentInstanciator.deleteDataOfInstance()",
          SilverpeasException.ERROR, "root.DELETING_DATA_OF_INSTANCE_FAILED",
          "componentId : " + componentId + ", delete query = " + deleteQuery,
          se);

      throw ie;
    } finally {
      try {
        stmt.close();
      } catch (SQLException err_closeStatement) {
        InstanciationException ie = new InstanciationException(
            "AttachmentInstanciator.deleteDataOfInstance()",
            SilverpeasException.ERROR, "root.EX_RESOURCE_CLOSE_FAILED", "");

        throw ie;
      }
    }

  }

}