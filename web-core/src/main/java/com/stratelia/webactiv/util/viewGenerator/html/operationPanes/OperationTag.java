package com.stratelia.webactiv.util.viewGenerator.html.operationPanes;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

public class OperationTag extends TagSupport {
  private static final long serialVersionUID = 1L;

  private String icon;
  private String altText;
  private String action;

  public void setIcon(String icon) {
    this.icon = icon;
  }

  public void setAltText(String altText) {
    this.altText = altText;
  }

  public void setAction(String action) {
    this.action = action;
  }

  @Override
  public int doEndTag() throws JspException {
    OperationPane pane = (OperationPane) pageContext.getAttribute(OperationPaneTag.OPERATION_PANE_PAGE_ATT);
    pane.addOperation(icon, altText, action);
    return EVAL_PAGE;
  }
}
