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

package org.silverpeas.core.questioncontainer.question.dao;

import org.silverpeas.core.questioncontainer.question.model.Question;
import org.silverpeas.core.questioncontainer.question.model.QuestionPK;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.questioncontainer.question.model.QuestionRuntimeException;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.exception.SilverpeasRuntimeException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class is made to access database only (table SB_Question_Question)
 * @author neysseri
 */
public class QuestionDAO {

  public static final String QUESTIONCOLUMNNAMES =
      "questionId, qcId, questionLabel, questionDescription, questionClue, questionImage, " +
          "questionIsQCM, questionType, questionIsOpen, questionCluePenalty, questionMaxTime, " +
          "questionDisplayOrder, questionNbPointsMin, questionNbPointsMax, instanceId, style";
  private static final String DELETE_ALL_QUESTIONS =
      "DELETE FROM SB_Question_Question WHERE instanceId = ?";

  /**
   * Build a Question object with data from the resultset
   * @param rs the Resultset which contains data
   * @param questionPK the context
   * @return a Question
   * @throws SQLException
   */
  private static Question getQuestionFromResultSet(ResultSet rs, QuestionPK questionPK)
      throws SQLException {
    String id = rs.getString(1);
    String fatherId = rs.getString(2);
    String label = rs.getString(3);
    String description = rs.getString(4);
    String clue = rs.getString(5);
    String image = rs.getString(6);
    boolean isQCM = false;

    if (rs.getInt(7) == 1) {
      isQCM = true;
    }
    int type = rs.getInt(8);
    boolean isOpen = false;

    if (rs.getInt(9) == 1) {
      isOpen = true;
    }
    int cluePenalty = rs.getInt(10);
    int maxTime = rs.getInt(11);
    int displayOrder = rs.getInt(12);
    int nbPointsMin = rs.getInt(13);
    int nbPointsMax = rs.getInt(14);
    // String instanceId = rs.getString(15); // not used but inside result set
    String style = rs.getString(16);

    Question result =
        new Question(new QuestionPK(id, questionPK), fatherId, label, description, clue, image,
            isQCM, type, isOpen, cluePenalty, maxTime, displayOrder, nbPointsMin, nbPointsMax,
            style);

    return result;
  }

  /**
   * Return a question
   * @param con the Connection to dataBase
   * @param questionPK the question id
   * @return a Question
   * @throws SQLException
   */
  public static Question getQuestion(Connection con, QuestionPK questionPK) throws SQLException {

    ResultSet rs = null;
    Question question = null;

    String selectStatement =
        "select " + QUESTIONCOLUMNNAMES + " from " + questionPK.getTableName() +
            " where questionId = ?";

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setInt(1, Integer.parseInt(questionPK.getId()));
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        question = getQuestionFromResultSet(rs, questionPK);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return question;
  }

  /**
   * Return the questions linked to a given father
   * @param con the Connection to dataBase
   * @param questionPK the question id
   * @param fatherId the father id
   * @return a Collection of Question
   * @throws SQLException
   */
  public static Collection<Question> getQuestionsByFatherPK(Connection con, QuestionPK questionPK,
      String fatherId) throws SQLException {
    SilverTrace
        .info("question", "QuestionDAO.getQuestionsByFatherPK()", "root.MSG_GEN_ENTER_METHOD",
            "questionPK = " + questionPK + ", fatherId = " + fatherId);

    List<Question> result = new ArrayList<>();
    ResultSet rs = null;
    Question question;

    String selectStatement =
        "select " + QUESTIONCOLUMNNAMES + " from " + questionPK.getTableName() +
            " where qcId = ? and instanceId = ? ORDER BY questionDisplayOrder";

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setInt(1, Integer.parseInt(fatherId));
      prepStmt.setString(2, questionPK.getComponentName());
      rs = prepStmt.executeQuery();

      while (rs.next()) {
        question = getQuestionFromResultSet(rs, questionPK);
        result.add(question);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return result;
  }

  /**
   * Create a new question
   * @param con the Connection to dataBase
   * @param question the Question to create
   * @return the QuestionPK of the new question
   * @throws SQLException
   */
  public static QuestionPK createQuestion(Connection con, Question question) throws SQLException {

    int newId;

    String insertStatement = "insert into " + (question.getPK()).getTableName() +
        " values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";

    try {
      // Get new PK identifier
      newId = DBUtil.getNextId(question.getPK().getTableName(), "questionId");
    } catch (Exception e) {
      throw new QuestionRuntimeException("QuestionDAO.createQuestion()",
          SilverpeasRuntimeException.ERROR, "root.EX_GET_NEXTID_FAILED", e);
    }

    QuestionPK questionPK = question.getPK();

    questionPK.setId(Integer.toString(newId));

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(insertStatement);
      prepStmt.setInt(1, newId);
      prepStmt.setInt(2, Integer.parseInt(question.getFatherId()));
      prepStmt.setString(3, question.getLabel());
      prepStmt.setString(4, question.getDescription());
      prepStmt.setString(5, question.getClue());
      prepStmt.setString(6, question.getImage());
      if (question.isQCM()) {
        prepStmt.setInt(7, 1);
      } else {
        prepStmt.setInt(7, 0);
      }
      prepStmt.setInt(8, question.getType());
      if (question.isOpen()) {
        prepStmt.setInt(9, 1);
      } else {
        prepStmt.setInt(9, 0);
      }
      prepStmt.setInt(10, question.getCluePenalty());
      prepStmt.setInt(11, question.getMaxTime());
      prepStmt.setInt(12, question.getDisplayOrder());
      prepStmt.setInt(13, question.getNbPointsMin());
      prepStmt.setInt(14, question.getNbPointsMax());
      prepStmt.setString(15, question.getPK().getComponentName());
      prepStmt.setString(16, question.getStyle());

      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }

    return questionPK;
  }

