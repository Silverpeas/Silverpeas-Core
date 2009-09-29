/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.webactiv.util.questionResult.control;

import javax.ejb.*;
import java.util.*;
import java.sql.*;
import java.rmi.RemoteException;

import com.silverpeas.util.ForeignPK;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.questionResult.model.*;
import com.stratelia.webactiv.util.questionResult.ejb.QuestionResultDAO;
import com.stratelia.webactiv.util.exception.*;

import com.stratelia.silverpeas.silvertrace.*;

/*
 * CVS Informations
 * 
 * $Id: QuestionResultBmEJB.java,v 1.2 2006/08/16 11:56:47 neysseri Exp $
 * 
 * $Log: QuestionResultBmEJB.java,v $
 * Revision 1.2.4.1  2009/08/21 13:26:34  sfariello
 * Gestion non anonyme des enquêtes
 *
 * Revision 1.2  2006/08/16 11:56:47  neysseri
 * no message
 *
 * Revision 1.1.1.1  2002/08/06 14:47:53  nchaix
 * no message
 *
 * Revision 1.9  2001/12/19 16:36:11  neysseri
 * Stabilisation Lot 2 :
 * Mise en place Exceptions et SilverTrace + javadoc
 *
 */
 
/**
 * QuestionResult Business Manager
 * See QuestionResultBmBusinessSkeleton for methods documentation
 *
 * @author neysseri
 */
public class QuestionResultBmEJB implements SessionBean, QuestionResultBmBusinessSkeleton
{

    private String           dbName = JNDINames.QUESTION_DATASOURCE;

    public QuestionResultBmEJB() {}

    public void setQuestionResultToUser(QuestionResult result) throws RemoteException
    {
		SilverTrace.info("questionResult", "QuestionResultBmEJB.setQuestionResultToUser()", "root.MSG_GEN_ENTER_METHOD", "questionResult =" + result);
        Connection con = null;

        try
        {
            con = getConnection();
            QuestionResultDAO.setQuestionResultToUser(con, result);
        }
        catch (Exception e)
        {
			throw new QuestionResultRuntimeException("QuestionResultBmEJB.getAnswersByQuestionPK()", SilverpeasRuntimeException.ERROR, "questionResult.RECORDING_RESPONSE_FAILED", e);
        }
        finally
        {
            freeConnection(con);
        }
    }

    public Collection getQuestionResultToQuestion(ForeignPK questionPK) throws RemoteException
    {
		SilverTrace.info("questionResult", "QuestionResultBmEJB.getQuestionResultToQuestion()", "root.MSG_GEN_ENTER_METHOD", "questionPK =" + questionPK);
        Connection con = null;

        try
        {
            con = getConnection();
            Collection result = QuestionResultDAO.getQuestionResultToQuestion(con, questionPK);

            return result;
        }
        catch (Exception e)
        {
			throw new QuestionResultRuntimeException("QuestionResultBmEJB.getQuestionResultToQuestion()", SilverpeasRuntimeException.ERROR, "questionResult.GETTING_RESPONSES_TO_QUESTION_FAILED", e);
        }
        finally
        {
            freeConnection(con);
        }
    }

    public Collection getUserQuestionResultsToQuestion(String userId, ForeignPK questionPK) throws RemoteException
    {
		SilverTrace.info("questionResult", "QuestionResultBmEJB.getUserQuestionResultsToQuestion()", "root.MSG_GEN_ENTER_METHOD", "userId = "+userId+", questionPK =" + questionPK);
        Connection con = null;

        try
        {
            con = getConnection();
            Collection result = QuestionResultDAO.getUserQuestionResultsToQuestion(con, userId, questionPK);

            return result;
        }
        catch (Exception e)
        {
			throw new QuestionResultRuntimeException("QuestionResultBmEJB.getUserQuestionResultsToQuestion()", SilverpeasRuntimeException.ERROR, "questionResult.GETTING_USER_RESPONSES_TO_QUESTION_FAILED", e);
        }
        finally
        {
            freeConnection(con);
        }
    }
    
