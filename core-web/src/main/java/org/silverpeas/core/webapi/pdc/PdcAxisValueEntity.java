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

import org.silverpeas.core.pdc.pdc.model.Value;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * A value of a PdC's axis. A value of an axis is a term in the vocabulary of the concept
 * represented by the axis, and it is related to the base value of the axis; It can be either a
 * parent or a child of the axis base value. As the axis is a tree, its values can be refined by its
 * branches. As such, the value can also be a part of an axis branch whose its level attribute
 * indicates its position in the tree from the root.
 */
@XmlRootElement
public class PdcAxisValueEntity extends PdcValueEntity {

  private static final long serialVersionUID = -1689709605873362349L;
  @XmlElement(required = true)
  @NotNull
  @Size(min = 1)
  private String term;
  @XmlElement(required = true)
  @NotNull
  @Min(0)
  private int level;
  @XmlElement(defaultValue = "false")
  private boolean ascendant = false;
  @XmlElement(defaultValue = "false")
  private boolean origin = false;
  @XmlElement(defaultValue = "0")
  private int classifiedContentsCount = 0;

  /**
   * Creates a new value of a PdC axis from the specified business PdC value and expressed in the
   * specified language.
   *
   * @param value the business PdC value.
   * @param inLanguage the language of the user.
   * @return a PdcAxisValue instance.
   */
  public static PdcAxisValueEntity fromValue(final Value value, String inLanguage) {
    String axisId = value.getAxisId();
    if (!isDefined(axisId) || axisId.equalsIgnoreCase("unknown")) {
      axisId = value.getTreeId();
    }
    PdcAxisValueEntity axisValue = new PdcAxisValueEntity(
        withId(value.getFullPath()),
        withTerm(value.getName(inLanguage)),
        inAxis(axisId)).
        withClassifiedContents(value.getNbObjects()).
        inTree(value.getTreeId(), atLevel(value.getLevelNumber()));
    return axisValue;
  }

  /**
   * Gets the translated term represented by this value.
   *
   * @return the term translated into the user language.
   */
  public String getTerm() {
    return term;
  }

  /**
   * Gets the position level of this value in the semantic tree or 0. 0 means its level is at the
   * axis.
   *
   * @return the position level or 0 if it doesn't belong to a tree.
   */
  public int getLevel() {
    return level;
  }

  /**
   * Is this value is ascendant from the axis origin ? When a PdC is parameterized for a given
   * Silverpeas component instance, the origin of each axis can be refined. As such, values between
   * the default and the new axis origin become ascendant to the latter.
   *
   * @return true if this value is ascendant to the configured axis origin.
   */
  public boolean isAscendant() {
    return ascendant;
  }

  /**
   * Gets the count of contents that are classified with this value.
   *
   * @return the count of contents classified with this value.
   */
  public int getClassifiedContentsCount() {
    return classifiedContentsCount;
  }

  /**
   * Is this value is the root one of the axis? A value is the root of an axis when its identifier
   * is equal to /0/ where 0 is the node identifier of the root in an axis.
   *
   * @return true if this value is a root one, false otherwise.
   */
  @XmlTransient
  public boolean isRootValue() {
    return getId().equals("/0/");
  }

  /**
   * Is this value the origin of the axis?
   *
   * @return true if this axis value is the origin of the axis, false otherwise.
   */
  public boolean isOrigin() {
    return origin;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final PdcAxisValueEntity other = (PdcAxisValueEntity) obj;
    if (!super.equals(other)) {
      return false;
    }
    if ((this.term == null) ? (other.term != null) : !this.term.equals(other.term)) {
      return false;
    }
    if (this.level != other.level) {
      return false;
    }
    if (this.ascendant != other.ascendant) {
      return false;
    }
    return this.origin == other.origin;
  }

  @Override
  public int hashCode() {
    int hash;
    hash = 83 * super.hashCode();
    hash = 83 * hash + (this.term != null ? this.term.hashCode() : 0);
    hash = 83 * hash + this.level;
    hash = 83 * hash + (this.ascendant ? 1 : 0);
    hash = 83 * hash + (this.origin ? 1 : 0);
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder synonymArray = new StringBuilder("[");
    for (String synonym : getSynonyms()) {
      synonymArray.append(synonym).append(", ");
    }
    if (synonymArray.length() > 1) {
      synonymArray.replace(synonymArray.length() - 2, synonymArray.length(), "]");
    } else {
      synonymArray.append("]");
    }
    return "PdcAxisValue{id=" + getId() + ", axisId=" + getAxisId() + ", treeId=" + getTreeId()
        + ", term=" + getTerm() + ", level=" + getLevel() + ", ascendant=" + isAscendant()
        + ", origin=" + isOrigin() + ", classifiedContentsCount="
        + getClassifiedContentsCount() + ", synonyms=" + synonymArray.toString() + "}";
  }

  private static String withId(String id) {
    return id;
  }

  private static String withTerm(String term) {
    return term;
  }

  private static int inAxis(String axisId) {
    return Integer.valueOf(axisId);
  }

  private static int atLevel(int levelNumber) {
    return levelNumber;
  }

  protected PdcAxisValueEntity() {
    super();
  }

  /**
   * Sets this axis value as the origin of the axis.
   */
  protected void setAsOriginValue() {
    this.origin = true;
    this.ascendant = false;
  }

  /**
   * Sets this axis value as ascendant to the axis origin value.
   */
  protected void setAsAscendant() {
    this.ascendant = true;
    this.origin = false;
  }

  private PdcAxisValueEntity(String withId, String withTerm, int inAxisId) {
    super(withId, inAxisId);
    this.term = withTerm;
  }

  private PdcAxisValueEntity inTree(String treeId, int levelInTree) {
    setTreeId(treeId);
    this.level = levelInTree;
    return this;
  }

  private PdcAxisValueEntity withClassifiedContents(int count) {
    this.classifiedContentsCount = count;
    return this;
  }
}
