package com.stratelia.webactiv.util.viewGenerator.html.frame;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;

public class FrameTag extends TagSupport {
  private static final String FRAME_PAGE_ATT = "pageContextFrame";

  public int doEndTag() throws JspException {
    Frame frame = (Frame) pageContext.getAttribute(FRAME_PAGE_ATT);
    try {
      pageContext.getOut().println(frame.printAfter());
    } catch (IOException e) {
      throw new JspException("Frame Tag", e);
    }
    return EVAL_PAGE;
  }

  public int doStartTag() throws JspException {
    GraphicElementFactory gef = (GraphicElementFactory) pageContext
        .getSession()
        .getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);
    Frame frame = gef.getFrame();
    pageContext.setAttribute(FRAME_PAGE_ATT, frame);
    try {
      pageContext.getOut().println(frame.printBefore());
    } catch (IOException e) {
      throw new JspException("Frame Tag", e);
    }
    return EVAL_BODY_INCLUDE;
  }

}
