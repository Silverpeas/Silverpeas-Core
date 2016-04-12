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
package org.silverpeas.core.questioncontainer.score.service;

import org.silverpeas.core.questioncontainer.score.dao.ScoreDAO;
import org.silverpeas.core.questioncontainer.score.model.ScoreDetail;
import org.silverpeas.core.questioncontainer.score.model.ScorePK;
import org.silverpeas.core.questioncontainer.score.model.ScoreRuntimeException;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.exception.SilverpeasRuntimeException;

import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.util.Collection;

/**
 * Default implementation of Score service
 */
@Singleton
@Transactional(Transactional.TxType.SUPPORTS)
public class DefaultScoreService implements ScoreService {

  /**
   * @return a database connection
   */
  private Connection getConnection() {
    try {
      return DBUtil.openConnection();
    } catch (Exception re) {
      throw new ScoreRuntimeException("DefaultScoreService.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", re);
    }
  }

  /**
   * @see ScoreService#getUserNbParticipationsByFatherId
   */
  @Override
  public int getUserNbParticipationsByFatherId(ScorePK scorePK, String fatherId, String userId) {
    Connection con = getConnection();
    try {
      return ScoreDAO.getUserNbParticipationsByFatherId(con, scorePK, fatherId, userId);
    } catch (Exception re) {
      throw new ScoreRuntimeException("DefaultScoreService.getUserNbParticipationsByFatherId()",
          SilverpeasRuntimeException.ERROR, "score.EX_GET_USER_NB_PARTICIPATION_FAILED", re);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * @see com.stratelia.webactiv.score.control
   * .ScoreService#getUserPositionByFatherIdAndParticipationId
   */
  private int getUserPositionByFatherIdAndParticipationId(ScorePK scorePK, String fatherId,
      String userId, int participationId) {
    Connection con = getConnection();
    try {
      return ScoreDAO.getUserPositionByFatherIdAndParticipationId(con, scorePK, fatherId, userId,
          participationId);
    } catch (Exception e) {
      throw new ScoreRuntimeException(
          "DefaultScoreService.getUserPositionByFatherIdAndParticipationId()",
          SilverpeasRuntimeException.ERROR, "score.EX_GET_USER_POSITION_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * @param scoreDetails the collection of score detail
   */
  private void setPositions(Collection<ScoreDetail> scoreDetails) {
    for (ScoreDetail scoreDetail : scoreDetails) {
      scoreDetail.setPosition(getUserPositionByFatherIdAndParticipationId(scoreDetail.getScorePK(),
          scoreDetail.getFatherId(), scoreDetail.getUserId(), scoreDetail.getParticipationId()));
    }
  }

  /**
   * @param scoreDetails the collection of score detail
   */
  private void setParticipations(Collection<ScoreDetail> scoreDetails) {
    for (ScoreDetail scoreDetail : scoreDetails) {
      scoreDetail.setNbParticipations(
          getUserNbParticipationsByFatherId(scoreDetail.getScorePK(), scoreDetail.getFatherId(),
              scoreDetail.getUserId()));
    }
  }

  /**
   * @param scoreDetail the score detail
   */
  private void setPosition(ScoreDetail scoreDetail) {
    scoreDetail.setPosition(getUserPositionByFatherIdAndParticipationId(scoreDetail.getScorePK(),
        scoreDetail.getFatherId(), scoreDetail.getUserId(), scoreDetail.getParticipationId()));
  }

  /**
   * @param scoreDetail the score detail
   */
  private void setNbParticipation(ScoreDetail scoreDetail) {
    scoreDetail.setNbParticipations(
        getUserNbParticipationsByFatherId(scoreDetail.getScorePK(), scoreDetail.getFatherId(),
            scoreDetail.getUserId()));
  }

  /**
   * @see ScoreService#addScore
   */
  @Transactional(Transactional.TxType.REQUIRED)
  @Override
  public void addScore(ScoreDetail scoreDetail) {
    Connection con = getConnection();
    try {
      ScoreDAO.addScore(con, scoreDetail);
    } catch (Exception e) {
      throw new ScoreRuntimeException("DefaultScoreService.addScore()",
          SilverpeasRuntimeException.ERROR, "score.EX_CREATE_SCORE_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * @see ScoreService#updateScore
   */
  @Transactional(Transactional.TxType.REQUIRED)
  @Override
  public void updateScore(ScoreDetail scoreDetail) {
    Connection con = getConnection();
    try {
      ScoreDAO.updateScore(con, scoreDetail);
    } catch (Exception e) {
      throw new ScoreRuntimeException("DefaultScoreService.updateScore()",
          SilverpeasRuntimeException.ERROR, "score.EX_UPDATE_SCORE_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * @see ScoreService#deleteScore
   */
  @Transactional(Transactional.TxType.REQUIRED)
  @Override
  public void deleteScore(ScorePK scorePK) {
    Connection con = getConnection();
    try {

      ScoreDAO.deleteScore(con, scorePK);
    } catch (Exception e) {
      throw new ScoreRuntimeException("DefaultScoreService.deleteScore()",
          SilverpeasRuntimeException.ERROR, "score.EX_DELETE_SCORE_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * @see ScoreService#deleteScoreByFatherPK
   */
  @Override
  public void deleteScoreByFatherPK(ScorePK scorePK, String fatherId) {
    Connection con = getConnection();
    try {
      ScoreDAO.deleteScoreByFatherPK(con, scorePK, fatherId);
    } catch (Exception e) {
      throw new ScoreRuntimeException("DefaultScoreService.deleteScoreByFatherPK()",
          SilverpeasRuntimeException.ERROR, "score.EX_DELETE_SCORE_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * @see ScoreService#getAllScores
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
      throw new ScoreRuntimeException("DefaultScoreService.getAllScores()",
          SilverpeasRuntimeException.ERROR, "score.EX_GET_ALL_SCORES_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * @see ScoreService#getUserScores
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
      throw new ScoreRuntimeException("DefaultScoreService.getUserScores()",
          SilverpeasRuntimeException.ERROR, "score.EX_GET_USER_SCORES_FAILED", e);
    } finally {
      DBUtil.close(con);
    }

  }

  /**
   * @see ScoreService#getUserScoresByFatherId
   */
  @Override
  public Collection<ScoreDetail> getUserScoresByFatherId(ScorePK scorePK, String fatherId,
      String userId) {
    Connection con = getConnection();
    try {
      Collection<ScoreDetail> userScores =
          ScoreDAO.getUserScoresByFatherId(con, scorePK, fatherId, userId);
      setParticipations(userScores);
      setPositions(userScores);
      return userScores;
    } catch (Exception e) {
      throw new ScoreRuntimeException("DefaultScoreService.getUserScoresByFatherId()",
          SilverpeasRuntimeException.ERROR, "score.EX_GET_PARTICIPATION_USER_SCORES_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * @see ScoreService#getBestScoresByFatherId
   */
  @Override
  public Collection<ScoreDetail> getBestScoresByFatherId(ScorePK scorePK, int nbBestScores,
      String fatherId) {
    Connection con = getConnection();
    try {
      Collection<ScoreDetail> bestScores =
          ScoreDAO.getBestScoresByFatherId(con, scorePK, nbBestScores, fatherId);
      setParticipations(bestScores);
      setPositions(bestScores);
      return bestScores;
    } catch (Exception e) {
      throw new ScoreRuntimeException("DefaultScoreService.getBestScoresByFatherId()",
          SilverpeasRuntimeException.ERROR, "score.EX_GET_BEST_SCORES_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * @see ScoreService#getWorstScoresByFatherId
   */
  @Override
  public Collection<ScoreDetail> getWorstScoresByFatherId(ScorePK scorePK, int nbWorstScores,
      String fatherId) {
    Connection con = getConnection();
    try {
      Collection<ScoreDetail> worstScores =
          ScoreDAO.getWorstScoresByFatherId(con, scorePK, nbWorstScores, fatherId);
      setParticipations(worstScores);
      setPositions(worstScores);
      return worstScores;
    } catch (Exception e) {
      throw new ScoreRuntimeException("DefaultScoreService.getWorstScoresByFatherId()",
          SilverpeasRuntimeException.ERROR, "score.EX_GET_WORST_SCORES_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * @see ScoreService#getNbVotersByFatherId
   */
  @Override
  public int getNbVotersByFatherId(ScorePK scorePK, String fatherId) {
    Connection con = getConnection();
    try {
      return ScoreDAO.getNbVotersByFatherId(con, scorePK, fatherId);
    } catch (Exception e) {
      throw new ScoreRuntimeException("DefaultScoreService.getNbVotersByFatherId()",
          SilverpeasRuntimeException.ERROR, "score.EX_GET_NB_PLAYERS_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * @see ScoreService#getAverageScoreByFatherId
   */
  @Override
  public float getAverageScoreByFatherId(ScorePK scorePK, String fatherId) {
    Connection con = getConnection();
    try {
      return ScoreDAO.getAverageScoreByFatherId(con, scorePK, fatherId);
    } catch (Exception e) {
      throw new ScoreRuntimeException("DefaultScoreService.getAverageScoreByFatherId()",
          SilverpeasRuntimeException.ERROR, "score.EX_GET_AVERAGE_SCORE_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * @see ScoreService#getUserScoreByFatherIdAndParticipationId
   */
  @Override
  public ScoreDetail getUserScoreByFatherIdAndParticipationId(ScorePK scorePK, String fatherId,
      String userId, int participationId) {
    Connection con = getConnection();
    try {
      ScoreDetail scoreDetail = ScoreDAO
          .getUserScoreByFatherIdAndParticipationId(con, scorePK, fatherId, userId,
              participationId);
      setNbParticipation(scoreDetail);
      setPosition(scoreDetail);
      return scoreDetail;
    } catch (Exception e) {
      throw new ScoreRuntimeException(
          "DefaultScoreService.getUserScoreByFatherIdAndParticipationId()",
          SilverpeasRuntimeException.ERROR, "score.EX_GET_USER_SCORE_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * @see ScoreService#getScoresByFatherId
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
      throw new ScoreRuntimeException("DefaultScoreService.getScoresByFatherId()",
          SilverpeasRuntimeException.ERROR, "score.EX_GET_SCORES_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }
}
