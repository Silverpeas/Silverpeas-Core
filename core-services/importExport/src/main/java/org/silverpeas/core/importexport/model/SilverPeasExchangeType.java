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

/*
 * Created on 24 janv. 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.silverpeas.core.importexport.model;

import org.silverpeas.core.importexport.admin.ComponentsType;
import org.silverpeas.core.node.importexport.NodeTreesType;
import org.silverpeas.core.pdc.pdc.importexport.PdcType;

/**
 * @author tleroi
 */
public class SilverPeasExchangeType {

  private String targetComponentId;
  private boolean usePOI = true;
  private PublicationsType publicationsType;
  private RepositoriesType repositoriesType;
  private NodeTreesType nodeTreesType;
  private PdcType pdcType;
  private ComponentsType componentsType;

  /**
   * @return
   */
  public String getTargetComponentId() {
    return targetComponentId;
  }

  /**
   * @param string
   */
  public void setTargetComponentId(String string) {
    targetComponentId = string;
  }

  /**
   * @return
   */
  public PublicationsType getPublicationsType() {
    return publicationsType;
  }

  /**
   * @param type
   */
  public void setPublicationsType(PublicationsType type) {
    publicationsType = type;
  }

  /**
   * @return Returns the repositoriesType.
   */
  public RepositoriesType getRepositoriesType() {
    return repositoriesType;
  }

  /**
   * @param repositoriesType The repositoriesType to set.
   */
  public void setRepositoriesType(RepositoriesType repositoriesType) {
    this.repositoriesType = repositoriesType;
  }

  public NodeTreesType getNodeTreesType() {
    return nodeTreesType;
  }

  public void setNodeTreesType(NodeTreesType nodeTreesType) {
    this.nodeTreesType = nodeTreesType;
  }

  public PdcType getPdcType() {
    return pdcType;
  }

  public void setPdcType(PdcType pdcType) {
    this.pdcType = pdcType;
  }

  public ComponentsType getComponentsType() {
    return componentsType;
  }

  public void setComponentsType(ComponentsType componentsType) {
    this.componentsType = componentsType;
  }

  public boolean isPOIUsed() {
    return usePOI;
  }

  public void setUsePOI(boolean b) {
    usePOI = b;
  }

  public boolean getUsePOI() {
    return usePOI;
  }

}