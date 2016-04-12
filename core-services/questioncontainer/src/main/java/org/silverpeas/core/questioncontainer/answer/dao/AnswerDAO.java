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

package org.silverpeas.core.questioncontainer.answer.dao;

import org.silverpeas.core.questioncontainer.answer.model.Answer;
import org.silverpeas.core.questioncontainer.answer.model.AnswerPK;
import org.silverpeas.core.questioncontainer.answer.model.AnswerRuntimeException;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.exception.SilverpeasRuntimeException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class is made to access database only (table SB_Question_Answer)
 * @author neysseri
 */
public class AnswerDAO {

  public static final String ANSWERCOLUMNNAMES =
      "answerId, questionId, answerLabel, answerNbPoints, answerIsSolution, answerComment, " +
          "answerNbVoters, answerIsOpened, answerImage, answerQuestionLink";

  private static final String SQL_DELETE_QUESTION_ANSWERS =
      "delete from SB_Question_Answer where questionId = ? ";

  private static final String SQL_DELETE_QUESTION_ANSWER =
      "delete from SB_Question_Answer where questionId = ? and answerId = ? ";

  private static final String SQL_INSERT_ANSWER =
      "insert into SB_Question_Answer values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

  private static final String SQL_DELETE_ALL_QUESTION_ANSWERS =
      "DELETE FROM SB_Question_Answer  WHERE questionId in (SELECT questionId FROM " +
          "SB_Question_Question WHERE instanceId = ?)";

  /**
   * Build an Answer objet with data containing in the Resulset
   * @param rs the Resultset which contains data from database
   * @param answerPK an AnswerPK to know context (spaceId, componentId)
   * @return an Answer
   * @throws SQLException
   */
  private static Answer getAnswerFromResultSet(ResultSet rs, AnswerPK answerPK)
      throws SQLException {
    String id = rs.getString(1);
    String questionId = rs.getString(2);
    String label = rs.getString(3);
    int nbPoints = rs.getInt(4);
    boolean isSolution = false;

    if (rs.getInt(5) != 0) {
      isSolution = true;
    }
    String comment = rs.getString(6);
    int nbVoters = rs.getInt(7);
    boolean isOpened = false;

    if (rs.getInt(8) != 0) {
      isOpened = true;
    }
    String image = rs.getString(9);
    String questionLink = rs.getString(10);
    Answer result =
        new Answer(new AnswerPK(id, answerPK), new ForeignPK(questionId, answerPK), label, nbPoints,
            isSolution, comment, nbVoters, isOpened, image, questionLink);

    return result;
  }

