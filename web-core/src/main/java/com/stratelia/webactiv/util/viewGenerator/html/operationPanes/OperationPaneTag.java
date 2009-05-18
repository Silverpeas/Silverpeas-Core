package com.stratelia.webactiv.util.viewGenerator.html.operationPanes;

import javax.servlet.jsp.JspException;

import com.stratelia.webactiv.util.viewGenerator.html.NeedWindowTag;

public class OperationPaneTag extends NeedWindowTag {
  static final String OPERATION_PANE_PAGE_ATT = "pageContextOperationPane";

  public int doStartTag() throws JspException {
    OperationPane pane = getWindow().getOperationPane();
    pageContext.setAttribute(OPERATION_PANE_PAGE_ATT, pane);
    return EVAL_BODY_INCLUDE;
  }

}
