/*
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along withWriter this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.pdc.web;

import com.silverpeas.rest.Exposable;
import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import edu.emory.mathcs.backport.java.util.Collections;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The PdC classification entity represents the web entity of the classification of a Silverpeas's
 * resource on the classification plan (PdC). As such, it publishes only some of the business
 * classification attributes.
 * The PdC classificiation is identified in the web by its unique identifier, its URI.
 * 
 * The PdC is a semantic referential that is made up of one or more axis, each of them representing
 * a semantic concept. A classification on the PdC is then a set of positions of the resource content
 * on the different axis; each position provides an atomic semantic information about the resource.
 * A position can be a semantic value of an axis as well a set of values on different axis.
 */
@XmlRootElement
public class PdcClassificationEntity implements Exposable {

  private static final long serialVersionUID = -2217575091640675000L;
  @XmlElement(defaultValue = "")
  private URI uri;
  @XmlElement
  private List<PdcPositionEntity> positions = new ArrayList<PdcPositionEntity>();

  /**
   * Creates a non-defined PdC classification.
   * Resources that are not classified on the PdC have a such undefined classification as they have
   * no classification positions on the PdC axis.
   * @return a web entity representing an undefined PdC classification.
   */
  public static PdcClassificationEntity undefinedClassification() {
    return new PdcClassificationEntity();
  }

  /**
   * Creates a PdC classification from the specified classification positions.
   * @return a web entity representing the PdC classification with the specified positions on the
   * axis Pdc.
   */
  public static PdcClassificationEntity fromPositions(final List<ClassifyPosition> positions,
          String inLanguage) {
    PdcClassificationEntity classification = new PdcClassificationEntity();
    classification.setClassificationPositions(fromClassifyPositions(positions, inLanguage));
    return classification;
  }

  /**
   * A convenient method to enhance the readability of creators.
   * @param language the language in which the terms in the classification are.
   * @return the language
   */
  public static String inLanguage(String language) {
    return language;
  }

  @Override
  public URI getURI() {
    return this.uri;
  }

  /**
   * Sets the URI of this classification entity.
   * @param uri the URI identifying uniquely in the Web this classification.
   * @return the itself.
   */
  public PdcClassificationEntity withURI(final URI uri) {
    this.uri = uri;
    return this;
  }

  /**
   * Gets all the positions on the PdC axis that defines this resource classification.
   * @return an unmodifiable list of a Web representation of each classification positions in this
   * classification.
   */
  public List<PdcPositionEntity> getClassificationPositions() {
    return Collections.unmodifiableList(positions);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final PdcClassificationEntity other = (PdcClassificationEntity) obj;
    if (this.uri != other.uri && (this.uri == null || !this.uri.equals(other.uri))) {
      return false;
    }
    if (this.positions != other.positions &&
            (this.positions == null || !this.positions.equals(other.positions))) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 59 * hash + (this.uri != null ? this.uri.hashCode() : 0);
    hash = 59 * hash + (this.positions != null ? this.positions.hashCode() : 0);
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder positionArray = new StringBuilder("[");
    for (PdcPositionEntity pdcPositionEntity : positions) {
      positionArray.append(pdcPositionEntity.toString()).append(", ");
    }
    if (positionArray.length() > 1) {
      positionArray.replace(positionArray.length() - 1, positionArray.length(), "]");
    } else {
      positionArray.append("]");
    }
    return "PdcClassificationEntity{" + "uri=" + uri + ", positions=" + positionArray.toString()
            + '}';
  }

  private static List<PdcPositionEntity> fromClassifyPositions(
          final List<ClassifyPosition> positions,
          String inLanguage) {
    List<PdcPositionEntity> positionEntities = new ArrayList<PdcPositionEntity>(positions.size());
    for (ClassifyPosition position : positions) {
      positionEntities.add(PdcPositionEntity.fromPositionValues(position.getListClassifyValue(),
              inLanguage));
    }
    return positionEntities;
  }

  private PdcClassificationEntity() {
  }

  private void setClassificationPositions(final List<PdcPositionEntity> positions) {
    this.positions.clear();
    this.positions.addAll(positions);
  }
}
