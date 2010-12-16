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

package com.stratelia.webactiv.util.question.control;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.ejb.SessionContext;

import com.silverpeas.util.ForeignPK;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.answer.control.AnswerBm;
import com.stratelia.webactiv.util.answer.control.AnswerBmHome;
import com.stratelia.webactiv.util.answer.model.Answer;
import com.stratelia.webactiv.util.answer.model.AnswerPK;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.question.ejb.QuestionDAO;
import com.stratelia.webactiv.util.question.model.Question;
import com.stratelia.webactiv.util.question.model.QuestionPK;
import com.stratelia.webactiv.util.question.model.QuestionRuntimeException;
import com.stratelia.webactiv.util.questionResult.control.QuestionResultBm;
import com.stratelia.webactiv.util.questionResult.control.QuestionResultBmHome;

/*
 * CVS Informations
 *
 * $Id: QuestionBmEJB.java,v 1.2 2006/08/16 11:56:33 neysseri Exp $
 *
 * $Log: QuestionBmEJB.java,v $
 * Revision 1.2  2006/08/16 11:56:33  neysseri
 * no message
 *
 * Revision 1.1.1.1  2002/08/06 14:47:53  nchaix
 * no message
 *
 * Revision 1.14  2002/04/16 10:03:06  santonio
 * ajout d'une methode pour transformer les carecteres speciaux comme € et ’
 *
 * Revision 1.13  2001/12/20 15:46:04  neysseri
 * Stabilisation Lot 2 :
 * Silvertrace et exceptions + javadoc
 *
 */

/**
 * Question Business Manager See QuestionBmBusinessSkeleton for methods documentation
 * @author neysseri
 */
public class QuestionBmEJB implements javax.ejb.SessionBean, QuestionBmBusinessSkeleton {

  private AnswerBm currentAnswerBm = null;
  private QuestionResultBm currentQuestionResultBm = null;
  private String dbName = JNDINames.QUESTION_DATASOURCE;

  public QuestionBmEJB() {
  }

  public Question getQuestion(QuestionPK questionPK) throws RemoteException {
    SilverTrace.info("question", "QuestionBmEJB.getQuestion()",
        "root.MSG_GEN_ENTER_METHOD", "questionPK = " + questionPK);
    Connection con = null;

    try {
      con = getConnection();
      Question question = QuestionDAO.getQuestion(con, questionPK);
      // try to fetch the possible answers to this question
      Collection<Answer> answers = this.getAnswersByQuestionPK(questionPK);

      question.setAnswers(answers);
      return question;
    } catch (Exception e) {
      throw new QuestionRuntimeException("QuestionBmEJB.getQuestion()",
          SilverpeasRuntimeException.ERROR, "question.GETTING_QUESTION_FAILED",
          e);
    } finally {
      freeConnection(con);
    }
  }

  private Collection<Answer> getAnswersByQuestionPK(QuestionPK questionPK)
      throws RemoteException {
    SilverTrace.info("question", "QuestionBmEJB.getAnswersByQuestionPK()",
        "root.MSG_GEN_ENTER_METHOD", "questionPK = " + questionPK);
    Collection<Answer> answers = getAnswerBm().getAnswersByQuestionPK(new ForeignPK(questionPK));
    return answers;
  }

