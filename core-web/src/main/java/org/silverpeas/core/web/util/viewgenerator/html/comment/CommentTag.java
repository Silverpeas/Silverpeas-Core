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

import javax.servlet.jsp.JspException;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.script;

/**
 * A tag that renders both an edition form for adding new comments and a list of available comments
 * on a given resource.
 */
public class CommentTag extends CommentWidget {

  private static final long serialVersionUID = -3052129864405446498L;

  private String indexed;

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
    script script = new script().setType("text/javascript").addElement(
        getCommentEditionTag().getCommentEditionScript() + "\n" + getCommentListTag().
        getCommentListRenderingScript());
    container.addElement(script);
    container.output(pageContext.getOut());
    return SKIP_BODY;
  }

  /**
   * Gets an instance of CommentEditionTag after having initialized it.
   * @return an initialized CommentEditionTag instance.
   */
  private CommentEditionTag getCommentEditionTag() {
    CommentEditionTag edition = new CommentEditionTag();
    edition.setComponentId(getComponentId());
    edition.setResourceId(getResourceId());
    edition.setResourceType(getResourceType());
    edition.setUserId(getUserId());
    edition.setIndexed(indexed);
    return edition;
  }

  /**
   * Gets an instance of CommentListTag.
   * @return a CommentListTag instance.
   */
  private CommentListTag getCommentListTag() {
    CommentListTag list = new CommentListTag();
    return list;
  }
}
