/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.questioncontainer.question.service;

import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.questioncontainer.answer.service.AnswerService;
import org.silverpeas.core.questioncontainer.answer.model.Answer;
import org.silverpeas.core.questioncontainer.answer.model.AnswerPK;
import org.silverpeas.core.questioncontainer.question.dao.QuestionDAO;
import org.silverpeas.core.questioncontainer.question.model.Question;
import org.silverpeas.core.questioncontainer.question.model.QuestionPK;
import org.silverpeas.core.questioncontainer.question.model.QuestionRuntimeException;
import org.silverpeas.core.questioncontainer.result.service.QuestionResultService;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.exception.SilverpeasRuntimeException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Question Business Manager See QuestionBmBusinessSkeleton for methods documentation
 * Stateless service to manage questions
 * @author neysseri
 */
@Singleton
@Transactional(Transactional.TxType.REQUIRED)
public class DefaultQuestionService implements QuestionService {

  @Inject
  private AnswerService currentAnswerService;
  @Inject
  private QuestionResultService currentQuestionResultService;

  public DefaultQuestionService() {
  }

  @Override
  @Transactional(Transactional.TxType.SUPPORTS)
  public Question getQuestion(QuestionPK questionPK) {

    Connection con = getConnection();
    try {
      Question question = QuestionDAO.getQuestion(con, questionPK);
      // try to fetch the possible answers to this question
      Collection<Answer> answers = this.getAnswersByQuestionPK(questionPK);
      question.setAnswers(answers);
      return question;
    } catch (Exception e) {
      throw new QuestionRuntimeException("DefaultQuestionService.getQuestion()",
          SilverpeasRuntimeException.ERROR, "question.GETTING_QUESTION_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  private Collection<Answer> getAnswersByQuestionPK(QuestionPK questionPK) {
    SilverTrace
        .info("question", "DefaultQuestionService.getAnswersByQuestionPK()", "root.MSG_GEN_ENTER_METHOD",
            "questionPK = " + questionPK);
    Collection<Answer> answers = currentAnswerService.getAnswersByQuestionPK(new ForeignPK(questionPK));
    return answers;
  }

  @Override
  @Transactional(Transactional.TxType.SUPPORTS)
  public Collection<Question> getQuestionsByFatherPK(QuestionPK questionPK, String fatherId) {
    SilverTrace
        .info("question", "DefaultQuestionService.getQuestionsByFatherPK()", "root.MSG_GEN_ENTER_METHOD",
            "questionPK = " + questionPK + ", fatherId = " + fatherId);
    Connection con = getConnection();
    try {
      Collection<Question> questions =
          QuestionDAO.getQuestionsByFatherPK(con, questionPK, fatherId);
      // try to fetch the possible answers for each questions
      List<Question> result = new ArrayList<>();
      for (Question question : questions) {
        Collection<Answer> answers = getAnswersByQuestionPK(question.getPK());
        question.setAnswers(answers);
        result.add(question);
      }
      return result;
    } catch (Exception e) {
      throw new QuestionRuntimeException("DefaultQuestionService.getQuestionsByFatherPK()",
          SilverpeasRuntimeException.ERROR, "question.GETTING_QUESTIONS_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public QuestionPK createQuestion(Question question) {

    Connection con = getConnection();
    try {
      QuestionPK questionPK = QuestionDAO.createQuestion(con, question);
      currentAnswerService.addAnswersToAQuestion(question.getAnswers(), new ForeignPK(questionPK));
      return questionPK;
    } catch (Exception e) {
      throw new QuestionRuntimeException("DefaultQuestionService.createQuestion()",
          SilverpeasRuntimeException.ERROR, "question.CREATING_QUESTION_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void createQuestions(Collection<Question> questions, String fatherId) {

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
    SilverTrace
        .info("question", "DefaultQuestionService.deleteQuestionsByFatherPK()", "root.MSG_GEN_ENTER_METHOD",
            "questionPK = " + questionPK + ", fatherId = " + fatherId);
    Connection con = getConnection();
    AnswerService answerService = currentAnswerService;
    QuestionResultService questionResultService = currentQuestionResultService;
    try {
      // get all questions to delete
      Collection<Question> questions = getQuestionsByFatherPK(questionPK, fatherId);
      for (final Question question : questions) {
        QuestionPK questionPKToDelete = question.getPK();
        // delete all results
        questionResultService.deleteQuestionResultsToQuestion(new ForeignPK(questionPKToDelete));
        // delete all answers
        answerService.deleteAnswersToAQuestion(new ForeignPK(questionPKToDelete));
      }
      // delete all questions
      QuestionDAO.deleteQuestionsByFatherPK(con, questionPK, fatherId);
    } catch (Exception e) {
      throw new QuestionRuntimeException("DefaultQuestionService.deleteQuestionsByFatherPK()",
          SilverpeasRuntimeException.ERROR, "question.DELETING_QUESTIONS_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void deleteQuestion(QuestionPK questionPK) {

    Connection con = getConnection();
    try {
      currentQuestionResultService.deleteQuestionResultsToQuestion(new ForeignPK(questionPK));
      // delete all answers
      deleteAnswersToAQuestion(questionPK);
      // delete question
      QuestionDAO.deleteQuestion(con, questionPK);
    } catch (Exception e) {
      throw new QuestionRuntimeException("DefaultQuestionService.deleteQuestion()",
          SilverpeasRuntimeException.ERROR, "question.DELETING_QUESTION_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void updateQuestion(Question questionDetail) {

    updateQuestionHeader(questionDetail);
    updateAnswersToAQuestion(questionDetail);
  }

  @Override
  public void updateQuestionHeader(Question questionDetail) {
    SilverTrace
        .info("question", "DefaultQuestionService.updateQuestionHeader()", "root.MSG_GEN_ENTER_METHOD",
            "questionDetail = " + questionDetail);
    Connection con = getConnection();
    try {
      QuestionDAO.updateQuestion(con, questionDetail);
    } catch (Exception e) {
      throw new QuestionRuntimeException("DefaultQuestionService.updateQuestion()",
          SilverpeasRuntimeException.ERROR, "question.UPDATING_QUESTION_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void updateAnswersToAQuestion(Question questionDetail) {
    SilverTrace
        .info("question", "DefaultQuestionService.updateAnswersToAQuestion()", "root.MSG_GEN_ENTER_METHOD",
            "questionDetail = " + questionDetail);
    deleteAnswersToAQuestion(questionDetail.getPK());
    createAnswersToAQuestion(questionDetail);
  }

  @Override
  public void updateAnswerToAQuestion(Answer answerDetail) {
    SilverTrace
        .info("question", "DefaultQuestionService.updateAnswerToAQuestion()", "root.MSG_GEN_ENTER_METHOD",
            "answerDetail = " + answerDetail);
    currentAnswerService.updateAnswerToAQuestion(answerDetail.getQuestionPK(), answerDetail);
  }

  @Override
  public void deleteAnswersToAQuestion(QuestionPK questionPK) {
    SilverTrace
        .info("question", "DefaultQuestionService.deleteAnswersToAQuestion()", "root.MSG_GEN_ENTER_METHOD",
            "questionPK = " + questionPK);
    Collection<Answer> answers = getAnswersByQuestionPK(questionPK);
    for (final Answer answer : answers) {
      AnswerPK answerPKToDelete = answer.getPK();
      deleteAnswerToAQuestion(answerPKToDelete, questionPK);
    }
  }

  @Override
  public void deleteAnswerToAQuestion(AnswerPK answerPK, QuestionPK questionPK) {
    SilverTrace
        .info("question", "DefaultQuestionService.deleteAnswerToAQuestion()", "root.MSG_GEN_ENTER_METHOD",
            "questionPK = " + questionPK + ", answerPK = " + answerPK);
    currentAnswerService.deleteAnswerToAQuestion(new ForeignPK(questionPK), answerPK.getId());
  }

  @Override
  public void createAnswersToAQuestion(Question questionDetail) {
    SilverTrace
        .info("question", "DefaultQuestionService.createAnswersToAQuestion()", "root.MSG_GEN_ENTER_METHOD",
            "questionDetail = " + questionDetail);
    Collection<Answer> answers = questionDetail.getAnswers();
    for (Answer answer : answers) {
      createAnswerToAQuestion(answer, questionDetail.getPK());
    }
  }

  @Override
  public AnswerPK createAnswerToAQuestion(Answer answerDetail, QuestionPK questionPK) {
    currentAnswerService.addAnswerToAQuestion(answerDetail, new ForeignPK(questionPK));
    return null;
  }

  private Connection getConnection() {
    try {
      return DBUtil.openConnection();
    } catch (Exception e) {
      throw new QuestionRuntimeException("DefaultQuestionService.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }
}
