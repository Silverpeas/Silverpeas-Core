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
package org.silverpeas.core.questioncontainer.container.service;

import org.silverpeas.core.questioncontainer.answer.model.AnswerPK;
import org.silverpeas.core.questioncontainer.question.model.Question;
import org.silverpeas.core.questioncontainer.question.model.QuestionPK;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerDetail;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerHeader;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerPK;
import org.silverpeas.core.questioncontainer.result.model.QuestionResult;
import org.silverpeas.core.questioncontainer.score.model.ScoreDetail;
import org.silverpeas.core.util.ServiceProvider;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author neysseri
 */
public interface QuestionContainerService {

  static QuestionContainerService getInstance() {
    return ServiceProvider.getService(QuestionContainerService.class);
  }

  /**
   * @param questionContainerPK the question container identifier
   * @param userId the user identifier
   * @param reply
   */
  public void recordReplyToQuestionContainerByUser(QuestionContainerPK questionContainerPK,
      String userId, Map<String, List<String>> reply);

  /**
   * @param questionContainerPK the question container identifier
   * @param userId the user identifier
   * @param reply
   * @param comment
   * @param isAnonymousComment
   */
  public void recordReplyToQuestionContainerByUser(QuestionContainerPK questionContainerPK,
      String userId, Map<String, List<String>> reply, String comment, boolean isAnonymousComment);

  /**
   * @param questionContainerPK the question container identifier
   * @return
   */
  public Collection<QuestionResult> getSuggestions(QuestionContainerPK questionContainerPK);

  /**
   * Return the suggestion of the user, for the question and the answer
   * @param userId the user identifier
   * @param questionPK
   * @param answerPK
   * @return QuestionResult
   */
  public QuestionResult getSuggestion(String userId, QuestionPK questionPK, AnswerPK answerPK);

  /**
   * @param questionContainerPK the question container identifier
   * @return
   */
  public Collection<QuestionContainerHeader> getQuestionContainers(
      QuestionContainerPK questionContainerPK);

  /**
   * @param pks A collection of QuestionContainer id
   * @return
   */
  public Collection<QuestionContainerHeader> getQuestionContainerHeaders(
      List<QuestionContainerPK> pks);

  /**
   * @param questionContainerPK the question container identifier
   * @param userId the user identifier
   * @return
   */
  public QuestionContainerDetail getQuestionContainer(QuestionContainerPK questionContainerPK,
      String userId);

  /**
   * @param questionContainerPK the question container identifier
   * @param userId the user identifier
   * @param participationId
   * @return
   */
  public QuestionContainerDetail getQuestionContainerByParticipationId(
      QuestionContainerPK questionContainerPK, String userId, int participationId);

  /**
   * @param questionContainerPK the question container identifier
   * @return
   */
  public Collection<QuestionContainerHeader> getNotClosedQuestionContainers(
      QuestionContainerPK questionContainerPK);

  /**
   * @param questionContainerPK the question container identifier
   * @return
   */
  public Collection<QuestionContainerHeader> getOpenedQuestionContainers(
      QuestionContainerPK questionContainerPK);

  /**
   * @param questionContainerPK the question container identifier
   * @return
   */
  public Collection<QuestionContainerHeader> getClosedQuestionContainers(
      QuestionContainerPK questionContainerPK);

  /**
   * @param questionContainerPK the question container identifier
   * @return
   */
  public Collection<QuestionContainerHeader> getInWaitQuestionContainers(
      QuestionContainerPK questionContainerPK);

  /**
   * @param questionContainerPK the question container identifier
   */
  public void closeQuestionContainer(QuestionContainerPK questionContainerPK);

  /**
   * @param questionContainerPK the question container identifier
   */
  public void openQuestionContainer(QuestionContainerPK questionContainerPK);

  /**
   * @param questionContainerPK the question container identifier
   * @return
   */
  public int getNbVotersByQuestionContainer(QuestionContainerPK questionContainerPK);

