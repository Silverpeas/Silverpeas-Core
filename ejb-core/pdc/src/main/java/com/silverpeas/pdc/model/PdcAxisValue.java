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
package com.silverpeas.pdc.model;

import com.silverpeas.pdc.PdcServiceFactory;
import com.stratelia.silverpeas.pdc.control.PdcBm;
import com.stratelia.silverpeas.pdc.model.ClassifyValue;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.pdc.model.PdcRuntimeException;
import com.stratelia.silverpeas.pdc.model.UsedAxis;
import com.stratelia.silverpeas.treeManager.model.TreeNode;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

/**
 * A value of one of the PdC's axis.
 * 
 * A value belongs to an axis. An axis represents a given concept for which it defines an hierarchic
 * tree of semantic terms belonging to the concept. A value of an axis is then the path from the
 * axis origin down to a given node of the tree, where each node is a term refining or specifying 
 * the parent term a little more.
 * 
 * For example, for an axis representing the concept of geography, one possible value can be
 * "France / Rhônes-Alpes / Isère / Grenoble" where France, Rhônes-Alpes, Isère and Grenoble are
 * each a term (thus a node) in the axis. "France" is another value, parent of the above one, and
 * that is also a base value of the axis as it has no parent (one of the root values of the axis).
 */
@Entity
public class PdcAxisValue implements Serializable {

  private static final long serialVersionUID = 2345886411781136417L;
  @Id
  private Long id;
  private Long axisId;
  @Transient
  private TreeNode treeNode;

  /**
   * Creates a value of a PdC's axis from the specified tree node.
   * Currently, an axis of the PdC is persited as an hierarchical tree in which each node is a value
   * of the axis.
   * @param treeNode the current persistence representation of the axis value.
   * @return a PdC axis value.
   */
  public static PdcAxisValue aPdcAxisValueFromTreeNode(final TreeNode treeNode) {
    return new PdcAxisValue().fromTreeNode(treeNode).inAxisId(treeNode.getTreeId());
  }

  public String getId() {
    return id.toString();
  }

  /**
   * Gets the unique identifier of the axis to which this value belongs to.
   * @return the unique identifier of the axis value.
   */
  public String getAxisId() {
    return axisId.toString();
  }

  /**
   * Gets all the values into which this one can be refined or specifying in a little more.
   * Theses values are the children of this one in the semantic tree represented by the axis to
   * which this value belongs.
   * @return an unmodifiable set of values that are children of this one. If this value is a leaf,
   * then an empty set is returned.
   */
  public Set<PdcAxisValue> getChildValues() {
    try {
      Set<PdcAxisValue> children = new HashSet<PdcAxisValue>();
      List<String> childNodeIds = getPdcBm().getDaughterValues(getAxisId(), getId());
      for (String aNodeId : childNodeIds) {
        children.add(new PdcAxisValue().withId(aNodeId).inAxisId(getAxisId()));
      }
      return Collections.unmodifiableSet(children);
    } catch (PdcException ex) {
      throw new PdcRuntimeException(getClass().getSimpleName() + ".getChildValues()",
                SilverpeasException.ERROR, ex.getMessage(), ex);
    }
  }

  /**
   * Gets the value this one refines or specifies a little more. The returned value is the parent of
   * this one in the semantic tree represented by the axis to which this value belongs.
   * @return the axis value parent of this one or null if this value has no parent (in that case,
   * this value is a base one).
   */
  public PdcAxisValue getParentValue() {
    PdcAxisValue parent = null;
    TreeNode node = getTreeNode();
    if (node.hasFather()) {
      parent = new PdcAxisValue().withId(node.getFatherId()).inAxisId(getAxisId());
    }
    return parent;
  }

  /**
   * Gets the term carried by this value.
   * @return the term of the value.
   */
  public String getTerm() {
    return getTreeNode().getName();
  }

  /**
   * Gets the term carried by this value and translated in the specified language.
   * @param language the language in which the term should be translated.
   * @return the term translated in the specified language. If no such translation exists, then
   * return the default term as get by calling getTerm() method.
   */
  public String getTermTranslatedIn(String language) {
    return getTreeNode().getName(language);
  }

  /**
   * Is this value is a base one?
   * @return true if this value is an axis base value.
   */
  public boolean isBaseValue() {
    return getParentValue() == null;
  }

