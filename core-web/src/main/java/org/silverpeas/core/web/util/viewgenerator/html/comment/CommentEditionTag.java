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

package org.silverpeas.core.web.util.viewgenerator.html.comment;

import org.silverpeas.core.admin.user.model.UserDetail;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.script;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;

import javax.servlet.jsp.JspException;

import static org.silverpeas.core.util.StringUtil.getBooleanValue;
import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * A tag for displaying an edition form for creating a new comment.
 */
public class CommentEditionTag extends CommentWidget {

  private static final long serialVersionUID = 5187369754489893222L;

  private String indexed;

  /**
   * Is new comments should be indexed? By default, new comments are indexed.
   * @return true if the comments should be indexed, false otherwise.
   */
  public boolean isCommentsIndexed() {
    if (!isDefined(indexed)) {
      return true;
    } else {
      return getBooleanValue(indexed);
    }
  }

  /**
   * Indicates whether new comments should be indexed.
   * @param indexed a flag indicating the indexation of comments.
   */
  public void setIndexed(String indexed) {
    this.indexed = indexed;
  }

  @Override
  public int doStartTag() throws JspException {
    ElementContainer container = initWidget();
    script script = new script().setType("text/javascript").
        addElement(getCommentEditionScript());
    container.addElement(script);
    container.output(pageContext.getOut());
    return SKIP_BODY;
  }

  /**
   * Gets the instructions for rendering the edition form for adding new comments.
   * @return the rendering instructions.
   */
  protected String getCommentEditionScript() {
    UserDetail user = OrganizationControllerProvider
        .getOrganisationController().getUserDetail(getUserId());
    String edition = "";
    if (!user.isAccessGuest() && !user.isAnonymous()) {
      edition =
          "$('#" + COMMENT_WIDGET_DIV_ID + "').comment('edition', function() {"
              + "return {author: {id: '" + getUserId() + "' }, componentId: '" + getComponentId() +
              "', resourceId: '" + getResourceId() + "', resourceType: '" + getResourceType() +
              "', indexed: " + isCommentsIndexed() + "} });";
    }
    return edition;
  }
}
