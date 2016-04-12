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

import org.silverpeas.core.contribution.model.SilverpeasContent;
import org.silverpeas.core.security.authorization.AccessController;
import org.silverpeas.core.security.authorization.AccessControllerProvider;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerException;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.questioncontainer.question.model.Question;
import org.silverpeas.core.questioncontainer.container.service.QuestionContainerContentManager;
import org.silverpeas.core.questioncontainer.result.model.QuestionResult;
import org.silverpeas.core.security.authorization.ComponentAccessControl;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

public class QuestionContainerDetail implements java.io.Serializable, SilverpeasContent {

  private static final long serialVersionUID = -2502073007907742590L;
  private QuestionContainerHeader header = null;
  private Collection<Question> questions = null;
  // Comments Collection
  private Collection<Comment> comments = null;
  // QuestionResult Collection of Current user
  private Collection<QuestionResult> votes = null;
  private String jsonPosition = null;
  private String silverObjectId = null;

  public QuestionContainerDetail() {
    super();
  }

  public QuestionContainerDetail(QuestionContainerHeader header,
      Collection<Question> questions, Collection<Comment> comments, Collection<QuestionResult> votes) {
    super();
    this.header = header;
    this.questions = questions;
    this.comments = comments;
    this.votes = votes;
  }

  /**
   * @param header the Question Container header to set
   */
  public void setHeader(QuestionContainerHeader header) {
    this.header = header;
  }

  /**
   * @param questions the collection of questions to set
   */
  public void setQuestions(Collection<Question> questions) {
    this.questions = questions;
  }

  /**
   * @param comments the collection of comments to set
   */
  public void setComments(Collection<Comment> comments) {
    this.comments = comments;
  }

  /**
   * @param votes the collection of QuestionResult to set
   */
  public void setCurrentUserVotes(Collection<QuestionResult> votes) {
    this.votes = votes;
  }

  /**
   * @return the question container header
   */
  public QuestionContainerHeader getHeader() {
    return this.header;
  }

  /**
   * @return the collection of questions
   */
  public Collection<Question> getQuestions() {
    return this.questions;
  }

  /**
   * @return the first question
   */
  public Question getFirstQuestion() {
    Question question = null;
    Collection<Question> questions = getQuestions();
    Iterator<Question> it = questions.iterator();
    if (it.hasNext()) {
      question = it.next();
    }
    return question;
  }

  /**
   * @return the collection of comments
   */
  public Collection<Comment> getComments() {
    return this.comments;
  }

  /**
   * @return the collection of question result
   */
  public Collection<QuestionResult> getCurrentUserVotes() {
    return this.votes;
  }



  /**
   * @return the jsonPosition
   */
  public String getJsonPosition() {
    return jsonPosition;
  }

  /**
   * @param jsonPosition the jsonPosition to set
   */
  public void setJsonPosition(String jsonPosition) {
    this.jsonPosition = jsonPosition;
  }

  @Override
  public String getComponentInstanceId() {
    return getHeader().getInstanceId();
  }

  @Override
  public String getContributionType() {
    // TODO Add an attribute in order to distinguish survey/poll or quizz contribution type
    return null;
  }

  /**
   * Is the specified user can access this container of questions?
   * <p/>
   * A user can access a container if it has enough rights to access the application instance in
   * which is managed this container.
   * @param user a user in Silverpeas.
   * @return true if the user can access this container of questions, false otherwise.
   */
  @Override
  public boolean canBeAccessedBy(final UserDetail user) {
    AccessController<String> accessController = AccessControllerProvider
        .getAccessController(ComponentAccessControl.class);
    return accessController.isUserAuthorized(user.getId(), getComponentInstanceId());
  }

  @Override
  public Date getCreationDate() {
    return null;
  }

  @Override
  public UserDetail getCreator() {
    return UserDetail.getById(getHeader().getCreatorId());
  }

  @Override
  public String getId() {
    return getHeader().getId();
  }

  @Override
  public String getSilverpeasContentId() {
    if (this.silverObjectId == null) {
      try {
        int objectId = QuestionContainerContentManager.getSilverObjectId(getId(), getComponentInstanceId());
        if (objectId >= 0) {
          this.silverObjectId = String.valueOf(objectId);
        }
      } catch (ContentManagerException ex) {
        this.silverObjectId = null;
      }
    }
    return this.silverObjectId;
  }

  protected void setSilverpeasContentId(String contentId) {
    this.silverObjectId = contentId;
  }

  @Override
  public String getTitle() {
    return getHeader().getTitle();
  }

  @Override
  public String getDescription() {
    return getHeader().getDescription();
  }

}