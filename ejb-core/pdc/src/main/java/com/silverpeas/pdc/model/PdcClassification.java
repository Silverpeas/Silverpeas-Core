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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A classification of a content in Silverpeas onto the classification plan (named PdC).
 * 
 * A classification of a content is made up of one or more positions on the axis of the PdC. Each
 * position consist of one or several values on some PdC's axis. A classification cannot have two or
 * more identical positions; each of them must be unique.
 */
public class PdcClassification {
  
  /**
   * Represents an empty classification (id est no classification onto the PdC).
   */
  public static final PdcClassification NO_DEFINED_CLASSIFICATION = new PdcClassification();
  
  /**
   * Creates a classification onto the PdC from the specified positions. By default the
   * classification is modifiable.
   * @param positions a set of positions with which a content is classified.
   * @return a modifiable PdC classification.
   */
  public static PdcClassification aClassificationFromPositions(final Collection<ClassifyPosition> positions) {
    PdcClassification classification = new PdcClassification().modifiable();
    classification.setPositions(positions);
    return classification;
  }
  
  private Set<ClassifyPosition> positions = new HashSet<ClassifyPosition>();
  private boolean modifiable = false;
  private String instanceId = "";
  private String resourceId = "";
  
  /**
   * Gets the positions on the PdC's axis with which the content is classified.
   * @return a set of positions of this classification.
   */
  public Set<ClassifyPosition> getPositions() {
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
  
  public PdcClassification forResource(String resourceId) {
    this.resourceId = resourceId;
    return this;
  }
  
  public PdcClassification inComponentInstance(String instanceId) {
    this.instanceId = instanceId;
    return this;
  }

  public String getComponentInstanceId() {
    return instanceId;
  }

  public String getResourceId() {
    return resourceId;
  }
  
  protected PdcClassification() {
    
  }
  
  /**
   * Sets the specified positions to this classification.
   * If the classification has already some positions, they are replaced with the specified ones.
   * @param positions the positions onto the PdC to set.
   */
  protected void setPositions(final Collection<ClassifyPosition> positions) {
    this.positions.clear();
    this.positions.addAll(positions);
  }
}
