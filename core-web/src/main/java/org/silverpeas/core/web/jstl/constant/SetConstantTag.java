/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.jstl.constant;

import javax.servlet.jsp.JspException;

import org.silverpeas.core.web.jstl.constant.reflect.ClassConstantInspector;
import org.silverpeas.core.web.jstl.util.AbstractSetVarTagSupport;

/**
 * Tag which defines a scoped variable created from the class constant.
 */
public class SetConstantTag extends AbstractSetVarTagSupport {

  private static final long serialVersionUID = 1L;
  private String fieldName;

  public void setConstant(String value) {
    this.fieldName = value;
  }

  @Override
  public int doEndTag() throws JspException {
    try {
      setScopedVariable(ClassConstantInspector.getValue(this.fieldName));
    } catch (Exception e) {
      throw new JspException("Exception setting constant " + this.fieldName, e);
    }
    return EVAL_PAGE;
  }
}
