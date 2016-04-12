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

package org.silverpeas.core.questioncontainer.container.model;

import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.i18n.AbstractBean;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentInterface;
import org.silverpeas.core.questioncontainer.score.model.ScoreDetail;

import java.util.Collection;

public class QuestionContainerHeader extends AbstractBean
    implements java.io.Serializable, SilverContentInterface {

  private static final long serialVersionUID = 6871118433726400355L;
  private QuestionContainerPK pk = null;
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
  //1 : résultats immédiat | 2 : résultats différés après validation initiateur
  private int resultMode;
  public static final int IMMEDIATE_RESULTS = 1;
  public static final int DELAYED_RESULTS = 2;
  //1 : n'affiche rien | 2 : vue classique | 3 : vue détaillée | 4 : vue classique et vue détaillée
  private int resultView;
  public static final int NOTHING_DISPLAY_RESULTS = 1;
  public static final int CLASSIC_DISPLAY_RESULTS = 2;
  public static final int DETAILED_DISPLAY_RESULTS = 3;
  public static final int TWICE_DISPLAY_RESULTS = 4;

  public QuestionContainerHeader(QuestionContainerPK questionContainerPK, String title,
      String description, String comment, String creatorId, String creationDate, String beginDate,
      String endDate, boolean isClosed, int nbVoters, int nbQuestionsPerPage,
      int nbMaxParticipations, int nbParticipationsBeforeSolution, int maxTime, int resultMode,
      int resultView) {
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
    setResultMode(resultMode);
    setResultView(resultView);
  }

  public QuestionContainerHeader(QuestionContainerPK questionContainerPK, String title,
      String description, String comment, String creatorId, String creationDate, String beginDate,
      String endDate, boolean isClosed, int nbVoters, int nbQuestionsPerPage,
      int nbMaxParticipations, int nbParticipationsBeforeSolution, int maxTime, boolean anonymous,
      int resultMode, int resultView) {
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
    setResultMode(resultMode);
    setResultView(resultView);
  }

  public QuestionContainerHeader(QuestionContainerPK questionContainerPK, String title,
      String description, String creatorId, String creationDate, String beginDate, String endDate,
      boolean isClosed, int nbVoters, int nbQuestionsPerPage, boolean anonymous, int resultMode,
      int resultView) {
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
    setResultMode(resultMode);
    setResultView(resultView);
  }

  public QuestionContainerPK getPK() {
    return pk;
  }

  public String getTitle() {
    return getName();
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
    setName(title);
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
    if (URLUtil.displayUniversalLinks()) {
      return URLUtil.getSimpleURL(URLUtil.URL_SURVEY, getId(), getInstanceId());
    }
    return null;
  }

  public boolean isAnonymous() {
    return anonymous;
  }

  public void setAnonymous(boolean anonymous) {
    this.anonymous = anonymous;
  }

  public int getResultMode() {
    return this.resultMode;
  }

  public void setResultMode(int resultMode) {
    this.resultMode = resultMode;
  }

  public int getResultView() {
    return this.resultView;
  }

  public void setResultView(int resultView) {
    this.resultView = resultView;
  }

}