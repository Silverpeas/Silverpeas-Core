package com.stratelia.webactiv.util.viewGenerator.html.board;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;

public class BoardTag extends TagSupport {
  private static final String BOARD_PAGE_ATT = "pageContextBoard";

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
    pageContext.setAttribute(BOARD_PAGE_ATT, board);
    try {
      pageContext.getOut().println(board.printBefore());
    } catch (IOException e) {
      throw new JspException("Board Tag", e);
    }
    return EVAL_BODY_INCLUDE;
  }

}
