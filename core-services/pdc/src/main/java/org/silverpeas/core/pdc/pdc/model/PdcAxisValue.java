/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.pdc.pdc.model;

import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.pdc.tree.model.TreeNode;
import org.silverpeas.core.persistence.datasource.model.CompositeEntityIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.BasicJpaEntity;

import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * A value of one of the PdC's axis. A value belongs to an axis. An axis represents a given concept
 * for which it defines an hierarchic tree of semantic terms belonging to the concept. A value of
 * an axis is then the path from the axis origin down to a given node of the tree, where each node
 * is a term refining or specifying the parent term a little more. For example, for an axis
 * representing the concept of geography, one possible value can be
 * "France / Rhônes-Alpes / Isère / Grenoble" where France, Rhônes-Alpes, Isère and Grenoble are
 * each a term (thus a node) in the axis.
 * "France" is another value, parent of the above one, and that is also a base value of the axis as
 * it has no parent (one of the root values of the axis).
 */
@Entity
@Table(name = "pdcaxisvalue")
@NamedQuery(name = "findByAxisId", query = "from PdcAxisValue where axisId = :axisId")
public class PdcAxisValue extends BasicJpaEntity<PdcAxisValue, PdcAxisValuePk> {

  private static final long serialVersionUID = 2345886411781136417L;
  @Transient
  private transient TreeNode treeNode;
  @Transient
  private transient TreeNodeList treeNodeParents = new TreeNodeList();

  protected PdcAxisValue() {
  }

  /**
   * Creates a value of a PdC's axis from the specified tree node. Currently, an axis of the PdC is
   * persisted as an hierarchical tree in which each node is a value of the axis.
   * @param treeNode the current persistence representation of the axis value.
   * @return a PdC axis value.
   */
  public static PdcAxisValue aPdcAxisValueFromTreeNode(final TreeNode treeNode) {
    try {
      List<? extends TreeNode> parents = null;
      if (treeNode.hasFather()) {
        PdcManager pdcManager = getPdcManager();
        parents = pdcManager.getFullPath(treeNode.getFatherId(), treeNode.getTreeId());
      }
      return new PdcAxisValue().fromTreeNode(treeNode).withAsTreeNodeParents(parents).
          inAxisId(treeNode.getTreeId());
    } catch (PdcException ex) {
      throw new PdcRuntimeException(ex);
    }
  }

  /**
   * Creates a value of a PdC's axis from the specified value information. Currently, an axis of the
   * PdC is persisted as an hierarchical tree in which each node is a value of the axis. The
   * parameters refers the unique identifier of the node and in the tree related to the axis
   * identifier.
   * @param valueId the unique identifier of the existing value.
   * @param axisId the unique identifier of the axis the value belongs to.
   * @return a PdC axis value.
   */
  public static PdcAxisValue aPdcAxisValue(String valueId, String axisId) {
    return new PdcAxisValue().setId(
        valueId + CompositeEntityIdentifier.COMPOSITE_SEPARATOR + axisId);
  }

  /**
   * Gets the unique identifier of the axis to which this value belongs to.
   * @return the unique identifier of the axis value.
   */
  public String getAxisId() {
    return getNativeId().getAxisId().toString();
  }

  /**
   * Gets the unique value identifier.
   * @return the unique value identifier.
   */
  public String getValueId() {
    return getNativeId().getValueId().toString();
  }


  /**
   * Gets all the values into which this one can be refined or specifying in a little more. Theses
   * values are the children of this one in the semantic tree represented by the axis to which this
   * value belongs.
   * @return an unmodifiable set of values that are children of this one. If this value is a leaf,
   * then an empty set is returned.
   */
  public Set<PdcAxisValue> getChildValues() {
    try {
      Set<PdcAxisValue> children = new HashSet<>();
      List<String> childNodeIds = getPdcManager().getDaughterValues(getAxisId(), getValueId());
      for (String aNodeId : childNodeIds) {
        children.add(aPdcAxisValue(aNodeId, getAxisId()));
      }
      return Collections.unmodifiableSet(children);
    } catch (PdcException ex) {
      throw new PdcRuntimeException(ex);
    }
  }

