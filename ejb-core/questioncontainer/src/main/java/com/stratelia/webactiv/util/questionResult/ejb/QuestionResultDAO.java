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

package com.stratelia.webactiv.util.questionResult.ejb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.silverpeas.util.ForeignPK;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.answer.model.AnswerPK;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.questionResult.model.QuestionResult;
import com.stratelia.webactiv.util.questionResult.model.QuestionResultPK;
import com.stratelia.webactiv.util.questionResult.model.QuestionResultRuntimeException;

/**
 * This class is made to access database only (table SB_Question_Answer)
 * @author neysseri
 */
public class QuestionResultDAO {

  public static final String QUESTIONRESULTCOLUMNNAMES =
      "qrId, questionId, userId, answerId, qrOpenAnswer, qrNbPoints, qrPollDate, qrElapsedTime, qrParticipationId";
  private static SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy/MM/dd");

  private static QuestionResult getQuestionResultFromResultSet(ResultSet rs, ForeignPK questionPK)
      throws SQLException {
    SilverTrace.info(
        "questionResult",
        "QuestionResultDAO.getQuestionResultFromResultSet()",
        "root.MSG_GEN_ENTER_METHOD",
        "questionPK =" + questionPK);
    String id = new Integer(rs.getInt(1)).toString();
    String questionId = new Integer(rs.getInt(2)).toString();
    String userId = rs.getString(3);
    String answerId = new Integer(rs.getInt(4)).toString();
    String openAnswer = rs.getString(5);
    int nbPoints = rs.getInt(6);
    String pollDate = rs.getString(7);
    int elapsedTime = rs.getInt(8);
    int participationId = rs.getInt(9);
    QuestionResult result =
        new QuestionResult(
        new QuestionResultPK(id, questionPK),
        new ForeignPK(questionId, questionPK),
        new AnswerPK(answerId, questionPK),
        userId,
        openAnswer,
        nbPoints,
        pollDate,
        elapsedTime,
        participationId);

    return result;
  }

