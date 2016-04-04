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
 * A tag for displaying a list of comments. Each comment in the list have two associated operations:
 * remove it and edit it.
 */
public class CommentListTag extends CommentWidget {

  private static final long serialVersionUID = -67747040807286867L;

  @Override
  public int doStartTag() throws JspException {
    ElementContainer container = initWidget();
    script script = new script().setType("text/javascript").
        addElement(getCommentListRenderingScript());
    container.addElement(script);
    container.output(pageContext.getOut());
    return SKIP_BODY;
  }

  /**
   * Gets the instructions that renders the list of comments.
   * @return the rendering instructions.
   */
  protected String getCommentListRenderingScript() {
    return "$('#" + COMMENT_WIDGET_DIV_ID + "').comment('list');";
  }
}
