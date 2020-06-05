/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.questioncontainer.question.service;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.questioncontainer.answer.model.Answer;
import org.silverpeas.core.questioncontainer.answer.model.AnswerPK;
import org.silverpeas.core.questioncontainer.answer.service.AnswerService;
import org.silverpeas.core.questioncontainer.question.dao.QuestionDAO;
import org.silverpeas.core.questioncontainer.question.model.Question;
import org.silverpeas.core.questioncontainer.question.model.QuestionPK;
import org.silverpeas.core.questioncontainer.question.model.QuestionRuntimeException;
import org.silverpeas.core.questioncontainer.result.service.QuestionResultService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.silverpeas.core.SilverpeasExceptionMessages.*;

/**
 * Question Business Manager See QuestionBmBusinessSkeleton for methods documentation
 * Stateless service to manage questions
 * @author neysseri
 */
@Singleton
@Transactional(Transactional.TxType.REQUIRED)
public class DefaultQuestionService implements QuestionService {

  private static final String QUESTION_EX_MSG = "question";

  @Inject
  private AnswerService currentAnswerService;
  @Inject
  private QuestionResultService currentQuestionResultService;

  /**
   * Hidden constructor.
   */
  protected DefaultQuestionService() {
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
      throw new QuestionRuntimeException(failureOnGetting(QUESTION_EX_MSG, questionPK.toString()), e);
    } finally {
      DBUtil.close(con);
    }
  }

  private Collection<Answer> getAnswersByQuestionPK(QuestionPK questionPK) {
    return currentAnswerService.getAnswersByQuestionPK(new ResourceReference(questionPK));
  }

  @Override
  @Transactional(Transactional.TxType.SUPPORTS)
  public Collection<Question> getQuestionsByFatherPK(QuestionPK questionPK, String fatherId) {
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
      throw new QuestionRuntimeException(failureOnGetting(QUESTION_EX_MSG, questionPK), e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public QuestionPK createQuestion(Question question) {
    Connection con = getConnection();
    try {
      QuestionPK questionPK = QuestionDAO.createQuestion(con, question);
      currentAnswerService.addAnswersToAQuestion(question.getAnswers(), new ResourceReference(questionPK));
      return questionPK;
    } catch (Exception e) {
      throw new QuestionRuntimeException(
          failureOnAdding("question on father", question.getFatherId()), e);
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
    Connection con = getConnection();
    AnswerService answerService = currentAnswerService;
    QuestionResultService questionResultService = currentQuestionResultService;
    try {
      // get all questions to delete
      Collection<Question> questions = getQuestionsByFatherPK(questionPK, fatherId);
      for (final Question question : questions) {
        QuestionPK questionPKToDelete = question.getPK();
        // delete all results
        questionResultService.deleteQuestionResultsToQuestion(new ResourceReference(questionPKToDelete));
        // delete all answers
        answerService.deleteAnswersToAQuestion(new ResourceReference(questionPKToDelete));
      }
      // delete all questions
      QuestionDAO.deleteQuestionsByFatherPK(con, questionPK, fatherId);
    } catch (Exception e) {
      throw new QuestionRuntimeException(failureOnDeleting(QUESTION_EX_MSG, questionPK), e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void deleteQuestion(QuestionPK questionPK) {

    Connection con = getConnection();
    try {
      currentQuestionResultService.deleteQuestionResultsToQuestion(new ResourceReference(questionPK));
      // delete all answers
      deleteAnswersToAQuestion(questionPK);
      // delete question
      QuestionDAO.deleteQuestion(con, questionPK);
    } catch (Exception e) {
      throw new QuestionRuntimeException(failureOnDeleting(QUESTION_EX_MSG, questionPK), e);
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
    Connection con = getConnection();
    try {
      QuestionDAO.updateQuestion(con, questionDetail);
    } catch (Exception e) {
      throw new QuestionRuntimeException(failureOnUpdate(QUESTION_EX_MSG, questionDetail.getPK()), e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void updateAnswersToAQuestion(Question questionDetail) {
    deleteAnswersToAQuestion(questionDetail.getPK());
    createAnswersToAQuestion(questionDetail);
  }

  @Override
  public void updateAnswerToAQuestion(Answer answerDetail) {
    currentAnswerService.updateAnswerToAQuestion(answerDetail.getQuestionPK(), answerDetail);
  }

  @Override
  public void deleteAnswersToAQuestion(QuestionPK questionPK) {
    Collection<Answer> answers = getAnswersByQuestionPK(questionPK);
    for (final Answer answer : answers) {
      AnswerPK answerPKToDelete = answer.getPK();
      deleteAnswerToAQuestion(answerPKToDelete, questionPK);
    }
  }

  @Override
  public void deleteAnswerToAQuestion(AnswerPK answerPK, QuestionPK questionPK) {
    currentAnswerService.deleteAnswerToAQuestion(new ResourceReference(questionPK), answerPK.getId());
  }

  @Override
  public void createAnswersToAQuestion(Question questionDetail) {
    Collection<Answer> answers = questionDetail.getAnswers();
    for (Answer answer : answers) {
      createAnswerToAQuestion(answer, questionDetail.getPK());
    }
  }

  @Override
  public AnswerPK createAnswerToAQuestion(Answer answerDetail, QuestionPK questionPK) {
    currentAnswerService.addAnswerToAQuestion(answerDetail, new ResourceReference(questionPK));
    return null;
  }

  private Connection getConnection() {
    try {
      return DBUtil.openConnection();
    } catch (Exception e) {
      throw new QuestionRuntimeException(e);
    }
  }
}
