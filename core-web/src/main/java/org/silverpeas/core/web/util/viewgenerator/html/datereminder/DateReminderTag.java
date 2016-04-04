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

package org.silverpeas.core.web.util.viewgenerator.html.datereminder;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;


/**
 * A tag for displaying a date reminder and a message form for creating a new date reminder.
 *
 * @author Cécile Bonin
 */
public class DateReminderTag extends TagSupport {

  private static final long serialVersionUID = 5187369754489893222L;

  private String resourceId;
  private String resourceType;
  private String userId;
  private String language;

  /**
   * Sets the unique identifier of the resource that use DateReminder.
   *
   * @param resourceId the unique identifier of the resource.
   */
  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  /**
   * Sets the type of the resource that use DateReminder.
   *
   * @param resourceType the type of the resource.
   */
  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  /**
   * Sets the unique identifier of the user that requested the page in which will be rendered the
   * widget.
   *
   * @param userId the user identifier.
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * Sets the language that requested the page in which will be rendered the
   * widget.
   *
   * @param language the user identifier.
   */
  public void setLanguage(String language) {
    this.language = language;
  }

  /**
   * Gets the unique identifier of the resource in Silverpeas.
   *
   * @return the resource identifier.
   */
  public String getResourceId() {
    return resourceId;
  }

  /**
   * Gets the type of the resource.
   *
   * @return
   */
  public String getResourceType() {
    return resourceType;
  }

  /**
   * Gets the unique identifier of the user that requested the page in which will be rendered the
   * widget.
   *
   * @return the unique identifier of the user.
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Gets the language that requested the page in which will be rendered the
   * widget.
   *
   * @return the unique identifier of the user.
   */
  public String getLanguage() {
    return language;
  }

  @Override
  public int doEndTag() throws JspException {

    DateReminder dateReminder = new DateReminder(getResourceId(),
        getResourceType(), getUserId(), getLanguage());

    try {
      pageContext.getOut().println(dateReminder.print());
    } catch (IOException e) {
      throw new JspException("DateReminderTag tag", e);
    }
    return EVAL_PAGE;
  }

  @Override
  public int doStartTag() throws JspException {
    return EVAL_BODY_INCLUDE;
  }
}