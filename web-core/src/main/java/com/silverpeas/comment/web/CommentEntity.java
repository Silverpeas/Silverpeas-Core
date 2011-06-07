/*
 * Copyright (C) 2000 - 2011 Silverpeas
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

import static com.silverpeas.util.StringUtil.isDefined;

import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.silverpeas.calendar.Date;
import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.model.CommentPK;
import com.silverpeas.rest.Exposable;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

/**
 * The comment entity is a comment object that is exposed in the web as an entity (web entity).
 * As such, it publishes only some od its attributes
 * It represents a comment in Silverpeas plus some additional information such as the URI for
 * accessing it.
 */
@XmlRootElement
public class CommentEntity implements Exposable {

  private static final long serialVersionUID = 8023645204584179638L;
  @XmlElement(defaultValue = "")
  private URI uri;
  @XmlElement(defaultValue = "")
  private String id;
  @XmlElement(required = true)
  private String componentId;
  @XmlElement(required = true)
  private String resourceId;
  @XmlElement(required = true)
  private String text;
  @XmlElement(required = true)
  private CommentAuthorEntity author;
  @XmlElement(required = true, defaultValue = "")
  private String creationDate;
  @XmlElement(required = true, defaultValue = "")
  private String modificationDate;
  @XmlElement
  private boolean indexed = false;

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
  public static List<CommentEntity> fromComments(final Comment... comments) {
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
    Comment comment = new Comment(new CommentPK(id, componentId), new PublicationPK(resourceId,
        componentId), Integer.valueOf(author.getId()), author.getFullName(), text,
        decodeFromDisplayDate(creationDate, getAuthor().getLanguage()),
        decodeFromDisplayDate(modificationDate, getAuthor().getLanguage()));
    comment.setOwnerDetail(author.toUser());
    return comment;
  }

  /**
   * Sets a URI to this entity.
   * With this URI, it can then be accessed through the Web.
   * @param uri the web entity URI.
   * @return itself.
   */
  public CommentEntity withURI(final URI uri) {
    this.uri = uri;
    return this;
  }

  /**
   * Gets the URI of this comment entity.
   * @return the URI with which this entity can be access through the Web.
   */
  @Override
  public URI getURI() {
    return uri;
  }

  /**
   * Gets the unique identifier of the comment.
   * @return the comment identifier.
   */
  public String getId() {
    return id;
  }

  /**
   * Gets the identifier of the Silverpeas component instance to which the commented content belongs.
   * @return the silverpeas component instance identifier.
   */
  public String getComponentId() {
    return componentId;
  }

  /**
   * Gets the identifier of the resource that is commented by this.
   * @return the commented resource identifier.
   */
  public String getResourceId() {
    return resourceId;
  }

  /**
   * Gets the user that has written this comment.
   * @return the author of the comment.
   */
  public CommentAuthorEntity getAuthor() {
    return author;
  }

  /**
   * Gets the text of the comment.
   * @return the text of the comment.
   */
  public String getText() {
    return text;
  }

  /**
   * Gets the date at which the comment was created.
   * @return the creation date of the comment.
   */
  public String getCreationDate() {
    return creationDate;
  }

  /**
   * Gets the date at which the comment was lastly updated.
   * @return the modification date of the comment.
   */
  public String getModificationDate() {
    return modificationDate;
  }

  /**
   * Is this comment indexed?
   * By default, the comment isn't indexed by the system.
   * @return true if the comment is indexed, false otherwise.
   */
  public boolean isIndexed() {
    return indexed;
  }

  /**
   * Changes the text of this comment by the specified one.
   * @param aText the new text.
   * @return itself.
   */
  public CommentEntity newText(final String aText) {
    this.text = aText;
    return this;
  }

  private CommentEntity(final Comment comment) {
    this.componentId = comment.getCommentPK().getInstanceId();
    this.id = comment.getCommentPK().getId();
    this.resourceId = comment.getForeignKey().getId();
    this.text = comment.getMessage();
    this.author = CommentAuthorEntity.fromUser(comment.getOwnerDetail());
    this.creationDate = encodeToDisplayDate(comment.getCreationDate(), this.author.getLanguage());
    this.modificationDate = encodeToDisplayDate(comment.getModificationDate(), this.author.
        getLanguage());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final CommentEntity other = (CommentEntity) obj;
    if (isDefined(id) && isDefined(other.getId())) {
      return id.equals(other.getId());
    } else {
      return componentId.equals(other.getComponentId()) && resourceId.equals(other.getResourceId())
          && text.equals(other.getText()) && creationDate.equals(other.getCreationDate())
          && modificationDate.equals(other.getModificationDate())
          && author.equals(other.getAuthor());
    }
  }

  @Override
  public int hashCode() {
    int hash = 7;
    if (isDefined(id)) {
      hash = 17 * hash + this.id.hashCode();
    } else {
      hash = 17 * hash + (this.componentId != null ? this.componentId.hashCode() : 0);
      hash = 17 * hash + (this.resourceId != null ? this.resourceId.hashCode() : 0);
      hash = 17 * hash + (this.text != null ? this.text.hashCode() : 0);
      hash = 17 * hash + (this.creationDate != null ? this.creationDate.hashCode() : 0);
      hash = 17 * hash + (this.modificationDate != null ? this.modificationDate.hashCode() : 0);
      hash = 17 * hash + (this.author != null ? this.author.hashCode() : 0);
    }
    return hash;
  }

  protected CommentEntity() {
  }

  /**
   * Encodes the specified date into a date to display by taking into account the user prefered
   * language.
   * If the specified date isn't defined, then a display date of today is returned.
   * @param date the date to encode.
   * @param the language to use to encode the display date.
   * @return the resulting display date.
   */
  private static String encodeToDisplayDate(String date, String language) {
    String displayDate = date;
    if (isDefined(date)) {
      try {
        displayDate = DateUtil.getOutputDate(date, language);
      } catch (ParseException ex) {
      }
    } else {
      displayDate = DateUtil.getOutputDate(Date.today(), language);
    }
    return displayDate;
  }

  /**
   * Decodes the specified display date into a date as it is defining in a Comment instance.
   * If the display date isn't defined, then the today date is returned.
   * @param displayDate the display date to decode.
   * @param language the language in which the date is encoded.
   * @return the resulting decoded date.
   */
  private static String decodeFromDisplayDate(String displayDate, String language) {
    String date = displayDate;
    if (isDefined(displayDate)) {
      try {
        date = DateUtil.date2SQLDate(date, language);
      } catch (ParseException ex) {
      }
    } else {
      date = DateUtil.date2SQLDate(Date.today());
    }
    return date;
  }
}
