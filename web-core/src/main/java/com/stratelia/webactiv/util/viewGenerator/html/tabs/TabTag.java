package com.stratelia.webactiv.util.viewGenerator.html.tabs;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

public class TabTag extends TagSupport {
  private String label;

  private String action;

  private boolean selected;

  public void setLabel(String label) {
    this.label = label;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public void setSelected(String selected) {
    this.selected = Boolean.valueOf(selected).booleanValue();
  }

  public int doEndTag() throws JspException {
    Tag parent = findAncestorWithClass(this, TabbedPaneTag.class);
    if (parent != null) {
      TabbedPane tabs = (TabbedPane) pageContext
          .getAttribute(TabbedPaneTag.TABBEDPANE_PAGE_ATT);
      tabs.addTab(label, action, selected);
    }
    return EVAL_PAGE;
  }

}
