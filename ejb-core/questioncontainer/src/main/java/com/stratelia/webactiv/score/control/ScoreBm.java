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

import java.util.Collection;

import javax.ejb.Local;

import com.stratelia.webactiv.score.model.ScoreDetail;
import com.stratelia.webactiv.score.model.ScorePK;

/**
 * Interface declaration
 *
 * @author
 */
@Local
public interface ScoreBm {

  /**
   * Method declaration
   *
   * @param scoreDetail
   *
   * @see
   */
  public void addScore(ScoreDetail scoreDetail);

  /*
   * Method: deleteScore
   *
   * @param scorePK
   *
   * @see
   */
  public void deleteScore(ScorePK scorePK);

  /*
   * Methode: deleteScoreByFatherPK
   *
   * @param scorePK
   * @param fatherId
   *
   * @see
   */
  public void deleteScoreByFatherPK(ScorePK scorePK, String fatherId);

  /*
   * Method: getAllScores
   */
  /**
   * Method declaration
   *
   * @param scorePK
   * @return
   *
   * @see
   */
  public Collection<ScoreDetail> getAllScores(ScorePK scorePK);

  /*
   * Method: getUserScores
   *
   * @param scorePK
   * @param userId
   * @return
   *
   * @see
   */
  public Collection<ScoreDetail> getUserScores(ScorePK scorePK, String userId);

  /*
   * Method: getUserScoresByFatherId
   *
   * @param scorePK
   * @param fatherId
   * @param userId
   * @return
   *
   * @see
   */
  public Collection<ScoreDetail> getUserScoresByFatherId(ScorePK scorePK, String fatherId,
      String userId);

  /*
   * Method: getBestScoresByFatherId
   *
   * @param scorePK
   * @param nbBestScores
   * @param fatherId
   * @return
   *
   * @see
   */
  public Collection<ScoreDetail> getBestScoresByFatherId(ScorePK scorePK, int nbBestScores,
      String fatherId);

  /*
   * Method: getWorstScoresByFatherId
   *
   * @param scorePK
   * @param nbWorstScores
   * @param fatherId
   * @return
   *
   * @see
   */
  public Collection<ScoreDetail> getWorstScoresByFatherId(ScorePK scorePK,
      int nbWorstScores, String fatherId);

  /*
   * Method: getNbVotersByFatherId
   *
   * @param scorePK
   * @param fatherId
   * @return
   *
   * @see
   */
  public int getNbVotersByFatherId(ScorePK scorePK, String fatherId);

  /*
   * Method: getAverageScoreByFatherId
   *
   * @param scorePK
   * @param fatherId
   * @return
   *
   * @see
   */
  public float getAverageScoreByFatherId(ScorePK scorePK, String fatherId);

  /*
   * Method: getUserScoresByFatherIdAndParticipationId
   *
   * @param scorePK
   * @param fatherId
   * @param userId
   * @param participationId
   * @return
   *
   * @see
   */
  public ScoreDetail getUserScoreByFatherIdAndParticipationId(ScorePK scorePK,
      String fatherId, String userId, int participationId);

  /*
   * Method: getScoresByFatherId
   */
  /**
   * Method declaration
   *
   * @param scorePK
   * @param fatherId
   * @return
   *
   * @see
   */
  public Collection<ScoreDetail> getScoresByFatherId(ScorePK scorePK, String fatherId);

  /**
   * Method declaration
   *
   * @param scorePK
   * @param fatherId
   * @param userId
   * @return
   *
   * @see
   */
  public int getUserNbParticipationsByFatherId(ScorePK scorePK,
      String fatherId, String userId);

  /**
   * Method declaration
   *
   * @param scoreDetail
   *
   * @see
   */
  public void updateScore(ScoreDetail scoreDetail);
}
