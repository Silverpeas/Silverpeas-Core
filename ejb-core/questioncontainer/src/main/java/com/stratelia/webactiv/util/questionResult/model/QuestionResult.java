package com.stratelia.webactiv.util.questionResult.model;

import java.io.Serializable;

import com.silverpeas.util.ForeignPK;
import com.stratelia.webactiv.util.answer.model.AnswerPK;

public class QuestionResult implements Serializable {
  private int elapsedTime = 0;
  private int participationId = 1;
  private QuestionResultPK pk = null;
  private AnswerPK answerPK = null;
  private ForeignPK questionPK = null;
  private String userId = null;
  private String openedAnswer = null;
  private String voteDate = null;
  private int nbPoints = 0;

  public QuestionResult(QuestionResultPK pk, ForeignPK questionPK,
      AnswerPK answerPK, String userId, String openedAnswer, String voteDate) {
    setQuestionResultPK(pk);
    setAnswerPK(answerPK);
    setQuestionPK(questionPK);
    setUserId(userId);
    setOpenedAnswer(openedAnswer);
    setVoteDate(voteDate);
    setElapsedTime(0);
    setParticipationId(1);
  }

  public QuestionResult(QuestionResultPK pk, ForeignPK questionPK,
      AnswerPK answerPK, String userId, String openedAnswer, String voteDate,
      int elapsedTime, int participationId) {
    setQuestionResultPK(pk);
    setAnswerPK(answerPK);
    setQuestionPK(questionPK);
    setUserId(userId);
    setOpenedAnswer(openedAnswer);
    setVoteDate(voteDate);
    setElapsedTime(elapsedTime);
    setParticipationId(participationId);
  }

  public QuestionResult(QuestionResultPK pk, ForeignPK questionPK,
      AnswerPK answerPK, String userId, String openedAnswer, int nbPoints,
      String voteDate, int elapsedTime, int participationId) {
    setQuestionResultPK(pk);
    setAnswerPK(answerPK);
    setQuestionPK(questionPK);
    setUserId(userId);
    setOpenedAnswer(openedAnswer);
    setNbPoints(nbPoints);
    setVoteDate(voteDate);
    setElapsedTime(elapsedTime);
    setParticipationId(participationId);
  }

  public QuestionResult(QuestionResultPK pk, ForeignPK questionPK,
      AnswerPK answerPK, String userId, String openedAnswer) {
    setQuestionResultPK(pk);
    setAnswerPK(answerPK);
    setQuestionPK(questionPK);
    setUserId(userId);
    setOpenedAnswer(openedAnswer);
    setVoteDate(null);
    setElapsedTime(0);
    setParticipationId(1);
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