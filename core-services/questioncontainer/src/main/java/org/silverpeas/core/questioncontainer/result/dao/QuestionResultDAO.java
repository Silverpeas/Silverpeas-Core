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

package org.silverpeas.core.questioncontainer.result.dao;

import org.silverpeas.core.questioncontainer.result.model.QuestionResult;
import org.silverpeas.core.questioncontainer.result.model.QuestionResultRuntimeException;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.questioncontainer.answer.model.AnswerPK;
import org.silverpeas.core.questioncontainer.result.model.QuestionResultPK;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.exception.SilverpeasRuntimeException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class is made to access database only (table SB_Question_Answer)
 * @author neysseri
 */
public class QuestionResultDAO {

  public static final String QUESTIONRESULTCOLUMNNAMES =
      "qrId, questionId, userId, answerId, qrOpenAnswer, qrNbPoints, qrPollDate, qrElapsedTime, " +
          "qrParticipationId";
  private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
  private static final String DELETE_ALL_QUESTION_RESULTS =
      "DELETE FROM SB_Question_QuestionResult  WHERE questionId in (SELECT questionId FROM " +
          "SB_Question_Question WHERE instanceId = ?)";

  private static QuestionResult getQuestionResultFromResultSet(ResultSet rs, ForeignPK questionPK)
      throws SQLException {

    String id = Integer.toString(rs.getInt(1));
    String questionId = Integer.toString(rs.getInt(2));
    String userId = rs.getString(3);
    String answerId = Integer.toString(rs.getInt(4));
    String openAnswer = rs.getString(5);
    int nbPoints = rs.getInt(6);
    String pollDate = rs.getString(7);
    int elapsedTime = rs.getInt(8);
    int participationId = rs.getInt(9);
    QuestionResult result = new QuestionResult(new QuestionResultPK(id, questionPK),
        new ForeignPK(questionId, questionPK), new AnswerPK(answerId, questionPK), userId,
        openAnswer, nbPoints, pollDate, elapsedTime, participationId);

    return result;
  }

