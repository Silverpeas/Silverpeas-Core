/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.webapi.comment;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.comment.model.CommentId;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.core.web.rs.WebEntity;
import org.silverpeas.core.webapi.profile.ProfileResourceBaseURIs;
import org.silverpeas.core.webapi.profile.UserProfileEntity;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.text.ParseException;
import java.util.Date;
import java.util.Objects;

import static org.silverpeas.kernel.util.StringUtil.isDefined;

/**
 * The comment entity is a comment object that is exposed in the web as an entity (web entity). As
 * such, it publishes only some of its attributes It represents a comment in Silverpeas plus some
 * additional information such as the URI for accessing it.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class CommentEntity implements WebEntity {

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
  private UserProfileEntity author;
  private String currentUserLanguage;
  @XmlElement(required = true, defaultValue = "")
  private String creationDate; //date in the format displayed for the current user
  @XmlElement(required = true, defaultValue = "")
  private String modificationDate; //date in the format displayed for the current user
  @XmlElement
  private boolean indexed = false;

  /**
   * Creates a new comment entity from the specified comment.
   * @param comment the comment to convert into a web entity.
   * @return the entity representing the specified comment.
   */
  public static CommentEntity fromComment(final Comment comment) {
    return new CommentEntity(comment);
  }

  /**
   * Gets the comment business objet this entity represent.
   * @return a comment instance.
   */
  public Comment toComment() {
    CommentId commentId = new CommentId(getComponentId(), getId());
    ResourceReference resourceRef = new ResourceReference(getResourceId(), getComponentId());
    Date authoredDate = decodeFromDisplayDate(getCreationDate(), getCurrentUserLanguage());
    Date updateDate = decodeFromDisplayDate(getModificationDate(), getCurrentUserLanguage());
    Comment comment =
        new Comment(commentId, getAuthor().getId(), getResourceType(), resourceRef, authoredDate);
    comment.setLastUpdateDate(updateDate);
    comment.setMessage(getText());
    return comment;
  }

  /**
   * Sets a URI to this entity. With this URI, it can then be accessed through the Web.
   * @param uri the web entity URI.
   * @return itself.
   */
  public CommentEntity withURI(final URI uri) {
    this.uri = uri;
    String baseURI = uri.toString();
    String usersURI =
        baseURI.substring(0, baseURI.indexOf("comments")) + ProfileResourceBaseURIs.USERS_BASE_URI;
    this.author = this.author.withAsUri(ProfileResourceBaseURIs.uriOfUser(author, usersURI));
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
   * Gets the identifier of the Silverpeas component instance to which the commented content
   * belongs.
   * @return the silverpeas component instance identifier.
   */
  public String getComponentId() {
    return componentId;
  }

  /**
   * Gets the type of the resource that is commented by this.
   * @return the commented resource type.
   */
  public String getResourceType() {
    return resourceType;
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
  public UserProfileEntity getAuthor() {
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
   * Gets the current user language.
   * @return the language of the current user.
   */
  public String getCurrentUserLanguage() {
    return currentUserLanguage;
  }

  /**
   * Sets a currentUserLanguage to this entity.
   * @param currentUserLanguage the language of the current user.
   * @return itself.
   */
  public CommentEntity withCurrentUserLanguage(final String currentUserLanguage) {
    this.currentUserLanguage = currentUserLanguage;

    //change values of creationDate and modificationDate according to language of the current user
    java.util.Date createDate =
        decodeFromDisplayDate(getCreationDate(), I18NHelper.DEFAULT_LANGUAGE);
    this.creationDate = encodeToDisplayDate(createDate, this.currentUserLanguage);

    java.util.Date updateDate =
        decodeFromDisplayDate(getModificationDate(), I18NHelper.DEFAULT_LANGUAGE);
    this.modificationDate = encodeToDisplayDate(updateDate, this.currentUserLanguage);
    return this;
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
   * Is this comment indexed? By default, the comment isn't indexed by the system.
   * @return true if the comment is indexed, false otherwise.
   */
  public boolean isIndexed() {
    return indexed;
  }

  protected CommentEntity(final Comment comment) {
    this.componentId = comment.getIdentifier().getComponentInstanceId();
    this.id = comment.getIdentifier().getLocalId();
    this.resourceType = comment.getResourceType();
    this.resourceId = comment.getResourceReference().getLocalId();
    this.text = comment.getMessage();
    this.author = UserProfileEntity.fromUser(comment.getCreator());
    //we don't even know the language of the current user (the currentUserLanguage attribute has
    // not been yet initialized
    this.creationDate = encodeToDisplayDate(comment.getCreationDate(), I18NHelper.DEFAULT_LANGUAGE);
    this.modificationDate =
        encodeToDisplayDate(comment.getLastUpdateDate(), I18NHelper.DEFAULT_LANGUAGE);
    this.indexed = comment.isIndexable();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final CommentEntity that = (CommentEntity) o;
    return Objects.equals(uri, that.uri) &&
        Objects.equals(id, that.id) && Objects.equals(componentId, that.componentId) &&
        Objects.equals(resourceType, that.resourceType) &&
        Objects.equals(resourceId, that.resourceId) && Objects.equals(text, that.text) &&
        Objects.equals(author, that.author) &&
        Objects.equals(currentUserLanguage, that.currentUserLanguage) &&
        Objects.equals(creationDate, that.creationDate) &&
        Objects.equals(modificationDate, that.modificationDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uri, id, componentId, resourceType, resourceId, text, author,
        currentUserLanguage, creationDate, modificationDate, indexed);
  }

  @SuppressWarnings("unused")
  protected CommentEntity() {
    // for JSON/XML serializer
  }

  /**
   * Encodes the specified date into a date to display by taking into account the user preferred
   * language. If the specified date isn't defined, then a display date of today is returned.
   * @param date the date to encode.
   * @param language the language to use to encode the display date.
   * @return the resulting display date.
   */
  private static String encodeToDisplayDate(java.util.Date date, String language) {
    String displayDate;
    displayDate = DateUtil.getOutputDate(Objects.requireNonNullElseGet(date, Date::new), language);
    return displayDate;
  }

  /**
   * Decodes the specified display date into a date as it is defining in a Comment instance. If the
   * display date isn't defined, then the today date is returned.
   * @param displayDate the display date to decode.
   * @param language the language in which the date is encoded.
   * @return the resulting decoded date.
   */
  private static java.util.Date decodeFromDisplayDate(String displayDate, String language) {
    java.util.Date date = new java.util.Date();
    if (isDefined(displayDate)) {
      try {
        String sqlDate = DateUtil.date2SQLDate(displayDate, language);
        date = DateUtil.parseDate(sqlDate);
      } catch (ParseException ex) {
        SilverLogger.getLogger(CommentEntity.class).warn(ex);
      }
    }
    return date;
  }
}
