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

package org.silverpeas.core.web.util.viewgenerator.html.frame;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;

public class FrameTag extends TagSupport {

  private static final long serialVersionUID = -1212798870876622646L;
  private static final String FRAME_PAGE_ATT = "pageContextFrame";
  private String title;

  @Override
  public int doEndTag() throws JspException {
    final Frame frame = (Frame) pageContext.getAttribute(FRAME_PAGE_ATT);
    try {
      pageContext.getOut().println(frame.printAfter());
    } catch (final IOException e) {
      throw new JspException("Frame Tag", e);
    }
    return EVAL_PAGE;
  }

  @Override
  public int doStartTag() throws JspException {
    final GraphicElementFactory gef = (GraphicElementFactory) pageContext
        .getSession().getAttribute(
        GraphicElementFactory.GE_FACTORY_SESSION_ATT);
    final Frame frame = gef.getFrame();
    if (title != null) {
      frame.addTitle("&nbsp;&nbsp;" + title);
    }
    pageContext.setAttribute(FRAME_PAGE_ATT, frame);
    try {
      pageContext.getOut().println(frame.printBefore());
    } catch (final IOException e) {
      throw new JspException("Frame Tag", e);
    }
    return EVAL_BODY_INCLUDE;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

}
