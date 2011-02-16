/*
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

package com.stratelia.webactiv.util.viewGenerator.html.comment;

import com.silverpeas.rest.RESTWebService;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.div;
import org.apache.ecs.xhtml.script;

/**
 * A tag for displaying a list of comments.
 * Each comment in the list have two associated operations: remove it and edit it.
 */
public class CommentListTag  extends TagSupport {
  private static final long serialVersionUID = -67747040807286867L;

  /**
   * The identifier of the XHTML div tag within which the list of comments is displayed.
   */
  public static final String COMMENT_LIST_DIV_ID = "comments";

  /**
   * The CSS class name of the XHTML div tag within which a comment of the list is displayed.
   */
  public static final String COMMENT_DIV_CLASS = "comment";

  /**
   * The CSS class name of the XHTML div tag within which the comment author name is displayed.
   */
  public static final String COMMENT_AUTHOR_DIV_CLASS = "author";

  /**
   * The CSS clas name of the XHTML div tag whithin which the comment text is displayed.
   */
  public static final String COMMENT_TEXT_DIV_CLASS = "text";

  private String sessionKey;
  private String componentId;
  private String resourceId;

  /**
   * Sets the unique identifier of the Silverpeas component instance to which the commented resource
   * belongs.
   * @param componentId the unique identifier of the instance of a Silverpeas component.
   */
  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

  /**
   * Sets the unique identifier of the resource that is commented out.
   * @param resourceId the unique identifier of the commented resource.
   */
  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  /**
   * Sets the session key with which the request must be done.
   * @param sessionKey the key of the user session.
   */
  public void setSessionKey(String sessionKey) {
    this.sessionKey = sessionKey;
  }

  @Override
  public int doStartTag() throws JspException {
    ElementContainer xhtmlcontainer = new ElementContainer();
    div comments = new div(COMMENT_LIST_DIV_ID);
    script script = new script().setType("text/javascript").addElement(listOfCommentsInJavascript());
    xhtmlcontainer.addElement(comments).addElement(script);
    xhtmlcontainer.output(pageContext.getOut());
    return SKIP_BODY;
  }

  /**
   * This method generates the Javascript instructions to retrieve in AJAX the comments on the given
   * resource and to display them.
   * The generated code is built upon the JQuery toolkit, so that it is required to be included
   * within the the XHTML header section.
   * @return the javascript code to handle a list of comments on a given resource.
   */
  private String listOfCommentsInJavascript() {
    StringBuilder builder = new StringBuilder();
    builder.append("$(function() { $.ajax('services/comments/").
        append(this.componentId).
        append("/").
        append(this.resourceId).
        append("'), { dataType: 'json', success: function(data) { for (var x = 0; x < data.length; x++) { ").
        append("var div = $('<div>').addClass('").
        append(COMMENT_DIV_CLASS).
        append("').appendTo('#").
        append(COMMENT_LIST_DIV_ID).
        append("'); ").
        append("$('<div>').addClass('").
        append(COMMENT_AUTHOR_DIV_CLASS).
        append("').text(data[x].author.fullName).appendTo(div);").
        append("$('<div>').addClass('").
        append(COMMENT_TEXT_DIV_CLASS).
        append("').text(data[x].text).appendTo(div); } } }); });");
    return builder.toString();
  }
}
