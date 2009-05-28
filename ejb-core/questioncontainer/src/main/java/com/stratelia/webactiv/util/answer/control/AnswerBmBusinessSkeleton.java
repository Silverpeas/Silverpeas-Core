/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.util.answer.control;

import java.util.Collection;
import java.rmi.RemoteException;

import com.silverpeas.util.ForeignPK;
import com.stratelia.webactiv.util.answer.model.Answer;
import com.stratelia.webactiv.util.answer.model.AnswerPK;

/**
 * Interface declaration
 *
 * @author neysseri
 */
public interface AnswerBmBusinessSkeleton
{
	/**
	 * Get answers which composed the question
	 *
	 * @param con			the Connection
	 * @param questionPK	the QuestionPK (question id)
	 *
	 * @return a Collection of Answer
	 *
	 */
	public Collection getAnswersByQuestionPK(ForeignPK questionPK) throws RemoteException;

	/**
	 * Record that the answer (answerPK) has been chosen to the question (questionPK)
	 *
	 * @param con			the Connection
	 * @param questionPK	the QuestionPK (question id)
	 * @param answerPK		the AnswerPK (answer id)
	 *
	 */
	public void recordThisAnswerAsVote(ForeignPK questionPK, AnswerPK answerPK) throws RemoteException;

	/**
	 * Add some answers to a question
	 *
	 * @param con			the Connection
	 * @param answers		a Collection of Answer
	 * @param questionPK	the QuestionPK (question id)
	 *
	 */
	public void addAnswersToAQuestion(Collection answers, ForeignPK questionPK) throws RemoteException;

	/**
	 * Add an answer to a question
	 *
	 * @param con			the Connection
	 * @param answer		the Answer
	 * @param questionPK	the QuestionPK (question id)
	 *
	 */
	public void addAnswerToAQuestion(Answer answer, ForeignPK questionPK) throws RemoteException;

	/**
	 * Update an answer to a question
	 *
	 * @param con			the Connection
	 * @param questionPK	the QuestionPK (question id)
	 * @param answer		the Answer
	 *
	 */
	public void updateAnswerToAQuestion(ForeignPK questionPK, Answer answer) throws RemoteException;

	/**
	 * Delete all answers to a given question
	 *
	 * @param con			the Connection
	 * @param questionPK	the QuestionPK (question id)
	 *
	 */
	public void deleteAnswersToAQuestion(ForeignPK questionPK) throws RemoteException;

	/**
	 * Delete an answer to a question
	 *
	 * @param con			the Connection
	 * @param questionPK	the QuestionPK (question id)
	 * @param answerId		the answer id
	 *
	 */
	public void deleteAnswerToAQuestion(ForeignPK questionPK, String answerId) throws RemoteException;
}
