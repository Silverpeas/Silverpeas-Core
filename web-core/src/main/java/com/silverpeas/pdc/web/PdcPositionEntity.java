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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.pdc.web;

import com.silverpeas.rest.Exposable;
import com.silverpeas.thesaurus.ThesaurusException;
import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import com.stratelia.silverpeas.pdc.model.ClassifyValue;
import edu.emory.mathcs.backport.java.util.Collections;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Web representation of the position of a Silverpeas's resource in the classification plan
 * (PdC). As such, it publishes only some of the business classification's position attributes.
 * 
 * A position of a resource on the PdC defines an atomic semantic information about its content.
 * As such, it can be a single or a set of values in the different PdC's axis. An axis value
 * can be a single semantic term or a hierarchical tree of semantic terms carrying a deeper
 * exactness about the concept's value. For example, in a geographic axis, the value France can be
 * a tree in which it is splited into regions, departments, towns, and so on, each of theses terms
 * giving a more accuracy about a geographic position (that is the semantic concept of the
 * geographic axis).
 */
@XmlRootElement
public class PdcPositionEntity implements Exposable {

  private static final long serialVersionUID = 6314816355055147378L;
  @XmlElement
  private List<PdcPositionValue> values = new ArrayList<PdcPositionValue>();
  @XmlElement(required = true)
  private URI uri;
  @XmlElement(required = true)
  private String id;

  public static PdcPositionEntity fromClassifyPosition(final ClassifyPosition position,
          String inLanguage,
          final URI atClassificationURI) {
    String withId = String.valueOf(position.getPositionId());
    PdcPositionEntity positionEntity = new PdcPositionEntity(atClassificationURI, withId);
    positionEntity.setPositionValues(fromClassifyValues(position.getListClassifyValue(), inLanguage));
    return positionEntity;
  }

  @Override
  public URI getURI() {
    return this.uri;
  }

  public String getId() {
    return id;
  }

  /**
   * Gets the values of this position.
   * @return an unmodifiable list of PdC position values.
   */
  public List<PdcPositionValue> getPositionValues() {
    return Collections.unmodifiableList(values);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final PdcPositionEntity other = (PdcPositionEntity) obj;
    if (this.uri != other.uri && (this.uri == null || !this.uri.equals(other.uri))) {
      return false;
    }
    if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
      return false;
    }
    if (this.values != other.values && (this.values == null
            || !this.values.equals(other.values))) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 79 * hash + (this.values != null ? this.values.hashCode() : 0);
    hash = 79 * hash + (this.uri != null ? this.uri.hashCode() : 0);
    hash = 79 * hash + (this.id != null ? this.id.hashCode() : 0);
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder valueArray = new StringBuilder("[");
    for (PdcPositionValue pdcPositionValue : values) {
      valueArray.append(pdcPositionValue.toString()).append(", ");
    }
    if (valueArray.length() > 1) {
      valueArray.replace(valueArray.length() - 1, valueArray.length(), "]");
    } else {
      valueArray.append("]");
    }
    return "PdcPositionEntity{id=" + id + ", uri=" + uri + ", positionValues="
            + valueArray.toString() + ", uri=" + uri + '}';
  }

  private static List<PdcPositionValue> fromClassifyValues(final List<ClassifyValue> values,
          String inLanguage) {
    List<PdcPositionValue> positionValues = new ArrayList<PdcPositionValue>(values.size());
    for (ClassifyValue value : values) {
      positionValues.add(PdcPositionValue.fromClassifiyValue(value, inLanguage));
    }
    return positionValues;
  }

  protected PdcPositionEntity() {
  }

  private PdcPositionEntity(final URI classificationURI, String positionId) {
    this.id = positionId;
    this.uri = URI.create(classificationURI.toString() + "/" + positionId);
  }

  private void setPositionValues(final List<PdcPositionValue> values) {
    this.values.clear();
    this.values.addAll(values);
  }

  /**
   * Sets the synonyms for each value of this position from the specified thesaurus.
   * @param userThesaurus a user thesaurus from which synonyms can be get.
   * @throws ThesaurusException if an error occurs while getting the synonyms of this position's values.
   */
  protected void setSynonymsFrom(final UserThesaurusHolder userThesaurus) throws ThesaurusException {
    for (PdcPositionValue pdcPositionValue : values) {
      pdcPositionValue.setSynonyms(userThesaurus.getSynonymsOf(pdcPositionValue));
    }
  }
}
