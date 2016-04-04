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

package org.silverpeas.core.web.util.viewgenerator.html.operationpanes;

import javax.servlet.jsp.JspException;

import org.silverpeas.core.web.util.viewgenerator.html.NeedWindowTag;
import org.silverpeas.core.web.util.viewgenerator.html.window.WindowTag;

public class OperationPaneTag extends NeedWindowTag {
  static final String OPERATION_PANE_PAGE_ATT = "pageContextOperationPane";
  private static final long serialVersionUID = 1L;

  private OperationPaneType type;

  @Override
  public int doStartTag() throws JspException {
    if (findAncestorWithClass(this, WindowTag.class) != null) {
      throw new JspException("OperationPane Tag should not be after a WindowTag but before");
    }
    OperationPane pane = getWindow().getOperationPane();
    pane.setType(type);
    pageContext.setAttribute(OPERATION_PANE_PAGE_ATT, pane);
    return EVAL_BODY_INCLUDE;
  }

  public void setType(OperationPaneType type) {
    this.type = type;
  }

  public OperationPaneType getType() {
    return type;
  }

}
