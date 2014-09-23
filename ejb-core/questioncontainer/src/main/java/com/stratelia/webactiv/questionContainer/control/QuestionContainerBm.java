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
package com.stratelia.webactiv.questionContainer.control;

import com.stratelia.webactiv.answer.model.AnswerPK;
import com.stratelia.webactiv.question.model.Question;
import com.stratelia.webactiv.question.model.QuestionPK;
import com.stratelia.webactiv.questionContainer.model.QuestionContainerDetail;
import com.stratelia.webactiv.questionContainer.model.QuestionContainerHeader;
import com.stratelia.webactiv.questionContainer.model.QuestionContainerPK;
import com.stratelia.webactiv.questionResult.model.QuestionResult;
import com.stratelia.webactiv.score.model.ScoreDetail;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.ejb.Local;

/**
 * Interface declaration
 *
 * @author neysseri
 */
@Local
public interface QuestionContainerBm {

  /**
   * Method declaration
   *
   * @param questionContainerPK
   * @param userId
   * @param reply
   * @
   * @see
   */
  public void recordReplyToQuestionContainerByUser(QuestionContainerPK questionContainerPK,
      String userId, Map<String, List<String>> reply);

  /**
   * Method declaration
   *
   * @param questionContainerPK
   * @param userId
   * @param reply
   * @param comment
   * @param isAnonymousComment
   * @
   * @see
   */
  public void recordReplyToQuestionContainerByUser(QuestionContainerPK questionContainerPK,
      String userId, Map<String, List<String>> reply, String comment,
      boolean isAnonymousComment);

  /**
   * Method declaration
   *
   * @param questionContainerPK
   * @return
   * @
   * @see
   */
  public Collection<QuestionResult> getSuggestions(QuestionContainerPK questionContainerPK);

  /**
   * Return the suggestion of the user, for the question and the answer
   *
   * @param userId
   * @param questionPK
   * @param answerPK
   * @return QuestionResult
   * @
   * @see
   */
  public QuestionResult getSuggestion(String userId, QuestionPK questionPK, AnswerPK answerPK);

  /**
   * Method declaration
   *
   * @param questionContainerPK
   * @return
   * @
   * @see
   */
  public Collection<QuestionContainerHeader> getQuestionContainers(
      QuestionContainerPK questionContainerPK);

  /**
   * Method declaration
   *
   * @param ids A collection of QuestionContainer id
   * @return
   * @
   * @see
   */
  public Collection<QuestionContainerHeader> getQuestionContainerHeaders(
      List<QuestionContainerPK> pks);

  /**
   * Method declaration
   *
   * @param questionContainerPK
   * @param userId
   * @return
   * @
   * @see
   */
  public QuestionContainerDetail getQuestionContainer(QuestionContainerPK questionContainerPK,
      String userId);

  /**
   * Method declaration
   *
   * @param questionContainerPK
   * @param userId
   * @param participationId
   * @return
   * @
   * @see
   */
  public QuestionContainerDetail getQuestionContainerByParticipationId(
      QuestionContainerPK questionContainerPK, String userId, int participationId);

  /**
   * Method declaration
   *
   * @param questionContainerPK
   * @return
   * @
   * @see
   */
  public Collection<QuestionContainerHeader> getNotClosedQuestionContainers(
      QuestionContainerPK questionContainerPK);

  /**
   * Method declaration
   *
   * @param questionContainerPK
   * @return
   * @
   * @see
   */
  public Collection<QuestionContainerHeader> getOpenedQuestionContainers(
      QuestionContainerPK questionContainerPK);

  /**
   * Method declaration
   *
   * @param questionContainerPK
   * @return
   * @
   * @see
   */
  public Collection<QuestionContainerHeader> getClosedQuestionContainers(
      QuestionContainerPK questionContainerPK);

  /**
   * Method declaration
   *
   * @param questionContainerPK
   * @return
   * @
   * @see
   */
  public Collection<QuestionContainerHeader> getInWaitQuestionContainers(
      QuestionContainerPK questionContainerPK);

  /**
   * Method declaration
   *
   * @param questionContainerPK
   * @
   * @see
   */
  public void closeQuestionContainer(QuestionContainerPK questionContainerPK);

  /**
   * Method declaration
   *
   * @param questionContainerPK
   * @
   * @see
   */
  public void openQuestionContainer(QuestionContainerPK questionContainerPK);

  /**
   * Method declaration
   *
   * @param questionContainerPK
   * @return
   * @
   * @see
   */
  public int getNbVotersByQuestionContainer(
      QuestionContainerPK questionContainerPK);

