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

public class Comment implements java.io.Serializable {

  private static final long serialVersionUID = 4715008538961567734L;
  private CommentPK commentPK = null;
  private QuestionContainerPK questionContainerPK = null;
  private String userId = null;
  private String comment = null;
  private boolean isAnonymous = false;
  private String date = null;

  public Comment(CommentPK commentPK, QuestionContainerPK questionContainerPK,
      String userId, String comment, boolean isAnonymous, String date) {
    this.commentPK = commentPK;
    this.questionContainerPK = questionContainerPK;
    this.userId = userId;
    this.comment = comment;
    this.isAnonymous = isAnonymous;
    this.date = date;
  }

  public CommentPK getPK() {
    return commentPK;
  }

  public QuestionContainerPK getQuestionContainerPK() {
    return questionContainerPK;
  }

  public String getUserId() {
    return userId;
  }

  public String getComment() {
    return comment;
  }

  public boolean isAnonymous() {
    return isAnonymous;
  }

  public String getDate() {
    return date;
  }
}