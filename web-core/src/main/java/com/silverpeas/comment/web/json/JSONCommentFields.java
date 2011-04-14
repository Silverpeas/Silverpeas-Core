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

package com.silverpeas.comment.web.json;

/**
 * All the fields in the JSON representation of a comment.
 */
public interface JSONCommentFields {

  /**
   * The URI of the comment.
   */
  static final String COMMENT_URI_FIELD = "uri";
  /**
   * The unique identifier of the comment.
   */
  static final String COMMENT_ID_FIELD = "id";
  /**
   * The unique identifier of the commented Silverpeas ressource (publication, image, ...)
   */
  static final String RESOURCE_ID_FIELD = "resourceId";
  /**
   * The unique identifier of the Silverpeas component instance the commented resource, and thus
   * the comment, belongs to.
   */
  static final String COMPONENT_ID_FIELD = "componentId";
  /**
   * The text of the comment.
   */
  static final String TEXT_FIELD = "text";
  /**
   * The user that wrote the comment.
   */
  static final String AUTHOR_FIELD = "author";
  /**
   * The unique identifier of the comment writer.
   */
  static final String AUTHOR_ID_FIELD = "id";
  /**
   * The name of the comment writer.
   */
  static final String AUTHOR_NAME_FIELD = "fullName";
  /**
   * The relative URL of the writer avatar.
   */
  static final String AUTHOR_AVATAR_FIELD = "avatar";
  /**
   * The date at which the comment was created.
   */
  static final String CREATION_DATE_FIELD = "creationDate";
  /**
   * The date at which the comment was modified.
   */
  static final String MODIFICATION_DATE_FIELD = "modificationDate";
}
