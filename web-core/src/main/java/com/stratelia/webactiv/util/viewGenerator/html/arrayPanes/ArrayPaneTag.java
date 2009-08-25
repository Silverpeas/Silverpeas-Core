package com.stratelia.webactiv.util.viewGenerator.html.arrayPanes;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Create a new ArrayPane.
 * @author cdm
 * 
 */
public class ArrayPaneTag extends TagSupport {

  private static final long serialVersionUID = 1370094709020971218L;
  private String var;
  private String title;
  private String routingAddress = null;
  public static final String ARRAY_PANE_PAGE_ATT = "pageContextArrayPane";

  public String getRoutingAddress() {
    return routingAddress;
  }

  public void setRoutingAddress(String routingAddress) {
    this.routingAddress = routingAddress;
  }

  @Override
  public int doStartTag() throws JspException {
    final GraphicElementFactory gef = (GraphicElementFactory) pageContext.getSession().getAttribute(
        "SessionGraphicElementFactory");
    ArrayPane arrayPane = gef.getArrayPane(var, routingAddress, pageContext.getRequest(), pageContext.getSession());
    if (title != null) {
      arrayPane.setTitle(title);
    }
    pageContext.setAttribute(ARRAY_PANE_PAGE_ATT, arrayPane);
    return EVAL_BODY_INCLUDE;
  }

  @Override
  public int doEndTag() throws JspException {
    final JspWriter out = pageContext.getOut();
    try {
      ArrayPane arrayPane = getArrayPane();
      pageContext.removeAttribute(ARRAY_PANE_PAGE_ATT);
      out.println(arrayPane.print());
    } catch (final IOException e) {
      throw new JspException("ArrayPane Tag", e);
    }
    return EVAL_PAGE;
  }

  public ArrayPane getArrayPane() {
    return (ArrayPane) pageContext.getAttribute(ARRAY_PANE_PAGE_ATT);
  }

  /**
   * The name of the HttpSession attribute that contains the ArrayPane.
   * @param name
   */
  public void setVar(final String name) {
    this.var = name;
  }

  public void setTitle(final String title) {
    this.title = title;
  }
}
