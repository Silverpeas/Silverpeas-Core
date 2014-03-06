/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.comment.web;

import com.silverpeas.calendar.Date;
import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.model.CommentPK;
import com.silverpeas.profile.web.ProfileResourceBaseURIs;
import com.silverpeas.profile.web.UserProfileEntity;
import com.silverpeas.web.Exposable;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import org.owasp.encoder.Encode;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.silverpeas.util.StringUtil.isDefined;

/**
 * The comment entity is a comment object that is exposed in the web as an entity (web entity). As
 * such, it publishes only some of its attributes It represents a comment in Silverpeas plus some
 * additional information such as the URI for accessing it.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class CommentEntity implements Exposable {

  private static final long serialVersionUID = 8023645204584179638L;
  @XmlElement(defaultValue = "")
  private URI uri;
  @XmlElement(defaultValue = "")
  private String id;
  @XmlElement(required = true)
  @NotNull
  @Size(min = 2)
  private String componentId;
  @XmlElement(required = true)
  @NotNull
  @Size(min = 1)
  private String resourceType;
  @XmlElement(required = true)
  @NotNull
  @Size(min = 1)
  private String resourceId;
  @XmlElement(required = true)
  @NotNull
  private String text;
  @XmlElement(required = true)
  @NotNull
  private String textForHtml;
  @XmlElement(required = true)
  @NotNull
  private UserProfileEntity author;
  @XmlElement(required = true, defaultValue = "")
  private String creationDate;
  @XmlElement(required = true, defaultValue = "")
  private String modificationDate;
  @XmlElement
  private boolean indexed = false;

  /**
   * Creates a new comment entity from the specified comment.
   *
   * @param comment the comment to entitify.
   * @return the entity representing the specified comment.
   */
  public static CommentEntity fromComment(final Comment comment) {
    return new CommentEntity(comment);
  }

  /**
   * Creates several new comment entities from the specified comments.
   *
   * @param comments the comments to entitify.
   * @return a list of entities representing each of then one of the specified comments.
   */
  public static List<CommentEntity> fromComments(final Comment... comments) {
    return fromComments(Arrays.asList(comments));
  }

  /**
   * Creates several new comment entities from the specified list of comments.
   *
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
   *
   * @return a comment instance.
   */
  public Comment toComment() {
    Comment comment = new Comment(new CommentPK(id, componentId), resourceType, new PublicationPK(
        resourceId, componentId), Integer.valueOf(author.getId()), author.getFullName(), text,
        decodeFromDisplayDate(creationDate, getAuthor().getLanguage()),
        decodeFromDisplayDate(modificationDate, getAuthor().getLanguage()));
    comment.setOwnerDetail(getAuthor().toUserDetail());
    return comment;
  }

  /**
   * Sets a URI to this entity. With this URI, it can then be accessed through the Web.
   *
   * @param uri the web entity URI.
   * @return itself.
   */
  public CommentEntity withURI(final URI uri) {
    this.uri = uri;
    String baseURI = uri.toString();
    String usersURI = baseURI.toString().substring(0, baseURI.indexOf("comments"))
        + ProfileResourceBaseURIs.USERS_BASE_URI;
    this.author = this.author.withAsUri(ProfileResourceBaseURIs.uriOfUser(author, usersURI));
    return this;
  }

  /**
   * Gets the URI of this comment entity.
   *
   * @return the URI with which this entity can be access through the Web.
   */
  @Override
  public URI getURI() {
    return uri;
  }

  /**
   * Gets the unique identifier of the comment.
   *
   * @return the comment identifier.
   */
  public String getId() {
    return id;
  }

  /**
   * Gets the identifier of the Silverpeas component instance to which the commented content
   * belongs.
   *
   * @return the silverpeas component instance identifier.
   */
  public String getComponentId() {
    return componentId;
  }

  /**
   * Gets the type of the resource that is commented by this.
   *
   * @return the commented resource type.
   */
  public String getResourceType() {
    return resourceType;
  }

  /**
   * Gets the identifier of the resource that is commented by this.
   *
   * @return the commented resource identifier.
   */
  public String getResourceId() {
    return resourceId;
  }

  /**
   * Gets the user that has written this comment.
   *
   * @return the author of the comment.
   */
  public UserProfileEntity getAuthor() {
    return author;
  }

  /**
   * Gets the text of the comment.
   *
   * @return the text of the comment.
   */
  public String getText() {
    return text;
  }

  /**
   * Gets the text encoded for HTLML of the comment.
   *
   * @return the text encoded for HTML of the comment.
   */
  public String getTextForHtml() {
    return textForHtml;
  }

  /**
   * Gets the date at which the comment was created.
   *
   * @return the creation date of the comment.
   */
  public String getCreationDate() {
    return creationDate;
  }

  /**
   * Gets the date at which the comment was lastly updated.
   *
   * @return the modification date of the comment.
   */
  public String getModificationDate() {
    return modificationDate;
  }

  /**
   * Is this comment indexed? By default, the comment isn't indexed by the system.
   *
   * @return true if the comment is indexed, false otherwise.
   */
  public boolean isIndexed() {
    return indexed;
  }

  /**
   * Changes the text of this comment by the specified one.
   *
   * @param aText the new text.
   * @return itself.
   */
  public CommentEntity newText(final String aText) {
    this.text = aText;
    this.textForHtml = Encode.forHtml(aText);
    return this;
  }

  private CommentEntity(final Comment comment) {
    this.componentId = comment.getCommentPK().getInstanceId();
    this.id = comment.getCommentPK().getId();
    this.resourceType = comment.getResourceType();
    this.resourceId = comment.getForeignKey().getId();
    this.text = comment.getMessage();
    this.textForHtml = Encode.forHtml(comment.getMessage());
    this.author = UserProfileEntity.fromUser(comment.getCreator());
    this.creationDate = encodeToDisplayDate(comment.getCreationDate(), this.author.getLanguage());
    this.modificationDate = encodeToDisplayDate(comment.getModificationDate(), this.author.
        getLanguage());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
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
      return componentId.equals(other.getComponentId()) && resourceType.equals(other.
          getResourceType()) && resourceId.equals(other.getResourceId())
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
      hash = 17 * hash + (this.resourceType != null ? this.resourceType.hashCode() : 0);
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
   * language. If the specified date isn't defined, then a display date of today is returned.
   *
   * @param date the date to encode.
   * @param language the language to use to encode the display date.
   * @return the resulting display date.
   */
  private static String encodeToDisplayDate(java.util.Date date, String language) {
    String displayDate;
    if (date != null) {
      displayDate = DateUtil.getOutputDate(date, language);
    } else {
      displayDate = DateUtil.getOutputDate(Date.today(), language);
    }
    return displayDate;
  }

  /**
   * Decodes the specified display date into a date as it is defining in a Comment instance. If the
   * display date isn't defined, then the today date is returned.
   *
   * @param displayDate the display date to decode.
   * @param language the language in which the date is encoded.
   * @return the resulting decoded date.
   */
  private static java.util.Date decodeFromDisplayDate(String displayDate, String language) {
    java.util.Date date = new java.util.Date();
    if (isDefined(displayDate)) {
      try {
        String sqlDate = DateUtil.date2SQLDate(displayDate, language);
        date = new Date(DateUtil.parseDate(sqlDate));
      } catch (ParseException ex) {
      }
    }
    return date;
  }
}
