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
import java.rmi.RemoteException;
import java.util.Collection;

import com.stratelia.webactiv.util.score.model.ScoreDetail;
import com.stratelia.webactiv.util.score.model.ScorePK;

/**
 * Interface declaration
 * @author
 */
public interface ScoreBmSkeleton {

  /*
   * Method: addScore
   */

  /**
   * Method declaration
   * @param scoreDetail
   * @throws RemoteException
   * @see
   */
  public void addScore(ScoreDetail scoreDetail) throws RemoteException;

  /*
   * Method: deleteScore
   */

  /**
   * Method declaration
   * @param scorePK
   * @throws RemoteException
   * @see
   */
  public void deleteScore(ScorePK scorePK) throws RemoteException;

  /*
   * Methode: deleteScoreByFatherPK
   */

  /**
   * Method declaration
   * @param scorePK
   * @param fatherId
   * @throws RemoteException
   * @see
   */
  public void deleteScoreByFatherPK(ScorePK scorePK, String fatherId)
      throws RemoteException;

  /*
   * Method: getAllScores
   */

  /**
   * Method declaration
   * @param scorePK
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection<ScoreDetail> getAllScores(ScorePK scorePK) throws RemoteException;

  /*
   * Method: getUserScores
   */

  /**
   * Method declaration
   * @param scorePK
   * @param userId
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection<ScoreDetail> getUserScores(ScorePK scorePK, String userId)
      throws RemoteException;

  /*
   * Method: getUserScoresByFatherId
   */

  /**
   * Method declaration
   * @param scorePK
   * @param fatherId
   * @param userId
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection<ScoreDetail> getUserScoresByFatherId(ScorePK scorePK, String fatherId,
      String userId) throws RemoteException;

  /*
   * Method: getBestScoresByFatherId
   */

  /**
   * Method declaration
   * @param scorePK
   * @param nbBestScores
   * @param fatherId
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection<ScoreDetail> getBestScoresByFatherId(ScorePK scorePK, int nbBestScores,
      String fatherId) throws RemoteException;

  /*
   * Method: getWorstScoresByFatherId
   */

  /**
   * Method declaration
   * @param scorePK
   * @param nbWorstScores
   * @param fatherId
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection<ScoreDetail> getWorstScoresByFatherId(ScorePK scorePK,
      int nbWorstScores, String fatherId) throws RemoteException;

  /*
   * Method: getNbVotersByFatherId
   */

  /**
   * Method declaration
   * @param scorePK
   * @param fatherId
   * @return
   * @throws RemoteException
   * @see
   */
  public int getNbVotersByFatherId(ScorePK scorePK, String fatherId)
      throws RemoteException;

  /*
   * Method: getAverageScoreByFatherId
   */

  /**
   * Method declaration
   * @param scorePK
   * @param fatherId
   * @return
   * @throws RemoteException
   * @see
   */
  public float getAverageScoreByFatherId(ScorePK scorePK, String fatherId)
      throws RemoteException;

  /*
   * Method: getUserScoresByFatherIdAndParticipationId
   */

  /**
   * Method declaration
   * @param scorePK
   * @param fatherId
   * @param userId
   * @param participationId
   * @return
   * @throws RemoteException
   * @see
   */
  public ScoreDetail getUserScoreByFatherIdAndParticipationId(ScorePK scorePK,
      String fatherId, String userId, int participationId)
      throws RemoteException;

  /*
   * Method: getScoresByFatherId
   */

  /**
   * Method declaration
   * @param scorePK
   * @param fatherId
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection<ScoreDetail> getScoresByFatherId(ScorePK scorePK, String fatherId)
      throws RemoteException;

  /**
   * Method declaration
   * @param scorePK
   * @param fatherId
   * @param userId
   * @return
   * @throws RemoteException
   * @see
   */
  public int getUserNbParticipationsByFatherId(ScorePK scorePK,
      String fatherId, String userId) throws RemoteException;

  /**
   * Method declaration
   * @param scoreDetail
   * @throws RemoteException
   * @see
   */
  public void updateScore(ScoreDetail scoreDetail) throws RemoteException;

}
