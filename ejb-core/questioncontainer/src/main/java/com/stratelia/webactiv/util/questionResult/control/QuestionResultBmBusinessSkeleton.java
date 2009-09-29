/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.webactiv.util.questionResult.control;

import java.util.Collection;
import java.rmi.RemoteException;

import com.silverpeas.util.ForeignPK;
import com.stratelia.webactiv.util.questionResult.model.QuestionResult;

/*
 * CVS Informations
 * 
 * $Id: QuestionResultBmBusinessSkeleton.java,v 1.2 2006/08/16 11:56:47 neysseri Exp $
 * 
 * $Log: QuestionResultBmBusinessSkeleton.java,v $
 * Revision 1.2.4.1  2009/08/21 13:26:34  sfariello
 * Gestion non anonyme des enquêtes
 *
 * Revision 1.2  2006/08/16 11:56:47  neysseri
 * no message
 *
 * Revision 1.1.1.1  2002/08/06 14:47:53  nchaix
 * no message
 *
 * Revision 1.6  2001/12/19 16:36:11  neysseri
 * Stabilisation Lot 2 :
 * Mise en place Exceptions et SilverTrace + javadoc
 *
 */
 
/**
 * QuestionResult Business Manager
 * The QuestionResult is the EJB which permit to know what a user response to any question
 *
 * @author neysseri
 */
public interface QuestionResultBmBusinessSkeleton
{


    /**
     * Return all result to a given question
     *
     * @param questionPK the Question id
     *
     * @return a QuestionResult Collection
     *
     */
    public Collection getQuestionResultToQuestion(ForeignPK questionPK) throws RemoteException;

    /**
     * Return all result to a given question for a given participation
     *
     * @param questionPK		the Question id
     * @param participationId	the number of the participation
     *
     * @return a QuestionResult Collection
     *
     */
    public Collection getQuestionResultToQuestionByParticipation(ForeignPK questionPK, int participationId) throws RemoteException;

    /**
     * Return all user result to a given question
     *
     * @param questionPK		the Question id
     * @param userId			the user id
     *
     * @return a QuestionResult Collection
     *
     */
    public Collection getUserQuestionResultsToQuestion(String userId, ForeignPK questionPK) throws RemoteException;
    
    /**
     * Return all users by a answer
     *
     * @param answerPK			the Answer id
     *
     * @return a String Collection
     *
     */
    public Collection<String> getUsersByAnswer(String answerId) throws RemoteException;

    /**
     * Return all user result to a given question for a given participation
     *
	 * @param userId			the user id
     * @param questionPK		the Question id
     * @param participationId	the number of the participation
     *
     * @return a QuestionResult Collection
     *
     */
    public Collection getUserQuestionResultsToQuestionByParticipation(String userId, ForeignPK questionPK, int participationId) throws RemoteException;

    /**
     * Store response given by a user
     *
     *
     * @param result the QuestionResult
     *
     */
    public void setQuestionResultToUser(QuestionResult result) throws RemoteException;

    /**
     * Store responses given by a user
     *
     * @param results a Collection of QuestionResult
     *
     */
    public void setQuestionResultsToUser(Collection results) throws RemoteException;

    /**
     * Delete all results for a question
     *
     *
     * @param questionPK the question id
     *
     */
    public void deleteQuestionResultsToQuestion(ForeignPK questionPK) throws RemoteException;
}
