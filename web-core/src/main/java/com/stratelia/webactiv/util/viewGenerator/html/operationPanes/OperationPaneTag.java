package com.stratelia.webactiv.util.viewGenerator.html.operationPanes;

import javax.servlet.jsp.JspException;

import com.stratelia.webactiv.util.viewGenerator.html.NeedWindowTag;
import com.stratelia.webactiv.util.viewGenerator.html.window.WindowTag;

public class OperationPaneTag extends NeedWindowTag {
  static final String OPERATION_PANE_PAGE_ATT = "pageContextOperationPane";
  private static final long serialVersionUID = 1L;

  @Override
  public int doStartTag() throws JspException {
    if(findAncestorWithClass(this, WindowTag.class) != null) {
      throw new JspException("OperationPane Tag should not be after a WindowTag but before");
    }
    OperationPane pane = getWindow().getOperationPane();
    pageContext.setAttribute(OPERATION_PANE_PAGE_ATT, pane);
    return EVAL_BODY_INCLUDE;
  }

}