    public Collection<String> getUsersByAnswer(String answerId) throws RemoteException
    {
        Connection con = null;
    	try
        {
            con = getConnection();
            return QuestionResultDAO.getUsersByAnswer(con, answerId);
        }
        catch (Exception e)
        {
			throw new QuestionResultRuntimeException("QuestionResultBmEJB.getUsersByAnswer()", SilverpeasRuntimeException.ERROR, "questionResult.GETTING_USER_RESPONSES_TO_QUESTION_FAILED", e);
        }
        finally
        {
            freeConnection(con);
        }
    }

    public void deleteQuestionResultsToQuestion(ForeignPK questionPK) throws RemoteException
    {
		SilverTrace.info("questionResult", "QuestionResultBmEJB.deleteQuestionResultsToQuestion()", "root.MSG_GEN_ENTER_METHOD", "questionPK =" + questionPK);
        Connection con = null;

        try
        {
            con = getConnection();
            QuestionResultDAO.deleteQuestionResultToQuestion(con, questionPK);
        }
        catch (Exception e)
        {
			throw new QuestionResultRuntimeException("QuestionResultBmEJB.deleteQuestionResultsToQuestion()", SilverpeasRuntimeException.ERROR, "questionResult.DELETING_RESPONSES_TO_QUESTION_FAILED", e);
        }
        finally
        {
            freeConnection(con);
        }
    }

    public Collection getQuestionResultToQuestionByParticipation(ForeignPK questionPK, int participationId) throws RemoteException
    {
		SilverTrace.info("questionResult", "QuestionResultBmEJB.getQuestionResultToQuestionByParticipation()", "root.MSG_GEN_ENTER_METHOD", "questionPK =" + questionPK+", participationId = "+participationId);
        Connection con = null;

        try
        {
            con = getConnection();
            return QuestionResultDAO.getQuestionResultToQuestionByParticipation(con, questionPK, participationId);
        }
        catch (Exception e)
        {
			throw new QuestionResultRuntimeException("QuestionResultBmEJB.getQuestionResultToQuestionByParticipation()", SilverpeasRuntimeException.ERROR, "questionResult.GETTING_RESPONSES_TO_QUESTION_AND_PARTICIPATION_FAILED", e);
        }
        finally
        {
            freeConnection(con);
        }
    }

    public Collection getUserQuestionResultsToQuestionByParticipation(String userId, ForeignPK questionPK, int participationId) throws RemoteException
    {
		SilverTrace.info("questionResult", "QuestionResultBmEJB.getUserQuestionResultsToQuestionByParticipation()", "root.MSG_GEN_ENTER_METHOD", "userId = "+userId+", questionPK =" + questionPK+", participationId = "+participationId);
        Connection con = null;

        try
        {
            con = getConnection();
            return QuestionResultDAO.getUserQuestionResultsToQuestionByParticipation(con, userId, questionPK, participationId);
        }
        catch (Exception e)
        {
			throw new QuestionResultRuntimeException("QuestionResultBmEJB.getUserQuestionResultsToQuestionByParticipation()", SilverpeasRuntimeException.ERROR, "questionResult.GETTING_USER_RESPONSES_TO_QUESTION_AND_PARTICIPATION_FAILED", e);
        }
        finally
        {
            freeConnection(con);
        }
    }

    public void setQuestionResultsToUser(Collection results) throws RemoteException
    {
		SilverTrace.info("questionResult", "QuestionResultBmEJB.setQuestionResultsToUser()", "root.MSG_GEN_ENTER_METHOD", "");
        if (results != null)
        {
            Iterator iterator = results.iterator();

            while (iterator.hasNext())
            {
                QuestionResult questionResult = (QuestionResult) iterator.next();
                setQuestionResultToUser(questionResult);
            }
        }
    }


	private Connection getConnection()
    {
        try
        {
            return DBUtil.makeConnection(dbName);
        }
        catch (Exception e)
        {
            throw new QuestionResultRuntimeException("QuestionResultBmEJB.getConnection()", SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
        }
    }

    private void freeConnection(Connection con)
    {
        if (con != null)
        {
            try
            {
                con.close();
            }
            catch (Exception e)
            {
                SilverTrace.error("answer", "QuestionResultBmEJB.freeConnection()", "root.EX_CONNECTION_CLOSE_FAILED", "", e);
            }
        }
    }
    
    public void ejbCreate()
    {
    }

    public void ejbRemove()
    {
    }

    public void ejbActivate()
    {
    }

    public void ejbPassivate()
    {
    }

    public void setSessionContext(SessionContext sc)
    {
    }

}