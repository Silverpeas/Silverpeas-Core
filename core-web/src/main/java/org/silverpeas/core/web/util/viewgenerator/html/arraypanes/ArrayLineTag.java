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
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Create a new line in an ArrayPane.
 * @author ehugonnet
 */
public class ArrayLineTag extends TagSupport {

  private static final long serialVersionUID = -5323133574049569236L;
  public static final String ARRAY_LINE_PAGE_ATT = "pageContextArrayLine";

  private String id = null;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public int doStartTag() throws JspException {
    ArrayLine arrayLine = getArrayPane().addArrayLine();
    arrayLine.setId(getId());
    pageContext.setAttribute(ARRAY_LINE_PAGE_ATT, arrayLine);
    return EVAL_BODY_INCLUDE;
  }

  @Override
  public int doEndTag() throws JspException {
    pageContext.removeAttribute(ARRAY_LINE_PAGE_ATT);
    return EVAL_PAGE;
  }

  public ArrayPane getArrayPane() {
    return (ArrayPane) pageContext.getAttribute(ArrayPaneTag.ARRAY_PANE_PAGE_ATT);
  }
}
