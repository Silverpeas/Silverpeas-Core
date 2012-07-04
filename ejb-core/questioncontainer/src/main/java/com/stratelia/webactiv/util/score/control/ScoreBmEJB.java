/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.util.score.control;

// Import Statements
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.SessionContext;

import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.score.ejb.ScoreDAO;
import com.stratelia.webactiv.util.score.model.ScoreDetail;
import com.stratelia.webactiv.util.score.model.ScorePK;
import com.stratelia.webactiv.util.score.model.ScoreRuntimeException;

/**
 * Class declaration
 * @author
 */
public class ScoreBmEJB implements javax.ejb.SessionBean, ScoreBmSkeleton {
  private static final long serialVersionUID = -8139560611772321449L;
  private String dbName = JNDINames.SCORE_DATASOURCE;

  /*
   * Method: Default Constructor
   */

  /**
   * Constructor declaration
   * @see
   */
  public ScoreBmEJB() {
  }

  /*
   * Method: ejbCreate
   */

  /**
   * Method declaration
   * @see
   */
  public void ejbCreate() {
  }

  /*
   * Method: ejbRemove
   */

  /**
   * Method declaration
   * @see
   */
  public void ejbRemove() {
  }

  /*
   * Method: ejbActivate
   */

  /**
   * Method declaration
   * @see
   */
  public void ejbActivate() {
  }

  /*
   * Method: ejbPassivate
   */

  /**
   * Method declaration
   * @see
   */
  public void ejbPassivate() {
  }

  /*
   * Method: setSessionContext
   */

