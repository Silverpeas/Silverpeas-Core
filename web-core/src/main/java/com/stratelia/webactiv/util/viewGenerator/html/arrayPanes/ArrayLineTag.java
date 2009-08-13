package com.stratelia.webactiv.util.viewGenerator.html.arrayPanes;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Create a new line in an ArrayPane.
 * @author ehugonnet
 */
public class ArrayLineTag extends TagSupport {

  private static final long serialVersionUID = -5323133574049569236L;
  private ArrayPane arrayPane;
  private ArrayLine arrayLine;

  @Override
  public int doStartTag() throws JspException {
    if (arrayPane == null) {
      ArrayPaneTag arrayPaneTag = (ArrayPaneTag) findAncestorWithClass(this, ArrayPaneTag.class);
      if (arrayPaneTag != null) {
        arrayPane = arrayPaneTag.getArrayPane();
      }
    }
    arrayLine = arrayPane.addArrayLine();
    return EVAL_BODY_INCLUDE;
  }

  public ArrayLine getArrayLine() {
    return arrayLine;
  }
}
