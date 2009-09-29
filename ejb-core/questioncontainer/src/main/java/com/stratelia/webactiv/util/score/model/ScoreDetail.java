/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

// Source file: d:\\webactiv\\util\\com\\stratelia\\webactiv\\util\\score\\model\\ScoreDetail.java
package com.stratelia.webactiv.util.score.model;

/*
 * CVS Informations
 * 
 * $Id: ScoreDetail.java,v 1.1.1.1 2002/08/06 14:47:53 nchaix Exp $
 * 
 * $Log: ScoreDetail.java,v $
 * Revision 1.1.1.1  2002/08/06 14:47:53  nchaix
 * no message
 *
 * Revision 1.4  2001/12/21 13:51:47  scotte
 * no message
 *
 */

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class ScoreDetail implements java.io.Serializable {
  private int participationId = 0;
  private int score = 0;
  private int elapsedTime = 0;
  private int nbParticipations = 0;
  private int position = 0;
  private String fatherId = null;
  private String userId = null;
  private String participationDate = null;
  private String suggestion = null;
  private ScorePK scorePK = null;

  /**
   * @roseuid 3ACC381C00DD
   */
  public ScoreDetail(ScorePK scorePK, String fatherId, String userId,
      int participationId, String participationDate, int score,
      int elapsedTime, String suggestion, int nbParticipations, int position) {
    setScorePK(scorePK);
    setFatherId(fatherId);
    setUserId(userId);
    setParticipationId(participationId);
    setParticipationDate(participationDate);
    setScore(score);
    setElapsedTime(elapsedTime);
    setSuggestion(suggestion);
    setNbParticipations(nbParticipations);
    setPosition(position);
  }

  /**
   * Constructor declaration
   * 
   * 
   * @param scorePK
   * @param fatherId
   * @param userId
   * @param participationId
   * @param participationDate
   * @param score
   * @param elapsedTime
   * @param suggestion
   * 
   * @see
   */
  public ScoreDetail(ScorePK scorePK, String fatherId, String userId,
      int participationId, String participationDate, int score,
      int elapsedTime, String suggestion) {
    setScorePK(scorePK);
    setFatherId(fatherId);
    setUserId(userId);
    setParticipationId(participationId);
    setParticipationDate(participationDate);
    setScore(score);
    setElapsedTime(elapsedTime);
    setSuggestion(suggestion);
  }

  /**
   * @roseuid 3ACC3A1500A7
   */
  public ScoreDetail(ScorePK scorePK) {
    setScorePK(scorePK);
  }

  /**
   * @roseuid 3ACC3A300291
   */
  public ScoreDetail() {
  }

  /**
   * @roseuid 3ACC35E80281
   */
  public void setScorePK(ScorePK scorePK) {
    this.scorePK = scorePK;
  }

  /**
   * @roseuid 3ACC3625006C
   */
  public void setFatherId(String fatherId) {
    this.fatherId = fatherId;
  }

  /**
   * @roseuid 3ACC3646004B
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * @roseuid 3ACC3661032F
   */
  public void setParticipationId(int participationId) {
    this.participationId = participationId;
  }

  /**
   * @roseuid 3ACC3684009B
   */
  public void setParticipationDate(String participationDate) {
    this.participationDate = participationDate;
  }

  /**
   * @roseuid 3ACC36970106
   */
  public void setScore(int score) {
    this.score = score;
  }

  /**
   * @roseuid 3ACC36AC0020
   */
  public void setElapsedTime(int elapsedTime) {
    this.elapsedTime = elapsedTime;
  }

  /**
   * @roseuid 3ACC36C401AB
   */
  public void setSuggestion(String suggestion) {
    this.suggestion = suggestion;
  }

  /**
   * @roseuid 3ACC36DD01C5
   */
  public void setNbParticipations(int nbParticipations) {
    this.nbParticipations = nbParticipations;
  }

  /**
   * @roseuid 3ACC375D00ED
   */
  public void setPosition(int position) {
    this.position = position;
  }

  /**
   * @roseuid 3ACC37790061
   */
  public ScorePK getScorePK() {
    return scorePK;
  }

  /**
   * @roseuid 3ACC37A6023C
   */
  public String getFatherId() {
    return fatherId;
  }

  /**
   * @roseuid 3ACC37BC0252
   */
  public String getUserId() {
    return userId;
  }

  /**
   * @roseuid 3ACC37C702A8
   */
  public int getParticipationId() {
    return participationId;
  }

  /**
   * @roseuid 3ACC37D302D7
   */
  public String getParticipationDate() {
    return participationDate;
  }

  /**
   * @roseuid 3ACC37DE03AF
   */
  public int getScore() {
    return score;
  }

  /**
   * @roseuid 3ACC37E5030F
   */
  public int getElapsedTime() {
    return elapsedTime;
  }

  /**
   * @roseuid 3ACC37F10212
   */
  public String getSuggestion() {
    return suggestion;
  }

  /**
   * @roseuid 3ACC37FA01C5
   */
  public int getNbParticipations() {
    return nbParticipations;
  }

  /**
   * @roseuid 3ACC38040378
   */
  public int getPosition() {
    return position;
  }

}