  public Collection<Question> getQuestionsByFatherPK(QuestionPK questionPK,
      String fatherId) throws RemoteException {
    SilverTrace.info("question", "QuestionBmEJB.getQuestionsByFatherPK()",
        "root.MSG_GEN_ENTER_METHOD", "questionPK = " + questionPK
        + ", fatherId = " + fatherId);
    Connection con = null;

    try {
      con = getConnection();
      Collection<Question> questions = QuestionDAO.getQuestionsByFatherPK(con,
          questionPK, fatherId);
      // try to fetch the possible answers for each questions
      Iterator<Question> it = questions.iterator();
      List<Question> result = new ArrayList<Question>();

      while (it.hasNext()) {
        Question question = (Question) it.next();
        Collection<Answer> answers = this.getAnswersByQuestionPK(question.getPK());

        question.setAnswers(answers);
        result.add(question);
      }
      return result;
    } catch (Exception e) {
      throw new QuestionRuntimeException(
          "QuestionBmEJB.getQuestionsByFatherPK()",
          SilverpeasRuntimeException.ERROR,
          "question.GETTING_QUESTIONS_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  public QuestionPK createQuestion(Question question) throws RemoteException {
    SilverTrace.info("question", "QuestionBmEJB.createQuestion()",
        "root.MSG_GEN_ENTER_METHOD", "question = " + question);
    Connection con = null;

    try {
      con = getConnection();

      // Transform the 'special' caracters for storing them in Database
      /*
       * question.setLabel(Encode.transformStringForBD(question.getLabel())); question
       * .setDescription(Encode.transformStringForBD(question.getDescription ()));
       * question.setClue(Encode.transformStringForBD(question.getClue()));
       */

      QuestionPK questionPK = QuestionDAO.createQuestion(con, question);

      getAnswerBm().addAnswersToAQuestion(question.getAnswers(),
          new ForeignPK(questionPK));
      return questionPK;
    } catch (Exception e) {
      throw new QuestionRuntimeException("QuestionBmEJB.createQuestion()",
          SilverpeasRuntimeException.ERROR,
          "question.CREATING_QUESTION_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  public void createQuestions(Collection<Question> questions, String fatherId)
      throws RemoteException {
    SilverTrace.info("question", "QuestionBmEJB.createQuestions()",
        "root.MSG_GEN_ENTER_METHOD", "fatherId = " + fatherId);
    Iterator<Question> it = questions.iterator();
    int displayOrder = 1;

    while (it.hasNext()) {
      Question question = it.next();

      question.setFatherId(fatherId);
      question.setDisplayOrder(displayOrder);
      createQuestion(question);
      displayOrder++;
    }
  }

  public void deleteQuestionsByFatherPK(QuestionPK questionPK, String fatherId)
      throws RemoteException {
    SilverTrace.info("question", "QuestionBmEJB.deleteQuestionsByFatherPK()",
        "root.MSG_GEN_ENTER_METHOD", "questionPK = " + questionPK
        + ", fatherId = " + fatherId);
    Connection con = null;
    AnswerBm answerBm = getAnswerBm();
    QuestionResultBm questionResultBm = getQuestionResultBm();

    try {
      con = getConnection();

      // get all questions to delete
      Collection<Question> questions = getQuestionsByFatherPK(questionPK, fatherId);

      Iterator<Question> iterator = questions.iterator();

      while (iterator.hasNext()) {
        QuestionPK questionPKToDelete = ((Question) iterator.next()).getPK();

        // delete all results
        questionResultBm.deleteQuestionResultsToQuestion(new ForeignPK(
            questionPKToDelete));
        // delete all answers
        answerBm.deleteAnswersToAQuestion(new ForeignPK(questionPKToDelete));
      }

      // delete all questions
      QuestionDAO.deleteQuestionsByFatherPK(con, questionPK, fatherId);
    } catch (Exception e) {
      throw new QuestionRuntimeException(
          "QuestionBmEJB.deleteQuestionsByFatherPK()",
          SilverpeasRuntimeException.ERROR,
          "question.DELETING_QUESTIONS_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  public void deleteQuestion(QuestionPK questionPK) throws RemoteException {
    SilverTrace.info("question", "QuestionBmEJB.deleteQuestion()",
        "root.MSG_GEN_ENTER_METHOD", "questionPK = " + questionPK);
    Connection con = null;

    try {
      con = getConnection();
      getQuestionResultBm().deleteQuestionResultsToQuestion(
          new ForeignPK(questionPK));
      // delete all answers
      deleteAnswersToAQuestion(questionPK);
      // delete question
      QuestionDAO.deleteQuestion(con, questionPK);
    } catch (Exception e) {
      throw new QuestionRuntimeException("QuestionBmEJB.deleteQuestion()",
          SilverpeasRuntimeException.ERROR,
          "question.DELETING_QUESTION_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  public void updateQuestion(Question questionDetail) throws RemoteException {
    SilverTrace.info("question", "QuestionBmEJB.updateQuestion()",
        "root.MSG_GEN_ENTER_METHOD", "questionDetail = " + questionDetail);
    // Transform the 'special' caracters for storing them in Database
    /*
     * questionDetail.setLabel(Encode.transformStringForBD(questionDetail.getLabel ()));
     * questionDetail.setDescription(Encode.transformStringForBD(questionDetail .getDescription()));
     * questionDetail.setClue(Encode.transformStringForBD(questionDetail .getClue()));
     */

    updateQuestionHeader(questionDetail);
    updateAnswersToAQuestion(questionDetail);
  }

  public void updateQuestionHeader(Question questionDetail)
      throws RemoteException {
    SilverTrace.info("question", "QuestionBmEJB.updateQuestionHeader()",
        "root.MSG_GEN_ENTER_METHOD", "questionDetail = " + questionDetail);
    Connection con = null;

    try {
      con = getConnection();

      // Transform the 'special' caracters for storing them in Database
      /*
       * questionDetail.setLabel(Encode.transformStringForBD(questionDetail.getLabel ()));
       * questionDetail.setDescription(Encode.transformStringForBD(questionDetail
       * .getDescription())); questionDetail.setClue(Encode.transformStringForBD(
       * questionDetail.getClue()));
       */

      QuestionDAO.updateQuestion(con, questionDetail);
    } catch (Exception e) {
      throw new QuestionRuntimeException("QuestionBmEJB.updateQuestion()",
          SilverpeasRuntimeException.ERROR,
          "question.UPDATING_QUESTION_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  public void updateAnswersToAQuestion(Question questionDetail)
      throws RemoteException {
    SilverTrace.info("question", "QuestionBmEJB.updateAnswersToAQuestion()",
        "root.MSG_GEN_ENTER_METHOD", "questionDetail = " + questionDetail);

    // Transform the 'special' caracters for storing them in Database
    /*
     * questionDetail.setLabel(Encode.transformStringForBD(questionDetail.getLabel ()));
     * questionDetail.setDescription(Encode.transformStringForBD(questionDetail .getDescription()));
     * questionDetail.setClue(Encode.transformStringForBD(questionDetail .getClue()));
     */

    deleteAnswersToAQuestion(questionDetail.getPK());
    createAnswersToAQuestion(questionDetail);
  }

  public void updateAnswerToAQuestion(Answer answerDetail)
      throws RemoteException {
    SilverTrace.info("question", "QuestionBmEJB.updateAnswerToAQuestion()",
        "root.MSG_GEN_ENTER_METHOD", "answerDetail = " + answerDetail);
    getAnswerBm().updateAnswerToAQuestion(answerDetail.getQuestionPK(),
        answerDetail);
  }

  public void deleteAnswersToAQuestion(QuestionPK questionPK)
      throws RemoteException {
    SilverTrace.info("question", "QuestionBmEJB.deleteAnswersToAQuestion()",
        "root.MSG_GEN_ENTER_METHOD", "questionPK = " + questionPK);

    Collection<Answer> answers = getAnswersByQuestionPK(questionPK);
    Iterator<Answer> iterator = answers.iterator();

    while (iterator.hasNext()) {
      AnswerPK answerPKToDelete = (iterator.next()).getPK();
      deleteAnswerToAQuestion(answerPKToDelete, questionPK);
    }
  }

  public void deleteAnswerToAQuestion(AnswerPK answerPK, QuestionPK questionPK)
      throws RemoteException {
    SilverTrace.info("question", "QuestionBmEJB.deleteAnswerToAQuestion()",
        "root.MSG_GEN_ENTER_METHOD", "questionPK = " + questionPK
        + ", answerPK = " + answerPK);
    getAnswerBm().deleteAnswerToAQuestion(new ForeignPK(questionPK),
        answerPK.getId());
  }

  public void createAnswersToAQuestion(Question questionDetail)
      throws RemoteException {
    SilverTrace.info("question", "QuestionBmEJB.createAnswersToAQuestion()",
        "root.MSG_GEN_ENTER_METHOD", "questionDetail = " + questionDetail);

    Collection<Answer> answers = questionDetail.getAnswers();
    Iterator<Answer> iterator = answers.iterator();

    while (iterator.hasNext()) {
      createAnswerToAQuestion(iterator.next(), questionDetail.getPK());
    }
  }

  public AnswerPK createAnswerToAQuestion(Answer answerDetail,
      QuestionPK questionPK) throws RemoteException {
    getAnswerBm().addAnswerToAQuestion(answerDetail, new ForeignPK(questionPK));
    return null;
  }

  /**************************************************************************************************************************/
  /* PRIVATE METHODS */
  /**************************************************************************************************************************/
  private AnswerBm getAnswerBm() {
    if (currentAnswerBm == null) {
      try {
        AnswerBmHome answerBmHome = (AnswerBmHome) EJBUtilitaire
            .getEJBObjectRef(JNDINames.ANSWERBM_EJBHOME, AnswerBmHome.class);

        currentAnswerBm = answerBmHome.create();
      } catch (Exception e) {
        throw new QuestionRuntimeException("QuestionBmEJB.getAnswerBm()",
            SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT",
            "Object = Answer", e);
      }
    }
    return currentAnswerBm;
  }

  private QuestionResultBm getQuestionResultBm() {
    if (currentQuestionResultBm == null) {
      try {
        QuestionResultBmHome questionResultBmHome = (QuestionResultBmHome) EJBUtilitaire
            .getEJBObjectRef(JNDINames.QUESTIONRESULTBM_EJBHOME,
            QuestionResultBmHome.class);

        currentQuestionResultBm = questionResultBmHome.create();
      } catch (Exception e) {
        throw new QuestionRuntimeException("QuestionBmEJB.getAnswerBm()",
            SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT",
            "Object = QuestionResult", e);
      }
    }
    return currentQuestionResultBm;
  }

  private Connection getConnection() {
    try {
      return DBUtil.makeConnection(dbName);
    } catch (Exception e) {
      throw new QuestionRuntimeException("QuestionBmEJB.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  private void freeConnection(Connection con) {
    if (con != null) {
      try {
        con.close();
      } catch (Exception e) {
        SilverTrace.error("answer", "QuestionBmEJB.freeConnection()",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

  /**************************************************************************************************************************/
  /* EJB METHODS */
  /**************************************************************************************************************************/

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
