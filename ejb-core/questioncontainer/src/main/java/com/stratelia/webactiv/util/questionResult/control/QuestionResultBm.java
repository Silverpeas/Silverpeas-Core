/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.stratelia.webactiv.util.questionResult.control;

import java.util.Collection;

import javax.ejb.Local;

import com.silverpeas.util.ForeignPK;

import com.stratelia.webactiv.util.questionResult.model.QuestionResult;

@Local
public interface QuestionResultBm {

  /**
   * Return all result to a given question
   *
   * @param questionPK the Question id
   * @return a QuestionResult Collection
   */
  public Collection<QuestionResult> getQuestionResultToQuestion(ForeignPK questionPK);

  /**
   * Return all result to a given question for a given participation
   *
   * @param questionPK the Question id
   * @param participationId the number of the participation
   * @return a QuestionResult Collection
   */
  public Collection<QuestionResult> getQuestionResultToQuestionByParticipation(
      ForeignPK questionPK, int participationId);

  /**
   * Return all user result to a given question
   *
   * @param questionPK the Question id
   * @param userId the user id
   * @return a QuestionResult Collection
   */
  public Collection<QuestionResult> getUserQuestionResultsToQuestion(String userId,
      ForeignPK questionPK);

  /**
   * Return all users by a answer
   *
   * @param answerId the Answer id
   * @return a String Collection
   */
  public Collection<String> getUsersByAnswer(String answerId);

  /**
   * Return all user result to a given question for a given participation
   *
   * @param userId the user id
   * @param questionPK the Question id
   * @param participationId the number of the participation
   * @return a QuestionResult Collection
   */
  public Collection<QuestionResult> getUserQuestionResultsToQuestionByParticipation(String userId,
      ForeignPK questionPK, int participationId);

  /**
   * Store response given by a user
   *
   * @param result the QuestionResult
   */
  public void setQuestionResultToUser(QuestionResult result);

  /**
   * Store responses given by a user
   *
   * @param results a Collection of QuestionResult
   */
  public void setQuestionResultsToUser(Collection<QuestionResult> results);

  /**
   * Delete all results for a question
   *
   * @param questionPK the question id
   */
  public void deleteQuestionResultsToQuestion(ForeignPK questionPK);
}