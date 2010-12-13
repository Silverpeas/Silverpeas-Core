/**
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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.comment.dao;

import java.io.Serializable;

public class CommentInfo implements Serializable {

  private static final long serialVersionUID = 4433090666272026427L;
  private int commentCount;
  private String componentId;
  private String elementId;

  public CommentInfo(int commentCount, String componentId, String elementId) {
    this.commentCount = commentCount;
    this.componentId = componentId;
    this.elementId = elementId;
  }

  public int getCommentCount() {
    return commentCount;
  }

  public String getComponentId() {
    return componentId;
  }

  public String getElementId() {
    return elementId;
  }

}
