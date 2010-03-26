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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.webactiv.util.questionContainer.control;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import com.stratelia.webactiv.util.questionContainer.model.QuestionContainerDetail;
import com.stratelia.webactiv.util.questionContainer.model.QuestionContainerHeader;
import com.stratelia.webactiv.util.questionContainer.model.QuestionContainerPK;
import com.stratelia.webactiv.util.score.model.ScoreDetail;

/*
 * CVS Informations
 * 
 * $Id: QuestionContainerBmSkeleton.java,v 1.8 2006/11/15 14:09:19 sfariello Exp $
 * 
 * $Log: QuestionContainerBmSkeleton.java,v $
 * Revision 1.8  2006/11/15 14:09:19  sfariello
 * no message
 *
 * Revision 1.7  2004/06/22 15:42:07  neysseri
 * implements new SilverContentInterface + nettoyage eclipse
 *
 * Revision 1.6  2003/02/28 15:51:31  neysseri
 * no message
 *
 * Revision 1.5  2002/12/02 12:43:02  neysseri
 * Quizz In PDC merging
 *
 * Revision 1.4.2.1  2002/11/29 15:06:06  pbialevich
 * no message
 *
 * Revision 1.3  2002/11/27 12:57:24  neysseri
 * no message
 *
 * Revision 1.2  2002/11/18 11:19:37  neysseri
 * PdcVisibility branch merging
 *
 * Revision 1.1.1.1.16.1  2002/11/14 17:35:41  neysseri
 * Adding of survey as a new content
 *
 * Revision 1.1.1.1  2002/08/06 14:47:53  nchaix
 * no message
 *
 * Revision 1.18  2002/01/04 14:22:46  neysseri
 * no message
 *
 */

/**
 * Interface declaration
 * @author neysseri
 */
public interface QuestionContainerBmSkeleton {

  /**
   * Method declaration
   * @param questionContainerPK
   * @param userId
   * @param reply
   * @throws RemoteException
   * @see
   */
  public void recordReplyToQuestionContainerByUser(
      QuestionContainerPK questionContainerPK, String userId, Hashtable reply)
      throws RemoteException;

  /**
   * Method declaration
   * @param questionContainerPK
   * @param userId
   * @param reply
   * @param comment
   * @param isAnonymousComment
   * @throws RemoteException
   * @see
   */
  public void recordReplyToQuestionContainerByUser(
      QuestionContainerPK questionContainerPK, String userId, Hashtable reply,
      String comment, boolean isAnonymousComment) throws RemoteException;

  /**
   * Method declaration
   * @param questionContainerPK
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection getSuggestions(QuestionContainerPK questionContainerPK)
      throws RemoteException;

  /**
   * Method declaration
   * @param questionContainerPK
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection getQuestionContainers(
      QuestionContainerPK questionContainerPK) throws RemoteException;

  /**
   * Method declaration
   * @param ids A collection of QuestionContainer id
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection getQuestionContainerHeaders(ArrayList pks)
      throws RemoteException;

  /**
   * Method declaration
   * @param questionContainerPK
   * @param userId
   * @return
   * @throws RemoteException
   * @see
   */
  public QuestionContainerDetail getQuestionContainer(
      QuestionContainerPK questionContainerPK, String userId)
      throws RemoteException;

  /**
   * Method declaration
   * @param questionContainerPK
   * @param userId
   * @param participationId
   * @return
   * @throws RemoteException
   * @see
   */
  public QuestionContainerDetail getQuestionContainerByParticipationId(
      QuestionContainerPK questionContainerPK, String userId,
      int participationId) throws RemoteException;

  /**
   * Method declaration
   * @param questionContainerPK
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection getNotClosedQuestionContainers(
      QuestionContainerPK questionContainerPK) throws RemoteException;

  /**
   * Method declaration
   * @param questionContainerPK
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection getOpenedQuestionContainers(
      QuestionContainerPK questionContainerPK) throws RemoteException;

  /**
   * Method declaration
   * @param questionContainerPK
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection getClosedQuestionContainers(
      QuestionContainerPK questionContainerPK) throws RemoteException;

  /**
   * Method declaration
   * @param questionContainerPK
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection getInWaitQuestionContainers(
      QuestionContainerPK questionContainerPK) throws RemoteException;

  /**
   * Method declaration
   * @param questionContainerPK
   * @throws RemoteException
   * @see
   */
  public void closeQuestionContainer(QuestionContainerPK questionContainerPK)
      throws RemoteException;

  /**
   * Method declaration
   * @param questionContainerPK
   * @throws RemoteException
   * @see
   */
  public void openQuestionContainer(QuestionContainerPK questionContainerPK)
      throws RemoteException;

