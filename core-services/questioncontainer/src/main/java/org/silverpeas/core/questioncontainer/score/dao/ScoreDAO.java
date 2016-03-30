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

package org.silverpeas.core.questioncontainer.score.dao;

import org.silverpeas.core.questioncontainer.score.model.ScoreDetail;
import org.silverpeas.core.questioncontainer.score.model.ScorePK;
import org.silverpeas.core.questioncontainer.score.model.ScoreRuntimeException;
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
 * Score Data Access Object layer
 */
public class ScoreDAO {

  public static final String SCORECOLUMNNAMES =
      "scoreId, qcId, userId, scoreParticipationId, scoreScore, scoreElapsedTime," +
          "scoreParticipationDate,scoreSuggestion";

  private static final String DELETE_SCORE_FOR_QUESTION =
      "DELETE FROM sb_question_score WHERE qcid = ? ";

  private static final String AVERAGE_SCORE_FOR_QUESTION =
      "SELECT SUM(scoreScore) FROM sb_question_score WHERE qcId = ? ";

  private static final String ADD_SCORE_FOR_QUESTION =
      "INSERT INTO sb_question_score values(?, ?, ?, ?, ?, ?, ?, ?) ";

  private static final String SELECT_SCORE_BY_FATHER_ID = "SELECT " + SCORECOLUMNNAMES +
      " FROM SB_Question_Score WHERE qcId = ? ORDER BY scoreScore DESC";

  private static final String DELETE_SCORES_FOR_ALL_QUESTIONS =
      "DELETE FROM SB_Question_Score WHERE qcId in (SELECT QC.qcId FROM SB_Question_Question Q, " +
          "SB_QuestionContainer_QC QC WHERE QC.qcId = Q.qcId AND Q.instanceId = ?)";

  /**
   * @param rs
   * @param scorePK the score identifier
   * @return
   * @throws SQLException
   */
  private static ScoreDetail getScoreFromResultSet(ResultSet rs, ScorePK scorePK)
      throws SQLException {
    String id = Integer.toString(rs.getInt(1));
    String fatherId = Integer.toString(rs.getInt(2));
    String userId = rs.getString(3);
    int participationId = rs.getInt(4);
    int score = rs.getInt(5);
    int elapsedTime = rs.getInt(6);
    String participationDate = rs.getString(7);
    String suggestion = rs.getString(8);

    ScoreDetail result =
        new ScoreDetail(new ScorePK(id, scorePK), fatherId, userId, participationId,
            participationDate, score, elapsedTime, suggestion);

    return result;
  }