  /**
   * Method declaration
   *
   * @param questionContainerPK
   * @param questionContainerDetail
   * @param userId
   * @return
   * @
   * @see
   */
  public QuestionContainerPK createQuestionContainer(QuestionContainerPK questionContainerPK,
      QuestionContainerDetail questionContainerDetail, String userId);

  /**
   * Method declaration
   *
   * @param questionContainerPK
   * @
   * @see
   */
  public void deleteQuestionContainer(QuestionContainerPK questionContainerPK);

  public void deleteVotes(QuestionContainerPK questionContainerPK);

  /**
   * Method declaration
   *
   * @param questionContainerHeader
   * @
   * @see
   */
  public void updateQuestionContainerHeader(QuestionContainerHeader questionContainerHeader);

  /**
   * Method declaration
   *
   * @param questionContainerPK
   * @param questions
   * @
   * @see
   */
  public void updateQuestions(QuestionContainerPK questionContainerPK,
      Collection<Question> questions);

  /**
   * Method declaration
   *
   * @param questionContainerPK
   * @return
   * @
   * @see
   */
  public float getAveragePoints(QuestionContainerPK questionContainerPK);

  /**
   * Method declaration
   *
   * @param questionContainerPK
   * @param userId
   * @return
   * @
   * @see
   */
  public Collection<QuestionContainerHeader> getOpenedQuestionContainersAndUserScores(
      QuestionContainerPK questionContainerPK, String userId);

  /**
   * Method declaration
   *
   * @param questionContainerPK
   * @return
   * @
   * @see
   */
  public Collection<QuestionContainerHeader> getQuestionContainersWithScores(
      QuestionContainerPK questionContainerPK);

  /**
   * Method declaration
   *
   * @param questionContainerPK
   * @param userId
   * @return
   * @
   * @see
   */
  public Collection<QuestionContainerHeader> getQuestionContainersWithUserScores(
      QuestionContainerPK questionContainerPK, String userId);

  /**
   * Method declaration
   *
   * @param questionContainerPK
   * @param userId
   * @return
   * @
   * @see
   */
  public Collection<ScoreDetail> getUserScoresByFatherId(
      QuestionContainerPK questionContainerPK, String userId);

  /**
   * Method declaration
   *
   * @param questionContainerPK
   * @param nbBestScores
   * @return
   * @
   * @see
   */
  public Collection<ScoreDetail> getBestScoresByFatherId(
      QuestionContainerPK questionContainerPK, int nbBestScores);

  /**
   * Method declaration
   *
   * @param questionContainerPK
   * @return
   * @
   * @see
   */
  public float getAverageScoreByFatherId(QuestionContainerPK questionContainerPK);

  /**
   * Method declaration
   *
   * @param questionContainerPK
   * @return
   * @
   * @see
   */
  public Collection<ScoreDetail> getScoresByFatherId(QuestionContainerPK questionContainerPK);

  /**
   * Method declaration
   *
   * @param questionContainerPK
   * @param userId
   * @return
   * @
   * @see
   */
  public int getUserNbParticipationsByFatherId(
      QuestionContainerPK questionContainerPK, String userId);

  /**
   * Method declaration
   *
   * @param questionContainerPK
   * @param userId
   * @param participationId
   * @return
   * @
   * @see
   */
  public ScoreDetail getUserScoreByFatherIdAndParticipationId(
      QuestionContainerPK questionContainerPK, String userId,
      int participationId);

  /**
   * Method declaration
   *
   * @param questionContainerPK
   * @param scoreDetail
   * @
   * @see
   */
  public void updateScore(QuestionContainerPK questionContainerPK,
      ScoreDetail scoreDetail);

  /**
   * Method declaration
   *
   * @param pk
   * @
   * @see
   */
  public void deleteIndex(QuestionContainerPK pk);

  public int getSilverObjectId(QuestionContainerPK pk);

  public String getHTMLQuestionPath(QuestionContainerDetail questionDetail);

  public QuestionContainerHeader getQuestionContainerHeader(
      QuestionContainerPK questionContainerPK);

  /**
   * create export file
   *
   * @param questionContainer : QuestionContainerDetail
   * @param addScore : boolean
   * @return export file name : String
   * @
   */
  public String exportCSV(QuestionContainerDetail questionContainer, boolean addScore);

  public Collection<ScoreDetail> getWorstScoresByFatherId(QuestionContainerPK questionContainerPK,
      int nbScores);
}
