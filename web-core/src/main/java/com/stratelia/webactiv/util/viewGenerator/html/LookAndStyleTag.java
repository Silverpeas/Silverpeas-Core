package com.stratelia.webactiv.util.viewGenerator.html;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

public class LookAndStyleTag extends TagSupport {
  private static final long serialVersionUID = 1L;

  @Override
  public int doStartTag() throws JspException {
    GraphicElementFactory gef = (GraphicElementFactory) pageContext
        .getSession().getAttribute("SessionGraphicElementFactory");
    try {
      pageContext.getOut().println(gef.getLookStyleSheet());
    } catch (IOException e) {
      throw new JspException("LookAndStyle Tag", e);
    }
    return SKIP_BODY;
  }

}
