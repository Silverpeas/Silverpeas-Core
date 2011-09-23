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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
@NamedQuery(name="PdcClassification.findPredefinedClassificationByComponentInstanceId",
        query="from PdcClassification where instanceId=?1 and contentId is null")
public class PdcClassification implements Serializable {
  
  /**
   * Represents an empty classification (id est no classification onto the PdC).
   */
  public static final PdcClassification NO_DEFINED_CLASSIFICATION = new PdcClassification();
  private static final long serialVersionUID = 4032206628783381447L;
  
  @Id
  @GeneratedValue(strategy= GenerationType.AUTO)
  private Long id;
  
  @OneToMany(cascade= CascadeType.ALL, orphanRemoval=true)
  private Set<PdcPosition> positions = new HashSet<PdcPosition>();
  private boolean modifiable = true;
  @Column(nullable=false)
  @NotNull
  @Size(min=2)
  private String instanceId = "";
  private String contentId = null;
  private String nodeId = null;
  
  /**
   * Gets the positions on the PdC's axis with which the content is classified.
   * Positions on the PdC can be added or removed with the returned set.
   * @return a set of positions of this classification.
   */
  public Set<PdcPosition> getPositions() {
    return positions;
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
  
  public PdcClassification forContent(String contentId) {
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
   * If this classification serves as a template for classifying contents in a whole component
   * instance or in a node, then true is returned.
   * If this classification is the one of a given content, then false is returned.
   * @return true if this classification is a predefined one, false otherwise.
   */
  public boolean isPredefinedClassification() {
    return contentId == null;
  }
  
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
}