  /**
   * Gets the meaning carried by this value. The meaning is in fact the complete path of terms
   * that made this value. For example, in an axis representing the geography, the meaning of 
   * the value "France / Rhônes-Alpes / Isère" is "Geography / France / Rhônes-Alpes / Isère".
   * @return the meaning carried by this value, in other words the complete path of this value.
   */
  public String getMeaning() {
    return getMeaningTranslatedIn("");
  }

  /**
   * Gets the meaning carried by this value translated in the specified language.
   * The meaning is in fact the complete path of translated terms that made this value.
   * For example, in an axis representing the geography, the meaning of the value 
   * "France / Rhônes-Alpes / Isère" is in french "Geographie / France / Rhônes-Alpes / Isère".
   * @return the meaning carried by this value, in other words the complete path of this value
   * translated in the specified language. If no such translations exist, then the result is
   * equivalent to the call of the getMeaning() method.
   */
  public String getMeaningTranslatedIn(String language) {
    String meaning, theLanguage = (language == null ? "" : language);
    PdcAxisValue theParent = getParentValue();
    if (theParent.isBaseValue()) {
      UsedAxis axis = getUsedAxis();
      meaning = axis._getAxisName(theLanguage);
    } else {
      meaning = theParent.getMeaningTranslatedIn(theLanguage) + " / " + getTerm();
    }
    return meaning;
  }

  /**
   * Gets the path of this value from the root value (that is a base value of the axis).
   * The path is made up of the identifiers of each parent value; for example : /0/2/3
   * @return the path of its value.
   */
  public String getValuePath() {
    return getTreeNode().getPath() + "/" + getId();
  }

  protected PdcAxisValue() {
  }

  /**
   * Gets the axis to which this value belongs to and that is used to classify contents on the PdC.
   * @return a PdC axis configured to be used in the classification of contents.
   */
  protected UsedAxis getUsedAxis() {
    try {
      PdcBm pdc = getPdcBm();
      return pdc.getUsedAxis(getAxisId());
    } catch (PdcException ex) {
      throw new PdcRuntimeException(getClass().getSimpleName() + ".getUsedAxis()",
              SilverpeasException.ERROR, ex.getMessage(), ex);
    }
  }

  /**
   * Gets the persisted representation of this axis value.
   * @return a tree node representing this axis value in the persistence layer.
   */
  protected TreeNode getTreeNode() {
    if (this.treeNode == null) {
      try {
        PdcBm pdc = getPdcBm();
        this.treeNode = pdc.getValue(getAxisId(), getId());
      } catch (PdcException ex) {
        throw new PdcRuntimeException(getClass().getSimpleName() + ".getTreeNode()",
                SilverpeasException.ERROR, ex.getMessage(), ex);
      }
    }
    return this.treeNode;
  }

  protected void setId(long id) {
    this.id = id;
  }
  
  protected PdcAxisValue withId(String id) {
    this.id = Long.valueOf(id);
    return this;
  }

  protected PdcAxisValue inAxisId(String axisId) {
    this.axisId = Long.valueOf(axisId);
    return this;
  }

  protected PdcAxisValue fromTreeNode(final TreeNode treeNode) {
    this.id = Long.valueOf(treeNode.getPK().getId());
    this.treeNode = treeNode;
    return this;
  }

  @Override
  public String toString() {
    return "PdcAxisValue{" + "id=" + getId() + ", parent=" + getParentValue() + ", term="
            + getTerm() + ", axisId=" + getAxisId() + '}';
  }

  /**
   * Converts this PdC axis value to a ClassifyValue instance. This method is for compatibility with
   * the old way to manage the classification.
   * @return a ClassifyValue instance.
   * @throws PdcException if an error occurs while transforming this value into a ClassifyValue
   * instance.
   */
  public ClassifyValue toClassifyValue() throws PdcException {
    ClassifyValue value = new ClassifyValue(Integer.valueOf(getAxisId()), getValuePath());
    PdcBm pdcBm = getPdcBm();
    String treeId = getAxisId();
    value.setFullPath(pdcBm.getFullPath(getId(), treeId));
    return value;
  }

  private PdcBm getPdcBm() {
    return PdcServiceFactory.getFactory().getPdcManager();
  }
}
