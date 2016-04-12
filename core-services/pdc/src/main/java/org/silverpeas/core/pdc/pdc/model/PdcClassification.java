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
package org.silverpeas.core.pdc.pdc.model;

import org.silverpeas.core.pdc.pdc.model.constraints.UniquePositions;
import org.silverpeas.core.persistence.datasource.model.identifier.UniqueLongIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.AbstractJpaCustomEntity;
import org.silverpeas.core.exception.SilverpeasException;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * A classification of a content in Silverpeas on the classification plan (named PdC). A
 * classification of a content is made up of one or more positions on the axis of the PdC. Each
 * position consists of one or several values on some PdC's axis. A classification cannot have two
 * or more identical positions; each of them must be unique. It can also represent, for a Silverpeas
 * component instance or for a node in a component instance, a predefined classification with which
 * any published contents can be classified on the PdC. In this case, the contentId attribute is
 * null. A classification can be or not modifiable; by default, a predefined classification, that is
 * used to classify new contents, is not modifiable whereas a classification of a content can be
 * modified.
 */
@Entity
@Table(name = "pdcclassification")
@NamedQueries({
    @NamedQuery(name = "findByComponentInstanceId", query = "from PdcClassification where " +
        "instanceId=:instanceId and contentId is null and nodeId is null"),
    @NamedQuery(name = "findByNodeId", query = "from PdcClassification where " +
        "instanceId=:instanceId and contentId is null and nodeId=:nodeId"),
    @NamedQuery(name = "findByPdcAxisValues", query = "select distinct c from PdcClassification c" +
        " join c.positions p join p.axisValues v where v in :values")})
