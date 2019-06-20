/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.util.viewgenerator.html;

import org.apache.ecs.xhtml.a;
import org.apache.ecs.xhtml.span;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * This tag prints out the full path of an application.
 *
 * @author neysseric
 */
public class ComponentPathTag extends SimpleTagSupport {

  private String componentId;
  private String nodeId;
  private String separator = " > ";
  private boolean includeComponent = true;
  private String language;
  private boolean link = false;

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

  public String getNodeId() {
    return nodeId;
  }

  public void setNodeId(final String nodeId) {
    this.nodeId = nodeId;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(final String language) {
    this.language = language;
  }

  public boolean isLink() {
    return link;
  }

  public void setLink(final boolean link) {
    this.link = link;
  }

  @Override
  public void doTag() throws JspException, IOException {
    OrganizationController oc = OrganizationControllerProvider.getOrganisationController();

    ComponentInstLight app = oc.getComponentInstLight(componentId);
    if (app == null) {
      getOut().print(componentId);
      return;
    }

    span path = new span();
    path.setClass("sp-path");
    span spacePath = new span();
    spacePath.setClass("sp-path-spaces");
    path.addElement(spacePath);
    Iterator<SpaceInstLight> spaces = oc.getPathToComponent(componentId).iterator();
    String spaceId = "";
    while (spaces.hasNext()) {
      SpaceInstLight space = spaces.next();
      spaceId = space.getId();
      span span = new span(space.getName(language));
      span.setClass("sp-path-space");
      spacePath.addElement(span);
      if (spaces.hasNext()) {
        spacePath.addElement(getSpanSeparator());
      }
    }

    if (includeComponent) {
      String appLabel = app.getLabel(language);
      span span = new span(appLabel);
      span.setClass("sp-path-app");

      path.addElement(getSpanSeparator());
      path.addElement(span);
    }

    appendPathOfNode(path);

    if (link) {
      a linkElement = new a();
      String href = URLUtil.getSimpleURL(URLUtil.URL_SPACE, spaceId);
      if (includeComponent && StringUtil.isDefined(getNodeId())) {
        href = URLUtil.getSimpleURL(URLUtil.URL_TOPIC, getNodeId(), componentId);
      } else if (includeComponent) {
        href = URLUtil.getSimpleURL(URLUtil.URL_COMPONENT, componentId);
      }
      linkElement.setHref(href);
      linkElement.setClass("sp-permalink");
      linkElement.addElement(path);
      linkElement.output(getOut());
    } else {
      path.output(getOut());
    }
  }

  private void appendPathOfNode(span path) {
    if (StringUtil.isDefined(getNodeId()) && !NodePK.ROOT_NODE_ID.equals(getNodeId())) {
      span nodePath = new span();
      nodePath.setClass("sp-path-nodes");

      List<NodeDetail> nodes = (List) NodeService.get().getPath(new NodePK(getNodeId(), componentId));
      Collections.reverse(nodes);
      Iterator<NodeDetail> nodesIt = nodes.iterator();
      while (nodesIt.hasNext()) {
        NodeDetail node = nodesIt.next();
        if (!node.getNodePK().isRoot()) {
          span span = new span(node.getName(language));
          span.setClass("sp-path-node");
          nodePath.addElement(span);
          if (nodesIt.hasNext()) {
            nodePath.addElement(getSpanSeparator());
          }
        }
      }

      path.addElement(getSpanSeparator());
      path.addElement(nodePath);
    }
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