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
package org.silverpeas.core.importexport.model;

import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.importexport.attachment.AttachmentDetail;
import org.silverpeas.core.importexport.coordinates.CoordinatesPositionsType;
import org.silverpeas.core.importexport.publication.PublicationContentType;
import org.silverpeas.core.importexport.versioning.Document;
import org.silverpeas.core.node.importexport.NodePositionType;
import org.silverpeas.core.pdc.pdc.model.ClassifyPosition;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(namespace = "http://www.silverpeas.org/exchange")
@XmlAccessorType(XmlAccessType.NONE)
public class PublicationType {

  @XmlAttribute
  private int id = -1;
  @XmlAttribute
  private String componentId;
  @XmlElement(name = "publicationHeader", namespace = "http://www.silverpeas.org/exchange")
  private PublicationDetail publicationDetail;
  @XmlElement(name = "publicationContent", namespace = "http://www.silverpeas.org/exchange")
  private PublicationContentType publicationContentType;
  @XmlElementWrapper(name = "attachments", namespace = "http://www.silverpeas.org/exchange")
  @XmlElement(name = "attachment", namespace = "http://www.silverpeas.org/exchange")
  private List<AttachmentDetail> attachmentsType;
  @XmlElementWrapper(name = "documents", namespace = "http://www.silverpeas.org/exchange")
  @XmlElement(name = "document", namespace = "http://www.silverpeas.org/exchange")
  private List<Document> documentsType;
  @XmlElementWrapper(name = "topicPositions", namespace = "http://www.silverpeas.org/exchange")
  @XmlElement(name = "topicPosition", namespace = "http://www.silverpeas.org/exchange")
  private List<NodePositionType> nodePositionsType;
  @XmlElementWrapper(name = "pdcPositions", namespace = "http://www.silverpeas.org/exchange")
  @XmlElement(name = "pdcPosition", namespace = "http://www.silverpeas.org/exchange")
  private List<ClassifyPosition> pdcPositionsType;
  @XmlElement(name = "coordinatesPositions", namespace = "http://www.silverpeas.org/exchange")
  private CoordinatesPositionsType coordinatesPositionsType;

  public PublicationType() {
    // This constructor is necessary with JAXB
  }

  public String getComponentId() {
    return componentId;
  }

  public int getId() {
    return id;
  }

  public PublicationDetail getPublicationDetail() {
    return publicationDetail;
  }

  public void setComponentId(String string) {
    componentId = string;
  }

  public void setId(int i) {
    id = i;
  }

  public void setPublicationDetail(PublicationDetail detail) {
    publicationDetail = detail;
  }

  public List<AttachmentDetail> getAttachmentsType() {
    return attachmentsType;
  }

  public void setAttachmentsType(List<AttachmentDetail> type) {
    attachmentsType = type;
  }

  public List<NodePositionType> getNodePositionsType() {
    return nodePositionsType;
  }

  public void setNodePositionsType(List<NodePositionType> type) {
    nodePositionsType = type;
  }

  public List<ClassifyPosition> getPdcPositionsType() {
    return pdcPositionsType;
  }

  public void setPdcPositionsType(List<ClassifyPosition> pdcPositionsType) {
    this.pdcPositionsType = pdcPositionsType;
  }

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

  public List<Document> getDocumentsType() {
    return documentsType;
  }

  public void setDocumentsType(List<Document> documentsType) {
    this.documentsType = documentsType;
  }
}