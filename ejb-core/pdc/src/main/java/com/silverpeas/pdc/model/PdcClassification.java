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

import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.pdc.model.PdcRuntimeException;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * A classification of a content in Silverpeas on the classification plan (named PdC).
 * 
 * A classification of a content is made up of one or more positions on the axis of the PdC. Each
 * position consists of one or several values on some PdC's axis. A classification cannot have two or
 * more identical positions; each of them must be unique.
 * 
 * It can also represent, for a Silverpeas component instance or for a node in a component instance,
 * a predefined classification with which any published contents can be classified on the PdC. In
 * this case, the resourceId attribute, if not null, will refer a node in the Silverpeas component
 * instance for which the contents will be classified with the classification.
 */
@Entity
@NamedQueries({
  @NamedQuery(name = "PdcClassification.findPredefinedClassificationByComponentInstanceId",
  query = "from PdcClassification where instanceId=?1 and contentId is null and nodeId is null"),
  @NamedQuery(name = "PdcClassification.findPredefinedClassificationByNodeId",
  query = "from PdcClassification where instanceId=?2 and contentId is null and nodeId=?1)")
})
public class PdcClassification implements Serializable {

  /**
   * Represents an empty classification (id est no classification on the PdC).
   */
  public static final PdcClassification NONE_CLASSIFICATION = new PdcClassification();
  private static final long serialVersionUID = 4032206628783381447L;
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  private Set<PdcPosition> positions = new HashSet<PdcPosition>();
  private boolean modifiable = true;
  @Column(nullable = false)
  @NotNull
  @Size(min = 2)
  private String instanceId = "";
  private String contentId = null;
  private String nodeId = null;

  /**
   * Creates a predefined classification for the new contents in the specified component instance.
   * By default, a predefined classification isn't modifiable and servs to classify automatically 
   * new contents published in the specied component instance.
   * @param instanceId the unique identifier of the component instance to which the predefined
   * classification will be attached.
   * @return a predefined classification.
   */
  public static PdcClassification aPredefinedPdcClassification(String inComponentInstanceId) {
    return new PdcClassification().unmodifiable().inComponentInstance(inComponentInstanceId);
  }

  /**
   * Gets the positions on the PdC's axis with which the content is classified.
   * Positions on the PdC can be added or removed with the returned set.
   * @return a set of positions of this classification.
   */
  public Set<PdcPosition> getPositions() {
    return positions;
  }
  
  /**
   * Is this classification empty?
   * @return true if this classification is an empty one, false otherwise.
   */
  public boolean isEmpty() {
    return getPositions().isEmpty();
  }

  /**
   * Is the PdC classifications generated from this template can be changed?
   * @return false if the content have to be automatically classified, true if the classifications
   * from this template should serv as a proposition of classification.
   */
  public boolean isModifiable() {
    return modifiable;
  }

  /**
   * Sets this PdC classification as modifiable.
   * @return itself.
   */
  public PdcClassification modifiable() {
    this.modifiable = true;
    return this;
  }

  /**
   * Sets this PdC classification as unmodifiable.
   * @return itself.
   */
  public PdcClassification unmodifiable() {
    this.modifiable = false;
    return this;
  }

  public PdcClassification ofContent(String contentId) {
    this.contentId = contentId;
    this.nodeId = null;
    return this;
  }

  public PdcClassification forNode(String nodeId) {
    this.nodeId = nodeId;
    this.contentId = null;
    return this;
  }

  public PdcClassification inComponentInstance(String instanceId) {
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
   * node or for the given whole component instance?
   * 
   * If this classification serves as a template for classifying the contents in a whole component
   * instance or in a node, then true is returned.
   * If this classification is the one of a given content, then false is returned.
   * @return true if this classification is a predefined one, false otherwise.
   */
  public boolean isPredefined() {
    return contentId == null;
  }

  /**
   * Is this classification on the PdC is a predefined one for the contents published in the given
   * whole component instance?
   * 
   * If this classification serves as a template for classifying the contents in a whole component
   * instance, then true is returned.
   * If this classification is a predefined one dedicated to classify only the content in a given
   * node, then false is returned.
   * If this classification is the one of a given content, then false is returned.
   * @return true if this classification is a predefined one for the whole component instance,
   * false otherwise.
   */
  public boolean isPredefinedForTheWholeComponentInstance() {
    return isPredefined() && nodeId == null;
  }

  /**
   * Is this classification on the PdC is a only predefined for the contents published in a given
   * node?
   * 
   * If this classification serves as a template for classifying the contents in a single node of
   * the  the component instance, then true is returned.
   * If this classification is the one of a given content, then false is returned.
   * If this classification is the predefined one for the whole component instance, then false
   * is returned.
   * @return true if this classification is a predefined one for only a given node,
   * false otherwise.
   */
  public boolean isOnlyPredefinedForANode() {
    return isPredefined() && nodeId != null;
  }

  /**
   * Creates an empty classification on the PdC, ready to be completed for a given content published
   * in a given component instance. By default, a classification of a content is modifiable.
   */
  public PdcClassification() {
  }

  /**
   * Sets the positions on the PdC for this classification.
   * @param thePositions the position to set in this classification.
   * @return itself.
   */
  public PdcClassification withPositions(final Set<PdcPosition> thePositions) {
    this.positions.clear();
    this.positions.addAll(thePositions);
    return this;
  }

  /**
   * Adds the specified position on the PdC in this classification.
   * @param aPosition a position on the PdC to add in this classification.
   * @return itself.
   */
  public PdcClassification withPosition(final PdcPosition aPosition) {
    this.positions.add(aPosition);
    return this;
  }

  protected PdcClassification(long id) {
    this.id = id;
  }

  protected Long getId() {
    return this.id;
  }
  
  protected void setId(Long id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return "PdcClassification{" + "id=" + id + ", positions=" + positions + ", modifiable="
            + modifiable + ", instanceId=" + instanceId + ", contentId=" + contentId + ", nodeId="
            + nodeId + '}';
  }

  /**
   * Gets the positions on the PdC of this classification in the form of ClassifyPosition instances.
   * This method is for compatibility with the old way to manage the classification.
   * @return a list of ClassifyPosition instances, each of them representing a position on the PdC.
   */
  public List<ClassifyPosition> getClassifyPositions() {
    List<ClassifyPosition> classifyPositions =
            new ArrayList<ClassifyPosition>(getPositions().size());
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

  public static PdcClassification aPdcClassificationFromClassifyPositions(
          final Collection<ClassifyPosition> positions) {
    PdcClassification classification = new PdcClassification();
    for (ClassifyPosition aPosition : positions) {
      
    }
    return classification;
  }
}
