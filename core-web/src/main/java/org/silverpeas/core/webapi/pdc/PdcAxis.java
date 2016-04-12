/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.webapi.pdc;

import org.silverpeas.core.pdc.thesaurus.model.ThesaurusException;
import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.pdc.pdc.model.Axis;
import org.silverpeas.core.pdc.pdc.model.UsedAxis;
import org.silverpeas.core.pdc.pdc.model.Value;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * An axis of the classification plan (named PdC). A PdC axis is defined by an identifier and it is
 * made up of a set of values. An axis in the PdC generally defines a concept (a meaning) or a
 * categorization of contents in Silverpeas. An axis of the PdC is a tree whose the leaves are the
 * values. The axis can have several branches, each of them representing then an hierarchic semantic
 * tree carrying a refinement of a meaning (of a value). For example, the values in the concept
 * 'geography' can be a tree in which each geographic area are divided into countries -> regions or
 * states -> departments or regions -> towns.
 */
@XmlRootElement
public class PdcAxis {

  public static final int PRIMARY_AXIS = 0;
  public static final int SECONDARY_AXIS = 1;
  @XmlElement(required = true)
  private int id;
  @XmlElement
  private boolean mandatory = false;
  @XmlElement(required = true)
  private String name;
  @XmlElement(required = true)
  private String originValue;
  @XmlElement(defaultValue = "")
  private String invariantValue;
  @XmlElement
  private boolean invariant = false;
  @XmlElement(defaultValue = "0")
  private int type = PRIMARY_AXIS;
  @XmlElement(required = true)
  private List<PdcAxisValueEntity> values = new ArrayList<>();

  /**
   * Creates a PdC axis from the specified configured axis for a specific component instance and in
   * which the terms are expressed in the specified language and whose synonyms are set with the
   * specified user thesaurus.
   *
   * @param axis an axis of the PdC potentially configured for a given Silverpeas component
   * instance.
   * @param inLanguage the language to use to translate the terms of the axis.
   * @param withThesaurus the thesaurus to use to set the synonyms of the axis values. Null if no
   * thesaurus are available or if no synonyms require to be set.
   * @return the PdcAxis instance corresponding to the specified business axis.
   * @throws ThesaurusException if an error occurs while using the thesaurus when setting up the PdC
   * axis.
   */
  public static PdcAxis fromTheUsedAxis(final UsedAxis axis, String inLanguage,
      final UserThesaurusHolder withThesaurus) throws ThesaurusException {
    String andOriginValue = axis._getBaseValuePath() + axis.getBaseValue() + "/";
    int axisType = (PdcManager.PRIMARY_AXIS.equals(axis._getAxisType()) ? PRIMARY_AXIS : SECONDARY_AXIS);
    List<PdcAxisValueEntity> theAxisValues =
        fromValues(axis._getAxisValues(), andOriginValue, inLanguage, withThesaurus);
    PdcAxis pdcAxis = new PdcAxis(axis.getAxisId(), axis._getAxisName(inLanguage)).
        ofType(axisType).
        withAsPdcAxisValues(theAxisValues, andOriginValue).
        withInvariance(axis.getVariant() == 0).
        withObligation(axis.getMandatory() == 1);
    if (isDefined(axis._getInvariantValue())) {
      pdcAxis.setInvariantValue(axis._getInvariantValue());
    }
    return pdcAxis;
  }

  /**
   * Creates a PdC axis from the specified configured axis for a specific component instance and in
   * which the terms are expressed in the specified language and whose synonyms are set with the
   * specified user thesaurus.
   *
   * @param axis an axis of the PdC potentially configured for a given Silverpeas component
   * instance.
   * @param inLanguage the language to use to translate the terms of the axis.
   * @param withThesaurus the thesaurus to use to set the synonyms of the axis values. Null if no
   * thesaurus are available or if no synonyms require to be set.
   * @return the PdcAxis instance corresponding to the specified business axis.
   * @throws ThesaurusException if an error occurs while using the thesaurus when setting up the PdC
   * axis.
   */
  public static PdcAxis fromTheAxis(final Axis axis, String inLanguage,
      final UserThesaurusHolder withThesaurus) throws ThesaurusException {
    String andOriginValue = "/0/";
    int axisType = (PdcManager.PRIMARY_AXIS.equals(axis.getAxisHeader().getAxisType()) ? PRIMARY_AXIS
        : SECONDARY_AXIS);
    List<PdcAxisValueEntity> theAxisValues =
        fromValues(axis.getValues(), andOriginValue, inLanguage, withThesaurus);
    PdcAxis pdcAxis = new PdcAxis(axis.getAxisHeader().getPK().getId(), axis.getAxisHeader().
        getName(inLanguage)).
        ofType(axisType).
        withAsPdcAxisValues(theAxisValues, andOriginValue);
    return pdcAxis;
  }

  /**
   * Gets the unique identifier of this axis in the PdC.
   *
   * @return the axis unique identifier.
   */
  public int getId() {
    return id;
  }

  /**
   * Is this axis is mandatory in the classification on the PdC.
   *
   * @return true if this axis is mandatory when classifying a content onto the PdC.
   */
  public boolean isMandatory() {
    return mandatory;
  }

