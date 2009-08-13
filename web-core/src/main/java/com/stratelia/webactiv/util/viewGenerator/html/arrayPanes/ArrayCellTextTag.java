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
  private ArrayLine arrayLine;
  private String text;
  private String content;

  @Override
  public int doEndTag() throws JspException {
    if (bodyContent != null && bodyContent.getString() != null) {
      this.content = bodyContent.getString();
    } else {
      this.content = text;
    }
    arrayLine.addArrayCellText(this.content);
    return EVAL_PAGE;
  }

  @Override
  public int doStartTag() throws JspException {
    ArrayLineTag arrayLineTag = (ArrayLineTag) findAncestorWithClass(this, ArrayLineTag.class);
    if (arrayLineTag != null) {
      arrayLine = arrayLineTag.getArrayLine();
    }
    return EVAL_BODY_INCLUDE;
  }

  public void setText(final String text) {
    this.text = text;
  }
}