  public static Collection<QuestionResult> getUserQuestionResultsToQuestion(Connection con,
      String userId, ForeignPK questionPK) throws SQLException {

    ResultSet rs = null;
    QuestionResult questionResult;
    String tableName = new QuestionResultPK("", questionPK).getTableName();

    String selectStatement =
        "select " + QUESTIONRESULTCOLUMNNAMES + " from " + tableName + " where questionId = ? " +
            " and userId = ? order By answerId";

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setInt(1, Integer.parseInt(questionPK.getId()));
      prepStmt.setString(2, userId);
      rs = prepStmt.executeQuery();
      List<QuestionResult> result = new ArrayList<>();
      while (rs.next()) {
        questionResult = getQuestionResultFromResultSet(rs, questionPK);
        result.add(questionResult);
      }
      return result;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public static Collection<String> getUsersByAnswer(Connection con, String answerId)
      throws SQLException {

    ResultSet rs = null;
    String tableName = "SB_Question_QuestionResult";

    String selectStatement = "select userId from " + tableName + " where answerId = ? ";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setInt(1, Integer.parseInt(answerId));
      rs = prepStmt.executeQuery();
      List<String> result = new ArrayList<>();
      String userId;
      while (rs.next()) {
        userId = rs.getString(1);
        result.add(userId);
      }
      return result;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public static Collection<QuestionResult> getUserQuestionResultsToQuestionByParticipation(
      Connection con, String userId, ForeignPK questionPK, int participationId)
      throws SQLException {
    ResultSet rs = null;
    QuestionResult questionResult;
    String tableName = new QuestionResultPK("", questionPK).getTableName();

    String selectStatement =
        "select " + QUESTIONRESULTCOLUMNNAMES + " from " + tableName + " where questionId = ? " +
            " and userId = ? " + " and qrParticipationId = ? ";

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setInt(1, Integer.parseInt(questionPK.getId()));
      prepStmt.setString(2, userId);
      prepStmt.setInt(3, participationId);
      rs = prepStmt.executeQuery();
      List<QuestionResult> result = new ArrayList<>();
      while (rs.next()) {
        questionResult = getQuestionResultFromResultSet(rs, questionPK);
        result.add(questionResult);
      }
      return result;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public static Collection<QuestionResult> getQuestionResultToQuestion(Connection con,
      ForeignPK questionPK) throws SQLException {

    ResultSet rs = null;
    QuestionResult questionResult;
    String tableName = new QuestionResultPK("", questionPK).getTableName();

    String selectStatement =
        "select " + QUESTIONRESULTCOLUMNNAMES + " from " + tableName + " where questionId = ? ";

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setInt(1, Integer.parseInt(questionPK.getId()));
      rs = prepStmt.executeQuery();
      List<QuestionResult> result = new ArrayList<>();
      while (rs.next()) {
        questionResult = getQuestionResultFromResultSet(rs, questionPK);
        result.add(questionResult);
      }
      return result;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public static Collection<QuestionResult> getQuestionResultToQuestionByParticipation(
      Connection con, ForeignPK questionPK, int participationId) throws SQLException {
    SilverTrace
        .info("questionResult", "QuestionResultDAO.getQuestionResultToQuestionByParticipation()",
            "root.MSG_GEN_ENTER_METHOD",
            "questionPK =" + questionPK + ", participationId = " + participationId);
    ResultSet rs = null;
    QuestionResult questionResult;
    String tableName = new QuestionResultPK("", questionPK).getTableName();

    String selectStatement =
        "select " + QUESTIONRESULTCOLUMNNAMES + " from " + tableName + " where questionId = ? " +
            " and qrParticipationId = ? ";

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setInt(1, Integer.parseInt(questionPK.getId()));
      prepStmt.setInt(2, participationId);
      rs = prepStmt.executeQuery();
      List<QuestionResult> result = new ArrayList<>();
      while (rs.next()) {
        questionResult = getQuestionResultFromResultSet(rs, questionPK);
        result.add(questionResult);
      }
      return result;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public static void setQuestionResultToUser(Connection con, QuestionResult result)
      throws SQLException {


    int newId;
    String questionId = result.getQuestionPK().getId();
    String answerId = result.getAnswerPK().getId();
    String userId = result.getUserId();
    String openAnswer = result.getOpenedAnswer();
    int nbPoints = result.getNbPoints();
    String voteDate = result.getVoteDate();

    if (voteDate == null) {
      voteDate = formatter.format(new java.util.Date());
    }
    int elapsedTime = result.getElapsedTime();
    int participationId = result.getParticipationId();

    try {
      // get new identifier
      newId = DBUtil.getNextId("sb_question_questionresult", "qrId");
    } catch (SQLException ue) {
      throw new QuestionResultRuntimeException("QuestionResultDAO.setQuestionResultToUser()",
          SilverpeasRuntimeException.ERROR, "root.EX_GET_NEXTID_FAILED", ue);
    }

    String selectStatement =
        "INSERT INTO sb_question_questionresult VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setInt(1, newId);
      prepStmt.setInt(2, Integer.parseInt(questionId));
      prepStmt.setString(3, userId);
      prepStmt.setInt(4, Integer.parseInt(answerId));
      prepStmt.setString(5, openAnswer);
      prepStmt.setInt(6, nbPoints);
      prepStmt.setString(7, voteDate);
      prepStmt.setInt(8, elapsedTime);
      prepStmt.setInt(9, participationId);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void deleteQuestionResultToQuestion(Connection con, ForeignPK questionPK)
      throws SQLException {


    String deleteStatement = "DELETE FROM sb_question_questionresult WHERE questionId = ? ";
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(deleteStatement);
      prepStmt.setInt(1, Integer.parseInt(questionPK.getId()));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void setDeleteAllQuestionResultsByInstanceId(Connection con, String instanceId)
      throws SQLException {
    try (PreparedStatement deletion = con.prepareStatement(DELETE_ALL_QUESTION_RESULTS)) {
      deletion.setString(1, instanceId);
      deletion.execute();
    }
  }

  public static QuestionResult getUserAnswerToQuestion(Connection con, String userId,
      ForeignPK questionPK, AnswerPK answerPK) throws SQLException {

    ResultSet rs = null;
    String tableName = "SB_Question_QuestionResult";

    String selectStatement =
        "select " + QUESTIONRESULTCOLUMNNAMES + " from " + tableName + " where questionId = ? " +
            " and answerId = ? " + " and userId = ? ";

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setInt(1, Integer.parseInt(questionPK.getId()));
      prepStmt.setInt(2, Integer.parseInt(answerPK.getId()));
      prepStmt.setString(3, userId);
      rs = prepStmt.executeQuery();
      QuestionResult result = null;
      if (rs.next()) {
        result = getQuestionResultFromResultSet(rs, questionPK);
      }
      return result;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

}