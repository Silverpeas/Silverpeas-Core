/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.stratelia.webactiv.util.viewGenerator.html;

import com.stratelia.silverpeas.util.ResourcesWrapper;
import com.stratelia.webactiv.util.DateUtil;
import java.util.Date;
import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import static com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory.*;
import static com.silverpeas.util.StringUtil.*;

/**
 * A tag to print out a localized date and time according to the language of the user.
 */
public class FormatDateTag extends TagSupport {

  private static final long serialVersionUID = -3961694293281722454L;
  private Date date = null;
  private String language = null;

  @Override
  public int doStartTag() throws JspException {
    try {
      pageContext.getOut().print(formatDate(date));
    } catch (IOException e) {
      throw new JspException("Silverpeas Resource URL Tag", e);
    }
    return EVAL_PAGE;
  }

  public Date getValue() {
    return new Date(date.getTime());
  }

  public void setValue(Date date) {
    this.date = new Date(date.getTime());
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  private String formatDate(final Date date) {
    String formattedDate;
    if (isDefined(getLanguage())) {
      formattedDate = DateUtil.getOutputDate(date, getLanguage());
    } else {
      ResourcesWrapper resources = (ResourcesWrapper) pageContext.getRequest().getAttribute(
          RESOURCES_KEY);
      if (resources != null) {
        formattedDate = resources.getOutputDate(date);
      } else {
        formattedDate = DateUtil.getOutputDate(date, "");
      }
    }
    return formattedDate;
  }
}
