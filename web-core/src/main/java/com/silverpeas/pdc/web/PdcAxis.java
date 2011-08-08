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

import com.silverpeas.thesaurus.ThesaurusException;
import com.stratelia.silverpeas.pdc.model.UsedAxis;
import com.stratelia.silverpeas.pdc.model.Value;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import static com.silverpeas.util.StringUtil.*;

/**
 * An axis of the classification plan (named PdC). A PdC axis is defined by an identifier and it is
 * made up of a set of values.
 * 
 * An axis in the PdC generally defines a concept (a meaning) or a categorization of contents in
 * Silverpeas. An axis of the PdC is a tree whose the leaves are the values. The axis can have several
 * branches, each of them representing then an hierarchic semantic tree carrying a refinement of a
 * meaning (of a value).
 * For example, the values in the concept 'geography' can be a tree in which each geographic area are
 * divided into countries -> regions or states -> departments or regions -> towns.
 */
@XmlRootElement
public class PdcAxis {

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
  @XmlElement(required = true)
  private List<PdcAxisValue> values = new ArrayList<PdcAxisValue>();

  /**
   * Creates a PdC axis from the specified configured axis for a specific component instance and in
   * which the terms are expressed in the specified language and whose synonyms are set with the
   * specified user thesaurus.
   * @param axis an axis of the PdC potentially configured for a given Silverpeas component instance.
   * @param inLanguage the language to use to translate the terms of the axis.
   * @param withThesaurus the thesaurus to use to set the synonyms of the axis values. Null if no
   * thesaurus are available or if no synonyms require to be set.
   * @return the PdcAxis instance corresponding to the specified business axis.
   * @throws ThesaurusException if an error occurs while using the thesaurus when setting up the
   * PdC axis.
   */
  public static PdcAxis fromTheUsedAxis(final UsedAxis axis, String inLanguage,
          final UserThesaurusHolder withThesaurus) throws ThesaurusException {
    String andOriginValue = axis._getBaseValuePath() + axis.getBaseValue() + "/";
    List<PdcAxisValue> theAxisValues =
            fromValues(axis._getAxisValues(), andOriginValue, inLanguage, withThesaurus);
    PdcAxis pdcAxis = new PdcAxis(axis.getAxisId(), axis._getAxisName(inLanguage)).
            withAsPdcAxisValues(theAxisValues, andOriginValue).
            withInvariance(axis.getVariant() == 0).
            withObligation(axis.getMandatory() == 1);
    if (isDefined(axis._getInvariantValue())) {
      pdcAxis.setInvariantValue(axis._getInvariantValue());
    }
    return pdcAxis;
  }

  /**
   * Gets the unique identifier of this axis in the PdC.
   * @return the axis unique identifier.
   */
  public int getId() {
    return id;
  }

  /**
   * Is this axis is mandatory in the classification on the PdC.
   * @return true if this axis is mandatory when classifying a content onto the PdC.
   */
  public boolean isMandatory() {
    return mandatory;
  }

  /**
   * Is this axis is invariant in the classification on the PdC. An invariant axis in a
   * classification is an axis that can have only a single possible value per classification, 
   * whatever the positions onto the PdC.
   * @return true if this axis in an invariant one when classifying a content onto the PdC.
   */
  public boolean isInvariant() {
    return invariant;
  }

  /**
   * Gets the name of this axis.
   * @return the axis name.
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the value in the axis that is invariant.
   * @return the identifier of the invariant value of the axis or an empty string if there is no
   * invariant value.
   */
  public String getInvariantValue() {
    return invariantValue;
  }

  /**
   * Gets the value that is set as the origin in this axis.
   * @return the identifier of the axis origin value.
   */
  public String getOriginValue() {
    return originValue;
  }

  /**
   * Gets the values that made up this axis.
   * @return an unmodifiable list of axis' values.
   */
  public List<PdcAxisValue> getValues() {
    return Collections.unmodifiableList(values);
  }

  /**
   * Adds the specified values into this axis and sets the value used as origin in this axis.
   * @param originValueId the identifier of the origin value.
   * @param values the PdC values to set.
   * @return itself.
   */
  public PdcAxis withAsPdcAxisValues(final List<PdcAxisValue> values, String originValueId) {
    this.originValue = originValueId;
    this.values.addAll(values);
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
    if (this.values != other.values && (this.values == null || !this.values.equals(other.values))) {
      return false;
    }
    return true;
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
    for (PdcAxisValue value : getValues()) {
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

  private static List<PdcAxisValue> fromValues(final List<Value> values, String originValueId,
          String inLanguage, final UserThesaurusHolder usingThesaurus) throws ThesaurusException {
    List<PdcAxisValue> axisValues = new ArrayList<PdcAxisValue>();
    for (Value value : values) {
      PdcAxisValue axisValue = PdcAxisValue.fromValue(value, inLanguage);
      if (isFather(axisValue.getId(), originValueId)) {
        axisValue.setAsAscendant();
        if (axisValue.getId().equals(originValueId)) {
          axisValue.setAsOriginValue();
        }
      } else if(!isChild(axisValue.getId(), originValueId)) {
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

  private static PdcAxisValue withSynonym(final PdcAxisValue axisValue,
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
