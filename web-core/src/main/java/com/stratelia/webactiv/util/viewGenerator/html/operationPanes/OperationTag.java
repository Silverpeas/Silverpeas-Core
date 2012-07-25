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

package com.stratelia.webactiv.util.viewGenerator.html.operationPanes;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

public class OperationTag extends TagSupport {
  private static final long serialVersionUID = 1L;

  private String icon;
  private String altText;
  private String action;

  public void setIcon(String icon) {
    this.icon = icon;
  }

  public void setAltText(String altText) {
    this.altText = altText;
  }

  public void setAction(String action) {
    this.action = action;
  }

  @Override
  public int doEndTag() throws JspException {
    OperationPane pane =
        (OperationPane) pageContext.getAttribute(OperationPaneTag.OPERATION_PANE_PAGE_ATT);
    pane.addOperation(icon, altText, action);
    return EVAL_PAGE;
  }
}
