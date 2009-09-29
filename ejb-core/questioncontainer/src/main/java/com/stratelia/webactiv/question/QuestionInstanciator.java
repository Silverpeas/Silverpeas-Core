/*
 * NewsInstanciator.java
 *
 * Created on 13 juillet 2000, 09:54
 */

package com.stratelia.webactiv.question;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.SQLRequest;
import com.stratelia.webactiv.beans.admin.instance.control.InstanciationException;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * 
 * @author squere
 * @version update by the Sébastien Antonio - Externalisation of the SQL request
 */
public class QuestionInstanciator extends SQLRequest {
  private static ResourceLocator settings = new ResourceLocator(
      "com.stratelia.webactiv.util.question.questionSettings", "fr");

  public QuestionInstanciator() {
  }

  public QuestionInstanciator(String fullPathName) {
    super("com.stratelia.webactiv.util.question");
  }

  public void create(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("question", "QuestionInstanciator.create()",
        "root.MSG_GEN_ENTER_METHOD", "spaceId = " + spaceId
            + ", componentId = " + componentId);
    try {
      // Create the images directory on the server disk
      createImagesDirectory(spaceId, componentId);
    } catch (Exception e) {
      throw new InstanciationException("QuestionInstanciator.create()",
          SilverpeasException.ERROR,
          "question.CREATING_IMAGES_DIRECTORY_FAILED", e);
    }
    SilverTrace.info("question", "QuestionInstanciator.create()",
        "root.MSG_GEN_EXIT_METHOD", "spaceId = " + spaceId + ", componentId = "
            + componentId);
  }

  public void delete(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("question", "QuestionInstanciator.delete()",
        "root.MSG_GEN_ENTER_METHOD", "spaceId = " + spaceId
            + ", componentId = " + componentId);

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
      throw new InstanciationException("QuestionInstanciator.delete()",
          SilverpeasException.ERROR,
          "question.DELETING_IMAGES_DIRECTORY_FAILED", e);
    }

    SilverTrace.info("question", "QuestionInstanciator.delete()",
        "root.MSG_GEN_EXIT_METHOD", "spaceId = " + spaceId + ", componentId = "
            + componentId);
  }

  private void createImagesDirectory(String spaceId, String componentId)
      throws java.lang.Exception {
    // Create the subdirectory for the images
    FileRepositoryManager.createAbsolutePath(componentId, settings
        .getString("imagesSubDirectory"));
  }

  private void deleteImagesDirectory(String spaceId, String componentId)
      throws java.lang.Exception {
    // Delete the subdirectory for the images
    FileRepositoryManager.deleteAbsolutePath(spaceId, componentId, settings
        .getString("imagesSubDirectory"));
  }

  /**
   * Delete all data of one forum instance from the forum table.
   * 
   * @param con
   *          (Connection) the connection to the data base
   * @param componentId
   *          (String) the instance id of the Silverpeas component forum.
   * @param suffixName
   *          (String) the suffixe of a Forum table
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
      throw new InstanciationException("QuestionInstanciator.create()",
          SilverpeasException.ERROR,
          "question.DELETING_DATA_OF_INSTANCE_FAILED", se);
    } finally {
      try {
        stmt.close();
      } catch (SQLException err_closeStatement) {
        SilverTrace.error("question",
            "QuestionInstanciator.deleteDataOfInstance()",
            "root.EX_RESOURCE_CLOSE_FAILED", "", err_closeStatement);
      }
    }

  }

}