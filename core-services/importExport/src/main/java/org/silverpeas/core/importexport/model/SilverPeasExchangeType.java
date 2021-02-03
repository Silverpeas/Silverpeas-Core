/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
/*
 * Created on 24 janv. 2005
 *
 */
package org.silverpeas.core.importexport.model;

import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.node.importexport.NodeTreeType;
import org.silverpeas.core.pdc.pdc.importexport.AxisType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.List;

/**
 * @author tleroi
 */
@XmlRootElement(name = "SilverpeasExchange", namespace = "http://www.silverpeas.org/exchange")
@XmlAccessorType(XmlAccessType.NONE)
public class SilverPeasExchangeType {

  @XmlAttribute
  private String targetComponentId;
  @XmlAttribute
  private boolean usePOI = true;
  @XmlElementWrapper(name = "publications", namespace = "http://www.silverpeas.org/exchange")
  @XmlElement(name = "publication", namespace = "http://www.silverpeas.org/exchange")
  private List<PublicationType> publicationsType;
  @XmlElementWrapper(name = "repositories", namespace = "http://www.silverpeas.org/exchange")
  @XmlElement(name = "repository", namespace = "http://www.silverpeas.org/exchange")
  private List<RepositoryType> repositoriesType;
  @XmlElementWrapper(name = "topicTrees", namespace = "http://www.silverpeas.org/exchange")
  @XmlElement(name = "topicTree", namespace = "http://www.silverpeas.org/exchange")
  private List<NodeTreeType> nodeTreesType;
  @XmlElementWrapper(name = "pdc", namespace = "http://www.silverpeas.org/exchange")
  @XmlElement(name = "axis", namespace = "http://www.silverpeas.org/exchange")
  private List<AxisType> pdcType;
  @XmlElementWrapper(name = "components", namespace = "http://www.silverpeas.org/exchange")
  @XmlElement(name = "component", namespace = "http://www.silverpeas.org/exchange")
  private List<ComponentInst> componentsType;

  public SilverPeasExchangeType() {
    // This constructor is necessary with JAXB
  }

  public String getTargetComponentId() {
    return targetComponentId;
  }

  public List<PublicationType> getPublicationsType() {
    return publicationsType;
  }

  public void setPublicationsType(List<PublicationType> type) {
    publicationsType = type == null ? Collections.emptyList() : type;
  }

  public List<RepositoryType> getRepositoriesType() {
    return repositoriesType;
  }

  public List<NodeTreeType> getNodeTreesType() {
    return nodeTreesType;
  }

  public void setNodeTreesType(List<NodeTreeType> nodeTreesType) {
    this.nodeTreesType = nodeTreesType;
  }

  public void setPdcType(List<AxisType> pdcType) {
    this.pdcType = pdcType;
  }

  public void setComponentsType(List<ComponentInst> componentsType) {
    this.componentsType = componentsType;
  }

  public boolean isPOIUsed() {
    return usePOI;
  }

  public boolean getUsePOI() {
    return usePOI;
  }

}