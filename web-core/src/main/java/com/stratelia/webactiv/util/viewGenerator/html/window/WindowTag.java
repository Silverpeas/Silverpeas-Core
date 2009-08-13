package com.stratelia.webactiv.util.viewGenerator.html.window;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import com.stratelia.webactiv.util.viewGenerator.html.NeedWindowTag;

public class WindowTag extends NeedWindowTag {

  public static final String WINDOW_PAGE_ATT = "pageContextWindow";
  private static final long serialVersionUID = 1L;

  @Override
  public int doEndTag() throws JspException {
    Window window = (Window) pageContext.getAttribute(WINDOW_PAGE_ATT);
    try {
      pageContext.getOut().println(window.printAfter());
    } catch (IOException e) {
      throw new JspException("Window Tag", e);
    }
    return EVAL_PAGE;
  }

  @Override
  public int doStartTag() throws JspException {
    Window window = getWindow();
    try {
      pageContext.getOut().println(window.printBefore());
    } catch (IOException e) {
      throw new JspException("Window Tag", e);
    }
    return EVAL_BODY_INCLUDE;
  }
}
