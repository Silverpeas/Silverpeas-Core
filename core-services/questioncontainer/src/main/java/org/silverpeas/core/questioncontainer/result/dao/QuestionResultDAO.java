/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.questioncontainer.result.dao;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.questioncontainer.answer.model.AnswerPK;
import org.silverpeas.core.questioncontainer.result.model.QuestionResult;
import org.silverpeas.core.questioncontainer.result.model.QuestionResultPK;
import org.silverpeas.core.questioncontainer.result.model.Results;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is made to access database only (table SB_Question_Answer)
 * @author neysseri
 */
public class QuestionResultDAO {

  public static final String QUESTIONRESULTCOLUMNNAMES =
      "qrId, questionId, userId, answerId, qrOpenAnswer, qrNbPoints, qrPollDate, qrElapsedTime, " +
          "qrParticipationId";
  private static final String DELETE_ALL_QUESTION_RESULTS =
      "DELETE FROM SB_Question_QuestionResult  WHERE questionId in (SELECT questionId FROM " +
          "SB_Question_Question WHERE instanceId = ?)";
  private static final String SELECT = "select ";
  private static final String FROM = " from ";
  private static final String WHERE_QUESTION_ID_EQUALITY = " where questionId = ? ";

  private static final FastDateFormat formatter = FastDateFormat.getInstance("yyyy/MM/dd");

  /**
   * Hidden constructor.
   */
  private QuestionResultDAO() {
  }

  private static QuestionResult getQuestionResultFromResultSet(ResultSet rs, ResourceReference questionPK)
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

