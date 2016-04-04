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

package com.silverpeas.comment.web;

import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.model.CommentPK;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import java.util.Date;

/**
 * The builder of comments for testing purpose.
 */
public class CommentBuilder {

  private String resourceType;
  private String resourceId;
  private String componentId;
  private UserDetail user;

  public CommentBuilder withUser(final UserDetail user) {
    this.user = user;
    return this;
  }

  public CommentBuilder commentTheResource(String resourceType, String resourceId) {
    this.resourceType = resourceType;
    this.resourceId = resourceId;
    return this;
  }

  public CommentBuilder inComponent(String componentId) {
    this.componentId = componentId;
    return this;
  }

  public Comment withAsText(String theText) {
    Date now = new Date();
    Comment comment = new Comment(new CommentPK("", componentId), resourceType, new PublicationPK(
        resourceId, componentId), Integer.valueOf(user.getId()), user.getDisplayedName(),
        theText,
        now, now);
    comment.setOwnerDetail(user);
    return comment;
  }
}
