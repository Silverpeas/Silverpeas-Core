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
package org.silverpeas.core.web.util.viewgenerator.html;

import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.space.SpaceInst;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.span;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.util.Iterator;

/**
 * This tag prints out the full path of an application.
 *
 * @author neysseric
 */
public class ComponentPathTag extends SimpleTagSupport {

  private String componentId;
  private String separator = " > ";
  private boolean includeComponent = true;

  public String getComponentId() {
    return componentId;
  }

  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

  public boolean isIncludeComponent() {
    return includeComponent;
  }

  public void setIncludeComponent(boolean includeComponent) {
    this.includeComponent = includeComponent;
  }

  public String getSeparator() {
    return separator;
  }

  public void setSeparator(String separator) {
    this.separator = separator;
  }

  @Override
  public void doTag() throws JspException, IOException {
    OrganizationController oc = OrganizationControllerProvider.getOrganisationController();

    span path = new span();
    path.setClass("sp-path");
    span spacePath = new span();
    spacePath.setClass("sp-path-spaces");
    path.addElement(spacePath);
    Iterator<SpaceInst> spaces = oc.getSpacePathToComponent(componentId).iterator();
    while (spaces.hasNext()) {
      SpaceInst space = spaces.next();
      span span = new span(space.getName());
      span.setClass("sp-path-space");
      spacePath.addElement(span);
      if (spaces.hasNext()) {
        spacePath.addElement(getSpanSeparator());
      }
    }

    if (includeComponent) {
      ComponentInstLight app = oc.getComponentInstLight(componentId);
      span span = new span(app.getLabel());
      span.setClass("sp-path-app");

      path.addElement(getSpanSeparator());
      path.addElement(span);
    }

    ElementContainer container = new ElementContainer();
    container.addElement(path);
    container.output(getOut());
  }

  private span getSpanSeparator() {
    span sep = new span(separator);
    sep.setClass("sp-path-sep");
    return sep;
  }

  protected JspWriter getOut() {
    return getJspContext().getOut();
  }
}