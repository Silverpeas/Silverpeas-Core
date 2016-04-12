/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.util.viewgenerator.html;

import java.io.IOException;
import java.util.Date;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.silverpeas.core.util.MultiSilverpeasBundle;
import org.silverpeas.core.util.DateUtil;

import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * A tag to print out a localized date according to the language of the user.
 */
public class FormatDateTimeTag extends TagSupport {

  private static final long serialVersionUID = -3961694293281722454L;
  private Date dateTime = null;
  private String language = null;

  @Override
  public int doStartTag() throws JspException {
    try {
      pageContext.getOut().print(formatDateTime(dateTime));
    } catch (IOException e) {
      throw new JspException("Silverpeas Resource URL Tag", e);
    }
    return EVAL_PAGE;
  }

  public Date getValue() {
    if (dateTime == null) {
      return null;
    }
    return new Date(dateTime.getTime());
  }

  public void setValue(Date value) {
    if (value == null) {
      this.dateTime = null;
    } else {
      this.dateTime = new Date(value.getTime());
    }
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  private String formatDateTime(final Date dateTime) {
    String formattedDateTime;
    if (isDefined(getLanguage())) {
      formattedDateTime = DateUtil.getOutputDateAndHour(dateTime, getLanguage());
    } else {
      MultiSilverpeasBundle resources = (MultiSilverpeasBundle) pageContext.getRequest().getAttribute(
          GraphicElementFactory.RESOURCES_KEY);
      if (resources != null) {
        formattedDateTime = resources.getOutputDateAndHour(dateTime);
      } else {
        formattedDateTime = DateUtil.getOutputDateAndHour(dateTime, "");
      }
    }
    return formattedDateTime;
  }
}