  /**
   * Method declaration
   * @param sc
   * @see
   */
  public void setSessionContext(SessionContext sc) {
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  private Connection getConnection() {
    try {
      Connection con = DBUtil.makeConnection(dbName);

      return con;
    } catch (Exception re) {
      throw new ScoreRuntimeException("ScoreBmEJB.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED",
          re);
    }
  }

  /**
   * Method declaration
   * @param con
   * @see
   */
  private void freeConnection(Connection con) {
    if (con != null) {
      try {
        con.close();
      } catch (SQLException re) {
        throw new ScoreRuntimeException("ScoreBmEJB.closeConnection()",
            SilverpeasRuntimeException.ERROR,
            "root.EX_CONNECTION_CLOSE_FAILED", re);
      }
    }
  }

  /**
   * Method declaration
   * @param scorePK
   * @param fatherId
   * @param userId
   * @return
   * @see
   */
  public int getUserNbParticipationsByFatherId(ScorePK scorePK,
      String fatherId, String userId) {
    Connection con = null;
    int userNbParticipations = 0;

    try {
      con = getConnection();
      userNbParticipations = ScoreDAO.getUserNbParticipationsByFatherId(con,
          scorePK, fatherId, userId);
    } catch (Exception re) {
      throw new ScoreRuntimeException(
          "ScoreBmEJB.getUserNbParticipationsByFatherId()",
          SilverpeasRuntimeException.ERROR,
          "score.EX_GET_USER_NB_PARTICIPATION_FAILED", re);
    } finally {
      freeConnection(con);
    }
    return userNbParticipations;
  }

  /**
   * Method declaration
   * @param scorePK
   * @param fatherId
   * @param userId
   * @param participationId
   * @return
   * @see
   */
  private int getUserPositionByFatherIdAndParticipationId(ScorePK scorePK,
      String fatherId, String userId, int participationId) {
    Connection con = null;
    int userPosition = 0;

    try {
      con = getConnection();
      userPosition = ScoreDAO.getUserPositionByFatherIdAndParticipationId(con,
          scorePK, fatherId, userId, participationId);
    } catch (Exception e) {
      throw new ScoreRuntimeException(
          "ScoreBmEJB.getUserPositionByFatherIdAndParticipationId()",
          SilverpeasRuntimeException.ERROR,
          "score.EX_GET_USER_POSITION_FAILED", e);
    } finally {
      freeConnection(con);
    }
    return userPosition;
  }

  /**
   * Method declaration
   * @param scoreDetails
   * @see
   */
  private void setPositions(Collection<ScoreDetail> scoreDetails) {
    Iterator<ScoreDetail> it = scoreDetails.iterator();

    while (it.hasNext()) {
      ScoreDetail scoreDetail = it.next();

      scoreDetail.setPosition(getUserPositionByFatherIdAndParticipationId(
          scoreDetail.getScorePK(), scoreDetail.getFatherId(), scoreDetail
          .getUserId(), scoreDetail.getParticipationId()));
    }
  }

  /**
   * Method declaration
   * @param scoreDetails
   * @see
   */
  private void setParticipations(Collection<ScoreDetail> scoreDetails) {
    Iterator<ScoreDetail> it = scoreDetails.iterator();

    while (it.hasNext()) {
      ScoreDetail scoreDetail = it.next();

      scoreDetail.setNbParticipations(getUserNbParticipationsByFatherId(
          scoreDetail.getScorePK(), scoreDetail.getFatherId(), scoreDetail
          .getUserId()));
    }
  }

  /**
   * Method declaration
   * @param scoreDetail
   * @see
   */
  private void setPosition(ScoreDetail scoreDetail) {
    scoreDetail.setPosition(getUserPositionByFatherIdAndParticipationId(
        scoreDetail.getScorePK(), scoreDetail.getFatherId(), scoreDetail
        .getUserId(), scoreDetail.getParticipationId()));
  }

  /**
   * Method declaration
   * @param scoreDetail
   * @see
   */
  private void setNbParticipation(ScoreDetail scoreDetail) {
    scoreDetail.setNbParticipations(getUserNbParticipationsByFatherId(
        scoreDetail.getScorePK(), scoreDetail.getFatherId(), scoreDetail
        .getUserId()));
  }

  /**
   * Method declaration
   * @param scoreDetail
   * @see
   */
  public void addScore(ScoreDetail scoreDetail) {
    Connection con = null;

    try {
      con = getConnection();
      ScoreDAO.addScore(con, scoreDetail);
    } catch (Exception e) {
      throw new ScoreRuntimeException("ScoreBmEJB.addScore()",
          SilverpeasRuntimeException.ERROR, "score.EX_CREATE_SCORE_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Method declaration
   * @param scoreDetail
   * @see
   */
  public void updateScore(ScoreDetail scoreDetail) {
    Connection con = null;

    try {
      con = getConnection();
      ScoreDAO.updateScore(con, scoreDetail);
    } catch (Exception e) {
      throw new ScoreRuntimeException("ScoreBmEJB.updateScore()",
          SilverpeasRuntimeException.ERROR, "score.EX_UPDATE_SCORE_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Method declaration
   * @param scorePK
   * @see
   */
  public void deleteScore(ScorePK scorePK) {
    Connection con = null;

    try {
      con = getConnection();
      ScoreDAO.deleteScore(con, scorePK);
    } catch (Exception e) {
      throw new ScoreRuntimeException("ScoreBmEJB.deleteScore()",
          SilverpeasRuntimeException.ERROR, "score.EX_DELETE_SCORE_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  // Begin Modif une table par instance by hguig

  /**
   * Method declaration
   * @param scorePK
   * @param fatherId
   * @see
   */
  public void deleteScoreByFatherPK(ScorePK scorePK, String fatherId) {
    Connection con = null;

    try {
      con = getConnection();
      ScoreDAO.deleteScoreByFatherPK(con, scorePK, fatherId);
    } catch (Exception e) {
      throw new ScoreRuntimeException("ScoreBmEJB.deleteScoreByFatherPK()",
          SilverpeasRuntimeException.ERROR, "score.EX_DELETE_SCORE_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  // End Modif

  /**
   * Method declaration
   * @param scorePK
   * @return
   * @see
   */
  public Collection<ScoreDetail> getAllScores(ScorePK scorePK) {
    Connection con = null;
    Collection<ScoreDetail> allScores = null;

    try {
      con = getConnection();
      allScores = ScoreDAO.getAllScores(con, scorePK);
      setParticipations(allScores);
      setPositions(allScores);
    } catch (Exception e) {
      throw new ScoreRuntimeException("ScoreBmEJB.getAllScores()",
          SilverpeasRuntimeException.ERROR, "score.EX_GET_ALL_SCORES_FAILED", e);
    } finally {
      freeConnection(con);
    }
    return allScores;
  }

  /**
   * Method declaration
   * @param scorePK
   * @param userId
   * @return
   * @see
   */
  public Collection<ScoreDetail> getUserScores(ScorePK scorePK, String userId) {
    Connection con = null;
    Collection<ScoreDetail> userScores = null;

    try {
      con = getConnection();
      userScores = ScoreDAO.getUserScores(con, scorePK, userId);
      setParticipations(userScores);
      setPositions(userScores);
    } catch (Exception e) {
      throw new ScoreRuntimeException("ScoreBmEJB.getUserScores()",
          SilverpeasRuntimeException.ERROR, "score.EX_GET_USER_SCORES_FAILED",
          e);
    } finally {
      freeConnection(con);
    }
    return userScores;
  }

  /**
   * Method declaration
   * @param scorePK
   * @param fatherId
   * @param userId
   * @return
   * @see
   */
  public Collection<ScoreDetail> getUserScoresByFatherId(ScorePK scorePK, String fatherId,
      String userId) {
    Connection con = null;
    Collection<ScoreDetail> userScores = null;

    try {
      con = getConnection();
      userScores = ScoreDAO.getUserScoresByFatherId(con, scorePK, fatherId,
          userId);
      setParticipations(userScores);
      setPositions(userScores);
    } catch (Exception e) {
      throw new ScoreRuntimeException("ScoreBmEJB.getUserScoresByFatherId()",
          SilverpeasRuntimeException.ERROR,
          "score.EX_GET_PARTICIPATION_USER_SCORES_FAILED", e);
    } finally {
      freeConnection(con);
    }
    return userScores;
  }

  /**
   * Method declaration
   * @param scorePK
   * @param nbBestScores
   * @param fatherId
   * @return
   * @see
   */
  public Collection<ScoreDetail> getBestScoresByFatherId(ScorePK scorePK, int nbBestScores,
      String fatherId) {
    Connection con = null;
    Collection<ScoreDetail> bestScores = null;

    try {
      con = getConnection();
      bestScores = ScoreDAO.getBestScoresByFatherId(con, scorePK, nbBestScores,
          fatherId);
      setParticipations(bestScores);
      setPositions(bestScores);
    } catch (Exception e) {
      throw new ScoreRuntimeException("ScoreBmEJB.getBestScoresByFatherId()",
          SilverpeasRuntimeException.ERROR, "score.EX_GET_BEST_SCORES_FAILED",
          e);
    } finally {
      freeConnection(con);
    }
    return bestScores;
  }

  /**
   * Method declaration
   * @param scorePK
   * @param nbWorstScores
   * @param fatherId
   * @return
   * @see
   */
  public Collection<ScoreDetail> getWorstScoresByFatherId(ScorePK scorePK,
      int nbWorstScores, String fatherId) {
    Connection con = null;
    Collection<ScoreDetail> worstScores = null;

    try {
      con = getConnection();
      worstScores = ScoreDAO.getWorstScoresByFatherId(con, scorePK,
          nbWorstScores, fatherId);
      setParticipations(worstScores);
      setPositions(worstScores);
    } catch (Exception e) {
      throw new ScoreRuntimeException("ScoreBmEJB.getWorstScoresByFatherId()",
          SilverpeasRuntimeException.ERROR, "score.EX_GET_WORST_SCORES_FAILED",
          e);
    } finally {
      freeConnection(con);
    }
    return worstScores;
  }

  /**
   * Method declaration
   * @param scorePK
   * @param fatherId
   * @return
   * @see
   */
  public int getNbVotersByFatherId(ScorePK scorePK, String fatherId) {
    Connection con = null;
    int nbVoters = 0;

    try {
      con = getConnection();
      nbVoters = ScoreDAO.getNbVotersByFatherId(con, scorePK, fatherId);
    } catch (Exception e) {
      throw new ScoreRuntimeException("ScoreBmEJB.getNbVotersByFatherId()",
          SilverpeasRuntimeException.ERROR, "score.EX_GET_NB_PLAYERS_FAILED", e);
    } finally {
      freeConnection(con);
    }
    return nbVoters;
  }

  /**
   * Method declaration
   * @param scorePK
   * @param fatherId
   * @return
   * @see
   */
  public float getAverageScoreByFatherId(ScorePK scorePK, String fatherId) {
    Connection con = null;
    float averageScore = 0;

    try {
      con = getConnection();
      averageScore = ScoreDAO.getAverageScoreByFatherId(con, scorePK, fatherId);
    } catch (Exception e) {
      throw new ScoreRuntimeException("ScoreBmEJB.getAverageScoreByFatherId()",
          SilverpeasRuntimeException.ERROR,
          "score.EX_GET_AVERAGE_SCORE_FAILED", e);
    } finally {
      freeConnection(con);
    }
    return averageScore;
  }

  /**
   * Method declaration
   * @param scorePK
   * @param fatherId
   * @param userId
   * @param participationId
   * @return
   * @see
   */
  public ScoreDetail getUserScoreByFatherIdAndParticipationId(ScorePK scorePK,
      String fatherId, String userId, int participationId) {
    Connection con = null;
    ScoreDetail scoreDetail = null;

    try {
      con = getConnection();
      scoreDetail = ScoreDAO.getUserScoreByFatherIdAndParticipationId(con,
          scorePK, fatherId, userId, participationId);
      setNbParticipation(scoreDetail);
      setPosition(scoreDetail);
    } catch (Exception e) {
      throw new ScoreRuntimeException(
          "ScoreBmEJB.getUserScoreByFatherIdAndParticipationId()",
          SilverpeasRuntimeException.ERROR, "score.EX_GET_USER_SCORE_FAILED", e);
    } finally {
      freeConnection(con);
    }
    return scoreDetail;
  }

  /**
   * Method declaration
   * @param scorePK
   * @param fatherId
   * @return
   * @see
   */
  public Collection<ScoreDetail> getScoresByFatherId(ScorePK scorePK, String fatherId) {
    Connection con = null;
    Collection<ScoreDetail> scores = null;

    try {
      con = getConnection();
      scores = ScoreDAO.getScoresByFatherId(con, scorePK, fatherId);
      setParticipations(scores);
      setPositions(scores);
    } catch (Exception e) {
      throw new ScoreRuntimeException("ScoreBmEJB.getScoresByFatherId()",
          SilverpeasRuntimeException.ERROR, "score.EX_GET_SCORES_FAILED", e);
    } finally {
      freeConnection(con);
    }
    return scores;
  }

}
