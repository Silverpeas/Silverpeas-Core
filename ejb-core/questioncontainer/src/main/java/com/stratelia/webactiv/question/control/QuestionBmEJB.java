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
package com.stratelia.webactiv.question.control;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.silverpeas.util.ForeignPK;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.util.DBUtil;
import org.silverpeas.util.JNDINames;
import com.stratelia.webactiv.answer.control.AnswerBm;
import com.stratelia.webactiv.answer.model.Answer;
import com.stratelia.webactiv.answer.model.AnswerPK;
import org.silverpeas.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.question.ejb.QuestionDAO;
import com.stratelia.webactiv.question.model.Question;
import com.stratelia.webactiv.question.model.QuestionPK;
import com.stratelia.webactiv.question.model.QuestionRuntimeException;
import com.stratelia.webactiv.questionResult.control.QuestionResultBm;

/**
 * Question Business Manager See QuestionBmBusinessSkeleton for methods documentation
 *
 * @author neysseri
 */
@Stateless(name = "Question", description = "Stateless EJB to manage questions.")
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class QuestionBmEJB implements QuestionBm {

  private static final long serialVersionUID = -6408050998586411706L;
  @EJB
  private AnswerBm currentAnswerBm;
  @EJB
  private QuestionResultBm currentQuestionResultBm;
  private String dbName = JNDINames.QUESTION_DATASOURCE;

  public QuestionBmEJB() {
  }

  @Override
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public Question getQuestion(QuestionPK questionPK) {
    SilverTrace.info("question", "QuestionBmEJB.getQuestion()",
        "root.MSG_GEN_ENTER_METHOD", "questionPK = " + questionPK);
    Connection con = getConnection();
    try {
      Question question = QuestionDAO.getQuestion(con, questionPK);
      // try to fetch the possible answers to this question
      Collection<Answer> answers = this.getAnswersByQuestionPK(questionPK);
      question.setAnswers(answers);
      return question;
    } catch (Exception e) {
      throw new QuestionRuntimeException("QuestionBmEJB.getQuestion()",
          SilverpeasRuntimeException.ERROR, "question.GETTING_QUESTION_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  private Collection<Answer> getAnswersByQuestionPK(QuestionPK questionPK) {
    SilverTrace.info("question", "QuestionBmEJB.getAnswersByQuestionPK()",
        "root.MSG_GEN_ENTER_METHOD", "questionPK = " + questionPK);
    Collection<Answer> answers = currentAnswerBm.getAnswersByQuestionPK(new ForeignPK(questionPK));
    return answers;
  }

  @Override
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public Collection<Question> getQuestionsByFatherPK(QuestionPK questionPK,
      String fatherId) {
    SilverTrace.info("question", "QuestionBmEJB.getQuestionsByFatherPK()",
        "root.MSG_GEN_ENTER_METHOD", "questionPK = " + questionPK + ", fatherId = " + fatherId);
    Connection con = getConnection();
    try {
      Collection<Question> questions = QuestionDAO.getQuestionsByFatherPK(con, questionPK, fatherId);
      // try to fetch the possible answers for each questions
      List<Question> result = new ArrayList<Question>();
      for (Question question : questions) {
        Collection<Answer> answers = getAnswersByQuestionPK(question.getPK());
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
      DBUtil.close(con);
    }
  }

  @Override
  public QuestionPK createQuestion(Question question) {
    SilverTrace.info("question", "QuestionBmEJB.createQuestion()",
        "root.MSG_GEN_ENTER_METHOD", "question = " + question);
    Connection con = getConnection();
    try {
      QuestionPK questionPK = QuestionDAO.createQuestion(con, question);
      currentAnswerBm.addAnswersToAQuestion(question.getAnswers(), new ForeignPK(questionPK));
      return questionPK;
    } catch (Exception e) {
      throw new QuestionRuntimeException("QuestionBmEJB.createQuestion()",
          SilverpeasRuntimeException.ERROR, "question.CREATING_QUESTION_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void createQuestions(Collection<Question> questions, String fatherId) {
    SilverTrace.info("question", "QuestionBmEJB.createQuestions()",
        "root.MSG_GEN_ENTER_METHOD", "fatherId = " + fatherId);
    int displayOrder = 1;
    for (Question question : questions) {
      question.setFatherId(fatherId);
      question.setDisplayOrder(displayOrder);
      createQuestion(question);
      displayOrder++;
    }
  }

  @Override
  public void deleteQuestionsByFatherPK(QuestionPK questionPK, String fatherId) {
    SilverTrace.info("question", "QuestionBmEJB.deleteQuestionsByFatherPK()",
        "root.MSG_GEN_ENTER_METHOD", "questionPK = " + questionPK + ", fatherId = " + fatherId);
    Connection con = getConnection();
    AnswerBm answerBm = currentAnswerBm;
    QuestionResultBm questionResultBm = currentQuestionResultBm;
    try {
      // get all questions to delete
      Collection<Question> questions = getQuestionsByFatherPK(questionPK, fatherId);
      Iterator<Question> iterator = questions.iterator();
      while (iterator.hasNext()) {
        QuestionPK questionPKToDelete = iterator.next().getPK();
        // delete all results
        questionResultBm.deleteQuestionResultsToQuestion(new ForeignPK(questionPKToDelete));
        // delete all answers
        answerBm.deleteAnswersToAQuestion(new ForeignPK(questionPKToDelete));
      }
      // delete all questions
      QuestionDAO.deleteQuestionsByFatherPK(con, questionPK, fatherId);
    } catch (Exception e) {
      throw new QuestionRuntimeException("QuestionBmEJB.deleteQuestionsByFatherPK()",
          SilverpeasRuntimeException.ERROR, "question.DELETING_QUESTIONS_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void deleteQuestion(QuestionPK questionPK) {
    SilverTrace.info("question", "QuestionBmEJB.deleteQuestion()",
        "root.MSG_GEN_ENTER_METHOD", "questionPK = " + questionPK);
    Connection con = getConnection();
    try {
      currentQuestionResultBm.deleteQuestionResultsToQuestion(new ForeignPK(questionPK));
      // delete all answers
      deleteAnswersToAQuestion(questionPK);
      // delete question
      QuestionDAO.deleteQuestion(con, questionPK);
    } catch (Exception e) {
      throw new QuestionRuntimeException("QuestionBmEJB.deleteQuestion()",
          SilverpeasRuntimeException.ERROR, "question.DELETING_QUESTION_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void updateQuestion(Question questionDetail) {
    SilverTrace.info("question", "QuestionBmEJB.updateQuestion()",
        "root.MSG_GEN_ENTER_METHOD", "questionDetail = " + questionDetail);
    updateQuestionHeader(questionDetail);
    updateAnswersToAQuestion(questionDetail);
  }

  @Override
  public void updateQuestionHeader(Question questionDetail) {
    SilverTrace.info("question", "QuestionBmEJB.updateQuestionHeader()",
        "root.MSG_GEN_ENTER_METHOD", "questionDetail = " + questionDetail);
    Connection con = getConnection();
    try {
      QuestionDAO.updateQuestion(con, questionDetail);
    } catch (Exception e) {
      throw new QuestionRuntimeException("QuestionBmEJB.updateQuestion()",
          SilverpeasRuntimeException.ERROR, "question.UPDATING_QUESTION_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void updateAnswersToAQuestion(Question questionDetail) {
    SilverTrace.info("question", "QuestionBmEJB.updateAnswersToAQuestion()",
        "root.MSG_GEN_ENTER_METHOD", "questionDetail = " + questionDetail);
    deleteAnswersToAQuestion(questionDetail.getPK());
    createAnswersToAQuestion(questionDetail);
  }

  @Override
  public void updateAnswerToAQuestion(Answer answerDetail) {
    SilverTrace.info("question", "QuestionBmEJB.updateAnswerToAQuestion()",
        "root.MSG_GEN_ENTER_METHOD", "answerDetail = " + answerDetail);
    currentAnswerBm.updateAnswerToAQuestion(answerDetail.getQuestionPK(),
        answerDetail);
  }

  @Override
  public void deleteAnswersToAQuestion(QuestionPK questionPK) {
    SilverTrace.info("question", "QuestionBmEJB.deleteAnswersToAQuestion()",
        "root.MSG_GEN_ENTER_METHOD", "questionPK = " + questionPK);
    Collection<Answer> answers = getAnswersByQuestionPK(questionPK);
    Iterator<Answer> iterator = answers.iterator();
    while (iterator.hasNext()) {
      AnswerPK answerPKToDelete = (iterator.next()).getPK();
      deleteAnswerToAQuestion(answerPKToDelete, questionPK);
    }
  }

  @Override
  public void deleteAnswerToAQuestion(AnswerPK answerPK, QuestionPK questionPK) {
    SilverTrace.info("question", "QuestionBmEJB.deleteAnswerToAQuestion()",
        "root.MSG_GEN_ENTER_METHOD", "questionPK = " + questionPK + ", answerPK = " + answerPK);
    currentAnswerBm.deleteAnswerToAQuestion(new ForeignPK(questionPK), answerPK.getId());
  }

  @Override
  public void createAnswersToAQuestion(Question questionDetail) {
    SilverTrace.info("question", "QuestionBmEJB.createAnswersToAQuestion()",
        "root.MSG_GEN_ENTER_METHOD", "questionDetail = " + questionDetail);
    Collection<Answer> answers = questionDetail.getAnswers();
    for (Answer answer : answers) {
      createAnswerToAQuestion(answer, questionDetail.getPK());
    }
  }

  @Override
  public AnswerPK createAnswerToAQuestion(Answer answerDetail, QuestionPK questionPK) {
    currentAnswerBm.addAnswerToAQuestion(answerDetail, new ForeignPK(questionPK));
    return null;
  }

  private Connection getConnection() {
    try {
      return DBUtil.openConnection();
    } catch (Exception e) {
      throw new QuestionRuntimeException("QuestionBmEJB.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }
}
