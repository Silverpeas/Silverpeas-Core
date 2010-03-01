package com.stratelia.webactiv.util.viewGenerator.html.browseBars;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

public class BrowseBarElementTag extends TagSupport {

  private String label;
  private String link;
  private String id;
  /**
   * 
   */
  private static final long serialVersionUID = -8148199393832209872L;

  public void setLabel(String label) {
    this.label = label;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public int doEndTag() throws JspException {
    BrowseBarElement element = new BrowseBarElement(label, link, id);
    BrowseBarTag browseBar =  (BrowseBarTag)getParent();
    browseBar.addElement(element);
    return EVAL_PAGE;
  }

}
