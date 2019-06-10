/*
 * Copyright (C) 2000 - 2018 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General License for more details.
 *
 * You should have received a copy of the GNU Affero General License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.questioncontainer.answer.service;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.questioncontainer.answer.model.Answer;
import org.silverpeas.core.questioncontainer.answer.model.AnswerPK;
import org.silverpeas.core.util.ServiceProvider;

import java.util.Collection;

public interface AnswerService {

  static AnswerService get() {
    return ServiceProvider.getService(AnswerService.class);
  }

  /**
   * Get answers which composed the question
   * @param questionPK the QuestionPK (question id)
   * @return a Collection of Answer
   */
  Collection<Answer> getAnswersByQuestionPK(ResourceReference questionPK);

  /**
   * Record that the answer (answerPK) has been chosen to the question (questionPK)
   * @param questionPK the QuestionPK (question id)
   * @param answerPK the AnswerPK (answer id)
   */
  void recordThisAnswerAsVote(ResourceReference questionPK, AnswerPK answerPK);

  /**
   * Add some answers to a question
   * @param answers a Collection of Answer
   * @param questionPK the QuestionPK (question id)
   */
  void addAnswersToAQuestion(Collection<Answer> answers, ResourceReference questionPK);

  /**
   * Add an answer to a question
   * @param answer the Answer
   * @param questionPK the QuestionPK (question id)
   */
  void addAnswerToAQuestion(Answer answer, ResourceReference questionPK);

  /**
   * Update an answer to a question
   * @param questionPK the QuestionPK (question id)
   * @param answer the Answer
   */
  void updateAnswerToAQuestion(ResourceReference questionPK, Answer answer);

  /**
   * Delete all answers to a given question
   * @param questionPK the QuestionPK (question id)
   */
  void deleteAnswersToAQuestion(ResourceReference questionPK);

  /**
   * Delete an answer to a question
   * @param questionPK the QuestionPK (question id)
   * @param answerId the answer id
   */
  void deleteAnswerToAQuestion(ResourceReference questionPK, String answerId);
}