    return new QuestionResult(new QuestionResultPK(id, questionPK),
        new ResourceReference(questionId, questionPK.getInstanceId()),
        new AnswerPK(answerId, questionPK), userId,
        openAnswer, nbPoints, pollDate, elapsedTime, participationId);
  }

  public static Collection<QuestionResult> getUserQuestionResultsToQuestion(Connection con,
      String userId, ResourceReference questionPK) throws SQLException {
    String tableName = new QuestionResultPK("", questionPK).getTableName();

    String selectStatement =
        SELECT + QUESTIONRESULTCOLUMNNAMES + FROM + tableName + WHERE_QUESTION_ID_EQUALITY +
            " and userId = ? order By answerId";

    try (PreparedStatement prepStmt = con.prepareStatement(selectStatement)) {
      prepStmt.setInt(1, Integer.parseInt(questionPK.getId()));
      prepStmt.setString(2, userId);
      try (ResultSet rs = prepStmt.executeQuery()) {
        List<QuestionResult> result = new ArrayList<>();
        while (rs.next()) {
          QuestionResult questionResult = getQuestionResultFromResultSet(rs, questionPK);
          result.add(questionResult);
        }
        return result;
      }
    }
  }

  public static Collection<String> getUsersByAnswer(Connection con, String answerId)
      throws SQLException {
    String tableName = "SB_Question_QuestionResult";

    String selectStatement = "select userId from " + tableName + " where answerId = ? ";

    try (PreparedStatement prepStmt = con.prepareStatement(selectStatement)) {
      prepStmt.setInt(1, Integer.parseInt(answerId));
      try (ResultSet rs = prepStmt.executeQuery()) {
        List<String> result = new ArrayList<>();
        while (rs.next()) {
          String userId = rs.getString(1);
          result.add(userId);
        }
        return result;
      }
    }
  }

  public static Collection<QuestionResult> getUserQuestionResultsToQuestionByParticipation(
      Connection con, String userId, ResourceReference questionPK, int participationId)
      throws SQLException {
    String tableName = new QuestionResultPK("", questionPK).getTableName();

    String selectStatement =
        SELECT + QUESTIONRESULTCOLUMNNAMES + FROM + tableName + WHERE_QUESTION_ID_EQUALITY +
            " and userId = ? " + " and qrParticipationId = ? ";

    try (PreparedStatement prepStmt = con.prepareStatement(selectStatement)) {
      prepStmt.setInt(1, Integer.parseInt(questionPK.getId()));
      prepStmt.setString(2, userId);
      prepStmt.setInt(3, participationId);
      try (ResultSet rs = prepStmt.executeQuery()) {
        List<QuestionResult> result = new ArrayList<>();
        while (rs.next()) {
          QuestionResult questionResult = getQuestionResultFromResultSet(rs, questionPK);
          result.add(questionResult);
        }
        return result;
      }
    }
  }

  public static Collection<QuestionResult> getQuestionResultToQuestion(Connection con,
      ResourceReference questionPK) throws SQLException {

    String tableName = new QuestionResultPK("", questionPK).getTableName();

    String selectStatement =
        SELECT + QUESTIONRESULTCOLUMNNAMES + FROM + tableName + WHERE_QUESTION_ID_EQUALITY;

    try(PreparedStatement prepStmt = con.prepareStatement(selectStatement)) {
      prepStmt.setInt(1, Integer.parseInt(questionPK.getId()));
      try (ResultSet rs = prepStmt.executeQuery()) {
        List<QuestionResult> result = new ArrayList<>();
        while (rs.next()) {
          QuestionResult questionResult = getQuestionResultFromResultSet(rs, questionPK);
          result.add(questionResult);
        }
        return result;
      }
    }
  }

  public static Collection<QuestionResult> getQuestionResultToQuestionByParticipation(
      Connection con, ResourceReference questionPK, int participationId) throws SQLException {
    String tableName = new QuestionResultPK("", questionPK).getTableName();

    String selectStatement =
        SELECT + QUESTIONRESULTCOLUMNNAMES + FROM + tableName + WHERE_QUESTION_ID_EQUALITY +
            " and qrParticipationId = ? ";

    try(PreparedStatement prepStmt = con.prepareStatement(selectStatement)) {
      prepStmt.setInt(1, Integer.parseInt(questionPK.getId()));
      prepStmt.setInt(2, participationId);
      try(ResultSet rs = prepStmt.executeQuery()) {
        List<QuestionResult> result = new ArrayList<>();
        while (rs.next()) {
          QuestionResult questionResult = getQuestionResultFromResultSet(rs, questionPK);
          result.add(questionResult);
        }
        return result;
      }
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
    String voteDate = result.getVoteDate() == null ? formatter.format(new java.util.Date()) :
        result.getVoteDate();
    int elapsedTime = result.getElapsedTime();
    int participationId = result.getParticipationId();

    // get new identifier
    newId = DBUtil.getNextId("sb_question_questionresult", "qrId");

    String selectStatement =
        "INSERT INTO sb_question_questionresult VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    try (PreparedStatement prepStmt = con.prepareStatement(selectStatement)) {
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
    }
  }

  public static void deleteQuestionResultToQuestion(Connection con, ResourceReference questionPK)
      throws SQLException {
    String deleteStatement = "DELETE FROM sb_question_questionresult WHERE questionId = ? ";
    try (PreparedStatement prepStmt = con.prepareStatement(deleteStatement)) {
      prepStmt.setInt(1, Integer.parseInt(questionPK.getId()));
      prepStmt.executeUpdate();
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
      ResourceReference questionPK, AnswerPK answerPK) throws SQLException {
    String tableName = "SB_Question_QuestionResult";

    String selectStatement =
        SELECT + QUESTIONRESULTCOLUMNNAMES + FROM + tableName + WHERE_QUESTION_ID_EQUALITY +
            " and answerId = ? " + " and userId = ? ";

    try (PreparedStatement prepStmt = con.prepareStatement(selectStatement)) {
      prepStmt.setInt(1, Integer.parseInt(questionPK.getId()));
      prepStmt.setInt(2, Integer.parseInt(answerPK.getId()));
      prepStmt.setString(3, userId);
      try (ResultSet rs = prepStmt.executeQuery()) {
        if (rs.next()) {
          return getQuestionResultFromResultSet(rs, questionPK);
        }
        return null;
      }
    }
  }

  public static Results getResults(Connection con, List<ResourceReference> pks) throws SQLException {
    String tableName = new QuestionResultPK("").getTableName();
    Results results = new Results();

    List<String> ids = pks.stream().map(ResourceReference::getId).collect(Collectors.toList());
    ResourceReference firstQuestionPK = pks.get(0);

    String selectStatement =
        SELECT + QUESTIONRESULTCOLUMNNAMES + FROM + tableName + " where questionId in (" +
            StringUtils.join(ids, ',') + ") order by answerId asc";

    try (PreparedStatement prepStmt = con.prepareStatement(selectStatement);
         ResultSet rs = prepStmt.executeQuery()) {
      while (rs.next()) {
        QuestionResult questionResult = getQuestionResultFromResultSet(rs, firstQuestionPK);
        results.addQuestionResult(questionResult);
      }
      return results;
    }
  }

}
