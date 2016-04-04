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

package org.silverpeas.core.web.util.viewgenerator.html.board;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;

public class BoardTag extends TagSupport {

  private static final long serialVersionUID = 8744598404692060576L;
  private static final String BOARD_PAGE_ATT = "pageContextBoard";

  private String classes = null;

  public void setClasses(final String classes) {
    this.classes = classes;
  }

  public int doEndTag() throws JspException {
    Board board = (Board) pageContext.getAttribute(BOARD_PAGE_ATT);
    try {
      pageContext.getOut().println(board.printAfter());
    } catch (IOException e) {
      throw new JspException("Board Tag", e);
    }
    return EVAL_PAGE;
  }

  public int doStartTag() throws JspException {
    GraphicElementFactory gef = (GraphicElementFactory) pageContext
        .getSession()
        .getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);
    Board board = gef.getBoard();
    board.setClasses(classes);
    pageContext.setAttribute(BOARD_PAGE_ATT, board);
    try {
      pageContext.getOut().println(board.printBefore());
    } catch (IOException e) {
      throw new JspException("Board Tag", e);
    }
    return EVAL_BODY_INCLUDE;
  }

}
