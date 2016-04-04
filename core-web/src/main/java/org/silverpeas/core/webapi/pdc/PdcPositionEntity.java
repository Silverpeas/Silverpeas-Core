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

package org.silverpeas.core.webapi.pdc;

import org.silverpeas.core.pdc.pdc.model.PdcAxisValue;
import org.silverpeas.core.pdc.pdc.model.PdcPosition;
import org.silverpeas.core.pdc.thesaurus.model.ThesaurusException;
import static org.silverpeas.core.util.StringUtil.isDefined;
import org.silverpeas.core.webapi.base.WebEntity;
import org.silverpeas.core.pdc.pdc.model.ClassifyPosition;
import org.silverpeas.core.pdc.pdc.model.ClassifyValue;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Web representation of the position of a Silverpeas's resource in the classification plan
 * (PdC). As such, it publishes only some of the business classification's position attributes. A
 * position of a resource on the PdC defines an atomic semantic information about its content. As
 * such, it can be a single or a set of values in the different PdC's axis. An axis value can be a
 * single semantic term or a hierarchical tree of semantic terms carrying a deeper exactness about
 * the concept's value. For example, in a geographic axis, the value France can be a tree in which
 * it is splited into regions, departments, towns, and so on, each of theses terms giving a more
 * accuracy about a geographic position (that is the semantic concept of the geographic axis).
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PdcPositionEntity implements WebEntity {

  private static final long serialVersionUID = 6314816355055147378L;
  @XmlElement(required = true) @NotNull @Size(min=1)
  private List<PdcPositionValueEntity> values = new ArrayList<>();
  @XmlElement(defaultValue = "")
  private URI uri;
  @XmlElement(defaultValue = "")
  private String id;

  /**
   * Creates a Web entity representing a position on the PdC from the specified business
   * classification position. It is expressed in the specified language and it is located in the PdC
   * classification identified by the specified URI.
   * @param position the ClassifyPosition instance the web entity should represent.
   * @param inLanguage the language in which the position should expressed.
   * @param inClassificationByURI the URI identifying the PdC classification to which the position
   * belongs.
   * @return a PdcPositionEntity instance.
   */
  public static PdcPositionEntity fromClassifyPosition(final ClassifyPosition position,
      String inLanguage, final URI inClassificationByURI) {
    String positionId = String.valueOf(position.getPositionId());
    return new PdcPositionEntity(
        fromClassifyValues(position.getListClassifyValue(), inLanguage)).withURI(
        inClassificationByURI, positionId).
        withId(positionId);
  }

  /**
   * Creates a Web entity representing a position on the PdC from the specified business
   * classification position. It is expressed in the specified language and it is located in the PdC
   * classification identified by the specified URI.
   * @param position the position on the PdC the web entity should represent.
   * @param inLanguage the language in which the position should expressed.
   * @param inClassificationByURI the URI identifying the PdC classification to which the position
   * belongs.
   * @return a PdcPositionEntity instance.
   */
  public static PdcPositionEntity fromPdcPosition(final PdcPosition position, String inLanguage,
      final URI inClassificationByURI) {
    return new PdcPositionEntity(fromPdcAxisValues(position.getValues(), inLanguage)).withURI(
        inClassificationByURI, position.getId()).withId(position.getId());
  }

  /**
   * Creates a web entity representing a new position on the Pdc and that is valued with the
   * specified PdC axis values.
   * @param values a list of representations of PdC axis values.
   * @return a PdcPositionEntity instance.
   */
  public static PdcPositionEntity createNewPositionWith(final List<PdcPositionValueEntity> values) {
    return new PdcPositionEntity(values);
  }

  /**
   * Gets the business classification position this web entity represents.
   * @return a ClassifyPosition instance represented by this web entity.
   */
  public ClassifyPosition toClassifyPosition() {
    List<ClassifyValue> classifyValues = new ArrayList<>(this.values.size());
    for (PdcPositionValueEntity value : values) {
      classifyValues.add(value.toClassifyValue());
    }
    int positionId = -1;
    if (isDefined(id)) {
      positionId = Integer.valueOf(id);
    }
    return new ClassifyPosition(positionId, classifyValues);
  }

  public PdcPosition toPdcPosition() {
    PdcPosition position = new PdcPosition().withId(id);
    for (PdcPositionValueEntity valueEntity : values) {
      position.getValues().add(valueEntity.toPdcAxisValue());
    }
    return position;
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
  public List<PdcPositionValueEntity> getPositionValues() {
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
    return !(this.values != other.values && (this.values == null || !this.values
        .equals(other.values)));
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
    for (PdcPositionValueEntity pdcPositionValue : getPositionValues()) {
      valueArray.append(pdcPositionValue.toString()).append(", ");
    }
    if (valueArray.length() > 1) {
      valueArray.replace(valueArray.length() - 2, valueArray.length(), "]");
    } else {
      valueArray.append("]");
    }
    String uriAsStr = "";
    if (getURI() != null) {
      uriAsStr = getURI().toString();
    }
    return "PdcPositionEntity{id=" + getId() + ", uri=" + uriAsStr + ", values="
        + valueArray.toString() + "}";
  }

  private static List<PdcPositionValueEntity> fromClassifyValues(final List<ClassifyValue> values,
      String inLanguage) {
    List<PdcPositionValueEntity> positionValues = new ArrayList<>(values.size());
    for (ClassifyValue value : values) {
      positionValues.add(PdcPositionValueEntity.fromClassifiyValue(value, inLanguage));
    }
    return positionValues;
  }

  private static List<PdcPositionValueEntity> fromPdcAxisValues(
      final Collection<PdcAxisValue> values,
      String inLanguage) {
    List<PdcPositionValueEntity> positionValues = new ArrayList<>(values.size());
    for (PdcAxisValue value : values) {
      positionValues.add(PdcPositionValueEntity.fromPdcAxisValue(value, inLanguage));
    }
    return positionValues;
  }

  private PdcPositionEntity() {
  }

  protected PdcPositionEntity(final List<PdcPositionValueEntity> values) {
    setPositionValues(values);
  }

  /**
   * Sets the synonyms for each value of this position from the specified thesaurus.
   * @param userThesaurus a user thesaurus from which synonyms can be get.
   * @throws ThesaurusException if an error occurs while getting the synonyms of this position's
   * values.
   */
  protected void setSynonymsFrom(final UserThesaurusHolder userThesaurus) throws ThesaurusException {
    for (PdcPositionValueEntity pdcPositionValue : values) {
      pdcPositionValue.setSynonyms(userThesaurus.getSynonymsOf(pdcPositionValue));
    }
  }

  private PdcPositionEntity withURI(final URI baseURI, String positionId) {
    final String positionPath = "/" + positionId;
    final String query = baseURI.getRawQuery();
    if (!isDefined(query)) {
      this.uri = URI.create(baseURI.toString() + positionPath);
    } else {
      String requestURI = baseURI.toString();
      this.uri = URI.create(requestURI.substring(0, requestURI.lastIndexOf("?")) + positionPath
          + "?" + query);
    }
    return this;
  }

  private PdcPositionEntity withId(final String id) {
    this.id = id;
    return this;
  }

  private void setPositionValues(final List<PdcPositionValueEntity> values) {
    this.values.clear();
    this.values.addAll(values);
  }
}