  /**
   * Get answers which composed the question
   * @param con the Connection
   * @param questionPK the QuestionPK (question id)
   * @return a Collection of Answer
   * @throws SQLException
   */
  public static Collection<Answer> getAnswersByQuestionPK(Connection con, ForeignPK questionPK)
      throws SQLException {

    ResultSet rs = null;
    Answer answer;
    AnswerPK answerPK = new AnswerPK(null, questionPK);

    String selectStatement = "select " + ANSWERCOLUMNNAMES +
        " from SB_Question_Answer where questionId = ? order by answerId ";

    List<Answer> result = new ArrayList<>();
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setInt(1, Integer.parseInt(questionPK.getId()));
      rs = prepStmt.executeQuery();

      while (rs.next()) {
        answer = getAnswerFromResultSet(rs, answerPK);
        result.add(answer);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return result;
  }

  /**
   * Record that the answer (answerPK) has been chosen to the question (questionPK)
   * @param con the Connection
   * @param questionPK the QuestionPK (question id)
   * @param answerPK the AnswerPK (answer id)
   * @throws SQLException
   */
  public static void recordThisAnswerAsVote(Connection con, ForeignPK questionPK, AnswerPK answerPK)
      throws SQLException {


    String updateStatement =
        "update SB_Question_Answer set answerNbVoters = answerNbVoters + 1 where answerId = ? and" +
            " questionId = ?";

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(updateStatement);
      prepStmt.setInt(1, Integer.parseInt(answerPK.getId()));
      prepStmt.setInt(2, Integer.parseInt(questionPK.getId()));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Add some answers to a question
   * @param con the Connection
   * @param answers a Collection of Answer
   * @param questionPK the QuestionPK (question id)
   * @throws SQLException
   */
  public static void addAnswersToAQuestion(Connection con, Collection<Answer> answers,
      ForeignPK questionPK) throws SQLException {

    if (answers != null) {
      for (Answer answer : answers) {
        addAnswerToAQuestion(con, answer, questionPK);
      }
    }
  }

  /**
   * Add an answer to a question
   * @param con the Connection
   * @param answer the Answer
   * @param questionPK the QuestionPK (question id)
   * @throws SQLException
   */
  public static void addAnswerToAQuestion(Connection con, Answer answer, ForeignPK questionPK)
      throws SQLException {

    int newId;

    AnswerPK answerPK = new AnswerPK("unknown", questionPK);

    try {
      newId = DBUtil.getNextId(answerPK.getTableName(), "answerId");
    } catch (Exception e) {
      throw new AnswerRuntimeException("AnswerDAO.addAnswerToAQuestion()",
          SilverpeasRuntimeException.ERROR, "root.EX_GET_NEXTID_FAILED", e);
    }
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(SQL_INSERT_ANSWER);
      prepStmt.setInt(1, newId);
      prepStmt.setInt(2, Integer.parseInt(questionPK.getId()));
      prepStmt.setString(3, answer.getLabel());
      prepStmt.setInt(4, answer.getNbPoints());
      if (answer.isSolution()) {
        prepStmt.setInt(5, 1);
      } else {
        prepStmt.setInt(5, 0);
      }
      prepStmt.setString(6, answer.getComment());
      prepStmt.setInt(7, 0);
      if (answer.isOpened()) {
        prepStmt.setInt(8, 1);
      } else {
        prepStmt.setInt(8, 0);
      }
      prepStmt.setString(9, answer.getImage());
      prepStmt.setString(10, answer.getQuestionLink());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Delete all answers to a given question
   * @param con the Connection
   * @param questionPK the QuestionPK (question id)
   * @throws SQLException
   */
  public static void deleteAnswersToAQuestion(Connection con, ForeignPK questionPK)
      throws SQLException {

    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(SQL_DELETE_QUESTION_ANSWERS);
      prepStmt.setInt(1, Integer.parseInt(questionPK.getId()));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Delete an answer to a question
   * @param con the Connection
   * @param questionPK the QuestionPK (question id)
   * @param answerId the answer id
   * @throws SQLException
   */
  public static void deleteAnswerToAQuestion(Connection con, ForeignPK questionPK, String answerId)
      throws SQLException {

    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(SQL_DELETE_QUESTION_ANSWER);
      prepStmt.setInt(1, Integer.parseInt(questionPK.getId()));
      prepStmt.setInt(2, Integer.parseInt(answerId));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Deletes all the answers of all the questions relative to the specified component instance.
   * @param con the connection to the database.
   * @param instanceId the unique identifier of the component instance.
   * @throws SQLException if an error occurs while deleting the answers.
   */
  public static void deleteAnswersToAllQuestions(Connection con, String instanceId)
      throws SQLException {
    try(PreparedStatement deletion = con.prepareStatement(SQL_DELETE_ALL_QUESTION_ANSWERS)) {
      deletion.setString(1, instanceId);
      deletion.execute();
    }
  }

  /**
   * Update an answer to a question
   * @param con the Connection
   * @param questionPK the QuestionPK (question id)
   * @param answer the Answer
   * @throws SQLException
   */
  public static void updateAnswerToAQuestion(Connection con, ForeignPK questionPK, Answer answer)
      throws SQLException {


    String updateStatement =
        "update SB_Question_Answer set questionId = ?," + " set answerLabel = ?," +
            " set answerNbPoints = ?," + " set answerIsSolution = ?," + " set answerComment = ?," +
            " set answerNbVoters = ?," + " set answerIsOpened = ?," + " set answerImage = ?," +
            " set answerQuestionLink = ?," + " where answerId = ? ";

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(updateStatement);
      prepStmt.setInt(1, Integer.parseInt(questionPK.getId()));
      prepStmt.setString(2, answer.getLabel());
      prepStmt.setInt(3, answer.getNbPoints());

      if (answer.isSolution()) {
        prepStmt.setInt(4, 1);
      } else {
        prepStmt.setInt(4, 0);
      }
      prepStmt.setString(5, answer.getComment());
      prepStmt.setInt(6, answer.getNbVoters());

      if (answer.isOpened()) {
        prepStmt.setInt(7, 1);
      } else {
        prepStmt.setInt(7, 0);
      }
      prepStmt.setString(8, answer.getImage());
      prepStmt.setString(9, answer.getQuestionLink());
      prepStmt.setString(10, answer.getPK().getId());

      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

}