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

package org.silverpeas.core.questioncontainer.result.model;

import java.io.Serializable;

import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.questioncontainer.answer.model.AnswerPK;

public class QuestionResult implements Serializable {
  private static final long serialVersionUID = -4595110484759953673L;
  private int elapsedTime = 0;
  private int participationId = 1;
  private QuestionResultPK pk = null;
  private AnswerPK answerPK = null;
  private ForeignPK questionPK = null;
  private String userId = null;
  private String openedAnswer = null;
  private String voteDate = null;
  private int nbPoints = 0;

  /**
   * Smallest QuestionResult Constructor
   * @param pk a QuestionResultPK object
   * @param questionPK a ForeignPK object
   * @param answerPK the answer id
   * @param userId the user id
   * @param openedAnswer
   */
  public QuestionResult(QuestionResultPK pk, ForeignPK questionPK, AnswerPK answerPK, String userId,
      String openedAnswer) {
    super();
    this.pk = pk;
    this.answerPK = answerPK;
    this.questionPK = questionPK;
    this.userId = userId;
    this.openedAnswer = openedAnswer;
  }

  public QuestionResult(QuestionResultPK pk, ForeignPK questionPK, AnswerPK answerPK, String userId,
      String openedAnswer, String voteDate) {
    this(pk, questionPK, answerPK, userId, openedAnswer);
    this.voteDate = voteDate;
  }

  public QuestionResult(QuestionResultPK pk, ForeignPK questionPK, AnswerPK answerPK, String userId,
      String openedAnswer, String voteDate, int elapsedTime, int participationId) {
    this(pk, questionPK, answerPK, userId, openedAnswer, voteDate);
    this.elapsedTime = elapsedTime;
    this.participationId = participationId;
  }

  public QuestionResult(QuestionResultPK pk, ForeignPK questionPK, AnswerPK answerPK, String userId,
      String openedAnswer, int nbPoints, String voteDate, int elapsedTime, int participationId) {
    this(pk, questionPK, answerPK, userId, openedAnswer, voteDate, elapsedTime, participationId);
    this.nbPoints = nbPoints;
  }

  public void setQuestionResultPK(QuestionResultPK pk) {
    this.pk = pk;
  }

  public void setAnswerPK(AnswerPK answerPK) {
    this.answerPK = answerPK;
  }

  public void setQuestionPK(ForeignPK questionPK) {
    this.questionPK = questionPK;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public void setOpenedAnswer(String openedAnswer) {
    this.openedAnswer = openedAnswer;
  }

  public void setNbPoints(int nbPoints) {
    this.nbPoints = nbPoints;
  }

  public void setVoteDate(String voteDate) {
    this.voteDate = voteDate;
  }

  public void setElapsedTime(int elapsedTime) {
    this.elapsedTime = elapsedTime;
  }

  public void setParticipationId(int participationId) {
    this.participationId = participationId;
  }

  public QuestionResultPK getQuestionResultPK() {
    return this.pk;
  }

  public AnswerPK getAnswerPK() {
    return this.answerPK;
  }

  public ForeignPK getQuestionPK() {
    return this.questionPK;
  }

  public String getUserId() {
    return this.userId;
  }

  public String getOpenedAnswer() {
    return this.openedAnswer;
  }

  public int getNbPoints() {
    return this.nbPoints;
  }

  public String getVoteDate() {
    return this.voteDate;
  }

  public int getElapsedTime() {
    return this.elapsedTime;
  }

  public int getParticipationId() {
    return this.participationId;
  }
}