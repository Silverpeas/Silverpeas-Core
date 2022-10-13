/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.pdc.pdc.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.TableGenerator;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * A position of a content on some axis of the classification plan (named PdC). The positions of a
 * given content define its classification on the PdC. A position on the PdC's axis represents an
 * atomic semantic information about its content. As such, it can be made up of one or more values
 * of axis. As a PdC axis is defined by an hierarchic tree of terms, each of them being a value in
 * the concept represented by the axis, a value in a position is defined by its path in the tree
 * from the root; the root being one of the base value of the axis. For example, for a position on
 * the axis representing the concept of geography, a possible value can be
 * "France / Rhônes-Alpes / Isère / Grenoble" where Grenoble is the last term of the axis valuation.
 */
@Entity
public class PdcPosition implements Serializable {

  private static final long serialVersionUID = 665144316569539208L;

  @Id
  @TableGenerator(name = "UNIQUE_ID_GEN", table = "uniqueId", pkColumnName = "tablename", valueColumnName = "maxId", pkColumnValue = "PdcPosition", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "UNIQUE_ID_GEN")
  private Long id;

  @ManyToMany(fetch = FetchType.EAGER)
  @NotNull
  @Size(min = 1)
  @Valid
  private Set<PdcAxisValue> axisValues = new HashSet<>();

  public PdcPosition() {
  }

  public String getId() {
    if (id != null) {
      return id.toString();
    } else {
      return null;
    }
  }

  /**
   * Sets the specified identifier to this position. If the identifier is null or empty, no
   * identifier is set.
   * @param id the unique identifier of the position.
   * @return itself.
   */
  public PdcPosition withId(String id) {
    if (isDefined(id)) {
      this.id = Long.valueOf(id);
    }
    return this;
  }

  /**
   * Is this position on the PdC empty?
   * @return true if this positions has no valuation in at least one of the PdC'axis, false
   * otherwise.
   */
  public boolean isEmpty() {
    return getValues().isEmpty();
  }

  /**
   * Gets the values of this position on the axis of the PdC. You can add or remove any values from
   * the returned set.
   * @return a set of PdC axis values.
   */
  public Set<PdcAxisValue> getValues() {
    return axisValues;
  }

  /**
   * Adds the specified value for this position.
   * @param value the value to add.
   * @return itself.
   */
  public PdcPosition withValue(final PdcAxisValue value) {
    axisValues.add(value);
    return this;
  }

  /**
   * Adds the specified collection of values for this position.
   * @param values the collection of values to add.
   * @return itself.
   */
  public PdcPosition withValues(final Collection<PdcAxisValue> values) {
    axisValues.addAll(values);
    return this;
  }

  protected PdcPosition copy() {
    PdcPosition position = new PdcPosition();
    for (PdcAxisValue aValue : axisValues) {
      position.getValues().add(aValue.copy());
    }
    return position;
  }

  protected PdcPosition(long id) {
    this.id = id;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final PdcPosition that = (PdcPosition) o;
    return Objects.equals(id, that.id) && Objects.equals(axisValues, that.axisValues);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, axisValues);
  }

  @Override
  public String toString() {
    return "PdcPosition{" + "id=" + id + ", axisValues=" + axisValues + '}';
  }

  /**
   * Converts this position to a ClassifyPosition instance. This method is for compatibility with
   * the old way to manage the classification.
   * @return a ClassifyPosition instance.
   * @throws PdcException if an error occurs while transforming this position.
   */
  public ClassifyPosition toClassifyPosition() throws PdcException {
    ClassifyPosition position = new ClassifyPosition(new ArrayList<>());
    if (getId() != null) {
      position.setPositionId(Integer.parseInt(getId()));
    }
    for (PdcAxisValue pdcAxisValue : getValues()) {
      position.getValues().add(pdcAxisValue.toClassifyValue());
    }
    return position;
  }

  /**
   * Gets the values of the specified axis that are present in this position.
   * @param axisId the unique identifier of the axis.
   * @return a set of values of the specified axis in this position.
   */
  public Set<PdcAxisValue> getValuesOfAxis(String axisId) {
    Set<PdcAxisValue> valuesOfTheAxis = new HashSet<>();
    for (PdcAxisValue pdcAxisValue : getValues()) {
      if (pdcAxisValue.getAxisId().equals(axisId)) {
        valuesOfTheAxis.add(pdcAxisValue);
      }
    }
    return valuesOfTheAxis;
  }
}
