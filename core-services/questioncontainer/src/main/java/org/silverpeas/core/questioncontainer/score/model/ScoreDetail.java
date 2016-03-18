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

package org.silverpeas.core.questioncontainer.score.model;

/**
 * Class declaration
 * @author
 */
public class ScoreDetail implements java.io.Serializable {
  private static final long serialVersionUID = 2322117586354034602L;
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
   * Default empty constructor
   */
  public ScoreDetail() {
    super();
  }

  /**
   * Unused constructor
   * @param scorePK the score primary key
   */
  public ScoreDetail(ScorePK scorePK) {
    this();
    this.scorePK = scorePK;
  }

  /**
   * Constructor declaration
   * @param scorePK
   * @param fatherId
   * @param userId
   * @param participationId
   * @param participationDate
   * @param score
   * @param elapsedTime
   * @param suggestion
   * @see
   */
  public ScoreDetail(ScorePK scorePK, String fatherId, String userId, int participationId,
      String participationDate, int score, int elapsedTime, String suggestion) {
    this(scorePK);
    this.fatherId = fatherId;
    this.userId = userId;
    this.participationId = participationId;
    this.participationDate = participationDate;
    this.score = score;
    this.elapsedTime = elapsedTime;
    this.suggestion = suggestion;
  }

  /**
   * Seems to be an unused constructor
   * @deprecated
   */
  public ScoreDetail(ScorePK scorePK, String fatherId, String userId, int participationId,
      String participationDate, int score, int elapsedTime, String suggestion,
      int nbParticipations, int position) {
    this(scorePK, fatherId, userId, participationId, participationDate, score, elapsedTime,
        suggestion);
    this.nbParticipations = nbParticipations;
    this.position = position;
  }

  /**
   * @param scorePK the Score Primary Key to set
   */
  public void setScorePK(ScorePK scorePK) {
    this.scorePK = scorePK;
  }

  /**
   * @param fatherId the father identifier to set
   */
  public void setFatherId(String fatherId) {
    this.fatherId = fatherId;
  }

  /**
   * @param userId the user identifier to set
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * @param participationId
   */
  public void setParticipationId(int participationId) {
    this.participationId = participationId;
  }

  /**
   * @param participationDate the participation date to set
   */
  public void setParticipationDate(String participationDate) {
    this.participationDate = participationDate;
  }

  /**
   * @param score the score to set
   */
  public void setScore(int score) {
    this.score = score;
  }

  /**
   * @param elapsedTime the elapsed time to set
   */
  public void setElapsedTime(int elapsedTime) {
    this.elapsedTime = elapsedTime;
  }

  /**
   * @param suggestion the suggestion to set
   */
  public void setSuggestion(String suggestion) {
    this.suggestion = suggestion;
  }

  /**
   * @param nbParticipations the number of participations to set
   */
  public void setNbParticipations(int nbParticipations) {
    this.nbParticipations = nbParticipations;
  }

  /**
   * @param position the postion to set
   */
  public void setPosition(int position) {
    this.position = position;
  }

  /**
   * @return ScorePK
   */
  public ScorePK getScorePK() {
    return scorePK;
  }

  /**
   */
  public String getFatherId() {
    return fatherId;
  }

  /**
   */
  public String getUserId() {
    return userId;
  }

  /**
   */
  public int getParticipationId() {
    return participationId;
  }

  /**
   */
  public String getParticipationDate() {
    return participationDate;
  }

  /**
   */
  public int getScore() {
    return score;
  }

  /**
   */
  public int getElapsedTime() {
    return elapsedTime;
  }

  /**
   */
  public String getSuggestion() {
    return suggestion;
  }

  /**
   */
  public int getNbParticipations() {
    return nbParticipations;
  }

  /**
   */
  public int getPosition() {
    return position;
  }

}
