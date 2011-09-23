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

import com.stratelia.silverpeas.pdc.control.PdcBm;
import com.stratelia.silverpeas.pdc.control.PdcBmImpl;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.pdc.model.PdcRuntimeException;
import com.stratelia.silverpeas.pdc.model.UsedAxis;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

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
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;
  @ManyToOne(optional = true)
  private PdcAxisValue parent;
  @OneToMany(mappedBy = "parent", cascade = {CascadeType.ALL}, orphanRemoval = true)
  private Set<PdcAxisValue> children = new HashSet<PdcAxisValue>();
  private String term;
  private Long axisId;
  
  public static PdcAxisValue aBaseValueOfAxis(String axisId, String term) {
    return new PdcAxisValue().inAxisId(axisId).withTerm(term);
  }
  
  public static PdcAxisValue aValueWithAsParent(final PdcAxisValue value, String term) {
    return new PdcAxisValue().inAxisId(value.getAxisId()).withTerm(term).withParentValue(value);
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
    return Collections.unmodifiableSet(children);
  }

  /**
   * Gets the value this one refines or specifies a little more. The returned value is the parent of
   * this one in the semantic tree represented by the axis to which this value belongs.
   * @return the axis value parent of this one or null if this value has no parent (in that case,
   * this value is a base one).
   */
  public PdcAxisValue getParentValue() {
    return parent;
  }

  /**
   * Gets the term carried by this value.
   * @return the term of the value.
   */
  public String getTerm() {
    return term;
  }

  /**
   * Is this value is a base one?
   * @return true if this value is an axis base value.
   */
  public boolean isBaseValue() {
    return parent == null;
  }

  /**
   * Gets the meaning carried by this value. The meaning is in fact the complete path of terms
   * that made this value. For example, in an axis representing the geography, the meaning of 
   * the value "France / Rhônes-Alpes / Isère" is "Geography / France / Rhônes-Alpes / Isère".
   * @return the meaning carried by this value, in other words the complete path of this value.
   */
  public String getMeaning() {
    String meaning;
    PdcAxisValue theParent = getParentValue();
    if (theParent.isBaseValue()) {
      try {
        UsedAxis axis = getUsedAxis();
        meaning = axis._getAxisName();
      } catch (PdcException ex) {
        throw new PdcRuntimeException(getClass().getSimpleName(), SilverTrace.TRACE_LEVEL_ERROR,
                "root.EX_NO_MESSAGE", ex);
      }
    } else {
      meaning = theParent.getMeaning() + " / " + getTerm();
    }
    return meaning;
  }

  /**
   * Gets the axis to which this value belongs to and that is used to classify contents on the PdC.
   * @return a PdC axis configured to be used in the classification of contents.
   */
  protected UsedAxis getUsedAxis() throws PdcException {
    PdcBm pdc = new PdcBmImpl();
    return pdc.getUsedAxis(getAxisId());
  }
  
  protected PdcAxisValue() {
    
  }

  protected PdcAxisValue inAxisId(String axisId) {
    this.axisId = Long.valueOf(axisId);
    return this;
  }

  protected PdcAxisValue withTerm(String term) {
    this.term = term;
    return this;
  }
  
  protected PdcAxisValue withParentValue(final PdcAxisValue value) {
    this.parent = value;
    return this;
  }
}