  /**
   * Is this axis is invariant in the classification on the PdC. An invariant axis in a
   * classification is an axis that can have only a single possible value per classification,
   * whatever the positions onto the PdC.
   *
   * @return true if this axis in an invariant one when classifying a content onto the PdC.
   */
  public boolean isInvariant() {
    return invariant;
  }

  /**
   * Gets the name of this axis.
   *
   * @return the axis name.
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the value in the axis that is invariant.
   *
   * @return the identifier of the invariant value of the axis or an empty string if there is no
   * invariant value.
   */
  public String getInvariantValue() {
    return invariantValue;
  }

  /**
   * Gets the value that is set as the origin in this axis.
   *
   * @return the identifier of the axis origin value.
   */
  public String getOriginValue() {
    return originValue;
  }

  /**
   * Gets the type of this axis.
   *
   * @return the type: PRIMARY_AXIS or SECONDARY_AXIS.
   */
  public int getType() {
    return type;
  }

  /**
   * Gets the values that made up this axis.
   *
   * @return an unmodifiable list of axis' values.
   */
  public List<PdcAxisValueEntity> getValues() {
    return Collections.unmodifiableList(values);
  }

  /**
   * Adds the specified values into this axis and sets the value used as origin in this axis.
   *
   * @param originValueId the identifier of the origin value.
   * @param values the PdC values to set.
   * @return itself.
   */
  public PdcAxis withAsPdcAxisValues(final List<PdcAxisValueEntity> values, String originValueId) {
    this.originValue = originValueId;
    this.values.addAll(values);
    return this;
  }

  /**
   * Sets the type of the axis.
   *
   * @param axisType the type of the axis. It accepts a value among PRIMARY_AXIS and SECONDARY_AXIS.
   * @return itself.
   */
  public PdcAxis ofType(int axisType) {
    this.type = axisType;
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final PdcAxis other = (PdcAxis) obj;
    if (this.id != other.id) {
      return false;
    }
    if (this.mandatory != other.mandatory) {
      return false;
    }
    if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
      return false;
    }
    if ((this.originValue == null) ? (other.originValue != null)
        : !this.originValue.equals(other.originValue)) {
      return false;
    }
    if ((this.invariantValue == null) ? (other.invariantValue != null)
        : !this.invariantValue.equals(other.invariantValue)) {
      return false;
    }
    return !(this.values != other.values && (this.values == null || !this.values.equals(
        other.values)));
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 53 * hash + this.id;
    hash = 53 * hash + (this.mandatory ? 1 : 0);
    hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
    hash = 53 * hash + (this.originValue != null ? this.originValue.hashCode() : 0);
    hash = 53 * hash + (this.invariantValue != null ? this.invariantValue.hashCode() : 0);
    hash = 53 * hash + (this.values != null ? this.values.hashCode() : 0);
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder axisValuesArray = new StringBuilder("[");
    for (PdcAxisValueEntity value : getValues()) {
      axisValuesArray.append(value.toString()).append(", ");
    }
    if (axisValuesArray.length() > 1) {
      axisValuesArray.replace(axisValuesArray.length() - 2, axisValuesArray.length(), "]");
    } else {
      axisValuesArray.append("]");
    }
    return "PdcAxis{id=" + getId() + ", name=" + getName() + ", mandatory=" + isMandatory()
        + ", originValue=" + getOriginValue() + ", invariantValue=" + getInvariantValue()
        + ", values=" + axisValuesArray.toString() + '}';
  }

  private static List<PdcAxisValueEntity> fromValues(final List<Value> values,
      String originValueId,
      String inLanguage, final UserThesaurusHolder usingThesaurus) throws ThesaurusException {
    List<PdcAxisValueEntity> axisValues = new ArrayList<PdcAxisValueEntity>();
    for (Value value : values) {
      PdcAxisValueEntity axisValue = PdcAxisValueEntity.fromValue(value, inLanguage);
      if (isFather(axisValue.getId(), originValueId)) {
        axisValue.setAsAscendant();
        if (axisValue.getId().equals(originValueId)) {
          axisValue.setAsOriginValue();
        }
      } else if (!isChild(axisValue.getId(), originValueId)) {
        continue;
      }
      if (usingThesaurus != null) {
        axisValues.add(withSynonym(axisValue, usingThesaurus));
      } else {
        axisValues.add(axisValue);
      }
    }
    return axisValues;
  }

  private static PdcAxisValueEntity withSynonym(final PdcAxisValueEntity axisValue,
      final UserThesaurusHolder thesaurus) throws ThesaurusException {
    axisValue.setSynonyms(thesaurus.getSynonymsOf(axisValue));
    return axisValue;
  }

  private static boolean isChild(String path, String anotherPath) {
    return path.startsWith(anotherPath);
  }

  private static boolean isFather(String path, String anotherPath) {
    return anotherPath.startsWith(path);
  }

  private PdcAxis() {
  }

  private PdcAxis(int axisId, String axisName) {
    this.id = axisId;
    this.name = axisName;
  }

  private PdcAxis(String axisId, String axisName) {
    this.id = Integer.valueOf(axisId);
    this.name = axisName;
  }

  private PdcAxis withObligation(boolean mandatory) {
    this.mandatory = mandatory;
    return this;
  }

  private PdcAxis withInvariance(boolean invariant) {
    this.invariant = invariant;
    return this;
  }

  private void setInvariantValue(String invariantValueId) {
    this.invariantValue = invariantValueId;
  }
}
