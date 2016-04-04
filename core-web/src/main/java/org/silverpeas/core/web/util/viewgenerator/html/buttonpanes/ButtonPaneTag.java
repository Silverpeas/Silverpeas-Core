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

package org.silverpeas.core.web.util.viewgenerator.html.buttonpanes;

import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;
import org.silverpeas.core.web.util.viewgenerator.html.buttons.Button;
import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * @author ehugonnet
 */
public class ButtonPaneTag extends TagSupport {

  private static final String BUTTON_PANE_ATT = "pageContextButtonPane";
  private static final long serialVersionUID = -3658916991820665360L;

  @Override
  public int doEndTag() throws JspException {
    final ButtonPane buttonPane = (ButtonPane) pageContext.getAttribute(BUTTON_PANE_ATT);
    try {
      pageContext.getOut().println(buttonPane.print());
    } catch (final IOException e) {
      throw new JspException("ButtonPane Tag", e);
    }
    pageContext.removeAttribute(BUTTON_PANE_ATT);
    return EVAL_PAGE;
  }

  @Override
  public int doStartTag() throws JspException {
    getCurrentButtonPane();
    return EVAL_BODY_INCLUDE;
  }

  public void addButton(Button button) {
    getCurrentButtonPane().addButton(button);
  }

  public void setVerticalPosition(boolean vertical) {
    if (vertical) {
      getCurrentButtonPane().setVerticalPosition();
    } else {
      getCurrentButtonPane().setHorizontalPosition();
    }
  }

  public void setHeight(String width) {
    getCurrentButtonPane().setVerticalWidth(width);
  }

  public void setHorizontalPosition(boolean horizontal) {
    setVerticalPosition(!horizontal);
  }

  private synchronized ButtonPane getCurrentButtonPane() {
    ButtonPane result = (ButtonPane) pageContext.getAttribute(BUTTON_PANE_ATT);
    if (result == null) {
      final GraphicElementFactory gef = (GraphicElementFactory) pageContext.getSession().
          getAttribute(
          GraphicElementFactory.GE_FACTORY_SESSION_ATT);
      result = gef.getButtonPane();
      pageContext.setAttribute(BUTTON_PANE_ATT, result);
    }
    return result;
  }
}
