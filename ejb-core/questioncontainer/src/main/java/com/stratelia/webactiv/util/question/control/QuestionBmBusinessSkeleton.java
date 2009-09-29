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

package com.stratelia.webactiv.util.question.control;

import java.rmi.RemoteException;
import java.util.Collection;

import com.stratelia.webactiv.util.answer.model.Answer;
import com.stratelia.webactiv.util.answer.model.AnswerPK;
import com.stratelia.webactiv.util.question.model.Question;
import com.stratelia.webactiv.util.question.model.QuestionPK;

/*
 * CVS Informations
 * 
 * $Id: QuestionBmBusinessSkeleton.java,v 1.2 2006/08/16 11:56:33 neysseri Exp $
 * 
 * $Log: QuestionBmBusinessSkeleton.java,v $
 * Revision 1.2  2006/08/16 11:56:33  neysseri
 * no message
 *
 * Revision 1.1.1.1  2002/08/06 14:47:53  nchaix
 * no message
 *
 * Revision 1.7  2001/12/20 15:46:04  neysseri
 * Stabilisation Lot 2 :
 * Silvertrace et exceptions + javadoc
 *
 */

/**
 * A question is composed by its self attributes (see Question.java) and some
 * possibles answers (Answer.java) Reply from a user is stored in
 * QuestionResult.java. It's the link between Question, Answer and User.
 * 
 * @author neysseri
 */
public interface QuestionBmBusinessSkeleton {

  /**
   * Get a question
   * 
   * @param questionPK
   *          the question id
   * 
   * @return a Question
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public Question getQuestion(QuestionPK questionPK) throws RemoteException;

  /**
   * Get all questions for a given father
   * 
   * @param questionPK
   *          the question id
   * @param fatherId
   *          the father id
   * 
   * @return a Collection of Question
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public Collection getQuestionsByFatherPK(QuestionPK questionPK,
      String fatherId) throws RemoteException;

  /**
   * Create a new question
   * 
   * @param question
   *          the question to create
   * 
   * @return the id of the new question
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public QuestionPK createQuestion(Question question) throws RemoteException;

  /**
   * Create some questions to a given father
   * 
   * @param questions
   *          a Collection of Question to create
   * @param fatherId
   *          the father id
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public void createQuestions(Collection questions, String fatherId)
      throws RemoteException;

  /**
   * Delete the questions of a father
   * 
   * @param questionPK
   *          the question context
   * @param fatherId
   *          the father id
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public void deleteQuestionsByFatherPK(QuestionPK questionPK, String fatherId)
      throws RemoteException;

  /**
   * Delete a question
   * 
   * @param questionPK
   *          the question id to delete
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public void deleteQuestion(QuestionPK questionPK) throws RemoteException;

  /**
   * Update a question
   * 
   * @param questionDetail
   *          the question to update
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public void updateQuestion(Question questionDetail) throws RemoteException;

  /**
   * Update a question header (self attributes)
   * 
   * @param questionDetail
   *          the question attributes
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public void updateQuestionHeader(Question questionDetail)
      throws RemoteException;

  /**
   * Update the answers to a question
   * 
   * @param questionDetail
   *          the question containing the answers
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public void updateAnswersToAQuestion(Question questionDetail)
      throws RemoteException;

  /**
   * Update an answer to a question
   * 
   * @param answerDetail
   *          the answer to update
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public void updateAnswerToAQuestion(Answer answerDetail)
      throws RemoteException;

  /**
   * Delete all answers to a question
   * 
   * @param questionPK
   *          the question id
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public void deleteAnswersToAQuestion(QuestionPK questionPK)
      throws RemoteException;

  /**
   * Delete an answer to a question
   * 
   * @param answerPK
   *          the answer id to delete
   * @param questionPK
   *          the question id
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public void deleteAnswerToAQuestion(AnswerPK answerPK, QuestionPK questionPK)
      throws RemoteException;

  /**
   * Create some answers to a question
   * 
   * @param questionDetail
   *          the question which contains the answers
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public void createAnswersToAQuestion(Question questionDetail)
      throws RemoteException;

  /**
   * Add an answer to a question
   * 
   * @param answerDetail
   *          the new answer
   * @param questionPK
   *          the question id
   * 
   * @return the PK of the new answer
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public AnswerPK createAnswerToAQuestion(Answer answerDetail,
      QuestionPK questionPK) throws RemoteException;
}