  /**
   * @param questionContainerPK the question container identifier
   * @param questionContainerDetail
   * @param userId the user identifier
   * @return
   */
  public QuestionContainerPK createQuestionContainer(QuestionContainerPK questionContainerPK,
      QuestionContainerDetail questionContainerDetail, String userId);

  /**
   * @param questionContainerPK the question container identifier
   */
  public void deleteQuestionContainer(QuestionContainerPK questionContainerPK);

  public void deleteVotes(QuestionContainerPK questionContainerPK);

  /**
   * @param questionContainerHeader
   */
  public void updateQuestionContainerHeader(QuestionContainerHeader questionContainerHeader);

  /**
   * @param questionContainerPK the question container identifier
   * @param questions
   */
  public void updateQuestions(QuestionContainerPK questionContainerPK,
      Collection<Question> questions);

  /**
   * @param questionContainerPK the question container identifier
   * @return
   */
  public float getAveragePoints(QuestionContainerPK questionContainerPK);

  /**
   * @param questionContainerPK the question container identifier
   * @param userId the user identifier
   * @return
   */
  public Collection<QuestionContainerHeader> getOpenedQuestionContainersAndUserScores(
      QuestionContainerPK questionContainerPK, String userId);

  /**
   * @param questionContainerPK the question container identifier
   * @return
   */
  public Collection<QuestionContainerHeader> getQuestionContainersWithScores(
      QuestionContainerPK questionContainerPK);

  /**
   * @param questionContainerPK the question container identifier
   * @param userId
   * @return
   */
  public Collection<QuestionContainerHeader> getQuestionContainersWithUserScores(
      QuestionContainerPK questionContainerPK, String userId);

  /**
   * @param questionContainerPK the question container identifier
   * @param userId the user identifier
   * @return
   */
  public Collection<ScoreDetail> getUserScoresByFatherId(QuestionContainerPK questionContainerPK,
      String userId);

  /**
   * @param questionContainerPK the question container identifier
   * @param nbBestScores
   * @return
   */
  public Collection<ScoreDetail> getBestScoresByFatherId(QuestionContainerPK questionContainerPK,
      int nbBestScores);

  /**
   * @param questionContainerPK the question container identifier
   * @return
   */
  public float getAverageScoreByFatherId(QuestionContainerPK questionContainerPK);

  /**
   * @param questionContainerPK the question container identifier
   * @return
   */
  public Collection<ScoreDetail> getScoresByFatherId(QuestionContainerPK questionContainerPK);

  /**
   * @param questionContainerPK the question container identifier
   * @param userId the user identifier
   * @return
   */
  public int getUserNbParticipationsByFatherId(QuestionContainerPK questionContainerPK,
      String userId);

  /**
   * @param questionContainerPK the question container identifier
   * @param userId the user identifier
   * @param participationId
   * @return
   */
  public ScoreDetail getUserScoreByFatherIdAndParticipationId(
      QuestionContainerPK questionContainerPK, String userId, int participationId);

  /**
   * @param questionContainerPK the question container identifier
   * @param scoreDetail
   */
  public void updateScore(QuestionContainerPK questionContainerPK, ScoreDetail scoreDetail);

  /**
   * @param pk
   */
  public void deleteIndex(QuestionContainerPK pk);

  public int getSilverObjectId(QuestionContainerPK pk);

  public String getHTMLQuestionPath(QuestionContainerDetail questionDetail);

  public QuestionContainerHeader getQuestionContainerHeader(
      QuestionContainerPK questionContainerPK);

  /**
   * create export file
   * @param questionContainer : QuestionContainerDetail
   * @param addScore : boolean
   * @return export file name : String
   */
  public String exportCSV(QuestionContainerDetail questionContainer, boolean addScore);

  public Collection<ScoreDetail> getWorstScoresByFatherId(QuestionContainerPK questionContainerPK,
      int nbScores);
}