public class PdcClassification
    extends AbstractJpaCustomEntity<PdcClassification, UniqueLongIdentifier> implements Cloneable {

  /**
   * Represents an empty classification (id est no classification on the PdC).
   */
  public static final PdcClassification NONE_CLASSIFICATION = new PdcClassification();

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @NotNull
  @Size(min = 1)
  @UniquePositions
  @Valid
  private Set<PdcPosition> positions = new HashSet<>();
  private boolean modifiable = true;
  @Column(nullable = false)
  @NotNull
  @Size(min = 2)
  private String instanceId = "";
  private String contentId = null;
  private String nodeId = null;

  /**
   * Creates an empty predefined classification for the contents that will published in the
   * specified component instance. By default, a predefined classification isn't modifiable and
   * serves to classify automatically new contents published in the specied component instance.
   *
   * @param instanceId the unique identifier of the component instance to which the predefined
   * classification will be attached.
   * @return an empty predefined classification.
   */
  public static PdcClassification aPredefinedPdcClassificationForComponentInstance(String instanceId) {
    return new PdcClassification().unmodifiable().inComponentInstance(instanceId);
  }

  /**
   * Creates an empty classification on the PdC of the specified content published in the specified
   * component instance. By default, the classification of a content can be updated.
   *
   * @param contentId the unique identifier of the content to classify.
   * @param inComponentInstanceId the unique identifier of the component instance in which the
   * content is published.
   * @return an empty classification on the PdC.
   */
  public static PdcClassification aPdcClassificationOfContent(String contentId,
      String inComponentInstanceId) {
    return new PdcClassification().modifiable().ofContent(contentId).inComponentInstance(
        inComponentInstanceId);
  }

  /**
   * Gets the positions on the PdC's axis with which the content is classified. Positions on the PdC
   * can be added or removed with the returned set.
   *
   * @return a set of positions of this classification.
   */
  public Set<PdcPosition> getPositions() {
    return positions;
  }

  /**
   * Is this classification empty?
   *
   * @return true if this classification is an empty one, false otherwise.
   */
  public boolean isEmpty() {
    return getPositions().isEmpty();
  }

  /**
   * Is the PdC classifications generated from this template can be changed?
   *
   * @return false if the content have to be automatically classified, true if the classifications
   * from this template should serv as a proposition of classification.
   */
  public boolean isModifiable() {
    return modifiable;
  }

  /**
   * Sets this PdC classification as modifiable.
   *
   * @return itself.
   */
  public PdcClassification modifiable() {
    this.modifiable = true;
    return this;
  }

  /**
   * Sets this PdC classification as unmodifiable.
   *
   * @return itself.
   */
  public PdcClassification unmodifiable() {
    this.modifiable = false;
    return this;
  }

  public PdcClassification ofContent(String contentId) {
    if (isDefined(contentId)) {
      this.contentId = contentId;
      this.nodeId = null;
    }
    return this;
  }

  public PdcClassification forNode(String nodeId) {
    if (isDefined(nodeId)) {
      this.nodeId = nodeId;
      this.contentId = null;
    }
    return this;
  }

  public PdcClassification inComponentInstance(String instanceId) {
    if (!isDefined(instanceId)) {
      throw new NullPointerException("The component instance identifier cannot be null!");
    }
    this.instanceId = instanceId;
    return this;
  }

  public String getComponentInstanceId() {
    return instanceId;
  }

  public String getContentId() {
    return contentId;
  }

  public String getNodeId() {
    return nodeId;
  }

  /**
   * Is this classification on the PdC is a predefined one to classify any new contents in the given
   * node or for the given whole component instance? If this classification serves as a template for
   * classifying the contents in a whole component instance or in a node, then true is returned. If
   * this classification is the one of a given content, then false is returned.
   *
   * @return true if this classification is a predefined one, false otherwise.
   */
  public boolean isPredefined() {
    return contentId == null;
  }

  /**
   * Is this classification on the PdC is a predefined one for the contents published in the given
   * whole component instance? If this classification serves as a template for classifying the
   * contents in a whole component instance, then true is returned. If this classification is a
   * predefined one dedicated to classify only the content in a given node, then false is returned.
   * If this classification is the one of a given content, then false is returned.
   *
   * @return true if this classification is a predefined one for the whole component instance, false
   * otherwise.
   */
  public boolean isPredefinedForTheWholeComponentInstance() {
    return isPredefined() && nodeId == null;
  }

  /**
   * Is this classification on the PdC is a predefined one for the contents published in a given
   * node? If this classification serves to classify the contents published in a single node of the
   * component instance, then true is returned. If this classification is the one of a given
   * content, then false is returned. If this classification is the predefined one for the whole
   * component instance, then false is returned.
   *
   * @return true if this classification is a predefined one for a given node, false otherwise.
   */
  public boolean isPredefinedForANode() {
    return isPredefined() && nodeId != null;
  }

  /**
   * Updates this classification by removing from its positions the specified values because they
   * will be deleted from the PdC's axis. This method is invoked at axis value deletion.
   * Accordingly, it performs an update of this classification by applying the following algorithm
   * for each deleted value:
   * <ul>
   * <li>The value is a base one of the axis: the value is removed from any positions of the
   * classification. If a position is empty (it has no values) it is then deleted (the
   * classification can be then found empty).</li>
   * <li>The value is a leaf in its value hierarchical tree: the value is replaced by its mother
   * value in any positions of this classification.</li>
   * </ul>
   *
   * @param deletedValues the values that are removed from a PdC's axis.
   */
  public void updateForPdcAxisValuesDeletion(final List<PdcAxisValue> deletedValues) {
    List<PdcPosition> positionsToDelete = new ArrayList<>();
    for (PdcPosition pdcPosition : getPositions()) {
      for (PdcAxisValue aDeletedValue : deletedValues) {
        if (pdcPosition.getValues().contains(aDeletedValue)) {
          pdcPosition.getValues().remove(aDeletedValue);
          if (!aDeletedValue.isBaseValue()) {
            PdcAxisValue parentValue = aDeletedValue.getParentValue();
            while (deletedValues.contains(parentValue) && !parentValue.isBaseValue()) {
              parentValue = parentValue.getParentValue();
            }
            if (!deletedValues.contains(parentValue)) {
              pdcPosition.getValues().add(parentValue);
            }
          }
          if (pdcPosition.isEmpty() || alreadyExists(pdcPosition)) {
            positionsToDelete.add(pdcPosition);
          }
        }
      }
    }
    getPositions().removeAll(positionsToDelete);
  }

  /**
   * Creates an empty classification on the PdC, ready to be completed for a given content published
   * in a given component instance. By default, a classification of a content is modifiable.
   */
  protected PdcClassification() {
  }

  /**
   * Sets the positions on the PdC for this classification.
   *
   * @param thePositions the position to set in this classification.
   * @return itself.
   */
  public PdcClassification withPositions(final Collection<PdcPosition> thePositions) {
    this.positions.clear();
    this.positions.addAll(thePositions);
    return this;
  }

  /**
   * Adds the specified position on the PdC in this classification.
   *
   * @param aPosition a position on the PdC to add in this classification.
   * @return itself.
   */
  public PdcClassification withPosition(final PdcPosition aPosition) {
    this.positions.add(aPosition);
    return this;
  }

  @Override
  public String toString() {
    return "PdcClassification{" + "id=" + getId() + ", positions=" + positions + ", modifiable="
        + modifiable + ", instanceId=" + instanceId + ", contentId=" + contentId + ", nodeId="
        + nodeId + '}';
  }

  @Override
  public PdcClassification clone() {
    PdcClassification classification = new PdcClassification().ofContent(contentId).
        forNode(nodeId).
        inComponentInstance(instanceId);
    classification.modifiable = modifiable;
    for (PdcPosition pdcPosition : positions) {
      classification.getPositions().add(pdcPosition.clone());
    }
    return classification;
  }

  /**
   * Gets the positions on the PdC of this classification in the form of ClassifyPosition instances.
   * This method is for compatibility with the old way to manage the classification.
   *
   * @return a list of ClassifyPosition instances, each of them representing a position on the PdC.
   */
  public List<ClassifyPosition> getClassifyPositions() {
    List<ClassifyPosition> classifyPositions = new ArrayList<>(getPositions().size());
    try {
      for (PdcPosition position : getPositions()) {
        classifyPositions.add(position.toClassifyPosition());
      }
    } catch (PdcException ex) {
      throw new PdcRuntimeException(getClass().getSimpleName() + ".getClassifyPositions()",
          SilverpeasException.ERROR, "root.EX_NO_MESSAGES", ex);
    }
    return classifyPositions;
  }

  private boolean alreadyExists(final PdcPosition pdcPosition) {
    boolean alreadyExist = false;
    for (PdcPosition aPosition : getPositions()) {
      if (!aPosition.getId().equals(pdcPosition.getId()) && aPosition.getValues().equals(
          pdcPosition.
          getValues())) {
        alreadyExist = true;
        break;
      }
    }
    return alreadyExist;
  }
}
