/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.questioncontainer.result.service;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.questioncontainer.answer.model.AnswerPK;
import org.silverpeas.core.questioncontainer.result.dao.QuestionResultDAO;
import org.silverpeas.core.questioncontainer.result.model.QuestionResult;
import org.silverpeas.core.questioncontainer.result.model.QuestionResultRuntimeException;

import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.util.Collection;

/**
 * QuestionResultService Stateless service to manage access to question results.
 * @author neysseri
 * @see QuestionResultService
 */
@Singleton
@Transactional(Transactional.TxType.SUPPORTS)
public class DefaultQuestionResultService implements QuestionResultService {

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void setQuestionResultToUser(QuestionResult result) {

    Connection con = getConnection();
    try {
      QuestionResultDAO.setQuestionResultToUser(con, result);
    } catch (Exception e) {
      throw new QuestionResultRuntimeException(e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<QuestionResult> getQuestionResultToQuestion(ResourceReference questionPK) {

    Connection con = getConnection();
    try {
      return QuestionResultDAO.getQuestionResultToQuestion(con, questionPK);
    } catch (Exception e) {
      throw new QuestionResultRuntimeException(e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<QuestionResult> getUserQuestionResultsToQuestion(String userId,
      ResourceReference questionPK) {
    Connection con = getConnection();
    try {
      return QuestionResultDAO.getUserQuestionResultsToQuestion(con, userId, questionPK);
    } catch (Exception e) {
      throw new QuestionResultRuntimeException(e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<String> getUsersByAnswer(String answerId) {
    Connection con = getConnection();
    try {
      return QuestionResultDAO.getUsersByAnswer(con, answerId);
    } catch (Exception e) {
      throw new QuestionResultRuntimeException(e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void deleteQuestionResultsToQuestion(ResourceReference questionPK) {
    Connection con = getConnection();
    try {
      QuestionResultDAO.deleteQuestionResultToQuestion(con, questionPK);
    } catch (Exception e) {
      throw new QuestionResultRuntimeException(e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<QuestionResult> getQuestionResultToQuestionByParticipation(ResourceReference questionPK,
      int participationId) {
    Connection con = getConnection();
    try {
      return QuestionResultDAO
          .getQuestionResultToQuestionByParticipation(con, questionPK, participationId);
    } catch (Exception e) {
      throw new QuestionResultRuntimeException(e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<QuestionResult> getUserQuestionResultsToQuestionByParticipation(String userId,
      ResourceReference questionPK, int participationId) {
    Connection con = getConnection();
    try {
      return QuestionResultDAO
          .getUserQuestionResultsToQuestionByParticipation(con, userId, questionPK,
              participationId);
    } catch (Exception e) {
      throw new QuestionResultRuntimeException(e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void setQuestionResultsToUser(Collection<QuestionResult> results) {

    if (results != null) {
      for (QuestionResult questionResult : results) {
        setQuestionResultToUser(questionResult);
      }
    }
  }

  @Override
  public QuestionResult getUserAnswerToQuestion(String userId, ResourceReference questionPK,
      AnswerPK answerPK) {
    Connection con = getConnection();
    try {
      return QuestionResultDAO.getUserAnswerToQuestion(con, userId, questionPK, answerPK);
    } catch (Exception e) {
      throw new QuestionResultRuntimeException(e);
    } finally {
      DBUtil.close(con);
    }
  }

  private Connection getConnection() {
    try {
      return DBUtil.openConnection();
    } catch (Exception e) {
      throw new QuestionResultRuntimeException(e);
    }
  }
}