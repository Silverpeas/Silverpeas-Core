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
/*
 * Created on 24 janv. 2005
 */
package org.silverpeas.core.node.importexport;

import org.silverpeas.core.node.model.NodeDetail;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Classe utilisée pour le (un)marshalling
 * @author sdevolder
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class NodeTreeType {

  @XmlAttribute
  private String componentId;
  @XmlElement(name = "topic")
  private NodeDetail nodeDetail;

  public NodeTreeType() {
    // This constructor is necessary with JAXB
  }

  public String getComponentId() {
    return componentId;
  }

  public NodeDetail getNodeDetail() {
    return nodeDetail;
  }

  public void setComponentId(String string) {
    componentId = string;
  }

  public void setNodeDetail(NodeDetail detail) {
    nodeDetail = detail;
  }

}