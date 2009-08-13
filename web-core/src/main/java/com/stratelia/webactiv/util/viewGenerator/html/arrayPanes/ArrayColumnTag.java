package com.stratelia.webactiv.util.viewGenerator.html.arrayPanes;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.Tag;

/**
 * Create a new column header in an ArrayPane
 * @author cdm
 * 
 */
public class ArrayColumnTag extends BodyTagSupport {

  private static final long serialVersionUID = 1L;
  private String title;
  private ArrayPane arrayPane;
  private Boolean sortable;

  @Override
  public void setParent(final Tag tag) {
    if (tag instanceof ArrayPaneTag) {
      arrayPane = ((ArrayPaneTag) tag).getArrayPane();
    }
    super.setParent(tag);
  }

  @Override
  public int doStartTag() throws JspException {
    ArrayColumn column = arrayPane.addArrayColumn(title);
    if (sortable != null) {
      column.setSortable(sortable);
    }
    return SKIP_BODY;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

  public void setSortable(boolean sortable) {
    this.sortable = sortable;
  }
}
