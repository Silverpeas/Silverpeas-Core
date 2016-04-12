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

import org.silverpeas.core.questioncontainer.score.model.ScoreDetail;
import org.silverpeas.core.questioncontainer.score.model.ScorePK;
import org.silverpeas.core.util.ServiceProvider;

import java.util.Collection;

/**
 * Score service interface which allow to manage a score
 */
public interface ScoreService {

  static ScoreService get() {
    return ServiceProvider.getService(ScoreService.class);
  }

  /**
   * @param scoreDetail the scrore detail to add
   */
  public void addScore(ScoreDetail scoreDetail);

  /**
   * Method: deleteScore
   * @param scorePK the score identifier
   */
  public void deleteScore(ScorePK scorePK);

  /**
   * Methode: deleteScoreByFatherPK
   * @param scorePK the score identifier
   * @param fatherId the father identifier
   */
  public void deleteScoreByFatherPK(ScorePK scorePK, String fatherId);

  /**
   * Method: getAllScores
   * @param scorePK the score identifier
   * @return
   */
  public Collection<ScoreDetail> getAllScores(ScorePK scorePK);

  /**
   * Method: getUserScores
   * @param scorePK the score identifier
   * @param userId the user identifier
   * @return
   */
  public Collection<ScoreDetail> getUserScores(ScorePK scorePK, String userId);

  /**
   * Method: getUserScoresByFatherId
   * @param scorePK the score identifier
   * @param fatherId the father identifier
   * @param userId the user identifier
   * @return
   */
  public Collection<ScoreDetail> getUserScoresByFatherId(ScorePK scorePK, String fatherId,
      String userId);

  /**
   * Method: getBestScoresByFatherId
   * @param scorePK the score identifier
   * @param nbBestScores
   * @param fatherId the father identifier
   * @return
   */
  public Collection<ScoreDetail> getBestScoresByFatherId(ScorePK scorePK, int nbBestScores,
      String fatherId);

  /**
   * Method: getWorstScoresByFatherId
   * @param scorePK the score identifier
   * @param nbWorstScores
   * @param fatherId the father identifier
   * @return
   */
  public Collection<ScoreDetail> getWorstScoresByFatherId(ScorePK scorePK, int nbWorstScores,
      String fatherId);

  /**
   * Method: getNbVotersByFatherId
   * @param scorePK the score identifier
   * @param fatherId the father identifier
   * @return
   */
  public int getNbVotersByFatherId(ScorePK scorePK, String fatherId);

  /**
   * Method: getAverageScoreByFatherId
   * @param scorePK the score identifier
   * @param fatherId the father identifier
   * @return
   */
  public float getAverageScoreByFatherId(ScorePK scorePK, String fatherId);

  /**
   * Method: getUserScoresByFatherIdAndParticipationId
   * @param scorePK the score identifier
   * @param fatherId the father identifier
   * @param userId the user identifier
   * @param participationId
   * @return
   */
  public ScoreDetail getUserScoreByFatherIdAndParticipationId(ScorePK scorePK, String fatherId,
      String userId, int participationId);

  /**
   * Method: getScoresByFatherId
   * @param scorePK the score identifier
   * @param fatherId the father identifier
   * @return
   */
  public Collection<ScoreDetail> getScoresByFatherId(ScorePK scorePK, String fatherId);

  /**
   * @param scorePK the score identifier
   * @param fatherId the father identifier
   * @param userId the user identifier
   * @return
   */
  public int getUserNbParticipationsByFatherId(ScorePK scorePK, String fatherId, String userId);

  /**
   * @param scoreDetail the score detail to update
   */
  public void updateScore(ScoreDetail scoreDetail);
}
