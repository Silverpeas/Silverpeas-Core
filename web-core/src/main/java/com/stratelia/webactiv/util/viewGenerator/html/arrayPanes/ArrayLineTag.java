package com.stratelia.webactiv.util.viewGenerator.html.arrayPanes;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Create a new line in an ArrayPane.
 * @author ehugonnet
 */
public class ArrayLineTag extends TagSupport {

  private static final long serialVersionUID = -5323133574049569236L;
  public static final String ARRAY_LINE_PAGE_ATT = "pageContextArrayLine";

  @Override
  public int doStartTag() throws JspException {
    ArrayLine arrayLine = getArrayPane().addArrayLine();
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
