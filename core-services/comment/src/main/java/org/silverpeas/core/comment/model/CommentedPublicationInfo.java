/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.comment.model;

import java.io.Serializable;

/**
 * Short information about a commented publication.
 */
public class CommentedPublicationInfo implements Serializable {

  private static final long serialVersionUID = 4433090666272026427L;
  private int commentCount;
  private String componentId;
  private String elementType;
  private String elementId;

  /**
   * Constructs a new CommentedPublicationInfo instance.
   * @param publicationType type of the commented publication.
   * @param publicationId identifier of the commented publication.
   * @param componentId identifier of the Silverpeas component to which the publication belongs.
   * @param commentCount number of comments on the publucation.
   */
  public CommentedPublicationInfo(String publicationType, String publicationId, String componentId,
      int commentCount) {
    this.commentCount = commentCount;
    this.componentId = componentId;
    this.elementType = publicationType;
    this.elementId = publicationId;
  }

  /**
   * Gets the number of comments on the publication.
   * @return the comments count.
   */
  public int getCommentCount() {
    return commentCount;
  }

  /**
   * Gets the identifier of the Silverpeas component to which the publication belongs.
   * @return the Silverpeas component instance identifier.
   */
  public String getComponentId() {
    return componentId;
  }

  /**
   * Gets the type of the publication.
   * @return the publication identifier.
   */
  public String getPublicationType() {
    return elementType;
  }

  /**
   * Gets the identifier of the publication.
   * @return the publication identifier.
   */
  public String getPublicationId() {
    return elementId;
  }
}