  /**
   * @param con the database connection
   * @param scoreDetail
   * @throws SQLException
   */
  public static void addScore(Connection con, ScoreDetail scoreDetail) throws SQLException {
    int newId;

    try {
      // get new score identifier
      newId = DBUtil.getNextId(scoreDetail.getScorePK().getTableName(), "scoreId");
    } catch (SQLException ue) {
      throw new ScoreRuntimeException("ScoreDAO.addScore()", SilverpeasRuntimeException.ERROR,
          "score.EX_RECORD_GETNEXTID_FAILED", "id = " + scoreDetail.getScorePK().getId());
    }

    ScorePK scorePK = scoreDetail.getScorePK();

    scorePK.setId(Integer.toString(newId));

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(ADD_SCORE_FOR_QUESTION);
      prepStmt.setInt(1, newId);
      prepStmt.setInt(2, Integer.parseInt(scoreDetail.getFatherId()));
      prepStmt.setString(3, scoreDetail.getUserId());
      prepStmt.setInt(4, scoreDetail.getParticipationId());
      prepStmt.setInt(5, scoreDetail.getScore());
      prepStmt.setInt(6, scoreDetail.getElapsedTime());
      prepStmt.setString(7, scoreDetail.getParticipationDate());
      prepStmt.setString(8, scoreDetail.getSuggestion());

      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }

  }

  /**
   * @param con the database connection
   * @param scoreDetail
   * @throws SQLException
   */
  public static void updateScore(Connection con, ScoreDetail scoreDetail) throws SQLException {
    String insertStatement = "update " + scoreDetail.getScorePK().getTableName() +
        " set scoreSuggestion = ? where scoreId = ?";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(insertStatement);
      prepStmt.setString(1, scoreDetail.getSuggestion());
      prepStmt.setInt(2, Integer.parseInt(scoreDetail.getScorePK().getId()));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * detele score identified by given scorePk parameter
   * @param con the database connection
   * @param scorePK the score identifier
   * @throws SQLException
   */
  public static void deleteScore(Connection con, ScorePK scorePK) throws SQLException {
    String deleteStatement = "delete from " + scorePK.getTableName() + " where scoreId = ? ";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(deleteStatement);
      prepStmt.setInt(1, Integer.parseInt(scorePK.getId()));
      prepStmt.executeUpdate();
      prepStmt.close();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * @param con the database connection
   * @param scorePK the score identifier
   * @param fatherId the father idenfifier
   * @throws SQLException
   */
  public static void deleteScoreByFatherPK(Connection con, ScorePK scorePK, String fatherId)
      throws SQLException {

    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(DELETE_SCORE_FOR_QUESTION);
      prepStmt.setInt(1, Integer.parseInt(fatherId));
      prepStmt.executeUpdate();
      prepStmt.close();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Deletes the scores of all the questions in the specified component instance.
   * @param con a connection to the data source into which are stored the scores.
   * @param instanceId the unique identifier of the component instance.
   * @throws SQLException if an error occurs while deleting the scores.
   */
  public static void deleteAllScoresByInstanceId(Connection con, String instanceId)
      throws SQLException {
    try (PreparedStatement deletion = con.prepareStatement(DELETE_SCORES_FOR_ALL_QUESTIONS)) {
      deletion.setString(1, instanceId);
      deletion.execute();
    }
  }

  /**
   *
   * @param con the database connection
   * @param scorePK the score identifier
   * @return
   * @throws SQLException
   */
  public static Collection<ScoreDetail> getAllScores(Connection con, ScorePK scorePK)
      throws SQLException {
    ResultSet rs = null;
    ScoreDetail scoreDetail;
    String selectStatement = "select " + SCORECOLUMNNAMES + " from " + scorePK.getTableName() +
        " order by scoreParticipationDate desc";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(selectStatement);
      rs = prepStmt.executeQuery();
      List<ScoreDetail> result = new ArrayList<>();

      while (rs.next()) {
        scoreDetail = getScoreFromResultSet(rs, scorePK);
        result.add(scoreDetail);
      }
      return result;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  /**
   *
   * @param con the database connection
   * @param scorePK the score identifier
   * @param userId the user identifier
   * @return
   * @throws SQLException
   */
  public static Collection<ScoreDetail> getUserScores(Connection con, ScorePK scorePK,
      String userId) throws SQLException {
    ResultSet rs = null;
    ScoreDetail scoreDetail;
    String selectStatement = "select " + SCORECOLUMNNAMES + " from " + scorePK.getTableName() +
        " where userId = ? order by scoreParticipationDate desc";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setInt(1, Integer.parseInt(userId));
      rs = prepStmt.executeQuery();
      List<ScoreDetail> result = new ArrayList<>();
      while (rs.next()) {
        scoreDetail = getScoreFromResultSet(rs, scorePK);
        result.add(scoreDetail);
      }
      return result;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  /**
   *
   * @param con the database connection
   * @param scorePK the score identifier
   * @param fatherId the father idenfifier
   * @param userId the user identifier
   * @return
   * @throws SQLException
   */
  public static Collection<ScoreDetail> getUserScoresByFatherId(Connection con, ScorePK scorePK,
      String fatherId, String userId) throws SQLException {
    ResultSet rs = null;
    ScoreDetail scoreDetail;
    String selectStatement = "select " + SCORECOLUMNNAMES + " from " + scorePK.getTableName() +
        " where qcId = ? and userId=? order by scoreParticipationDate desc";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setInt(1, Integer.parseInt(fatherId));
      prepStmt.setString(2, userId);
      rs = prepStmt.executeQuery();
      List<ScoreDetail> result = new ArrayList<>();
      while (rs.next()) {
        scoreDetail = getScoreFromResultSet(rs, scorePK);
        result.add(scoreDetail);
      }
      return result;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  /**
   * Method declaration
   * @param con the database connection
   * @param scorePK the score identifier
   * @param fatherId the father idenfifier
   * @return
   * @throws SQLException
   * @see
   */
  public static Collection<ScoreDetail> getScoresByFatherId(Connection con, ScorePK scorePK,
      String fatherId) throws SQLException {
    ResultSet rs = null;
    ScoreDetail scoreDetail;
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(SELECT_SCORE_BY_FATHER_ID);
      prepStmt.setInt(1, Integer.parseInt(fatherId));
      rs = prepStmt.executeQuery();
      List<ScoreDetail> result = new ArrayList<>();
      while (rs.next()) {
        scoreDetail = getScoreFromResultSet(rs, scorePK);
        result.add(scoreDetail);
      }
      return result;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  /**
   *
   * @param con the database connection
   * @param scorePK the score identifier
   * @param nbBestScores
   * @param fatherId the father idenfifier
   * @return
   * @throws SQLException
   */
  public static Collection<ScoreDetail> getBestScoresByFatherId(Connection con, ScorePK scorePK,
      int nbBestScores, String fatherId) throws SQLException {
    ResultSet rs = null;
    ScoreDetail scoreDetail;
    int nbRecord = 0;
    String selectStatement = "select " + SCORECOLUMNNAMES + " from " + scorePK.getTableName() +
        " where qcId = ? order by scoreScore desc";

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setInt(1, Integer.parseInt(fatherId));
      rs = prepStmt.executeQuery();
      List<ScoreDetail> result = new ArrayList<>();
      while ((rs.next()) && (nbRecord < nbBestScores)) {
        scoreDetail = getScoreFromResultSet(rs, scorePK);
        result.add(scoreDetail);
        nbRecord++;
      }
      return result;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  /**
   *
   * @param con the database connection
   * @param scorePK the score identifier
   * @param nbWorstScores
   * @param fatherId the father idenfifier
   * @return
   * @throws SQLException
   */
  public static Collection<ScoreDetail> getWorstScoresByFatherId(Connection con, ScorePK scorePK,
      int nbWorstScores, String fatherId) throws SQLException {
    ResultSet rs = null;
    ScoreDetail scoreDetail;
    int nbRecord = 0;
    String selectStatement = "select " + SCORECOLUMNNAMES + " from " + scorePK.getTableName() +
        " where qcId = ? order by scoreScore";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setInt(1, Integer.parseInt(fatherId));
      rs = prepStmt.executeQuery();
      List<ScoreDetail> result = new ArrayList<>();
      while ((rs.next()) && (nbRecord < nbWorstScores)) {
        scoreDetail = getScoreFromResultSet(rs, scorePK);
        result.add(scoreDetail);
        nbRecord++;
      }
      return result;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  /**
   *
   * @param con the database connection
   * @param scorePK the score identifier
   * @param fatherId the father idenfifier
   * @return
   * @throws SQLException
   */
  public static int getNbVotersByFatherId(Connection con, ScorePK scorePK, String fatherId)
      throws SQLException {
    ResultSet rs = null;
    String selectStatement = "select count(*) from " + scorePK.getTableName() + " where qcId = ?";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setInt(1, Integer.parseInt(fatherId));
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        return rs.getInt(1);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return 0;
  }

  /**
   *
   * @param con the database connection
   * @param scorePK the score identifier
   * @param fatherId the father idenfifier
   * @return
   * @throws SQLException
   */
  public static float getAverageScoreByFatherId(Connection con, ScorePK scorePK, String fatherId)
      throws SQLException {
    int nbVoters = getNbVotersByFatherId(con, scorePK, fatherId);
    float average = 0;
    int sumPoints;

    if (nbVoters > 0) {
      ResultSet rs = null;
      PreparedStatement prepStmt = null;

      try {
        prepStmt = con.prepareStatement(AVERAGE_SCORE_FOR_QUESTION);
        prepStmt.setInt(1, Integer.parseInt(fatherId));
        rs = prepStmt.executeQuery();
        if (rs.next()) {
          sumPoints = rs.getInt(1);
          average = Math.round((sumPoints / nbVoters) * 10) / 10;
        }
      } finally {
        DBUtil.close(rs, prepStmt);
      }

    }
    return average;
  }

  /**
   *
   * @param con the database connection
   * @param scorePK the score identifier
   * @param fatherId the father idenfifier
   * @param userId the user identifier
   * @param participationId
   * @return
   * @throws SQLException
   */
  public static ScoreDetail getUserScoreByFatherIdAndParticipationId(Connection con,
      ScorePK scorePK, String fatherId, String userId, int participationId) throws SQLException {
    ResultSet rs = null;
    String selectStatement = "select " + SCORECOLUMNNAMES + " from " + scorePK.getTableName() +
        " where qcId = ? and userId = ? and scoreParticipationId= ?";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setInt(1, Integer.parseInt(fatherId));
      prepStmt.setString(2, userId);
      prepStmt.setInt(3, participationId);
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        return getScoreFromResultSet(rs, scorePK);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return null;
  }

  /**
   *
   * @param con the database connection
   * @param scorePK the score identifier
   * @param fatherId the father idenfifier
   * @param userId the user identifier
   * @return
   * @throws SQLException
   */
  public static int getUserNbParticipationsByFatherId(Connection con, ScorePK scorePK,
      String fatherId, String userId) throws SQLException {
    ResultSet rs = null;
    String selectStatement =
        "select count(*) from " + scorePK.getTableName() + " where qcId = ? and userId = ?";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setInt(1, Integer.parseInt(fatherId));
      prepStmt.setString(2, userId);
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        return rs.getInt(1);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return 0;
  }

  /**
   * @param con the database connection
   * @param scorePK the score identifier
   * @param fatherId
   * @param userId the user identifier
   * @param participationId
   * @return
   * @throws SQLException
   */
  public static int getUserPositionByFatherIdAndParticipationId(Connection con, ScorePK scorePK,
      String fatherId, String userId, int participationId) throws SQLException {
    Collection<ScoreDetail> scoreDetails = getScoresByFatherId(con, scorePK, fatherId);
    int position = 0;
    int nbPosition = 0;
    String previousScore = null;

    for (final ScoreDetail scoreDetail : scoreDetails) {
      if ((previousScore != null) && (scoreDetail.getScore() == Integer.parseInt(previousScore))) {
        nbPosition++;
      } else {
        position += nbPosition + 1;
        nbPosition = 0;
      }

      if ((scoreDetail.getUserId().equals(userId)) &&
          (scoreDetail.getParticipationId() == participationId)) {
        return position;
      }
      previousScore = Integer.toString(scoreDetail.getScore());

    }
    return 0;
  }

}