  /**
   * Method declaration
   * @param questionContainerPK
   * @return
   * @throws RemoteException
   * @see
   */
  public int getNbVotersByQuestionContainer(
      QuestionContainerPK questionContainerPK) throws RemoteException;

  /**
   * Method declaration
   * @param questionContainerPK
   * @param questionContainerDetail
   * @param userId
   * @return
   * @throws RemoteException
   * @see
   */
  public QuestionContainerPK createQuestionContainer(
      QuestionContainerPK questionContainerPK,
      QuestionContainerDetail questionContainerDetail, String userId)
      throws RemoteException;

  /**
   * Method declaration
   * @param questionContainerPK
   * @throws RemoteException
   * @see
   */
  public void deleteQuestionContainer(QuestionContainerPK questionContainerPK)
      throws RemoteException;

  public void deleteVotes(QuestionContainerPK questionContainerPK)
      throws RemoteException;

  /**
   * Method declaration
   * @param questionContainerHeader
   * @throws RemoteException
   * @see
   */
  public void updateQuestionContainerHeader(
      QuestionContainerHeader questionContainerHeader) throws RemoteException;

  /**
   * Method declaration
   * @param questionContainerPK
   * @param questions
   * @throws RemoteException
   * @see
   */
  public void updateQuestions(QuestionContainerPK questionContainerPK,
      Collection questions) throws RemoteException;

  /**
   * Method declaration
   * @param questionContainerPK
   * @return
   * @throws RemoteException
   * @see
   */
  public float getAveragePoints(QuestionContainerPK questionContainerPK)
      throws RemoteException;

  /**
   * Method declaration
   * @param questionContainerPK
   * @param userId
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection getOpenedQuestionContainersAndUserScores(
      QuestionContainerPK questionContainerPK, String userId)
      throws RemoteException;

  /**
   * Method declaration
   * @param questionContainerPK
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection getQuestionContainersWithScores(
      QuestionContainerPK questionContainerPK) throws RemoteException;

  /**
   * Method declaration
   * @param questionContainerPK
   * @param userId
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection getQuestionContainersWithUserScores(
      QuestionContainerPK questionContainerPK, String userId)
      throws RemoteException;

  /**
   * Method declaration
   * @param questionContainerPK
   * @param userId
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection getUserScoresByFatherId(
      QuestionContainerPK questionContainerPK, String userId)
      throws RemoteException;

  /**
   * Method declaration
   * @param questionContainerPK
   * @param nbBestScores
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection getBestScoresByFatherId(
      QuestionContainerPK questionContainerPK, int nbBestScores)
      throws RemoteException;

  /**
   * Method declaration
   * @param questionContainerPK
   * @return
   * @throws RemoteException
   * @see
   */
  public float getAverageScoreByFatherId(QuestionContainerPK questionContainerPK)
      throws RemoteException;

  /**
   * Method declaration
   * @param questionContainerPK
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection getScoresByFatherId(QuestionContainerPK questionContainerPK)
      throws RemoteException;

  /**
   * Method declaration
   * @param questionContainerPK
   * @param userId
   * @return
   * @throws RemoteException
   * @see
   */
  public int getUserNbParticipationsByFatherId(
      QuestionContainerPK questionContainerPK, String userId)
      throws RemoteException;

  /**
   * Method declaration
   * @param questionContainerPK
   * @param userId
   * @param participationId
   * @return
   * @throws RemoteException
   * @see
   */
  public ScoreDetail getUserScoreByFatherIdAndParticipationId(
      QuestionContainerPK questionContainerPK, String userId,
      int participationId) throws RemoteException;

  /**
   * Method declaration
   * @param questionContainerPK
   * @param scoreDetail
   * @throws RemoteException
   * @see
   */
  public void updateScore(QuestionContainerPK questionContainerPK,
      ScoreDetail scoreDetail) throws RemoteException;

  /**
   * Method declaration
   * @param pk
   * @throws RemoteException
   * @see
   */
  public void deleteIndex(QuestionContainerPK pk) throws RemoteException;

  public int getSilverObjectId(QuestionContainerPK pk) throws RemoteException;

  public String getHTMLQuestionPath(QuestionContainerDetail questionDetail)
      throws RemoteException;

  public QuestionContainerHeader getQuestionContainerHeader(
      QuestionContainerPK questionContainerPK) throws RemoteException;

  /**
   * create export file
   * @param questionContainer : QuestionContainerDetail
   * @param addScore : boolean
   * @return export file name : String
   * @throws RemoteException
   */
  public String exportCSV(QuestionContainerDetail questionContainer, boolean addScore)
      throws RemoteException;
}