  public static Collection<QuestionResult> getUserQuestionResultsToQuestion(Connection con,
      String userId,
      ForeignPK questionPK)
      throws SQLException {
    SilverTrace.info(
        "questionResult",
        "QuestionResultDAO.getUserQuestionResultsToQuestion()",
        "root.MSG_GEN_ENTER_METHOD",
        "userId = " + userId + ", questionPK =" + questionPK);
    ResultSet rs = null;
    QuestionResult questionResult = null;
    String tableName = new QuestionResultPK("", questionPK).getTableName();

    String selectStatement =
        "select "
        + QUESTIONRESULTCOLUMNNAMES
        + " from "
        + tableName
        + " where questionId = ? "
        + " and userId = ? order By answerId";

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setInt(1, new Integer(questionPK.getId()).intValue());
      prepStmt.setString(2, userId);
      rs = prepStmt.executeQuery();
      List<QuestionResult> result = new ArrayList<QuestionResult>();
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
    SilverTrace.info("questionResult", "QuestionResultDAO.getUserQuestionResultsToQuestion()",
        "root.MSG_GEN_ENTER_METHOD", "answerId = " + answerId);
    ResultSet rs = null;
    String tableName = "SB_Question_QuestionResult";

    String selectStatement = "select userId from " + tableName + " where answerId = ? ";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setInt(1, new Integer(answerId).intValue());
      rs = prepStmt.executeQuery();
      ArrayList<String> result = new ArrayList<String>();
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
      Connection con,
      String userId,
      ForeignPK questionPK,
      int participationId)
      throws SQLException {
    SilverTrace.info(
        "questionResult",
        "QuestionResultDAO.getUserQuestionResultsToQuestionByParticipation()",
        "root.MSG_GEN_ENTER_METHOD",
        "userId = " + userId + ", questionPK =" + questionPK + ", participationId = " +
        participationId);
    ResultSet rs = null;
    QuestionResult questionResult = null;
    String tableName = new QuestionResultPK("", questionPK).getTableName();

    String selectStatement =
        "select "
        + QUESTIONRESULTCOLUMNNAMES
        + " from "
        + tableName
        + " where questionId = ? "
        + " and userId = ? "
        + " and qrParticipationId = ? ";

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setInt(1, new Integer(questionPK.getId()).intValue());
      prepStmt.setString(2, userId);
      prepStmt.setInt(3, participationId);
      rs = prepStmt.executeQuery();
      List<QuestionResult> result = new ArrayList<QuestionResult>();
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
      ForeignPK questionPK)
      throws SQLException {
    SilverTrace.info(
        "questionResult",
        "QuestionResultDAO.getQuestionResultToQuestion()",
        "root.MSG_GEN_ENTER_METHOD",
        "questionPK =" + questionPK);
    ResultSet rs = null;
    QuestionResult questionResult = null;
    String tableName = new QuestionResultPK("", questionPK).getTableName();

    String selectStatement =
        "select " + QUESTIONRESULTCOLUMNNAMES + " from " + tableName + " where questionId = ? ";

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setInt(1, new Integer(questionPK.getId()).intValue());
      rs = prepStmt.executeQuery();
      List<QuestionResult> result = new ArrayList<QuestionResult>();
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
      Connection con,
      ForeignPK questionPK,
      int participationId)
      throws SQLException {
    SilverTrace.info(
        "questionResult",
        "QuestionResultDAO.getQuestionResultToQuestionByParticipation()",
        "root.MSG_GEN_ENTER_METHOD",
        "questionPK =" + questionPK + ", participationId = " + participationId);
    ResultSet rs = null;
    QuestionResult questionResult = null;
    String tableName = new QuestionResultPK("", questionPK).getTableName();

    String selectStatement =
        "select "
        + QUESTIONRESULTCOLUMNNAMES
        + " from "
        + tableName
        + " where questionId = ? "
        + " and qrParticipationId = ? ";

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setInt(1, new Integer(questionPK.getId()).intValue());
      prepStmt.setInt(2, participationId);
      rs = prepStmt.executeQuery();
      List<QuestionResult> result = new ArrayList<QuestionResult>();
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
    SilverTrace.info(
        "questionResult",
        "QuestionResultDAO.setQuestionResultToUser()",
        "root.MSG_GEN_ENTER_METHOD",
        "questionResult =" + result);

    int newId = 0;
    String tableName = new QuestionResultPK("", result.getQuestionPK()).getTableName();
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
      /* Recherche de la nouvelle PK de la table */
      newId = DBUtil.getNextId(tableName, "qrId");
    } catch (UtilException ue) {
      throw new QuestionResultRuntimeException(
          "QuestionResultDAO.setQuestionResultToUser()",
          SilverpeasRuntimeException.ERROR,
          "root.EX_GET_NEXTID_FAILED",
          ue);
    }

    String selectStatement = "insert into " + tableName + " values (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setInt(1, newId);
      prepStmt.setInt(2, new Integer(questionId).intValue());
      prepStmt.setString(3, userId);
      prepStmt.setInt(4, new Integer(answerId).intValue());
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

  public static void setQuestionResultsToUser(Connection con, Collection results) {
  }

  public static void deleteQuestionResultToQuestion(Connection con, ForeignPK questionPK)
      throws SQLException {
    SilverTrace.info(
        "questionResult",
        "QuestionResultDAO.deleteQuestionResultToQuestion()",
        "root.MSG_GEN_ENTER_METHOD",
        "questionPK =" + questionPK);
    QuestionResultPK questionResultPK = new QuestionResultPK("unknown", questionPK);

    String deleteStatement =
        "delete from " + questionResultPK.getTableName() + " where questionId = ? ";

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(deleteStatement);
      prepStmt.setInt(1, new Integer(questionPK.getId()).intValue());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

}