  /**
   * Update a question
   * @param con the Connection to dataBase
   * @param question the Question to update
   * @throws SQLException
   */
  public static void updateQuestion(Connection con, Question question) throws SQLException {


    String insertStatement = "update " + question.getPK().getTableName() +
        " set questionLabel = ?, questionDescription = ?, questionClue = ?," +
        " questionImage = ?, questionIsQCM = ?, questionType = ?, questionIsOpen = ?," +
        " questionCluePenalty = ?, questionMaxTime = ?, questionDisplayOrder = ?," +
        " questionNbPointsMin = ?, questionNbPointsMax = ?, instanceId = ?, style = ? " +
        " where questionId = ?";

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(insertStatement);
      prepStmt.setString(1, question.getLabel());
      prepStmt.setString(2, question.getDescription());
      prepStmt.setString(3, question.getClue());
      prepStmt.setString(4, question.getImage());
      if (question.isQCM()) {
        prepStmt.setInt(5, 1);
      } else {
        prepStmt.setInt(5, 0);
      }
      prepStmt.setInt(6, question.getType());
      if (question.isOpen()) {
        prepStmt.setInt(7, 1);
      } else {
        prepStmt.setInt(7, 0);
      }
      prepStmt.setInt(8, question.getCluePenalty());
      prepStmt.setInt(9, question.getMaxTime());
      prepStmt.setInt(10, question.getDisplayOrder());
      prepStmt.setInt(11, question.getNbPointsMin());
      prepStmt.setInt(12, question.getNbPointsMax());
      prepStmt.setString(13, question.getPK().getComponentName());
      prepStmt.setString(14, question.getStyle());
      prepStmt.setInt(15, Integer.parseInt(question.getPK().getId()));

      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Delete a question
   * @param con the Connection to dataBase
   * @param questionPK the question id
   * @throws SQLException
   */
  public static void deleteQuestion(Connection con, QuestionPK questionPK) throws SQLException {


    String deleteStatement = "DELETE FROM sb_question_question WHERE questionId = ? ";

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(deleteStatement);
      prepStmt.setInt(1, Integer.parseInt(questionPK.getId()));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Delete all questions linked to a given father
   * @param con the Connection to dataBase
   * @param questionPK to know the context
   * @param fatherId the father id
   * @throws SQLException
   */
  public static void deleteQuestionsByFatherPK(Connection con, QuestionPK questionPK,
      String fatherId) throws SQLException {
    SilverTrace
        .info("question", "QuestionDAO.deleteQuestionsByFatherPK()", "root.MSG_GEN_ENTER_METHOD",
            "questionPK = " + questionPK + ", fatherId = " + fatherId);

    String deleteStatement = "DELETE FROM sb_question_question WHERE qcId = ? AND instanceId = ?";

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(deleteStatement);
      prepStmt.setInt(1, Integer.parseInt(fatherId));
      prepStmt.setString(2, questionPK.getComponentName());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Deletes all the questions relative to the specified component instance.
   * @param con the connection to the database.
   * @param instanceId the unique identifier of the component instance.
   * @throws SQLException if an error occurs while deleting the questions.
   */
  public static void deleteAllQuestionsByInstanceId(Connection con, String instanceId)
      throws SQLException {
    try(PreparedStatement deletion = con.prepareStatement(DELETE_ALL_QUESTIONS)) {
      deletion.setString(1, instanceId);
      deletion.execute();
    }
  }

}