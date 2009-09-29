package com.stratelia.webactiv.util.viewGenerator.html.iconPanes;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;

public class IconPaneTag extends TagSupport {

  static public final String ICONPANE_PAGE_ATT = "pageContextIconPane";

  private String orientation = "horizontal";
  private String spacing = null;

  /**
   * @return the orientation
   */
  public String getOrientation() {
    return orientation;
  }

  /**
   * @param orientation
   *          the orientation to set
   */
  public void setOrientation(String orientation) {
    this.orientation = orientation;
  }

  /**
   * @return the spacing
   */
  public String getSpacing() {
    return spacing;
  }

  /**
   * @param spacing
   *          the spacing to set
   */
  public void setSpacing(String spacing) {
    this.spacing = spacing;
  }

  public int doEndTag() throws JspException {
    try {
      IconPane icons = (IconPane) pageContext.getAttribute(ICONPANE_PAGE_ATT);
      pageContext.getOut().println(icons.print());
    } catch (IOException e) {
      throw new JspException("TabbedPane Tag", e);
    }
    return EVAL_PAGE;
  }

  public int doStartTag() throws JspException {
    GraphicElementFactory gef = (GraphicElementFactory) pageContext
        .getSession()
        .getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);
    IconPane icons = gef.getIconPane();
    pageContext.setAttribute(ICONPANE_PAGE_ATT, icons);
    return EVAL_BODY_INCLUDE;
  }

}
