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
package com.stratelia.webactiv.util.question.control;

import java.util.Collection;

import javax.ejb.*;

import com.stratelia.webactiv.util.answer.model.Answer;
import com.stratelia.webactiv.util.answer.model.AnswerPK;
import com.stratelia.webactiv.util.question.model.Question;
import com.stratelia.webactiv.util.question.model.QuestionPK;

/**
 * Interface declaration
 *
 * @author
 */
@Local
public interface QuestionBm {

  /**
   * Get a question
   *
   * @param questionPK the question id
   * @return a Question
   * @
   * @see
   */
  public Question getQuestion(QuestionPK questionPK);

  /**
   * Get all questions for a given father
   *
   * @param questionPK the question id
   * @param fatherId the father id
   * @return a Collection of Question
   * @
   * @see
   */
  public Collection<Question> getQuestionsByFatherPK(QuestionPK questionPK, String fatherId);

  /**
   * Create a new question
   *
   * @param question the question to create
   * @return the id of the new question
   * @
   * @see
   */
  public QuestionPK createQuestion(Question question);

  /**
   * Create some questions to a given father
   *
   * @param questions a Collection of Question to create
   * @param fatherId the father id
   * @see
   */
  public void createQuestions(Collection<Question> questions, String fatherId);

  /**
   * Delete the questions of a father
   *
   * @param questionPK the question context
   * @param fatherId the father id
   * @see
   */
  public void deleteQuestionsByFatherPK(QuestionPK questionPK, String fatherId);

  /**
   * Delete a question
   *
   * @param questionPK the question id to delete
   * @see
   */
  public void deleteQuestion(QuestionPK questionPK);

  /**
   * Update a question
   *
   * @param questionDetail the question to update
   * @
   * @see
   */
  public void updateQuestion(Question questionDetail);

  /**
   * Update a question header (self attributes)
   *
   * @param questionDetail the question attributes
   * @see
   */
  public void updateQuestionHeader(Question questionDetail);

  /**
   * Update the answers to a question
   *
   * @param questionDetail the question containing the answers
   * @see
   */
  public void updateAnswersToAQuestion(Question questionDetail);

  /**
   * Update an answer to a question
   *
   * @param answerDetail the answer to update
   * @see
   */
  public void updateAnswerToAQuestion(Answer answerDetail);

  /**
   * Delete all answers to a question
   *
   * @param questionPK the question 
   * @see
   */
  public void deleteAnswersToAQuestion(QuestionPK questionPK);

  /**
   * Delete an answer to a question
   *
   * @param answerPK the answer id to delete
   * @param questionPK the question id
   * @
   * @see
   */
  public void deleteAnswerToAQuestion(AnswerPK answerPK, QuestionPK questionPK);

  /**
   * Create some answers to a question
   *
   * @param questionDetail the question which contains the answers
   * @
   * @see
   */
  public void createAnswersToAQuestion(Question questionDetail);

  /**
   * Add an answer to a question
   *
   * @param answerDetail the new answer
   * @param questionPK the question id
   * @return the PK of the new answer
   * @
   * @see
   */
  public AnswerPK createAnswerToAQuestion(Answer answerDetail, QuestionPK questionPK);
}
