/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.core.web.jstl;

import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.web.jstl.util.AbstractSetVarTagSupport;
import org.silverpeas.core.util.StringUtil;

import javax.servlet.jsp.JspException;

/**
 * Simple tag to obtain the value of the parameter for the specified component.
 * @author ehugonnet
 */
public class ComponentParameterTag extends AbstractSetVarTagSupport {

  private static final long serialVersionUID = 1L;
  private String parameter;
  private String componentId;



  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

  public void setParameter(String parameter) {
    this.parameter = parameter;
  }

  @Override
  public int doEndTag() throws JspException {
    if (StringUtil.isDefined(getVar())) {
      pageContext.setAttribute(getVar(), OrganizationControllerProvider.getOrganisationController().
          getComponentParameterValue(componentId, parameter), getScope());
    }
    return EVAL_PAGE;
  }
}
