package com.stratelia.webactiv.util.viewGenerator.html.arrayPanes;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * Create a new cell in an ArrayPane
 * @author cdm
 * 
 */
public class ArrayCellTextTag extends BodyTagSupport {

  private static final long serialVersionUID = -719577480679901247L;
  private String text;  

  @Override
  public int doEndTag() throws JspException {
    if (bodyContent != null && bodyContent.getString() != null) {
      getArrayLine().addArrayCellText(bodyContent.getString());
    } else {
      getArrayLine().addArrayCellText(text);
    }
    return EVAL_PAGE;
  }

  @Override
  public int doStartTag() throws JspException {
    return EVAL_BODY_INCLUDE;
  }

  public void setText(final String text) {
    this.text = text;
  }

  public ArrayLine getArrayLine() {
    return (ArrayLine) pageContext.getAttribute(ArrayLineTag.ARRAY_LINE_PAGE_ATT);
  }
}
