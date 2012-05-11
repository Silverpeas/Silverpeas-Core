/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.util.questionContainer.model;

import java.util.Collection;
import java.util.Iterator;

import com.silverpeas.util.i18n.AbstractI18NBean;
import com.stratelia.silverpeas.contentManager.*;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.util.score.model.ScoreDetail;

public class QuestionContainerHeader extends AbstractI18NBean implements java.io.Serializable,
    SilverContentInterface {

  private static final long serialVersionUID = 6871118433726400355L;
  private QuestionContainerPK pk = null;
  private String title = null;
  private String description = null;
  private String comment = null;
  private String creatorId = null;
  private String creationDate = null;
  private String beginDate = null;
  private String endDate = null;
  private boolean isClosed = false;
  private int nbVoters = 0;
  private int nbRegistered = 0;
  private int nbQuestionsPerPage = 0;
  private int nbMaxParticipations = 0;
  private int nbParticipationsBeforeSolution = 0;
  private int maxTime = 0;
  private int nbMaxPoints = 0;
  private Collection<ScoreDetail> scores = null;
  private boolean anonymous;

  private String iconUrl;

  public QuestionContainerHeader(QuestionContainerPK questionContainerPK,
      String title, String description, String comment, String creatorId,
      String creationDate, String beginDate, String endDate, boolean isClosed,
      int nbVoters, int nbQuestionsPerPage, int nbMaxParticipations,
      int nbParticipationsBeforeSolution, int maxTime) {
    setPK(questionContainerPK);
    setTitle(title);
    setDescription(description);
    setComment(comment);
    setCreatorId(creatorId);
    setCreationDate(creationDate);
    setBeginDate(beginDate);
    setEndDate(endDate);
    close(isClosed);
    setNbVoters(nbVoters);
    setNbQuestionsPerPage(nbQuestionsPerPage);
    setNbMaxParticipations(nbMaxParticipations);
    setNbParticipationsBeforeSolution(nbParticipationsBeforeSolution);
    setMaxTime(maxTime);
  }

  public QuestionContainerHeader(QuestionContainerPK questionContainerPK,
      String title, String description, String comment, String creatorId,
      String creationDate, String beginDate, String endDate, boolean isClosed,
      int nbVoters, int nbQuestionsPerPage, int nbMaxParticipations,
      int nbParticipationsBeforeSolution, int maxTime, boolean anonymous) {
    setPK(questionContainerPK);
    setTitle(title);
    setDescription(description);
    setComment(comment);
    setCreatorId(creatorId);
    setCreationDate(creationDate);
    setBeginDate(beginDate);
    setEndDate(endDate);
    close(isClosed);
    setNbVoters(nbVoters);
    setNbQuestionsPerPage(nbQuestionsPerPage);
    setNbMaxParticipations(nbMaxParticipations);
    setNbParticipationsBeforeSolution(nbParticipationsBeforeSolution);
    setMaxTime(maxTime);
    setAnonymous(anonymous);
  }

  // @deprecated
  public QuestionContainerHeader(QuestionContainerPK questionContainerPK,
      String title, String description, String creatorId, String creationDate,
      String beginDate, String endDate, boolean isClosed, int nbVoters,
      int nbQuestionsPerPage) {
    setPK(questionContainerPK);
    setTitle(title);
    setDescription(description);
    setComment(comment);
    setCreatorId(creatorId);
    setCreationDate(creationDate);
    setBeginDate(beginDate);
    setEndDate(endDate);
    close(isClosed);
    setNbVoters(nbVoters);
    setNbQuestionsPerPage(nbQuestionsPerPage);
  }

  public QuestionContainerHeader(QuestionContainerPK questionContainerPK,
      String title, String description, String creatorId, String creationDate,
      String beginDate, String endDate, boolean isClosed, int nbVoters,
      int nbQuestionsPerPage, boolean anonymous) {
    setPK(questionContainerPK);
    setTitle(title);
    setDescription(description);
    setComment(comment);
    setCreatorId(creatorId);
    setCreationDate(creationDate);
    setBeginDate(beginDate);
    setEndDate(endDate);
    close(isClosed);
    setNbVoters(nbVoters);
    setNbQuestionsPerPage(nbQuestionsPerPage);
    setAnonymous(anonymous);
  }

  public QuestionContainerPK getPK() {
    return pk;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public String getComment() {
    return comment;
  }

  public String getCreatorId() {
    return creatorId;
  }

  public String getCreationDate() {
    return creationDate;
  }

  public String getBeginDate() {
    return beginDate;
  }

  public String getEndDate() {
    return endDate;
  }

  public boolean isClosed() {
    return this.isClosed;
  }

  public int getNbVoters() {
    return this.nbVoters;
  }

  public int getNbRegistered() {
    return this.nbRegistered;
  }

  public int getNbQuestionsPerPage() {
    return this.nbQuestionsPerPage;
  }

  public int getNbMaxParticipations() {
    return this.nbMaxParticipations;
  }

  public int getNbParticipationsBeforeSolution() {
    return this.nbParticipationsBeforeSolution;
  }

  public int getMaxTime() {
    return this.maxTime;
  }

  public int getNbMaxPoints() {
    return this.nbMaxPoints;
  }

  public Collection<ScoreDetail> getScores() {
    return this.scores;
  }

  public void setPK(QuestionContainerPK pk) {
    this.pk = pk;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public void setCreatorId(String creatorId) {
    this.creatorId = creatorId;
  }

  public void setCreationDate(String creationDate) {
    this.creationDate = creationDate;
  }

  public void setBeginDate(String beginDate) {
    this.beginDate = beginDate;
  }

  public void setEndDate(String endDate) {
    this.endDate = endDate;
  }

  public void close(boolean isClosed) {
    this.isClosed = isClosed;
  }

  public void setNbVoters(int nb) {
    this.nbVoters = nb;
  }

  public void setNbQuestionsPerPage(int nb) {
    this.nbQuestionsPerPage = nb;
  }

  public void setNbRegistered(int nb) {
    this.nbRegistered = nb;
  }

  public void setNbMaxParticipations(int nb) {
    this.nbMaxParticipations = nb;
  }

  public void setNbParticipationsBeforeSolution(int nb) {
    this.nbParticipationsBeforeSolution = nb;
  }

  public void setMaxTime(int nb) {
    this.maxTime = nb;
  }

  public void setNbMaxPoints(int nb) {
    this.nbMaxPoints = nb;
  }

  public void setScores(Collection<ScoreDetail> scores) {
    this.scores = scores;
  }

  // methods to be implemented by SilverContentInterface

  public String getName() {
    return getTitle();
  }

  public String getURL() {
    return "searchResult?Type=QuestionContainer&Id=" + getId();
  }

  public String getId() {
    return getPK().getId();
  }

  public String getInstanceId() {
    return getPK().getComponentName();
  }

  public String getDate() {
    return getCreationDate();
  }

  public String getSilverCreationDate() {
    return getCreationDate();
  }

  public void setIconUrl(String iconUrl) {
    this.iconUrl = iconUrl;
  }

  public String getIconUrl() {
    return this.iconUrl;
  }

  public String getPermalink() {
    if (URLManager.displayUniversalLinks()) {
      return URLManager.getSimpleURL(URLManager.URL_SURVEY, getId(), getInstanceId());
    }
    return null;
  }

  public String getDescription(String language) {
    return getDescription();
  }

  public String getName(String language) {
    return getName();
  }

  public Iterator<String> getLanguages() {
    return null;
  }

  public boolean isAnonymous() {
    return anonymous;
  }

  public void setAnonymous(boolean anonymous) {
    this.anonymous = anonymous;
  }

}