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

import com.stratelia.silverpeas.pdc.model.Value;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A value of a PdC's axis.
 * 
 * A value of an axis is a term in the vocabulary of the concept represented by the axis, and it is
 * related to the base value of the axis; It can be either a parent or a child of the axis base value. 
 * 
 * As the axis can be made up of hierarchical semantic trees, a term can belong to a such tree. In
 * that case, its level attribute indicates its position in the tree from the root.
 */
@XmlRootElement
public class PdcAxisValue extends PdcValue {
  private static final long serialVersionUID = -1689709605873362349L;
  
  @XmlElement(required=true)
  private String term;
  @XmlElement(required=true)
  private int level;
  @XmlElement(defaultValue="true")
  private boolean activated = true;
  @XmlElement(defaultValue="false")
  private boolean origin = false;
  
  /**
   * Creates a new value of a PdC axis from the specified business PdC value and expressed in the
   * specified language.
   * @param value the business PdC value.
   * @param inLanguage the language of the user.
   * @return a PdcAxisValue instance.
   */
  public static PdcAxisValue fromValue(final Value value, String inLanguage) {
    PdcAxisValue axisValue = new PdcAxisValue(
            withId(value.getFullPath()),
            withTerm(value.getName(inLanguage)),
            inAxis(value.getAxisId())).
            inTree(value.getTreeId(), atLevel(value.getLevelNumber()));
    return axisValue;
  }

  /**
   * Gets the translated term represented by this value.
   * @return the term translated into the user language.
   */
  public String getTerm() {
    return term;
  }

  /**
   * Gets the position level of this value in the semantic tree or 0. 0 means its level is at the
   * axis.
   * @return the position level or 0 if it doesn't belong to a tree.
   */
  public int getLevel() {
    return level;
  }

  /**
   * Is this axis value is activated? If true, then it can be used to create a position on the axis.
   * If false, then the value cannot be choosen and it is just read only.
   * @return true if this value can be used to create a position on the axis, false otherwise.
   */
  public boolean isActivated() {
    return activated;
  }

  /**
   * Is this value the origin of the axis?
   * @return true if this axis value is the origin of the axis, false otherwise.
   */
  public boolean isOrigin() {
    return origin;
  }
  
  /**
   * Activates this axis value. So it can be used in the classification of contents on the PdC.
   */
  public void activate() {
    this.activated = true;
  }

  /**
   * Deactivates this axis value. So it cannot be used in the classification of contents of the PdC.
   * It is just in read-only.
   */
  public void deactivate() {
    this.activated = false;
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
  
  protected PdcAxisValue() {
    super();
  }
  
  /**
   * Sets this axis value as the origin of the axis.
   */
  protected void setAsOriginValue() {
    this.origin = true;
  }
  
  private PdcAxisValue(String withId, String withTerm, int inAxisId) {
    super(withId, inAxisId);
    this.term = withTerm;
  }
  
  private PdcAxisValue inTree(String treeId, int levelInTree) {
    setTreeId(treeId);
    this.level = levelInTree;
    return this;
  }
}