  /**
   * Gets the value this one refines or specifies a little more. The returned value is the parent
   * of this one in the semantic tree represented by the axis to which this value belongs.
   * @return the axis value parent of this one or null if this value has no parent (in that case,
   * this value is a base one).
   */
  public PdcAxisValue getParentValue() {
    final PdcAxisValue parent;
    TreeNode node = getTreeNode();
    if (node.hasFather()) {
      int lastNodeIndex = treeNodeParents.size() - 1;
      TreeNode aTreeNode = treeNodeParents.get(lastNodeIndex);
      String valueId = aTreeNode.getPK().getId();
      String axisId = getAxisId();
      PdcAxisValue pdcAxisValue = new PdcAxisValue().setId(
          valueId + CompositeEntityIdentifier.COMPOSITE_SEPARATOR + axisId);
      parent =
          pdcAxisValue.fromTreeNode(aTreeNode).inAxisId(getAxisId())
              .withAsTreeNodeParents(treeNodeParents.subList(0, lastNodeIndex));
    } else {
      parent = null;
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
    // as the root in the tree represents the axis itself, a base value is a direct children of the
    // root.
    return getTreeNodeParents().size() <= 1;
  }

  /**
   * Gets the meaning carried by this value. The meaning is in fact the complete path of terms that
   * made this value. For example, in an axis representing the geography, the meaning of the value
   * "France / Rhônes-Alpes / Isère" is "Geography / France / Rhônes-Alpes / Isère".
   * @return the meaning carried by this value, in other words the complete path of this value.
   */
  public String getMeaning() {
    return getMeaningTranslatedIn("");
  }

  /**
   * Gets the meaning carried by this value translated in the specified language. The meaning is in
   * fact the complete path of translated terms that made this value. For example, in an axis
   * representing the geography, the meaning of the value "France / Rhônes-Alpes / Isère" is in
   * french "Geographie / France / Rhônes-Alpes / Isère".
   * @return the meaning carried by this value, in other words the complete path of this value
   * translated in the specified language. If no such translations exist, then the result is
   * equivalent to the call of the getMeaning() method.
   */
  public String getMeaningTranslatedIn(String language) {
    final String meaning;
    final String theLanguage = (language == null ? "" : language);
    PdcAxisValue theParent = getParentValue();
    if (theParent != null) {
      meaning = theParent.getMeaningTranslatedIn(theLanguage) + " / ";
    } else {
      meaning = "";
    }
    return meaning + getTerm();
  }

  /**
   * Gets the path of this value from the root value (that is a base value of the axis). The path
   * is
   * made up of the identifiers of each parent value; for example : /0/2/3
   * @return the path of its value.
   */
  public String getValuePath() {
    return getTreeNode().getPath() + getValueId();
  }

  /**
   * Copies this value into another one. In fact, the attributes of the copy refers to the same
   * object referred by the attributes of this instance.
   * @return a copy of this PdC axis value.
   */
  protected PdcAxisValue copy() {
    PdcAxisValue copy = PdcAxisValue.aPdcAxisValue(getValueId(), getAxisId());
    copy.treeNode = treeNode;
    copy.treeNodeParents = treeNodeParents;
    return copy;
  }

  /**
   * Gets the axis to which this value belongs to and that is used to classify contents on the PdC.
   * @return a PdC axis configured to be used in the classification of contents.
   */
  protected UsedAxis getUsedAxis() {
    try {
      PdcManager pdc = getPdcManager();
      UsedAxis usedAxis = pdc.getUsedAxis(getAxisId());
      AxisHeader axisHeader = pdc.getAxisHeader(getAxisId());
      usedAxis._setAxisHeader(axisHeader);
      usedAxis._setAxisName(axisHeader.getName());
      return usedAxis;
    } catch (PdcException ex) {
      throw new PdcRuntimeException(ex);
    }
  }

  /**
   * Gets the persisted representation of this axis value. By the same way, the parents of this
   * tree node are also set.
   * @return a tree node representing this axis value in the persistence layer.
   */
  protected TreeNode getTreeNode() {
    if (this.treeNode == null || (this.treeNodeParents == null && this.treeNode.hasFather())) {
      loadTreeNodes();
    }
    return this.treeNode;
  }

  protected void setId(long id) {
    getNativeId().setValueId(id);
  }

  protected PdcAxisValue withId(String id) {
    getNativeId().setValueId(Long.valueOf(id));
    return this;
  }

  protected PdcAxisValue inAxisId(String axisId) {
    getNativeId().setAxisId(Long.valueOf(axisId));
    return this;
  }

  protected PdcAxisValue fromTreeNode(final TreeNode treeNode) {
    getNativeId().setValueId(Long.valueOf(treeNode.getPK().getId()));
    this.treeNode = treeNode;
    return this;
  }

  protected PdcAxisValue withAsTreeNodeParents(final List<? extends TreeNode> parents) {
    this.treeNodeParents.setAll(parents);
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
    final PdcAxisValue other = (PdcAxisValue) obj;
    if (this.getNativeId().getValueId() != other.getNativeId().getValueId() &&
        (this.getNativeId().getValueId() == null ||
            !this.getNativeId().getValueId().equals(other.getNativeId().getValueId()))) {
      return false;
    }
    return this.getNativeId().getAxisId() == other.getNativeId().getAxisId() ||
        (this.getNativeId().getAxisId() != null &&
            !this.getNativeId().getAxisId().equals(other.getNativeId().getAxisId()));
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 89 * hash +
        (this.getNativeId().getValueId() != null ? this.getNativeId().getValueId().hashCode() : 0);
    hash = 89 * hash +
        (this.getNativeId().getAxisId() != null ? this.getNativeId().getAxisId().hashCode() : 0);
    return hash;
  }

  @Override
  public String toString() {
    return "";
  }

  /**
   * Converts this PdC axis value to a ClassifyValue instance. This method is for compatibility
   * with the old way to manage the classification.
   * @return a ClassifyValue instance.
   * @throws PdcException if an error occurs while transforming this value into a ClassifyValue
   * instance.
   */
  public ClassifyValue toClassifyValue() {
    ClassifyValue value = new ClassifyValue(Integer.valueOf(getAxisId()), getValuePath() + "/");
    List<Value> fullPath = new ArrayList<>();
    for (TreeNode aTreeNode : getTreeNodeParents()) {
      fullPath.add(new Value(aTreeNode.getPK().getId(), aTreeNode.getTreeId(), aTreeNode.getName(),
          aTreeNode.getDescription(), aTreeNode.getCreationDate(), aTreeNode.getCreatorId(),
          aTreeNode.getPath(), aTreeNode.getLevelNumber(), aTreeNode.
          getOrderNumber(), aTreeNode.getFatherId()));
    }
    TreeNode lastValue = getTreeNode();
    fullPath.add(new Value(lastValue.getPK().getId(), lastValue.getTreeId(), lastValue.getName(),
        lastValue.getDescription(), lastValue.getCreationDate(), lastValue.getCreatorId(),
        lastValue.getPath(), lastValue.getLevelNumber(), lastValue.getOrderNumber(),
        lastValue.getFatherId()));
    value.setFullPath(fullPath);
    return value;
  }

  protected TreeNodeList getTreeNodeParents() {
    if (this.treeNodeParents == null) {
      loadTreeNodes();
    }
    return this.treeNodeParents;
  }

  private void loadTreeNodes() {
    try {
      PdcManager pdc = getPdcManager();
      String treeId = pdc.getTreeId(getAxisId());
      List<? extends TreeNode> paths = pdc.getFullPath(getValueId(), treeId);
      int lastNodeIndex = paths.size() - 1;
      this.treeNode = paths.get(lastNodeIndex);
      this.treeNodeParents.setAll(paths.subList(0, lastNodeIndex));
    } catch (PdcException ex) {
      throw new PdcRuntimeException(ex);
    }
  }

  private static PdcManager getPdcManager() {
    return PdcManager.get();
  }

  private class TreeNodeList implements Iterable<TreeNode> {

    private final List<TreeNode> treeNodes = new ArrayList<>();

    public int size() {
      return treeNodes.size();
    }

    public TreeNode get(final int index) {
      return treeNodes.get(index);
    }

    public List<TreeNode> subList(final int fromIndex, final int toIndex) {
      return treeNodes.subList(fromIndex, toIndex);
    }

    public void setAll(final Collection<? extends TreeNode> nodes) {
      this.treeNodes.clear();
      this.treeNodes.addAll(nodes);
    }

    @Override
    public Iterator<TreeNode> iterator() {
      return this.treeNodes.iterator();
    }

    @Override
    public void forEach(final Consumer<? super TreeNode> action) {
      this.treeNodes.forEach(action);
    }

    @Override
    public Spliterator<TreeNode> spliterator() {
      return this.treeNodes.spliterator();
    }
  }
}
