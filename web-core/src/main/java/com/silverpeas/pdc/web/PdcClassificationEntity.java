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
import com.silverpeas.thesaurus.ThesaurusException;
import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import edu.emory.mathcs.backport.java.util.Collections;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

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

  public static PdcClassificationEntity aPdcClassificationEntity(
          final List<ClassifyPosition> fromPositions,
          String inLanguage,
          final URI atURI) {
    PdcClassificationEntity classification = new PdcClassificationEntity(atURI);
    classification.setClassificationPositions(
            fromClassifyPositions(fromPositions, inLanguage, atURI));
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

  /**
   * A convenient method to enhance the readability of creators.
   * @param uri the URI at which the classification is published.
   * @return the classification URI.
   */
  public static URI atURI(final URI uri) {
    return uri;
  }

  /**
   * A convenient method to enhance the readability of creators.
   * @param positions a list of classify positions.
   * @return the classify positions.
   */
  public static List<ClassifyPosition> fromPositions(final List<ClassifyPosition> positions) {
    return positions;
  }
  
  /**
   * This web entity represents the undefined classification of a resource on the PdC.
   * @return true if this web entity is the representation of the undefined classification, false
   * otherwise.
   */
  @XmlTransient
  public boolean isUndefined() {
    return uri == null;
  }

  @Override
  public URI getURI() {
    return this.uri;
  }

  /**
   * Sets this classification entity with the synonyms of each position value that are present in
   * the specified user thesaurus.
   * @param userThesaurus a holder of the thesaurus of the user that asked for this PdC
   * classification.
   * @return itself.
   * @throws ThesaurusException if an error occurs while getting the synonyms of the values of the
   * different classification positions.
   */
  public PdcClassificationEntity withSynonymsFrom(final UserThesaurusHolder userThesaurus) throws
          ThesaurusException {
    for (PdcPositionEntity pdcPositionEntity : positions) {
      pdcPositionEntity.setSynonymsFrom(userThesaurus);
    }
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
    if (this.positions != other.positions && (this.positions == null
            || !this.positions.equals(other.positions))) {
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
      positionArray.replace(positionArray.length() - 2, positionArray.length(), "]");
    } else {
      positionArray.append("]");
    }
    return "PdcClassificationEntity{" + "uri=" + uri + ", positions=" + positionArray.toString()
            + '}';
  }

  private static List<PdcPositionEntity> fromClassifyPositions(
          final List<ClassifyPosition> positions,
          String inLanguage,
          final URI atBaseURI) {
    SortedSet<PdcPositionEntity> positionEntities = new TreeSet<PdcPositionEntity>(new PositionComparator());
    for (ClassifyPosition position : positions) {
      positionEntities.add(PdcPositionEntity.fromClassifyPosition(position, inLanguage, atBaseURI));
    }
    return new ArrayList<PdcPositionEntity>(positionEntities);
  }

  private PdcClassificationEntity() {
  }

  private PdcClassificationEntity(final URI uri) {
    this.uri = uri;
  }

  public void setClassificationPositions(final List<PdcPositionEntity> positions) {
    this.positions.clear();
    this.positions.addAll(positions);
  }
  
  private static class PositionComparator implements Comparator<PdcPositionEntity> {

    @Override
    public int compare(PdcPositionEntity t, PdcPositionEntity t1) {
      return t.getId().compareTo(t1.getId());
    }
    
  }
}
