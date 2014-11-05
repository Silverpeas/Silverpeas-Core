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
package com.stratelia.webactiv.questionResult.control;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.answer.model.AnswerPK;
import com.stratelia.webactiv.questionResult.dao.QuestionResultDAO;
import com.stratelia.webactiv.questionResult.model.QuestionResult;
import com.stratelia.webactiv.questionResult.model.QuestionResultRuntimeException;
import org.silverpeas.util.DBUtil;
import org.silverpeas.util.ForeignPK;
import org.silverpeas.util.exception.SilverpeasRuntimeException;

import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.util.Collection;

/**
 * QuestionResultService Stateless service to manage access to question results.
 * @author neysseri
 * @see com.stratelia.webactiv.questionResult.control.QuestionResultService
 */
@Singleton
@Transactional(Transactional.TxType.SUPPORTS)
public class DefaultQuestionResultService implements QuestionResultService {

  public DefaultQuestionResultService() {
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void setQuestionResultToUser(QuestionResult result) {
    SilverTrace.info("questionResult", "DefaultQuestionResultService.setQuestionResultToUser()",
        "root.MSG_GEN_ENTER_METHOD", "questionResult =" + result);
    Connection con = getConnection();
    try {
      QuestionResultDAO.setQuestionResultToUser(con, result);
    } catch (Exception e) {
      throw new QuestionResultRuntimeException(
          "DefaultQuestionResultService.getAnswersByQuestionPK()", SilverpeasRuntimeException.ERROR,
          "questionResult.RECORDING_RESPONSE_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<QuestionResult> getQuestionResultToQuestion(ForeignPK questionPK) {
    SilverTrace.info("questionResult", "DefaultQuestionResultService.getQuestionResultToQuestion()",
        "root.MSG_GEN_ENTER_METHOD", "questionPK =" + questionPK);
    Connection con = getConnection();
    try {
      return QuestionResultDAO.getQuestionResultToQuestion(con, questionPK);
    } catch (Exception e) {
      throw new QuestionResultRuntimeException(
          "DefaultQuestionResultService.getQuestionResultToQuestion()",
          SilverpeasRuntimeException.ERROR, "questionResult.GETTING_RESPONSES_TO_QUESTION_FAILED",
          e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<QuestionResult> getUserQuestionResultsToQuestion(String userId,
      ForeignPK questionPK) {
    SilverTrace
        .info("questionResult", "DefaultQuestionResultService.getUserQuestionResultsToQuestion()",
            "root.MSG_GEN_ENTER_METHOD", "userId = " + userId + ", questionPK =" + questionPK);
    Connection con = getConnection();
    try {
      return QuestionResultDAO.getUserQuestionResultsToQuestion(con, userId, questionPK);
    } catch (Exception e) {
      throw new QuestionResultRuntimeException(
          "DefaultQuestionResultService.getUserQuestionResultsToQuestion()",
          SilverpeasRuntimeException.ERROR,
          "questionResult.GETTING_USER_RESPONSES_TO_QUESTION_FAILED", e);
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
      throw new QuestionResultRuntimeException("DefaultQuestionResultService.getUsersByAnswer()",
          SilverpeasRuntimeException.ERROR,
          "questionResult.GETTING_USER_RESPONSES_TO_QUESTION_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void deleteQuestionResultsToQuestion(ForeignPK questionPK) {
    SilverTrace
        .info("questionResult", "DefaultQuestionResultService.deleteQuestionResultsToQuestion()",
            "root.MSG_GEN_ENTER_METHOD", "questionPK =" + questionPK);
    Connection con = getConnection();
    try {
      QuestionResultDAO.deleteQuestionResultToQuestion(con, questionPK);
    } catch (Exception e) {
      throw new QuestionResultRuntimeException(
          "DefaultQuestionResultService.deleteQuestionResultsToQuestion()",
          SilverpeasRuntimeException.ERROR, "questionResult.DELETING_RESPONSES_TO_QUESTION_FAILED",
          e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<QuestionResult> getQuestionResultToQuestionByParticipation(ForeignPK questionPK,
      int participationId) {
    SilverTrace.info("questionResult",
        "DefaultQuestionResultService.getQuestionResultToQuestionByParticipation()",
        "root.MSG_GEN_ENTER_METHOD",
        "questionPK =" + questionPK + ", participationId = " + participationId);
    Connection con = getConnection();
    try {
      return QuestionResultDAO
          .getQuestionResultToQuestionByParticipation(con, questionPK, participationId);
    } catch (Exception e) {
      throw new QuestionResultRuntimeException(
          "DefaultQuestionResultService.getQuestionResultToQuestionByParticipation()",
          SilverpeasRuntimeException.ERROR,
          "questionResult.GETTING_RESPONSES_TO_QUESTION_AND_PARTICIPATION_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<QuestionResult> getUserQuestionResultsToQuestionByParticipation(String userId,
      ForeignPK questionPK, int participationId) {
    SilverTrace.info("questionResult",
        "DefaultQuestionResultService.getUserQuestionResultsToQuestionByParticipation()",
        "root.MSG_GEN_ENTER_METHOD",
        "userId = " + userId + ", questionPK =" + questionPK + ", participationId = " +
            participationId);
    Connection con = getConnection();
    try {
      return QuestionResultDAO
          .getUserQuestionResultsToQuestionByParticipation(con, userId, questionPK,
              participationId);
    } catch (Exception e) {
      throw new QuestionResultRuntimeException(
          "DefaultQuestionResultService.getUserQuestionResultsToQuestionByParticipation()",
          SilverpeasRuntimeException.ERROR,
          "questionResult.GETTING_USER_RESPONSES_TO_QUESTION_AND_PARTICIPATION_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void setQuestionResultsToUser(Collection<QuestionResult> results) {
    SilverTrace.info("questionResult", "DefaultQuestionResultService.setQuestionResultsToUser()",
        "root.MSG_GEN_ENTER_METHOD", "");
    if (results != null) {
      for (QuestionResult questionResult : results) {
        setQuestionResultToUser(questionResult);
      }
    }
  }

  @Override
  public QuestionResult getUserAnswerToQuestion(String userId, ForeignPK questionPK,
      AnswerPK answerPK) {
    SilverTrace.info("questionResult", "DefaultQuestionResultService.getUserAnswerToQuestion()",
        "root.MSG_GEN_ENTER_METHOD",
        "userId = " + userId + ", questionPK =" + questionPK + ", answerPK =" + answerPK);
    Connection con = getConnection();
    try {
      return QuestionResultDAO.getUserAnswerToQuestion(con, userId, questionPK, answerPK);
    } catch (Exception e) {
      throw new QuestionResultRuntimeException(
          "DefaultQuestionResultService.getUserAnswerToQuestion()",
          SilverpeasRuntimeException.ERROR,
          "questionResult.GETTING_USER_RESPONSES_TO_QUESTION_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  private Connection getConnection() {
    try {
      return DBUtil.openConnection();
    } catch (Exception e) {
      throw new QuestionResultRuntimeException("DefaultQuestionResultService.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }
}