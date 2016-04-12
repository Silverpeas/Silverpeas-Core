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
package org.silverpeas.core.questioncontainer.answer.service;

import org.silverpeas.core.questioncontainer.answer.dao.AnswerDAO;
import org.silverpeas.core.questioncontainer.answer.model.Answer;
import org.silverpeas.core.questioncontainer.answer.model.AnswerPK;
import org.silverpeas.core.questioncontainer.answer.model.AnswerRuntimeException;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.exception.SilverpeasRuntimeException;

import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.util.Collection;

/**
 * Answer Business Manager See AnswerService for methods documentation
 * Stateless service to manage answer
 * @author neysseri
 */
@Singleton
@Transactional(Transactional.TxType.REQUIRED)
public class DefaultAnswerService implements AnswerService {

  public DefaultAnswerService() {
  }

  @Override
  @Transactional(Transactional.TxType.SUPPORTS)
  public Collection<Answer> getAnswersByQuestionPK(ForeignPK questionPK) {

    Connection con = getConnection();
    try {
      Collection<Answer> answers = AnswerDAO.getAnswersByQuestionPK(con, questionPK);
      return answers;
    } catch (Exception e) {
      throw new AnswerRuntimeException("DefaultAnswerService.getAnswersByQuestionPK()",
          SilverpeasRuntimeException.ERROR, "answer.ANSWER_LIST_TO_QUESTION_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void recordThisAnswerAsVote(ForeignPK questionPK, AnswerPK answerPK) {

    Connection con = getConnection();
    try {
      AnswerDAO.recordThisAnswerAsVote(con, questionPK, answerPK);
    } catch (Exception e) {
      throw new AnswerRuntimeException("DefaultAnswerService.recordThisAnswerAsVote()",
          SilverpeasRuntimeException.ERROR, "answer.RECORDING_RESPONSE_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void addAnswersToAQuestion(Collection<Answer> answers, ForeignPK questionPK) {
    SilverTrace
        .info("answer", "DefaultAnswerService.addAnswersToAQuestion()", "root.MSG_GEN_ENTER_METHOD",
            "questionPK=" + questionPK);
    Connection con = getConnection();
    try {
      AnswerDAO.addAnswersToAQuestion(con, answers, questionPK);
    } catch (Exception e) {
      throw new AnswerRuntimeException("DefaultAnswerService.addAnswersToAQuestion()",
          SilverpeasRuntimeException.ERROR, "answer.ADDING_ANSWERS_TO_QUESTION_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void addAnswerToAQuestion(Answer answer, ForeignPK questionPK) {
    SilverTrace
        .info("answer", "DefaultAnswerService.addAnswerToAQuestion()", "root.MSG_GEN_ENTER_METHOD",
            "questionPK=" + questionPK + " and answer = " + answer);
    Connection con = getConnection();
    try {
      AnswerDAO.addAnswerToAQuestion(con, answer, questionPK);
    } catch (Exception e) {
      throw new AnswerRuntimeException("DefaultAnswerService.addAnswerToAQuestion()",
          SilverpeasRuntimeException.ERROR, "answer.ADDING_ANSWER_TO_QUESTION_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void deleteAnswersToAQuestion(ForeignPK questionPK) {

    Connection con = getConnection();
    try {
      AnswerDAO.deleteAnswersToAQuestion(con, questionPK);
    } catch (Exception e) {
      throw new AnswerRuntimeException("DefaultAnswerService.deleteAnswersToAQuestion()",
          SilverpeasRuntimeException.ERROR, "answer.DELETING_ANSWERS_TO_QUESTION_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void deleteAnswerToAQuestion(ForeignPK questionPK, String answerId) {

    Connection con = getConnection();
    try {
      AnswerDAO.deleteAnswerToAQuestion(con, questionPK, answerId);
    } catch (Exception e) {
      throw new AnswerRuntimeException("DefaultAnswerService.deleteAnswerToAQuestion()",
          SilverpeasRuntimeException.ERROR, "answer.DELETING_ANSWER_TO_QUESTION_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void updateAnswerToAQuestion(ForeignPK questionPK, Answer answer) {

    Connection con = getConnection();
    try {
      AnswerDAO.updateAnswerToAQuestion(con, questionPK, answer);
    } catch (Exception e) {
      throw new AnswerRuntimeException("DefaultAnswerService.updateAnswerToAQuestion()",
          SilverpeasRuntimeException.ERROR, "answer.UPDATING_ANSWER_TO_QUESTION_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  private Connection getConnection() {
    try {
      return DBUtil.openConnection();
    } catch (Exception e) {
      throw new AnswerRuntimeException("DefaultAnswerService.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }
}
