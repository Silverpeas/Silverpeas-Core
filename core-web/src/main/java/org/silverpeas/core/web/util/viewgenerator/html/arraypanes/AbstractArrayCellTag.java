/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

import javax.servlet.jsp.tagext.BodyTagSupport;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;
import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * Create a new cell in an ArrayPane
 * @author cdm
 */
public abstract class AbstractArrayCellTag extends BodyTagSupport {
  private static final long serialVersionUID = -2409388744815834712L;

  private String classes;
  private String nullStringValue;

  @Override
  public final int doEndTag() {
    ArrayCell cell = doCreateCell();
    if (isDefined(classes)) {
      cell.setStyleSheet(classes);
    }
    return EVAL_PAGE;
  }

  public void setClasses(final String classes) {
    this.classes = classes;
  }

  public void setNullStringValue(final String nullStringValue) {
    this.nullStringValue = nullStringValue;
  }

  abstract ArrayCell doCreateCell();

  String getContentValue(String valueIfNoBodyContent) {
    String value = valueIfNoBodyContent;
    if (bodyContent != null) {
      final String bodyContentString = bodyContent.getString();
      if (isDefined(bodyContentString)) {
        value = bodyContentString;
      }
    }
    if (nullStringValue != null && !EMPTY.equals(nullStringValue.trim()) &&
        (value == null || value.equals(nullStringValue))) {
      return nullStringValue;
    }
    return defaultStringIfNotDefined(value);
  }

  protected ArrayLine getArrayLine() {
    return (ArrayLine) pageContext.getAttribute(ArrayLineTag.ARRAY_LINE_PAGE_ATT);
  }
}
