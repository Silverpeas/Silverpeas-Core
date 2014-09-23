/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.score.control;

// Import Statements
import java.sql.Connection;
import java.util.Collection;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.score.ejb.ScoreDAO;
import com.stratelia.webactiv.score.model.ScoreDetail;
import com.stratelia.webactiv.score.model.ScorePK;
import com.stratelia.webactiv.score.model.ScoreRuntimeException;

/**
 * Class declaration
 *
 * @author
 */
@Stateless(name = "Score", description = "Stateless EJB to manage score in a question container.")
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class ScoreBmEJB implements ScoreBm {

  private static final long serialVersionUID = -8139560611772321449L;
  private String dbName = JNDINames.SCORE_DATASOURCE;

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  private Connection getConnection() {
    try {
      Connection con = DBUtil.makeConnection(dbName);
      return con;
    } catch (Exception re) {
      throw new ScoreRuntimeException("ScoreBmEJB.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", re);
    }
  }

  /**
   * Method declaration
   *
   * @param scorePK
   * @param fatherId
   * @param userId
   * @return
   * @see
   */
  @Override
  public int getUserNbParticipationsByFatherId(ScorePK scorePK, String fatherId, String userId) {
    Connection con = getConnection();
    try {
      return ScoreDAO.getUserNbParticipationsByFatherId(con, scorePK, fatherId, userId);
    } catch (Exception re) {
      throw new ScoreRuntimeException("ScoreBmEJB.getUserNbParticipationsByFatherId()",
          SilverpeasRuntimeException.ERROR, "score.EX_GET_USER_NB_PARTICIPATION_FAILED", re);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   *
   * @param scorePK
   * @param fatherId
   * @param userId
   * @param participationId
   * @return
   * @see
   */
  private int getUserPositionByFatherIdAndParticipationId(ScorePK scorePK, String fatherId,
      String userId, int participationId) {
    Connection con = getConnection();
    try {
      return ScoreDAO.getUserPositionByFatherIdAndParticipationId(con, scorePK, fatherId, userId,
          participationId);
    } catch (Exception e) {
      throw new ScoreRuntimeException("ScoreBmEJB.getUserPositionByFatherIdAndParticipationId()",
          SilverpeasRuntimeException.ERROR, "score.EX_GET_USER_POSITION_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   *
   * @param scoreDetails
   * @see
   */
  private void setPositions(Collection<ScoreDetail> scoreDetails) {
    for (ScoreDetail scoreDetail : scoreDetails) {
      scoreDetail.setPosition(getUserPositionByFatherIdAndParticipationId(scoreDetail.getScorePK(),
          scoreDetail.getFatherId(), scoreDetail.getUserId(), scoreDetail.getParticipationId()));
    }
  }

  /**
   * Method declaration
   *
   * @param scoreDetails
   * @see
   */
  private void setParticipations(Collection<ScoreDetail> scoreDetails) {
    for (ScoreDetail scoreDetail : scoreDetails) {
      scoreDetail.setNbParticipations(getUserNbParticipationsByFatherId(scoreDetail.getScorePK(),
          scoreDetail.getFatherId(), scoreDetail.getUserId()));
    }
  }

  /**
   * Method declaration
   *
   * @param scoreDetail
   * @see
   */
  private void setPosition(ScoreDetail scoreDetail) {
    scoreDetail.setPosition(getUserPositionByFatherIdAndParticipationId(scoreDetail.getScorePK(),
        scoreDetail.getFatherId(), scoreDetail.getUserId(), scoreDetail.getParticipationId()));
  }

  /**
   * Method declaration
   *
   * @param scoreDetail
   * @see
   */
  private void setNbParticipation(ScoreDetail scoreDetail) {
    scoreDetail.setNbParticipations(getUserNbParticipationsByFatherId(scoreDetail.getScorePK(),
        scoreDetail.getFatherId(), scoreDetail.getUserId()));
  }

  /**
   * Method declaration
   *
   * @param scoreDetail
   * @see
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  @Override
  public void addScore(ScoreDetail scoreDetail) {
    Connection con = getConnection();
    try {
      ScoreDAO.addScore(con, scoreDetail);
    } catch (Exception e) {
      throw new ScoreRuntimeException("ScoreBmEJB.addScore()",
          SilverpeasRuntimeException.ERROR, "score.EX_CREATE_SCORE_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   *
   * @param scoreDetail
   * @see
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  @Override
  public void updateScore(ScoreDetail scoreDetail) {
    Connection con = getConnection();
    try {
      ScoreDAO.updateScore(con, scoreDetail);
    } catch (Exception e) {
      throw new ScoreRuntimeException("ScoreBmEJB.updateScore()",
          SilverpeasRuntimeException.ERROR, "score.EX_UPDATE_SCORE_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   *
   * @param scorePK
   * @see
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  @Override
  public void deleteScore(ScorePK scorePK) {
    Connection con = getConnection();
    try {

      ScoreDAO.deleteScore(con, scorePK);
    } catch (Exception e) {
      throw new ScoreRuntimeException("ScoreBmEJB.deleteScore()",
          SilverpeasRuntimeException.ERROR, "score.EX_DELETE_SCORE_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  // Begin Modif une table par instance by hguig
  /**
   * Method declaration
   *
   * @param scorePK
   * @param fatherId
   * @see
   */
  @Override
  public void deleteScoreByFatherPK(ScorePK scorePK, String fatherId) {
    Connection con = getConnection();
    try {
      ScoreDAO.deleteScoreByFatherPK(con, scorePK, fatherId);
    } catch (Exception e) {
      throw new ScoreRuntimeException("ScoreBmEJB.deleteScoreByFatherPK()",
          SilverpeasRuntimeException.ERROR, "score.EX_DELETE_SCORE_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  // End Modif
  /**
   * Method declaration
   *
   * @param scorePK
   * @return
   * @see
   */
  @Override
  public Collection<ScoreDetail> getAllScores(ScorePK scorePK) {
    Connection con = getConnection();
    try {
      Collection<ScoreDetail> allScores = ScoreDAO.getAllScores(con, scorePK);
      setParticipations(allScores);
      setPositions(allScores);
      return allScores;
    } catch (Exception e) {
      throw new ScoreRuntimeException("ScoreBmEJB.getAllScores()",
          SilverpeasRuntimeException.ERROR, "score.EX_GET_ALL_SCORES_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   *
   * @param scorePK
   * @param userId
   * @return
   * @see
   */
  @Override
  public Collection<ScoreDetail> getUserScores(ScorePK scorePK, String userId) {
    Connection con = getConnection();
    try {
      Collection<ScoreDetail> userScores = ScoreDAO.getUserScores(con, scorePK, userId);
      setParticipations(userScores);
      setPositions(userScores);
      return userScores;
    } catch (Exception e) {
      throw new ScoreRuntimeException("ScoreBmEJB.getUserScores()",
          SilverpeasRuntimeException.ERROR, "score.EX_GET_USER_SCORES_FAILED", e);
    } finally {
      DBUtil.close(con);
    }

  }

  /**
   * Method declaration
   *
   * @param scorePK
   * @param fatherId
   * @param userId
   * @return
   * @see
   */
  @Override
  public Collection<ScoreDetail> getUserScoresByFatherId(ScorePK scorePK, String fatherId,
      String userId) {
    Connection con = getConnection();
    try {
      Collection<ScoreDetail> userScores = ScoreDAO.getUserScoresByFatherId(con, scorePK, fatherId,
          userId);
      setParticipations(userScores);
      setPositions(userScores);
      return userScores;
    } catch (Exception e) {
      throw new ScoreRuntimeException("ScoreBmEJB.getUserScoresByFatherId()",
          SilverpeasRuntimeException.ERROR,
          "score.EX_GET_PARTICIPATION_USER_SCORES_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   *
   * @param scorePK
   * @param nbBestScores
   * @param fatherId
   * @return
   * @see
   */
  @Override
  public Collection<ScoreDetail> getBestScoresByFatherId(ScorePK scorePK, int nbBestScores,
      String fatherId) {
    Connection con = getConnection();
    try {
      Collection<ScoreDetail> bestScores = ScoreDAO.getBestScoresByFatherId(con, scorePK,
          nbBestScores, fatherId);
      setParticipations(bestScores);
      setPositions(bestScores);
      return bestScores;
    } catch (Exception e) {
      throw new ScoreRuntimeException("ScoreBmEJB.getBestScoresByFatherId()",
          SilverpeasRuntimeException.ERROR, "score.EX_GET_BEST_SCORES_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   *
   * @param scorePK
   * @param nbWorstScores
   * @param fatherId
   * @return
   * @see
   */
  @Override
  public Collection<ScoreDetail> getWorstScoresByFatherId(ScorePK scorePK,
      int nbWorstScores, String fatherId) {
    Connection con = getConnection();
    try {
      Collection<ScoreDetail> worstScores = ScoreDAO.getWorstScoresByFatherId(con, scorePK,
          nbWorstScores, fatherId);
      setParticipations(worstScores);
      setPositions(worstScores);
      return worstScores;
    } catch (Exception e) {
      throw new ScoreRuntimeException("ScoreBmEJB.getWorstScoresByFatherId()",
          SilverpeasRuntimeException.ERROR, "score.EX_GET_WORST_SCORES_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   *
   * @param scorePK
   * @param fatherId
   * @return
   * @see
   */
  @Override
  public int getNbVotersByFatherId(ScorePK scorePK, String fatherId) {
    Connection con = getConnection();
    try {
      return ScoreDAO.getNbVotersByFatherId(con, scorePK, fatherId);
    } catch (Exception e) {
      throw new ScoreRuntimeException("ScoreBmEJB.getNbVotersByFatherId()",
          SilverpeasRuntimeException.ERROR, "score.EX_GET_NB_PLAYERS_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   *
   * @param scorePK
   * @param fatherId
   * @return
   * @see
   */
  @Override
  public float getAverageScoreByFatherId(ScorePK scorePK, String fatherId) {
    Connection con = getConnection();
    try {
      return ScoreDAO.getAverageScoreByFatherId(con, scorePK, fatherId);
    } catch (Exception e) {
      throw new ScoreRuntimeException("ScoreBmEJB.getAverageScoreByFatherId()",
          SilverpeasRuntimeException.ERROR,
          "score.EX_GET_AVERAGE_SCORE_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   *
   * @param scorePK
   * @param fatherId
   * @param userId
   * @param participationId
   * @return
   * @see
   */
  @Override
  public ScoreDetail getUserScoreByFatherIdAndParticipationId(ScorePK scorePK,
      String fatherId, String userId, int participationId) {
    Connection con = getConnection();
    try {
      ScoreDetail scoreDetail = ScoreDAO.getUserScoreByFatherIdAndParticipationId(con,
          scorePK, fatherId, userId, participationId);
      setNbParticipation(scoreDetail);
      setPosition(scoreDetail);
      return scoreDetail;
    } catch (Exception e) {
      throw new ScoreRuntimeException(
          "ScoreBmEJB.getUserScoreByFatherIdAndParticipationId()",
          SilverpeasRuntimeException.ERROR, "score.EX_GET_USER_SCORE_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   *
   * @param scorePK
   * @param fatherId
   * @return
   * @see
   */
  @Override
  public Collection<ScoreDetail> getScoresByFatherId(ScorePK scorePK, String fatherId) {
    Connection con = getConnection();
    try {
      Collection<ScoreDetail> scores = ScoreDAO.getScoresByFatherId(con, scorePK, fatherId);
      setParticipations(scores);
      setPositions(scores);
      return scores;
    } catch (Exception e) {
      throw new ScoreRuntimeException("ScoreBmEJB.getScoresByFatherId()",
          SilverpeasRuntimeException.ERROR, "score.EX_GET_SCORES_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }
}
