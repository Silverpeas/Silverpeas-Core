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

package org.silverpeas.core.web.util.viewgenerator.html.arraypanes;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * Create a new cell in an ArrayPane
 * @author cdm
 */
public class ArrayCellTextTag extends BodyTagSupport {

  private static final long serialVersionUID = -719577480679901247L;
  private String text;

  @Override
  public int doEndTag() throws JspException {
    if (bodyContent != null && bodyContent.getString() != null) {
      getArrayLine().addArrayCellText(bodyContent.getString());
    } else {
      getArrayLine().addArrayCellText(text);
    }
    return EVAL_PAGE;
  }

  @Override
  public int doStartTag() throws JspException {
    return EVAL_BODY_INCLUDE;
  }

  public void setText(final String text) {
    this.text = text;
  }

  public ArrayLine getArrayLine() {
    return (ArrayLine) pageContext.getAttribute(ArrayLineTag.ARRAY_LINE_PAGE_ATT);
  }
}
