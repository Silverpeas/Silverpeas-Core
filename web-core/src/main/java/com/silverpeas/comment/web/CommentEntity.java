/*
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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

package com.silverpeas.comment.web;

import com.silverpeas.comment.model.Comment;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The comment entity is a comment object that is exposed in the web as an entity (web entity).
 * As such, it publishes only some od its attributes
 * It represents a comment in Silverpeas plus some additional information such as the URI for
 * accessing it.
 */
public class CommentEntity implements Serializable {
  private static final long serialVersionUID = 1L;
  private final Comment comment;
  private URI uri = null;

  /**
   * Creates a new comment entity from the specified comment.
   * @param comment the comment to entitify.
   * @return the entity representing the specified comment.
   */
  public static CommentEntity fromComment(final Comment comment) {
    return new CommentEntity(comment);
  }

  /**
   * Creates several new comment entities from the specified comments.
   * @param comments the comments to entitify.
   * @return a list of entities representing each of then one of the specified comments.
   */
  public static List<CommentEntity> fromComments(final Comment ... comments) {
    return fromComments(Arrays.asList(comments));
  }

  /**
   * Creates several new comment entities from the specified list of comments.
   * @param comments the list of comments to entitify.
   * @return a list of entities representing each of then one of the specified comments.
   */
  public static List<CommentEntity> fromComments(final List<Comment> comments) {
    List<CommentEntity> entities = new ArrayList<CommentEntity>();
    for (Comment comment : comments) {
      entities.add(fromComment(comment));
    }
    return entities;
  }

  /**
   * Gets the comment business objet this entity represent.
   * @return a comment instance.
   */
  public Comment toComment() {
    return this.comment;
  }

  /**
   * Sets a URI to this entity.
   * With this URI, it can then be accessed through the Web.
   * @param uri the web entity URI.
   * @return the entity itself.
   */
  public CommentEntity withURI(final URI uri) {
    this.uri = uri;
    return this;
  }

  /**
   * Gets the URI of this comment entity.
   * @return the URI with which this entity can be access through the Web.
   */
  public URI getURI() {
    return uri;
  }

  /**
   * Gets the unique identifier of the comment.
   * @return the comment identifier.
   */
  public String getId() {
    return comment.getCommentPK().getId();
  }

  /**
   * Gets the identifier of the Silverpeas component instance to which the commented content belongs.
   * @return the silverpeas component instance identifier.
   */
  public String getComponentId() {
    return comment.getCommentPK().getInstanceId();
  }

  /**
   * Gets the identifier of the content that is commented by this.
   * @return the content identifier.
   */
  public String getContentId() {
    return comment.getForeignKey().getId();
  }

  /**
   * Gets the user that has written this comment.
   * @return the comment writer.
   */
  public CommentWriterEntity getWriter() {
    return CommentWriterEntity.fromUser(comment.getOwnerDetail());
  }

  /**
   * Gets the text of the comment.
   * @return the text of the comment.
   */
  public String getText() {
    return comment.getMessage();
  }

  /**
   * Gets the date at which the comment was created.
   * @return the creation date of the comment.
   */
  public String getCreationDate() {
    return comment.getCreationDate();
  }

  /**
   * Gets the date at which the comment was lastly updated.
   * @return the modification date of the comment.
   */
  public String getModificationDate() {
    return comment.getModificationDate();
  }

  private CommentEntity(final Comment comment) {
    this.comment = comment;
  }
}
