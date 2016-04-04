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

package org.silverpeas.core.importexport.model;

import org.silverpeas.core.importexport.attachment.AttachmentsType;
import org.silverpeas.core.importexport.versioning.DocumentsType;

import org.silverpeas.core.importexport.coordinates.CoordinatesPositionsType;
import org.silverpeas.core.node.importexport.NodePositionsType;
import org.silverpeas.core.pdc.pdc.importexport.PdcPositionsType;
import org.silverpeas.core.importexport.publication.PublicationContentType;

import org.silverpeas.core.contribution.publication.model.PublicationDetail;

public class PublicationType {

  private int id = -1;
  private String componentId;
  private PublicationDetail publicationDetail;
  private PublicationContentType publicationContentType;
  private AttachmentsType attachmentsType;
  private NodePositionsType nodePositionsType;
  private PdcPositionsType pdcPositionsType;
  private CoordinatesPositionsType coordinatesPositionsType;
  private DocumentsType documentsType;

  /**
   * @return
   */
  public String getComponentId() {
    return componentId;
  }

  /**
   * @return
   */
  public int getId() {
    return id;
  }

  /**
   * @return
   */
  public PublicationDetail getPublicationDetail() {
    return publicationDetail;
  }

  /**
   * @param string
   */
  public void setComponentId(String string) {
    componentId = string;
  }

  /**
   * @param i
   */
  public void setId(int i) {
    id = i;
  }

  /**
   * @param detail
   */
  public void setPublicationDetail(PublicationDetail detail) {
    publicationDetail = detail;
  }

  /**
   * @return
   */
  public AttachmentsType getAttachmentsType() {
    return attachmentsType;
  }

  /**
   * @param type
   */
  public void setAttachmentsType(AttachmentsType type) {
    attachmentsType = type;
  }

  /**
   * @return
   */
  public NodePositionsType getNodePositionsType() {
    return nodePositionsType;
  }

  /**
   * @param type
   */
  public void setNodePositionsType(NodePositionsType type) {
    nodePositionsType = type;
  }

  public PdcPositionsType getPdcPositionsType() {
    return pdcPositionsType;
  }

  public void setPdcPositionsType(PdcPositionsType pdcPositionsType) {
    this.pdcPositionsType = pdcPositionsType;
  }

  /**
   * @return Returns the publicationContentType.
   */
  public PublicationContentType getPublicationContentType() {
    return publicationContentType;
  }

  /**
   * @param publicationContentType The publicationContentType to set.
   */
  public void setPublicationContentType(
      PublicationContentType publicationContentType) {
    this.publicationContentType = publicationContentType;
  }

  /**
   * Get Coordinates Positions
   * @return Set of CoordinatePositions
   */
  public CoordinatesPositionsType getCoordinatesPositionsType() {
    return coordinatesPositionsType;
  }

  public void setCoordinatesPositionsType(
      CoordinatesPositionsType coordinatesPositionsType) {
    this.coordinatesPositionsType = coordinatesPositionsType;
  }

  public DocumentsType getDocumentsType() {
    return documentsType;
  }

  public void setDocumentsType(DocumentsType documentsType) {
    this.documentsType = documentsType;
  }
}