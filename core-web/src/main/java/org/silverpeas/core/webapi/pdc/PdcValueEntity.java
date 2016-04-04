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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * A value in the classification plan (named PdC). It is the base class of the different
 * representations of a value in the PdC according to its use (a value in a position of a resource
 * on the PdC, a value of a PdC axis, ...).
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class PdcValueEntity implements Serializable {
  private static final long serialVersionUID = 7830451202912691112L;

  @XmlElement(required = true)
  private String id;
  @XmlElement(required = true)
  private int axisId;
  @XmlElement(defaultValue = "")
  private String treeId = "";
  @XmlElement
  private List<String> synonyms = new ArrayList<String>();

  /**
   * Gets the identifier of this value. The identifier is expressed in the form of an absolute path
   * relative to its axis, so it starts with the slash character. If the value is a single term of
   * the axis, the identifier does then contains only one node that is the identifier of the term.
   * If it belongs to an hierarchic semantic tree, then the identifier is the path of term
   * identifiers in that tree.
   * @return the value identifier.
   */
  public String getId() {
    return id;
  }

  /**
   * Gets the unique identifier of the term of this value. If the term belongs to a semantic tree
   * (not a single term), it is the one of the last term of the path in the tree.
   * @return the term identifier.
   */
  @XmlTransient
  public String getTermId() {
    String[] idNodes = id.split("/");
    return idNodes[idNodes.length - 1];
  }

  /**
   * Gets the unique identifier of the PdC's axis related by this value.
   * @return the PdC's axis identifier.
   */
  public int getAxisId() {
    return axisId;
  }

  /**
   * Gets the unique identifier of the tree to which this value belongs.
   * @return the tree identifier or an empty identifier if the value is a single term (and then
   * doesn't belong to an hierachical tree of terms)
   */
  public String getTreeId() {
    return treeId;
  }

  /**
   * Is this value is a node in an hierachical semantic tree? The value belong to a tree when it
   * defines a more exactness meaning for a given position value.
   * @return true if the value is a node in an hierarchical semantic tree, false otherwise.
   */
  public boolean belongToATree() {
    return isDefined(this.treeId) && !this.treeId.isEmpty();
  }

  /**
   * Gets the synonyms of this value according to a given thesaurus.
   * @return an unmodifiable list of synonyms to this value.
   */
  public List<String> getSynonyms() {
    return Collections.unmodifiableList(synonyms);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof PdcValueEntity)) {
      return false;
    }
    final PdcValueEntity other = (PdcValueEntity) obj;
    if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
      return false;
    }
    if (this.axisId != other.axisId) {
      return false;
    }
    if ((this.treeId == null) ? (other.treeId != null) : !this.treeId.equals(other.treeId)) {
      return false;
    }
    return !(this.synonyms != other.synonyms && (this.synonyms == null || !this.synonyms
        .equals(other.synonyms)));
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 89 * hash + (this.id != null ? this.id.hashCode() : 0);
    hash = 89 * hash + this.axisId;
    hash = 89 * hash + (this.treeId != null ? this.treeId.hashCode() : 0);
    hash = 89 * hash + (this.synonyms != null ? this.synonyms.hashCode() : 0);
    return hash;
  }

  protected PdcValueEntity() {
  }

  protected PdcValueEntity(String id, int axisId) {
    this.id = id;
    this.axisId = axisId;
  }

  protected void setTreeId(String treeId) {
    this.treeId = treeId;
  }

  protected void setSynonyms(final Collection<String> synonyms) {
    this.synonyms.clear();
    this.synonyms.addAll(synonyms);
  }

}
