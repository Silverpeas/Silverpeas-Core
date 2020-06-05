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
package org.silverpeas.core.questioncontainer.answer.service;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.questioncontainer.answer.dao.AnswerDAO;
import org.silverpeas.core.questioncontainer.answer.model.Answer;
import org.silverpeas.core.questioncontainer.answer.model.AnswerPK;
import org.silverpeas.core.questioncontainer.answer.model.AnswerRuntimeException;

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

  protected DefaultAnswerService() {
  }

  @Override
  @Transactional(Transactional.TxType.SUPPORTS)
  public Collection<Answer> getAnswersByQuestionPK(ResourceReference questionPK) {

    Connection con = getConnection();
    try {
      return AnswerDAO.getAnswersByQuestionPK(con, questionPK);
    } catch (Exception e) {
      throw new AnswerRuntimeException(e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void recordThisAnswerAsVote(ResourceReference questionPK, AnswerPK answerPK) {

    Connection con = getConnection();
    try {
      AnswerDAO.recordThisAnswerAsVote(con, questionPK, answerPK);
    } catch (Exception e) {
      throw new AnswerRuntimeException(e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void addAnswersToAQuestion(Collection<Answer> answers, ResourceReference questionPK) {
    Connection con = getConnection();
    try {
      AnswerDAO.addAnswersToAQuestion(con, answers, questionPK);
    } catch (Exception e) {
      throw new AnswerRuntimeException(e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void addAnswerToAQuestion(Answer answer, ResourceReference questionPK) {
    Connection con = getConnection();
    try {
      AnswerDAO.addAnswerToAQuestion(con, answer, questionPK);
    } catch (Exception e) {
      throw new AnswerRuntimeException(e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void deleteAnswersToAQuestion(ResourceReference questionPK) {

    Connection con = getConnection();
    try {
      AnswerDAO.deleteAnswersToAQuestion(con, questionPK);
    } catch (Exception e) {
      throw new AnswerRuntimeException(e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void deleteAnswerToAQuestion(ResourceReference questionPK, String answerId) {

    Connection con = getConnection();
    try {
      AnswerDAO.deleteAnswerToAQuestion(con, questionPK, answerId);
    } catch (Exception e) {
      throw new AnswerRuntimeException(e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void updateAnswerToAQuestion(ResourceReference questionPK, Answer answer) {

    Connection con = getConnection();
    try {
      AnswerDAO.updateAnswerToAQuestion(con, questionPK, answer);
    } catch (Exception e) {
      throw new AnswerRuntimeException(e);
    } finally {
      DBUtil.close(con);
    }
  }

  private Connection getConnection() {
    try {
      return DBUtil.openConnection();
    } catch (Exception e) {
      throw new AnswerRuntimeException(e);
    }
  }
}
