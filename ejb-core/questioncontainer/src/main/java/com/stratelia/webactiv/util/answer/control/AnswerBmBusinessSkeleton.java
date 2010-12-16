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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.stratelia.webactiv.util.answer.control;

import java.util.Collection;
import java.rmi.RemoteException;

import com.silverpeas.util.ForeignPK;
import com.stratelia.webactiv.util.answer.model.Answer;
import com.stratelia.webactiv.util.answer.model.AnswerPK;

/**
 * Interface declaration
 * @author neysseri
 */
public interface AnswerBmBusinessSkeleton {
  /**
   * Get answers which composed the question
   * @param con the Connection
   * @param questionPK the QuestionPK (question id)
   * @return a Collection of Answer
   */
  public Collection<Answer> getAnswersByQuestionPK(ForeignPK questionPK)
      throws RemoteException;

  /**
   * Record that the answer (answerPK) has been chosen to the question (questionPK)
   * @param con the Connection
   * @param questionPK the QuestionPK (question id)
   * @param answerPK the AnswerPK (answer id)
   */
  public void recordThisAnswerAsVote(ForeignPK questionPK, AnswerPK answerPK)
      throws RemoteException;

  /**
   * Add some answers to a question
   * @param con the Connection
   * @param answers a Collection of Answer
   * @param questionPK the QuestionPK (question id)
   */
  public void addAnswersToAQuestion(Collection<Answer> answers, ForeignPK questionPK)
      throws RemoteException;

  /**
   * Add an answer to a question
   * @param con the Connection
   * @param answer the Answer
   * @param questionPK the QuestionPK (question id)
   */
  public void addAnswerToAQuestion(Answer answer, ForeignPK questionPK)
      throws RemoteException;

  /**
   * Update an answer to a question
   * @param con the Connection
   * @param questionPK the QuestionPK (question id)
   * @param answer the Answer
   */
  public void updateAnswerToAQuestion(ForeignPK questionPK, Answer answer)
      throws RemoteException;

  /**
   * Delete all answers to a given question
   * @param con the Connection
   * @param questionPK the QuestionPK (question id)
   */
  public void deleteAnswersToAQuestion(ForeignPK questionPK)
      throws RemoteException;

  /**
   * Delete an answer to a question
   * @param con the Connection
   * @param questionPK the QuestionPK (question id)
   * @param answerId the answer id
   */
  public void deleteAnswerToAQuestion(ForeignPK questionPK, String answerId)
      throws RemoteException;
}
