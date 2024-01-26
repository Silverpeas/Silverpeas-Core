/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.util.viewgenerator.html.arraypanes;

import org.silverpeas.kernel.util.StringUtil;

import javax.el.LambdaExpression;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Create a new column header in an ArrayPane
 * @author cdm
 */
public class ArrayColumnTag extends BodyTagSupport {

  private static final long serialVersionUID = 1L;
  private String title;
  private Boolean sortable;
  private transient LambdaExpression compareOn;
  private String width;

  @Override
  public int doStartTag() throws JspException {
    final ArrayPane arrayPane = getArrayPaneTag().getArrayPane();
    ArrayColumn column = arrayPane.addArrayColumn(title);
    new CompareOnConfigurator(this, column, compareOn).configure();
    if (StringUtil.isDefined(width)) {
      if (StringUtil.isInteger(width)) {
        column.setWidth(width + "px");
      } else {
        column.setWidth(width);
      }
    }
    return SKIP_BODY;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

  public void setCompareOn(final LambdaExpression compareOn) {
    this.compareOn = compareOn;
  }

  public void setSortable(boolean sortable) {
    this.sortable = sortable;
  }

  public void setWidth(final String width) {
    this.width = width;
  }

  private ArrayPaneTag getArrayPaneTag() {
    return (ArrayPaneTag) findAncestorWithClass(this, ArrayPaneTag.class);
  }

  /**
   * This class has been created in order to get a right context for the {@link Function} delivered
   * by the execution of {@link LambdaExpression} of {@link #compareOn} attribute.<br>
   * Without this class which creates a sub context for Lambda, compareOn of each arrayColumn tag
   * has the same comparator, the last one.
   */
  private static class CompareOnConfigurator {

    private final ArrayColumnTag tag;
    private final ArrayColumn column;
    private final LambdaExpression compareOn;

    CompareOnConfigurator(final ArrayColumnTag tag, final ArrayColumn column,
        final LambdaExpression compareOn) {
      this.tag = tag;
      this.column = column;
      this.compareOn = compareOn;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    void configure() {
      if (compareOn != null) {
        compareOn.setELContext(tag.pageContext.getELContext());
        final BiFunction function = compareOn::invoke;
        column.setCompareOn(function);
      } else if (tag.sortable != null) {
        column.setSortable(tag.sortable);
      }
    }
  }
}
