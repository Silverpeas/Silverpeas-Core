/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.question;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.silverpeas.admin.components.InstanciationException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.SQLRequest;
import org.silverpeas.util.FileRepositoryManager;
import org.silverpeas.util.ResourceLocator;
import org.silverpeas.util.SettingBundle;
import org.silverpeas.util.exception.SilverpeasException;

public class QuestionInstanciator extends SQLRequest {
  private static SettingBundle settings =
      ResourceLocator.getSettingBundle("org.silverpeas.question.questionSettings");

  public QuestionInstanciator() {
  }

  public QuestionInstanciator(String fullPathName) {
    super("org.silverpeas.question");
  }

  public void create(Connection con, String spaceId, String componentId, String userId)
      throws InstanciationException {

    try {
      // Create the images directory on the server disk
      createImagesDirectory(spaceId, componentId);
    } catch (Exception e) {
      throw new InstanciationException("QuestionInstanciator.create()", SilverpeasException.ERROR,
          "question.CREATING_IMAGES_DIRECTORY_FAILED", e);
    }

  }

  public void delete(Connection con, String spaceId, String componentId, String userId)
      throws InstanciationException {


    // read the property file which contains all SQL queries to delete rows
    setDeleteQueries();

    deleteDataOfInstance(con, componentId, "Answer");
    deleteDataOfInstance(con, componentId, "QuestionResult");
    deleteDataOfInstance(con, componentId, "Score");
    deleteDataOfInstance(con, componentId, "Question");

    try {
      // Delete the images directory on the server disk
      deleteImagesDirectory(spaceId, componentId);
    } catch (Exception e) {
      throw new InstanciationException("QuestionInstanciator.delete()", SilverpeasException.ERROR,
          "question.DELETING_IMAGES_DIRECTORY_FAILED", e);
    }


  }

  private void createImagesDirectory(String spaceId, String componentId)
      throws java.lang.Exception {
    // Create the subdirectory for the images
    FileRepositoryManager.createAbsolutePath(componentId, settings.getString("imagesSubDirectory"));
  }

  private void deleteImagesDirectory(String spaceId, String componentId)
      throws java.lang.Exception {
    // Delete the subdirectory for the images
    FileRepositoryManager
        .deleteAbsolutePath(spaceId, componentId, settings.getString("imagesSubDirectory"));
  }

  /**
   * Delete all data of one forum instance from the forum table.
   * @param con (Connection) the connection to the data base
   * @param componentId (String) the instance id of the Silverpeas component forum.
   * @param suffixName (String) the suffixe of a Forum table
   */
  private void deleteDataOfInstance(Connection con, String componentId, String suffixName)
      throws InstanciationException {

    Statement stmt = null;

    // get the delete query from the external file
    String deleteQuery = getDeleteQuery(componentId, suffixName);

    // execute the delete query
    try {
      stmt = con.createStatement();
      stmt.executeUpdate(deleteQuery);
      stmt.close();
    } catch (SQLException se) {
      throw new InstanciationException("QuestionInstanciator.create()", SilverpeasException.ERROR,
          "question.DELETING_DATA_OF_INSTANCE_FAILED", se);
    } finally {
      try {
        stmt.close();
      } catch (SQLException err_closeStatement) {
        SilverTrace.error("question", "QuestionInstanciator.deleteDataOfInstance()",
            "root.EX_RESOURCE_CLOSE_FAILED", "", err_closeStatement);
      }
    }
  }

}