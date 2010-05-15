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

import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.Collection;

import javax.ejb.SessionContext;

import com.silverpeas.util.ForeignPK;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.answer.ejb.AnswerDAO;
import com.stratelia.webactiv.util.answer.model.Answer;
import com.stratelia.webactiv.util.answer.model.AnswerPK;
import com.stratelia.webactiv.util.answer.model.AnswerRuntimeException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

/**
 * Answer Business Manager See AnswerBmBusinessSkeleton for methods documentation
 * @author neysseri
 */
public class AnswerBmEJB implements javax.ejb.SessionBean, AnswerBmBusinessSkeleton {
  private String dbName = JNDINames.ANSWER_DATASOURCE;

  public AnswerBmEJB() {
  }

  public Collection getAnswersByQuestionPK(ForeignPK questionPK)
      throws RemoteException {
    SilverTrace.info("answer", "AnswerBmEJB.getAnswersByQuestionPK()",
        "root.MSG_GEN_ENTER_METHOD", "questionPK =" + questionPK);
    Connection con = null;

    try {
      con = getConnection();
      Collection answers = AnswerDAO.getAnswersByQuestionPK(con, questionPK);

      return answers;
    } catch (Exception e) {
      throw new AnswerRuntimeException("AnswerBmEJB.getAnswersByQuestionPK()",
          SilverpeasRuntimeException.ERROR,
          "answer.ANSWER_LIST_TO_QUESTION_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  public void recordThisAnswerAsVote(ForeignPK questionPK, AnswerPK answerPK)
      throws RemoteException {
    SilverTrace.info("answer", "AnswerBmEJB.recordThisAnswerAsVote()",
        "root.MSG_GEN_ENTER_METHOD", "questionPK=" + questionPK + ", answerPK="
        + answerPK);
    Connection con = null;

    try {
      con = getConnection();
      AnswerDAO.recordThisAnswerAsVote(con, questionPK, answerPK);
    } catch (Exception e) {
      throw new AnswerRuntimeException("AnswerBmEJB.recordThisAnswerAsVote()",
          SilverpeasRuntimeException.ERROR, "answer.RECORDING_RESPONSE_FAILED",
          e);
    } finally {
      freeConnection(con);
    }
  }

  public void addAnswersToAQuestion(Collection answers, ForeignPK questionPK)
      throws RemoteException {
    SilverTrace.info("answer", "AnswerBmEJB.addAnswersToAQuestion()",
        "root.MSG_GEN_ENTER_METHOD", "questionPK=" + questionPK);
    Connection con = null;

    try {
      con = getConnection();

      // Transform the 'special' caracters for storing them in Database
      /*
       * Iterator i = answers.iterator(); while (i.hasNext()) { Answer ans = (Answer) i.next();
       * ans.setLabel(Encode.transformStringForBD(ans.getLabel()));
       * ans.setComment(Encode.transformStringForBD(ans.getComment())); }
       */

      AnswerDAO.addAnswersToAQuestion(con, answers, questionPK);
    } catch (Exception e) {
      throw new AnswerRuntimeException("AnswerBmEJB.addAnswersToAQuestion()",
          SilverpeasRuntimeException.ERROR,
          "answer.ADDING_ANSWERS_TO_QUESTION_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  public void addAnswerToAQuestion(Answer answer, ForeignPK questionPK)
      throws RemoteException {
    SilverTrace.info("answer", "AnswerBmEJB.addAnswerToAQuestion()",
        "root.MSG_GEN_ENTER_METHOD", "questionPK=" + questionPK
        + " and answer = " + answer.toString());
    Connection con = null;

    try {
      con = getConnection();

      // Transform the 'special' caracters for storing them in Database
      /*
       * answer.setLabel(Encode.transformStringForBD(answer.getLabel()));
       * answer.setComment(Encode.transformStringForBD(answer.getComment()));
       */

      AnswerDAO.addAnswerToAQuestion(con, answer, questionPK);
    } catch (Exception e) {
      throw new AnswerRuntimeException("AnswerBmEJB.addAnswerToAQuestion()",
          SilverpeasRuntimeException.ERROR,
          "answer.ADDING_ANSWER_TO_QUESTION_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  public void deleteAnswersToAQuestion(ForeignPK questionPK)
      throws RemoteException {
    SilverTrace.info("answer", "AnswerBmEJB.deleteAnswersToAQuestion()",
        "root.MSG_GEN_ENTER_METHOD", "questionPK=" + questionPK);
    Connection con = null;

    try {
      con = getConnection();
      AnswerDAO.deleteAnswersToAQuestion(con, questionPK);
    } catch (Exception e) {
      throw new AnswerRuntimeException(
          "AnswerBmEJB.deleteAnswersToAQuestion()",
          SilverpeasRuntimeException.ERROR,
          "answer.DELETING_ANSWERS_TO_QUESTION_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  public void deleteAnswerToAQuestion(ForeignPK questionPK, String answerId)
      throws RemoteException {
    SilverTrace.info("answer", "AnswerBmEJB.deleteAnswerToAQuestion()",
        "root.MSG_GEN_ENTER_METHOD", "questionPK=" + questionPK + ", answerId="
        + answerId);
    Connection con = null;

    try {
      con = getConnection();
      AnswerDAO.deleteAnswerToAQuestion(con, questionPK, answerId);
    } catch (Exception e) {
      throw new AnswerRuntimeException("AnswerBmEJB.deleteAnswerToAQuestion()",
          SilverpeasRuntimeException.ERROR,
          "answer.DELETING_ANSWER_TO_QUESTION_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  public void updateAnswerToAQuestion(ForeignPK questionPK, Answer answer)
      throws RemoteException {
    SilverTrace.info("answer", "AnswerBmEJB.updateAnswerToAQuestion()",
        "root.MSG_GEN_ENTER_METHOD", "questionPK=" + questionPK + ", answer="
        + answer.toString());
    Connection con = null;

    try {
      con = getConnection();

      // Transform the 'special' caracters for storing them in Database
      /*
       * answer.setLabel(Encode.transformStringForBD(answer.getLabel()));
       * answer.setComment(Encode.transformStringForBD(answer.getComment()));
       */

      AnswerDAO.updateAnswerToAQuestion(con, questionPK, answer);
    } catch (Exception e) {
      throw new AnswerRuntimeException("AnswerBmEJB.updateAnswerToAQuestion()",
          SilverpeasRuntimeException.ERROR,
          "answer.UPDATING_ANSWER_TO_QUESTION_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  private Connection getConnection() {
    try {
      return DBUtil.makeConnection(dbName);
    } catch (Exception e) {
      throw new AnswerRuntimeException("AnswerBmEJB.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  private void freeConnection(Connection con) {
    if (con != null) {
      try {
        con.close();
      } catch (Exception e) {
        SilverTrace.error("answer", "AnswerBmEJB.freeConnection()",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

  /* Ejb Methods */

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
