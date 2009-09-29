/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.workflow.engine.model;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Vector;

import com.silverpeas.workflow.api.model.Consequence;
import com.silverpeas.workflow.api.model.Consequences;

/**
 * Class implementing the representation of the &lt;consequences&gt; element of
 * a Process Model.
 */
public class ConsequencesImpl implements Consequences, Serializable {
  private Vector consequenceList;

  /**
   * Constructor
   */
  public ConsequencesImpl() {
    consequenceList = new Vector();
  }

  /**
   * Get the actions
   * 
   * @return the actions as a Hashtable
   */
  public Vector getConsequenceList() {
    return consequenceList;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.silverpeas.workflow.api.model.Consequences#addConsequence(com.silverpeas
   * .workflow.api.model.Consequence)
   */
  public void addConsequence(Consequence consequence) {
    consequenceList.add(consequence);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.silverpeas.workflow.api.model.Consequences#createConsequence()
   */
  public Consequence createConsequence() {
    return new ConsequenceImpl();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.silverpeas.workflow.api.model.Consequences#iterateConsequence()
   */
  public Iterator iterateConsequence() {
    return consequenceList.iterator();
  }
}