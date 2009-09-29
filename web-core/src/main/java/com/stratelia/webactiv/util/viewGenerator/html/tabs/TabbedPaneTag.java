package com.stratelia.webactiv.util.viewGenerator.html.tabs;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;

public class TabbedPaneTag extends TagSupport {

  static final String TABBEDPANE_PAGE_ATT = "pageContextTabbedPane";

  public int doEndTag() throws JspException {
    try {
      TabbedPane tabs = (TabbedPane) pageContext
          .getAttribute(TABBEDPANE_PAGE_ATT);
      pageContext.getOut().println(tabs.print());
    } catch (IOException e) {
      throw new JspException("TabbedPane Tag", e);
    }
    return EVAL_PAGE;
  }

  public int doStartTag() throws JspException {
    GraphicElementFactory gef = (GraphicElementFactory) pageContext
        .getSession()
        .getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);
    TabbedPane tabs = gef.getTabbedPane();
    pageContext.setAttribute(TABBEDPANE_PAGE_ATT, tabs);
    return EVAL_BODY_INCLUDE;
  }

}
