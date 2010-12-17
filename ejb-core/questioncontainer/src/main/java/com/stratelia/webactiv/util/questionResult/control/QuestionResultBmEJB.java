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

package com.stratelia.webactiv.util.questionResult.control;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import com.silverpeas.util.ForeignPK;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.questionResult.ejb.QuestionResultDAO;
import com.stratelia.webactiv.util.questionResult.model.QuestionResult;
import com.stratelia.webactiv.util.questionResult.model.QuestionResultRuntimeException;

/**
 * QuestionResult Business Manager See QuestionResultBmBusinessSkeleton for methods documentation
 * @author neysseri
 */
public class QuestionResultBmEJB implements SessionBean, QuestionResultBmBusinessSkeleton {

  private static final long serialVersionUID = -1184974708719525868L;
  private String dbName = JNDINames.QUESTION_DATASOURCE;

  public QuestionResultBmEJB() {
  }

  public void setQuestionResultToUser(QuestionResult result) throws RemoteException {
    SilverTrace.info("questionResult", "QuestionResultBmEJB.setQuestionResultToUser()",
        "root.MSG_GEN_ENTER_METHOD", "questionResult =" + result);
    Connection con = null;

    try {
      con = getConnection();
      QuestionResultDAO.setQuestionResultToUser(con, result);
    } catch (Exception e) {
      throw new QuestionResultRuntimeException("QuestionResultBmEJB.getAnswersByQuestionPK()",
          SilverpeasRuntimeException.ERROR, "questionResult.RECORDING_RESPONSE_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  public Collection<QuestionResult> getQuestionResultToQuestion(ForeignPK questionPK)
      throws RemoteException {
    SilverTrace.info("questionResult", "QuestionResultBmEJB.getQuestionResultToQuestion()",
        "root.MSG_GEN_ENTER_METHOD", "questionPK =" + questionPK);
    Connection con = null;

    try {
      con = getConnection();
      Collection<QuestionResult> result =
          QuestionResultDAO.getQuestionResultToQuestion(con, questionPK);

      return result;
    } catch (Exception e) {
      throw new QuestionResultRuntimeException("QuestionResultBmEJB.getQuestionResultToQuestion()",
          SilverpeasRuntimeException.ERROR, "questionResult.GETTING_RESPONSES_TO_QUESTION_FAILED",
          e);
    } finally {
      freeConnection(con);
    }
  }

  public Collection<QuestionResult> getUserQuestionResultsToQuestion(String userId,
      ForeignPK questionPK)
      throws RemoteException {
    SilverTrace.info("questionResult", "QuestionResultBmEJB.getUserQuestionResultsToQuestion()",
        "root.MSG_GEN_ENTER_METHOD", "userId = " + userId + ", questionPK =" + questionPK);
    Connection con = null;

    try {
      con = getConnection();
      Collection<QuestionResult> result =
          QuestionResultDAO.getUserQuestionResultsToQuestion(con, userId, questionPK);

      return result;
    } catch (Exception e) {
      throw new QuestionResultRuntimeException(
          "QuestionResultBmEJB.getUserQuestionResultsToQuestion()",
          SilverpeasRuntimeException.ERROR,
          "questionResult.GETTING_USER_RESPONSES_TO_QUESTION_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  public Collection<String> getUsersByAnswer(String answerId) throws RemoteException {
    Connection con = null;
    try {
      con = getConnection();
      return QuestionResultDAO.getUsersByAnswer(con, answerId);
    } catch (Exception e) {
      throw new QuestionResultRuntimeException("QuestionResultBmEJB.getUsersByAnswer()",
          SilverpeasRuntimeException.ERROR,
          "questionResult.GETTING_USER_RESPONSES_TO_QUESTION_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  public void deleteQuestionResultsToQuestion(ForeignPK questionPK) throws RemoteException {
    SilverTrace.info("questionResult", "QuestionResultBmEJB.deleteQuestionResultsToQuestion()",
        "root.MSG_GEN_ENTER_METHOD", "questionPK =" + questionPK);
    Connection con = null;

    try {
      con = getConnection();
      QuestionResultDAO.deleteQuestionResultToQuestion(con, questionPK);
    } catch (Exception e) {
      throw new QuestionResultRuntimeException(
          "QuestionResultBmEJB.deleteQuestionResultsToQuestion()",
          SilverpeasRuntimeException.ERROR, "questionResult.DELETING_RESPONSES_TO_QUESTION_FAILED",
          e);
    } finally {
      freeConnection(con);
    }
  }

  public Collection<QuestionResult> getQuestionResultToQuestionByParticipation(
      ForeignPK questionPK,
      int participationId) throws RemoteException {
    SilverTrace.info("questionResult",
        "QuestionResultBmEJB.getQuestionResultToQuestionByParticipation()",
        "root.MSG_GEN_ENTER_METHOD", "questionPK =" + questionPK + ", participationId = " +
        participationId);
    Connection con = null;

    try {
      con = getConnection();
      return QuestionResultDAO.getQuestionResultToQuestionByParticipation(con, questionPK,
          participationId);
    } catch (Exception e) {
      throw new QuestionResultRuntimeException(
          "QuestionResultBmEJB.getQuestionResultToQuestionByParticipation()",
          SilverpeasRuntimeException.ERROR,
          "questionResult.GETTING_RESPONSES_TO_QUESTION_AND_PARTICIPATION_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  public Collection<QuestionResult> getUserQuestionResultsToQuestionByParticipation(String userId,
      ForeignPK questionPK, int participationId) throws RemoteException {
    SilverTrace.info("questionResult",
        "QuestionResultBmEJB.getUserQuestionResultsToQuestionByParticipation()",
        "root.MSG_GEN_ENTER_METHOD", "userId = " + userId + ", questionPK =" + questionPK +
        ", participationId = " + participationId);
    Connection con = null;

    try {
      con = getConnection();
      return QuestionResultDAO.getUserQuestionResultsToQuestionByParticipation(con, userId,
          questionPK, participationId);
    } catch (Exception e) {
      throw new QuestionResultRuntimeException(
          "QuestionResultBmEJB.getUserQuestionResultsToQuestionByParticipation()",
          SilverpeasRuntimeException.ERROR,
          "questionResult.GETTING_USER_RESPONSES_TO_QUESTION_AND_PARTICIPATION_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  public void setQuestionResultsToUser(Collection<QuestionResult> results) throws RemoteException {
    SilverTrace.info("questionResult", "QuestionResultBmEJB.setQuestionResultsToUser()",
        "root.MSG_GEN_ENTER_METHOD", "");
    if (results != null) {
      Iterator<QuestionResult> iterator = results.iterator();

      while (iterator.hasNext()) {
        QuestionResult questionResult = iterator.next();
        setQuestionResultToUser(questionResult);
      }
    }
  }

  private Connection getConnection() {
    try {
      return DBUtil.makeConnection(dbName);
    } catch (Exception e) {
      throw new QuestionResultRuntimeException("QuestionResultBmEJB.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  private void freeConnection(Connection con) {
    if (con != null) {
      try {
        con.close();
      } catch (Exception e) {
        SilverTrace.error("answer", "QuestionResultBmEJB.freeConnection()",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

  public void ejbCreate() {
  }

  public void ejbRemove() {
  }

  public void ejbActivate() {
  }

  public void ejbPassivate() {
  }

  public void setSessionContext(SessionContext sc) {
  }

}