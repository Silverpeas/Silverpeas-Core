/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.web.util.viewgenerator.html.arraypanes;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.Tag;

/**
 * Create a new column header in an ArrayPane
 * @author cdm
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
